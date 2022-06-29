package page.nafuchoco.neobot.core.executor;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.api.command.CommandValueOption;
import page.nafuchoco.neobot.api.command.SubCommandOption;

import java.io.File;

public class ModuleCommand extends CommandExecutor {

    public ModuleCommand(String name) {
        super(name);

        this.getOptions().add(new ModuleLoadCommand("load"));
        this.getOptions().add(new ModuleUnloadCommand("unload"));
        this.getOptions().add(new ModuleEnableCommand("enable"));
        this.getOptions().add(new ModuleDisableCommand("disable"));
    }

    @Override
    public void onInvoke(@NotNull CommandContext commandContext) {
    }

    @Override
    public @NotNull String getDescription() {
        return "Manage modules";
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }

    public static class ModuleLoadCommand extends SubCommandOption {

        protected ModuleLoadCommand(String name) {
            super(name);

            getOptions().add(new CommandValueOption(OptionType.STRING, "module", "Module to load", false, false));
        }

        @Override
        public void onInvoke(@NotNull CommandContext commandContext) {
            var module = (String) commandContext.getOptions().get("module").getValue();
            if (commandContext.getLauncher().getModuleManager().loadModule(new File("modules", module))) {
                commandContext.getResponseSender().sendMessage("Module loaded successfully").queue();
            } else {
                commandContext.getResponseSender().sendMessage("Module could not be loaded").queue();
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Loads a module";
        }
    }


    public static class ModuleUnloadCommand extends SubCommandOption {

        protected ModuleUnloadCommand(String name) {
            super(name);

            getOptions().add(new CommandValueOption(OptionType.STRING, "module", "Module to unload", false, false));
        }

        @Override
        public void onInvoke(@NotNull CommandContext commandContext) {
            var module = (String) commandContext.getOptions().get("module").getValue();
            if (module.equals("all")) {
                commandContext.getLauncher().getModuleManager().unloadAllModules();
            } else {
                commandContext.getLauncher().getModuleManager().disableModule(module);
                commandContext.getLauncher().getModuleManager().unloadModule(module);
            }

            commandContext.getResponseSender().sendMessage("Module unloaded successfully").queue();
        }

        @Override
        public @NotNull String getDescription() {
            return "Unloads a module";
        }
    }


    public static class ModuleEnableCommand extends SubCommandOption {

        protected ModuleEnableCommand(String name) {
            super(name);

            getOptions().add(new CommandValueOption(OptionType.STRING, "module", "Module to enable", false, false));
        }

        @Override
        public void onInvoke(@NotNull CommandContext commandContext) {
            var module = (String) commandContext.getOptions().get("module").getValue();
            if (module.equals("all")) {
                commandContext.getLauncher().getModuleManager().enableAllModules();
                commandContext.getResponseSender().sendMessage("All modules enabled successfully").queue();
            } else {
                if (commandContext.getLauncher().getModuleManager().enableModule(module)) {
                    commandContext.getResponseSender().sendMessage("Module enabled successfully").queue();
                } else {
                    commandContext.getResponseSender().sendMessage("Module could not be enabled").queue();
                }

            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Enable a module";
        }
    }


    public static class ModuleDisableCommand extends SubCommandOption {

        protected ModuleDisableCommand(String name) {
            super(name);

            getOptions().add(new CommandValueOption(OptionType.STRING, "module", "Module to disable", false, false));
        }

        @Override
        public void onInvoke(@NotNull CommandContext commandContext) {
            var module = (String) commandContext.getOptions().get("module").getValue();
            if (module.equals("all")) {
                commandContext.getLauncher().getModuleManager().disableAllModules();
                commandContext.getResponseSender().sendMessage("All modules disabled successfully").queue();
            } else {
                if (commandContext.getLauncher().getModuleManager().disableModule(module)) {
                    commandContext.getResponseSender().sendMessage("Module disabled successfully").queue();
                } else {
                    commandContext.getResponseSender().sendMessage("Module could not be disabled").queue();
                }
            }
        }

        @Override
        public @NotNull String getDescription() {
            return "Disable a module";
        }
    }
}
