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
import lombok.val;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import page.nafuchoco.neobot.api.Launcher;
import page.nafuchoco.neobot.api.command.CommandExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class CommandRegistrar {
    private final Launcher launcher;

    private CommandListUpdateAction updateAction;
    private List<Command> registeredCommands = new ArrayList<>();
    private boolean globalCommandListUpdated = false;

    protected CommandRegistrar(Launcher launcher) {
        this.launcher = launcher;
    }

    protected void registerCommandToDiscord(CommandExecutor executor) {
        val command = Commands.slash(executor.getName(), executor.getDescription());
        addCommandOptions(command, executor);
        addCommands(command);
    }

    protected void unregisterCommandFromDiscord(CommandExecutor executor) {
        if (executor == null)
            return;

        if (globalCommandListUpdated) {
            var commandData = registeredCommands.stream().filter(command -> executor.getName().equals(command.getName())).findFirst().orElse(null);
            if (commandData != null) {
                launcher.getDiscordApi().getShardById(0).deleteCommandById(commandData.getId()).queue();
                registeredCommands.remove(commandData);
            }
        } else {
            throw new IllegalStateException("Global command list has not been updated yet.");
        }
    }

    private void addCommands(CommandData... commands) {
        if (globalCommandListUpdated) {
            Arrays.stream(commands).forEach(command -> {
                launcher.getDiscordApi().getShardById(0).upsertCommand(command).queue(reg -> registeredCommands.add(reg));
            });
        } else {
            if (updateAction == null)
                updateAction = launcher.getDiscordApi().getShardById(0).updateCommands();

            updateAction.addCommands(commands);
        }
    }

    private void addCommandOptions(SlashCommandData command, CommandExecutor executor) {
        executor.getValueOptions().stream().map(option -> {
                    val optionData = new OptionData(option.optionType(), option.optionName(), option.optionDescription(), option.required(), option.autoComplete());
                    if (!option.getChoices().isEmpty()) {
                        switch (option.optionType()) {
                            case STRING -> option.getChoices().forEach((key, value) -> optionData.addChoice(key, (String) value));
                            case INTEGER -> option.getChoices().forEach((key, value) -> optionData.addChoice(key, (Long) value));
                            case NUMBER -> option.getChoices().forEach((key, value) -> optionData.addChoice(key, (Double) value));
                            default -> throw new IllegalStateException("Unexpected value: " + option.optionType());
                        }
                    }
                    return optionData;
                }).forEach(command::addOptions);
        executor.getSubCommands().forEach(sub -> {
            val subCommand = new SubcommandData(sub.optionName(), sub.optionDescription());
            sub.getValueOptions().forEach(option -> subCommand.addOption(option.optionType(), option.optionName(), option.optionDescription(), option.required(), option.autoComplete()));
            command.addSubcommands(subCommand);
        });
    }

    protected void queue() {
        updateAction.queue(commands -> registeredCommands = new ArrayList<>(commands));
        updateAction = null;
        globalCommandListUpdated = true;
    }

    protected List<Command> getRegisteredCommands() {
        return registeredCommands;
    }
}
