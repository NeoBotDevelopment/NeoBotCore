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

package page.nafuchoco.neobot.core;

import page.nafuchoco.neobot.api.IDatabaseType;

public enum DatabaseType implements IDatabaseType {
    MARIADB("org.mariadb.jdbc.Driver", "jdbc:mariadb://"),
    MYSQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://");

    private final String jdbcClass;
    private final String addressPrefix;

    DatabaseType(String jdbcClass, String addressPrefix) {
        this.jdbcClass = jdbcClass;
        this.addressPrefix = addressPrefix;
    }

    @Override
    public String getJdbcClass() {
        return jdbcClass;
    }

    @Override
    public String getAddressPrefix() {
        return addressPrefix;
    }
}
