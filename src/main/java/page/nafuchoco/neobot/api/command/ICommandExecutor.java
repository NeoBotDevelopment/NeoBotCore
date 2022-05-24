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

package page.nafuchoco.neobot.api.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICommandExecutor {
    /**
     * @return Name of the command executor
     */
    @NotNull
    String getName();

    /**
     * Execute the command.
     *
     * @param context The command context to use at runtime
     */
    @Nullable
    void onInvoke(CommandContext context);

    /**
     * @return Description of the command
     */
    @NotNull
    String getDescription();

    /**
     * @return Command response is ephemeral
     */
    @NotNull
    boolean isEphemeral();
}
