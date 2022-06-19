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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Slf4j
public class ConfigLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private ConfigLoader() {
        throw new IllegalStateException();
    }

    public static <T> T loadConfig(@NotNull File configurationFile, Class<T> clazz) {
        try (var configInput = new FileInputStream(configurationFile)) {
            return MAPPER.readValue(configInput, clazz);
            //log.info("The configuration file has been successfully loaded.");
        } catch (FileNotFoundException e) {
            log.error("The configuration file could not be found. Do not delete the configuration file after starting the program.\n" +
                    "If you don't know what it is, please report it to the developer.", e);
        } catch (IOException e) {
            log.error("An error occurred while loading the configuration file.", e);
        }

        return null;
    }
}
