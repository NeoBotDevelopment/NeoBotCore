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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class DataStore {
    private final DatabaseConnector connector;
    private final String name;
    private final Map<String, Class> indexes;

    protected DataStore(DatabaseConnector connector, String name, Map<String, Class> indexes) {
        this.connector = connector;
        this.name = connector.getPrefix() + name;
        this.indexes = indexes;
    }

    public String getName() {
        return name;
    }

    /**
     * Gets the data stored in the data store.
     *
     * @param id    The guild id of the data to get.
     * @param index The index of the data to get.
     * @return The data stored in the data store.
     */
    public <T> T getStoreData(long id, String index) {
        T data = (T) get(id, index, indexes.get(index));

        return data;
    }

    /**
     * Update the data registered in the data store.
     *
     * @param id    The guild id of the data to save.
     * @param index The index of the data to save.
     * @param value The value of the data to save.
     * @param <T>   The type of the data to save.
     */
    public <T> void saveStoreData(long id, String index, T value) {
        update(id, index, value);
    }

    /**
     * Register the data with the new ID in the data store.
     *
     * @param id     The guild id of the data to register.
     * @param values The values of the data to register.
     *               The order of the values must be the same as the order of the indexes.
     */
    public void registerStoreData(Long id, Object... values) {
        if (values.length != indexes.size())
            throw new IllegalArgumentException("The number of values must be equal to the number of indexes.");

        set(id, values);
    }

    /**
     * Deletes the data stored in the data store.
     *
     * @param id The guild id of the data to delete.
     */
    public void deleteStoredData(long id) {
        try (var connection = connector.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + name + " WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private <T> T get(long id, String key, Class<T> type) {
        try (var connection = connector.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT " + key + " FROM " + name + " WHERE id = ?")) {
            ps.setLong(1, id);
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next())
                    return resultSet.getObject(key, type);
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void set(long id, Object... values) {
        StringBuilder statement = new StringBuilder("INSERT INTO " + name + " (id, ");
        indexes.entrySet().stream().map(Map.Entry::getKey).forEach(key -> statement.append(key).append(", "));
        statement.delete(statement.length() - 2, statement.length());
        statement.append(") VALUES (?, ");
        indexes.entrySet().stream().map(Map.Entry::getKey).forEach(key -> statement.append("?, "));
        statement.delete(statement.length() - 2, statement.length());
        statement.append(") ON DUPLICATE KEY UPDATE ");
        indexes.entrySet().stream().map(Map.Entry::getKey).forEach(key -> statement.append(key).append(" = ?, "));
        statement.delete(statement.length() - 2, statement.length());

        try (var connection = connector.getConnection();
             PreparedStatement ps = connection.prepareStatement(statement.toString())) {
            ps.setLong(1, id);
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 2, values[i]);
                ps.setObject(i + values.length + 2, values[i]); // duplicate key update statement parameter
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void update(long id, String key, T value) {
        try (var connection = connector.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + name + " SET " + key + " = ? WHERE id = ?")) {
            ps.setObject(1, value);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
