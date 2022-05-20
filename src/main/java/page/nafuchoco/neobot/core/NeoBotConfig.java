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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NeoBotConfig {
    @JsonProperty("basic")
    private BasicConfigSection basicConfig;

    @JsonProperty("sentryDsn")
    private String sentryDsn;

    @Getter
    @ToString
    public static class BasicConfigSection {
        @JsonProperty("discordToken")
        private String discordToken;
        @JsonProperty("database")
        private DatabaseSection database;
    }


    @Getter
    @ToString
    public static class DatabaseSection {
        @JsonProperty("databaseType")
        private DatabaseType databaseType;
        @JsonProperty("tablePrefix")
        private String tablePrefix;
        @JsonProperty("address")
        private String address;
        @JsonProperty("database")
        private String database;
        @JsonProperty("username")
        private String username;
        @JsonProperty("password")
        private String password;
    }
}
