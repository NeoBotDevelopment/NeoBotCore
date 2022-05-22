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
        this.name = name;
        this.indexes = indexes;
    }

    public String getName() {
        return name;
    }

    public <T> T getStoreData(long id, String index) {
        T data = (T) get(id, index, indexes.get(index));

        return data;
    }

    /**
     * Gets the data stored in the data store.
     *
     * @param id   The guild id of the data to get.
     * @param key  The key of the data to get.
     * @param type The type of the data to get.
     * @param <T>  The type of the data to get.
     * @return The data stored in the data store.
     */
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

    /**
     * Save to the data store.
     *
     * @param id    The guild id of the data to save.
     * @param key   The key of the data to save.
     * @param value The value of the data to save.
     * @param <T>   The type of the data to save.
     */
    private <T> void set(long id, String key, T value) {
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

    /**
     * Deletes the data stored in the data store.
     *
     * @param id The guild id of the data to delete.
     */
    public void delete(long id) {
        try (var connection = connector.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + name + " WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
