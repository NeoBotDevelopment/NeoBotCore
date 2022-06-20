package page.nafuchoco.neobot.core.console;

import java.util.Arrays;

public class ConsoleCommandParser {
    private final ConsoleCommandRegistry registry;

    public ConsoleCommandParser(ConsoleCommandRegistry registry) {
        this.registry = registry;
    }

    public void fireCommand(String input) {
        // レジストリが登録されてない場合は無視
        if (registry == null)
            return;

        // コマンドオプションを分割
        String[] args = input.split("\\p{javaSpaceChar}+");
        if (args.length == 0)
            return;
        String commandTrigger = args[0];

        // コマンドクラスの取得
        ConsoleCommandExecutor command = registry.getExecutor(commandTrigger.toLowerCase());
        ConsoleCommandContext context;
        if (command == null)
            return;
        else
            context = new ConsoleCommandContext(commandTrigger, Arrays.copyOfRange(args, 1, args.length), command);

        context.commandExecutor().onInvoke(context);
    }
}
