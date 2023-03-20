/*
 * Copyright 2022 NAFU_at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package page.nafuchoco.neobot.core.datastore;

import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neobot.api.DatabaseConnector;
import page.nafuchoco.neobot.api.datastore.DataStore;
import page.nafuchoco.neobot.api.datastore.DataStoreBuilder;
import page.nafuchoco.neobot.api.datastore.exception.DataStoreGenerateException;

import java.sql.SQLException;
import java.util.*;

import static org.apache.commons.lang3.CharSetUtils.count;

public class DefaultDataStoreBuilder implements DataStoreBuilder {
    private final DatabaseConnector connector;
    private final Map<String, Class> indexes;
    private String storeName;

    protected DefaultDataStoreBuilder(DatabaseConnector connector) {
        this.connector = connector;
        indexes = new LinkedHashMap<>();
    }

    /**
     * Returns {@link DataStore}.
     *
     * @return Built {@link DataStore}
     */
    @Override
    public DataStore build() {
        // check if the store name is set
        if (storeName == null || indexes.isEmpty())
            throw new IllegalStateException("store name and index must be set");

        storeName = toSnakeCase(storeName);

        // add an entry to store the guild id.
        var indexMap = new LinkedHashMap<String, Class>();
        indexMap.put("id", Long.class);
        indexMap.putAll(indexes);

        // create database tables corresponding to the data store.
        try (var connection = connector.getConnection()) {
            // check if the table exists.
            try (var checkTableStatement = connection.prepareStatement("SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = ?;")) {
                checkTableStatement.setString(1, connector.getPrefix() + storeName);
                var result = checkTableStatement.executeQuery();

                if (result.getFetchSize() > 0) { // if the table exists, check if the index is correct.
                    List<String> columnNames = new ArrayList<>();
                    try (var columnsShowStatement = connection.prepareStatement("SHOW COLUMNS FROM " + connector.getPrefix() + storeName)) {
                        var columnsInfoResult = columnsShowStatement.executeQuery();
                        while (columnsInfoResult.next()) {
                            columnNames.add(columnsInfoResult.getString("Field"));
                        }
                    }

                    // Checks for non-matching items to determine if they should be added or deleted.
                    List<String> addColumns = new ArrayList<>();
                    for (var index : indexMap.entrySet()) {
                        if (!columnNames.contains(index.getKey())) {
                            addColumns.add(index.getKey());
                        }
                    }
                    List<String> deleteColumns = new ArrayList<>();
                    for (var columnName : columnNames) {
                        if (!indexMap.containsKey(columnName)) {
                            deleteColumns.add(columnName);
                        }
                    }

                    // Alter the table to add or delete columns.
                    try (var createTableStatement = connection.prepareStatement(generateAlterTableStatement(addColumns, deleteColumns))) {
                        createTableStatement.execute();
                    }
                } else { // if the table does not exist, create a new table.
                    try (var createTableStatement = connection.prepareStatement(generateCreateTableStatement(indexMap))) {
                        createTableStatement.execute();

                        // set unique index for the guild id.
                        try (var createIndexStatement = connection.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS " + storeName + "_id ON " + connector.getPrefix() + storeName + "(id)")) {
                            createIndexStatement.execute();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataStoreGenerateException(e);
        }

        return new DataStoreImpl(connector, storeName, indexes);
    }

    /**
     * It searches for uppercase letters in the string, converts them to lowercase, and adds an underscore before them.
     */
    private String toSnakeCase(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Obtains the corresponding database type string from the class type.
     */
    private String getTypeString(Class type) {
        if (type.equals(String.class)) {
            return "TEXT";
        } else if (type.equals(Integer.class)) {
            return "INTEGER";
        } else if (type.equals(Long.class)) {
            return "BIGINT";
        } else if (type.equals(Double.class)) {
            return "DOUBLE";
        } else if (type.equals(Boolean.class)) {
            return "BOOLEAN";
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    /**
     * Generate a createTable statement from a column list.
     */
    private String generateCreateTableStatement(Map<String, Class> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(connector.getPrefix()).append(storeName).append(" (");
        for (Map.Entry<String, Class> entry : columns.entrySet()) {
            sb.append(toSnakeCase(entry.getKey())).append(" ").append(getTypeString(entry.getValue())).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length()).append(")");
        return sb.toString();
    }

    /**
     * Generate an alterTable statement from a add column list and a delete column list.
     */
    private String generateAlterTableStatement(List<String> addColumns, List<String> deleteColumns) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(connector.getPrefix()).append(storeName).append(" ");
        for (String addColumn : addColumns) {
            sb.append("ADD COLUMN ").append(toSnakeCase(addColumn)).append(" ").append(getTypeString(indexes.get(addColumn))).append(", ");
        }
        for (String deleteColumn : deleteColumns) {
            sb.append("DROP COLUMN ").append(toSnakeCase(deleteColumn)).append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }


    /**
     * Sets the name of the data store.
     *
     * @param storeName the name of the data store<br>
     *                  This name is usually recommended for Camel case or Snake case.
     * @return this builder
     */
    @Override
    public DataStoreBuilder storeName(@NotNull String storeName) {
        this.storeName = storeName;
        return this;
    }

    /**
     * Register the data items to be saved in the data store.
     *
     * @param clazz     the class of the data item
     * @param indexName the name of the index<br>
     *                  This name is usually recommended for Camel case or Snake case.
     * @return this builder
     */
    @Override
    public DataStoreBuilder addIndex(@NotNull Class clazz, @NotNull String indexName) {
        getTypeString(clazz); // check the type is supported
        Objects.requireNonNull(indexName);

        indexes.put(indexName, clazz);
        return this;
    }

    /**
     * Deletes the specified item from the registered data items.
     *
     * @param indexName the name of the index
     * @return this builder
     */
    @Override
    public DataStoreBuilder removeIndex(@NotNull String indexName) {
        indexes.remove(indexName);
        return this;
    }
}
