package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;

public class GrimHelp implements BuildableCommand {
    @Override
    public void register(CommandManager<Sender> commandManager) {
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("help", Description.of("Display help information"))
                        .permission("grim.help")
                        .handler(this::handleHelp)
        );
    }

    private void handleHelp(@NonNull CommandContext<Sender> context) {
        Sender sender = context.sender();

        for (String string : GrimAPI.INSTANCE.getConfigManager().getConfig().getStringList("help")) {
            string = MessageUtil.replacePlaceholders(sender, string);
            sender.sendMessage(MessageUtil.miniMessage(string));
        }
    }
}
