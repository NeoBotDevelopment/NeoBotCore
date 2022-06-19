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

import java.util.ArrayList;
import java.util.List;

public abstract class CommandExecutor implements ICommandExecutor {
    private final List<CommandOption> options = new ArrayList<>();

    private final String name;

    protected CommandExecutor(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<CommandOption> getOptions() {
        return options;
    }

    /**
     * Returns an immutable list of registered {@link CommandValueOption}.
     *
     * @return Immutable list of registered {@link CommandValueOption}
     */
    public List<CommandValueOption> getValueOptions() {
        return options.stream()
                .filter(CommandValueOption.class::isInstance)
                .map(CommandValueOption.class::cast)
                .toList();
    }

    /**
     * Returns an immutable list of registered {@link SubCommandOption}.
     *
     * @return Immutable list of registered {@link SubCommandOption}
     */
    public List<SubCommandOption> getSubCommands() {
        return options.stream()
                .filter(SubCommandOption.class::isInstance)
                .map(SubCommandOption.class::cast)
                .toList();
    }

    @Override
    public @NotNull boolean isEphemeral() {
        return true;
    }
}
