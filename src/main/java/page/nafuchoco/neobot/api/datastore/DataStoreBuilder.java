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

package page.nafuchoco.neobot.api.datastore;

import page.nafuchoco.neobot.api.DatabaseConnector;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataStoreBuilder {
    private final DatabaseConnector connector;
    private final Map<String, Class> indexes;
    private String storeName;

    protected DataStoreBuilder(DatabaseConnector connector) {
        this.connector = connector;
        indexes = new LinkedHashMap<>();
    }

    /**
     * Returns {@link DataStore}.
     *
     * @return Built {@link DataStore}
     */
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
        try (var connection = connector.getConnection();
             var createTableStatement = connection.prepareStatement(generateCreateTableStatement(indexMap))) {
            createTableStatement.execute();

            // set unique index for the guild id.
            try (var createIndexStatement = connection.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS " + storeName + "_id ON " + connector.getPrefix() + storeName + "(id)")) {
                createIndexStatement.execute();
            }
        } catch (SQLException e) {
            throw new DataStoreGenerateException(e);
        }

        return new DataStore(connector, storeName, indexes);
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
     * Sets the name of the data store.
     *
     * @param storeName the name of the data store<br>
     *                  This name is usually recommended for Camel case or Snake case.
     * @return this builder
     */
    public DataStoreBuilder storeName(String storeName) {
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
    public DataStoreBuilder addIndex(Class clazz, String indexName) {
        indexes.put(indexName, clazz);
        return this;
    }

    /**
     * Deletes the specified item from the registered data items.
     *
     * @param indexName the name of the index
     * @return this builder
     */
    public DataStoreBuilder removeIndex(String indexName) {
        indexes.remove(indexName);
        return this;
    }
}
