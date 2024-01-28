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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.neobot.api.Launcher;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandValueOption;

import java.util.*;

@Slf4j
public abstract class CommandRegistrar {
    private final Launcher launcher;

    private CommandListUpdateAction globalUpdateAction;
    private final Map<Long, CommandListUpdateAction> guildUpdateActions = new HashMap<>();
    private final Map<Long, List<Command>> registeredCommands = new HashMap<>();
    private boolean commandListUpdated = false;

    protected CommandRegistrar(Launcher launcher) {
        this.launcher = launcher;
        registeredCommands.put(null, new ArrayList<>());
    }

    protected void registerCommandToDiscord(CommandExecutor executor, Guild guild) {
        val command = Commands.slash(executor.getName(), executor.getDescription());
        addCommandOptions(command, executor);
        addCommands(guild, command);
    }

    protected void unregisterCommandFromDiscord(@NotNull CommandExecutor executor, @Nullable Guild guild) {
        if (executor == null)
            return;

        if (commandListUpdated) {
            if (guild == null) {
                registeredCommands.get(null).stream().filter(command -> executor.getName().equals(command.getName())).findFirst().ifPresent(command -> {
                    launcher.getDiscordApi().getShardById(0).deleteCommandById(command.getId()).queue();
                    registeredCommands.get(null).remove(command);
                });
            } else {
                registeredCommands.get(guild.getIdLong()).stream().filter(command -> executor.getName().equals(command.getName())).findFirst().ifPresent(command -> {
                    guild.deleteCommandById(command.getId()).queue();
                    registeredCommands.get(guild.getIdLong()).remove(command);
                });
            }
        } else {
            throw new IllegalStateException("Global command list has not been updated yet.");
        }
    }

    private void addCommands(Guild guild, CommandData... commands) {
        if (commandListUpdated) {
            if (guild == null) {
                Arrays.stream(commands).forEach(command -> {
                    launcher.getDiscordApi().getShardById(0).upsertCommand(command).queue(reg -> registeredCommands.get(null).add(reg));
                });
            } else {
                Arrays.stream(commands).forEach(command -> {
                    guild.upsertCommand(command).queue(reg -> registeredCommands.computeIfAbsent(guild.getIdLong(), key -> new ArrayList<>()).add(reg));
                });
            }
        } else {
            if (guild == null) {
                if (globalUpdateAction == null)
                    globalUpdateAction = launcher.getDiscordApi().getShardById(0).updateCommands();
                globalUpdateAction.addCommands(commands);

            } else {
                guildUpdateActions.computeIfAbsent(guild.getIdLong(), key -> guild.updateCommands()).addCommands(commands);
            }
        }
    }

    private void addCommandOptions(SlashCommandData command, CommandExecutor executor) {
        executor.getValueOptions().stream()
                .sorted(Comparator.comparing(CommandValueOption::required).reversed())
                .map(option -> {
                    val optionData = new OptionData(option.optionType(), option.optionName(), option.optionDescription(), option.required(), option.autoComplete());
                    if (!option.getChoices().isEmpty()) {
                        switch (option.optionType()) {
                            case STRING ->
                                    option.getChoices().forEach((key, value) -> optionData.addChoice(key, (String) value));
                            case INTEGER ->
                                    option.getChoices().forEach((key, value) -> optionData.addChoice(key, (Long) value));
                            case NUMBER ->
                                    option.getChoices().forEach((key, value) -> optionData.addChoice(key, (Double) value));
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
        globalUpdateAction.queue(commands -> registeredCommands.put(null, new ArrayList<>(commands)));
        guildUpdateActions.forEach((guildId, action) -> action.queue(commands -> registeredCommands.computeIfAbsent(guildId, key -> new ArrayList<>(commands))));
        globalUpdateAction = null;
        guildUpdateActions.clear();
        commandListUpdated = true;
    }

    protected List<Command> getRegisteredCommands() {
        return registeredCommands.values().stream().flatMap(Collection::stream).toList();
    }
}
