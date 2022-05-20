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

import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public final class Main {

    public static void main(String[] args) {
        log.info("Welcome to NeoBotCore. Starting v" + Main.class.getPackage().getImplementationVersion() + ".");
        var startTime = System.currentTimeMillis();

        /*
        if (BootOptions.isDebug()) {
            var root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            var jdaLogger = (Logger) LoggerFactory.getLogger("net.dv8tion");
            var cpLogger = (Logger) LoggerFactory.getLogger("com.zaxxer.hikari");
            root.setLevel(Level.DEBUG);
            jdaLogger.setLevel(Level.DEBUG);
            cpLogger.setLevel(Level.DEBUG);
        }
        */

        new NeoBotLauncher();
        log.info("Done! ({}s)", (double) (System.currentTimeMillis() - startTime) / 1000);

        new Thread(() -> {
            var console = new Scanner(System.in);
            while (true) {
                switch (console.nextLine()) {
                    case "exit":
                    case "stop":
                        Runtime.getRuntime().exit(0);
                        break;

                    case "threadList":
                        for (Thread thread : Thread.getAllStackTraces().keySet()) {
                            log.debug("Found active thread: {} ({})", thread, thread.getClass().getClassLoader());
                        }
                        break;
                }
            }
        }).start();
    }
}
