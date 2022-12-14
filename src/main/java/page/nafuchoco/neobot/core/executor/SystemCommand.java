package page.nafuchoco.neobot.core.executor;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.neobot.api.command.CommandContext;
import page.nafuchoco.neobot.api.command.CommandExecutor;
import page.nafuchoco.neobot.core.Main;

import java.lang.management.ManagementFactory;
import java.util.stream.Collectors;

public class SystemCommand extends CommandExecutor {

    public SystemCommand(String name) {
        super(name);
    }

    @Override
    public void onInvoke(@NotNull CommandContext commandContext) {
        long max = Runtime.getRuntime().maxMemory() / 1048576L;
        long total = Runtime.getRuntime().totalMemory() / 1048576L;
        long free = Runtime.getRuntime().freeMemory() / 1048576L;
        long used = total - free;
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        val shardManager = commandContext.getLauncher().getDiscordApi();

        val builder = new StringBuilder();
        builder.append("This Bot has been running for ").append(formatTime(uptime)).append(" since it was started.\n");
        builder.append("```\n");
        builder.append("====== System Info ======\n");
        builder.append("Operating System:      ").append(System.getProperty("os.name")).append("\n");
        builder.append("JVM Version:           ").append(System.getProperty("java.version")).append("\n");
        builder.append("NeoBotCore Version:    ").append(Main.class.getPackage().getImplementationVersion()).append("\n\n");
        builder.append("====== Memory Info ======\n");
        builder.append("Reserved memory:       ").append(total).append("MB\n");
        builder.append("  -> Used:             ").append(used).append("MB\n");
        builder.append("  -> Free:             ").append(free).append("MB\n");
        builder.append("Max. reserved memory:  ").append(max).append("MB\n\n");
        builder.append("====== Statistic Info ======\n");
        builder.append("Total shards:          ").append(shardManager.getShards().size()).append("\n");
        builder.append("Loaded modules:        ").append(commandContext.getLauncher().getModuleManager().getModules().size()).append("\n");
        builder.append(commandContext.getLauncher().getModuleManager().getModules().stream()
                .map(module -> module.getDescription().getName() + ": " + module.getDescription().getVersion())
                .sorted(String::compareTo)
                .collect(Collectors.joining(", ")));
        builder.append("```");
        commandContext.getResponseSender().sendMessage(builder.toString()).queue();
    }

    private String formatTime(long millis) {
        long t = millis / 1000L;
        int sec = (int) (t % 60L);
        int min = (int) ((t % 3600L) / 60L);
        int hrs = (int) (t / 3600L);

        String timestamp;

        if (hrs != 0)
            timestamp = hrs + "hr. " + min + "min. " + sec + "sec.";
        else
            timestamp = min + "min. " + sec + "sec.";
        return timestamp;
    }

    @Override
    public @NotNull String getDescription() {
        return "Displays information about the bot.";
    }

    @Override
    public boolean isEphemeral() {
        return true;
    }
}
