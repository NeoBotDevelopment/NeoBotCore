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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import page.nafuchoco.neobot.api.ExceptionUtil;
import page.nafuchoco.neobot.api.Launcher;

import java.util.HashMap;

@Slf4j
@AllArgsConstructor
public class SlashCommandEventHandler extends ListenerAdapter {
    private final Launcher launcher;
    private final CommandRegistry registry;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // とりあえずDiscordにコマンドを受け付けた事を返す
        event.deferReply(true).queue();
        val hook = event.getHook();
        // hook.setEphemeral(true);
        val responseSender = new SlashCommandResponse(hook);

        // レジストリが登録されて居ない場合は無視
        if (registry == null)
            return;

        //neoGuild.setLastJoinedChannel(event.getTextChannel());

        // コマンドクラスの取得
        CommandExecutor command = registry.getExecutor(event.getName());
        CommandExecutor subCommand = command.getSubCommands().stream().filter(option -> option.optionName().equals(event.getSubcommandName())).findAny().orElse(null);

        // オプションの処理
        val optionsMap = new HashMap<String, AssignedCommandValueOption>();
        val options = subCommand != null ? subCommand.getOptions() : command.getOptions();
        for (CommandOption option : options) {
            val optionMapping = event.getOption(option.optionName());
            if (optionMapping == null)
                continue;

            val assignedCommandOption = parseCommandOptions(option, optionMapping);
            optionsMap.put(option.optionName(), assignedCommandOption);
        }

        // コマンドコンテキストの生成
        CommandContext context = new CommandContext(
                launcher,
                event.getGuild(),
                event.getTextChannel(),
                event.getMember(),
                hook,
                event.getName(),
                optionsMap,
                command,
                subCommand,
                responseSender);


        log.debug("Command Received: {}", context);

        try {
            if (context.getSubCommand() == null)
                context.getCommand().onInvoke(context);
            else
                context.getSubCommand().onInvoke(context);

            if (!responseSender.isExecutorResponded())
                hook.sendMessage("Your request has been processed!").setEphemeral(true).queue();
        } catch (Exception e) {
            hook.sendMessage(ExceptionUtil.getStackTrace(e, "Failed to execute the command.")).setEphemeral(true).queue();
        }
    }

    private AssignedCommandValueOption parseCommandOptions(CommandOption option, OptionMapping mapping) {
        return switch (option.optionType()) {
            case STRING -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsString());

            case INTEGER -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsInt());

            case BOOLEAN -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsBoolean());

            case USER -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsUser());

            case CHANNEL -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsGuildChannel());

            case ROLE -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsRole());

            case MENTIONABLE ->
                    new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsMentionable());

            case NUMBER -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsDouble());

            case ATTACHMENT -> new AssignedCommandValueOption<>((CommandValueOption) option, mapping.getAsAttachment());

            default -> new AssignedCommandValueOption((CommandValueOption) option, mapping);
        };
    }
}
