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

package page.nafuchoco.neobot.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import page.nafuchoco.neobot.core.DatabaseType;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnector {
    private final HikariDataSource dataSource;

    private String prefix;

    public DatabaseConnector(DatabaseType databaseType, String address, String database, String username, String password) {
        var hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(databaseType.getJdbcClass());
        hikariConfig.setJdbcUrl(databaseType.getAddressPrefix() + address + "/" + database);
        hikariConfig.addDataSourceProperty("user", username);
        hikariConfig.addDataSourceProperty("password", password);
        dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        dataSource.close();
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
