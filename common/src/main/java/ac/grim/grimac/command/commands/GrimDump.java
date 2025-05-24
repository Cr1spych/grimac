package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.platform.api.PlatformPlugin;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import ac.grim.grimac.utils.reflection.ReflectionUtils;
import ac.grim.grimac.utils.reflection.ViaVersionUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;

public class GrimDump implements BuildableCommand {

    private static final boolean PAPER = ReflectionUtils.hasClass("com.destroystokyo.paper.PaperConfig")
            || ReflectionUtils.hasClass("io.papermc.paper.configuration.Configuration");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String link = null; // these links should not expire for a while

    @Override
    public void register(CommandManager<Sender> commandManager) {
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("dump", Description.of("Generate a debug dump"))
                        .permission("grim.dump")
                        .handler(this::handleDump)
        );
    }

    private void handleDump(@NonNull CommandContext<Sender> context) {
        Sender sender = context.sender();

        if (link != null) {
            sender.sendMessage(MessageUtil.miniMessage(GrimAPI.INSTANCE.getConfigManager().getConfig()
                    .getStringElse("upload-log", "%prefix% &fUploaded debug to: %url%")
                    .replace("%url%", link)));
            return;
        }
        // TODO: change this back to application/json once allowed
        GrimLog.sendLogAsync(sender, generateDump(), string -> link = string, "text/yaml");
    }

    /**
     * Generates a diagnostic dump in JSON format that contains various metadata
     * about the system, platform, and plugins. This dump is primarily used for
     * debugging and finding potential issues with the environment.
     * @return A JSON-formatted string containing the diagnostic dump.
     */
    private String generateDump() {
        JsonObject base = new JsonObject();
        base.addProperty("type", "dump");
        base.addProperty("timestamp", System.currentTimeMillis());
        // versions
        JsonObject versions = new JsonObject();
        base.add("versions", versions);
        versions.addProperty("grim", GrimAPI.INSTANCE.getExternalAPI().getGrimVersion());
        versions.addProperty("packetevents", PacketEvents.getAPI().getVersion().toString());
        versions.addProperty("server", PacketEvents.getAPI().getServerManager().getVersion().getReleaseName());
        versions.addProperty("implementation", GrimAPI.INSTANCE.getPlatformServer().getPlatformImplementationString());
        // state of different properties
        JsonObject states = new JsonObject();
        base.add("states", states);
        if (GrimAPI.INSTANCE.isInitialized()) states.addProperty("platform", GrimAPI.INSTANCE.getPlatform().toString());
        if (ViaVersionUtil.isAvailable()) states.addProperty("has_viaversion", true);
        if (PAPER) states.addProperty("has_paper", true);
        // system
        JsonObject system = new JsonObject();
        base.add("system", system);
        system.addProperty("os_name", System.getProperty("os.name"));
        system.addProperty("java_version", System.getProperty("java.version"));
        system.addProperty("user_language", System.getProperty("user.language"));
        // plugins
        JsonArray plugins = new JsonArray();
        base.add("plugins", plugins);
        for (PlatformPlugin plugin : GrimAPI.INSTANCE.getPluginManager().getPlugins()) {
            JsonObject pluginJson = new JsonObject();
            pluginJson.addProperty("enabled", plugin.isEnabled());
            pluginJson.addProperty("name", plugin.getName());
            pluginJson.addProperty("version", plugin.getVersion());
            plugins.add(pluginJson);
        }
        return gson.toJson(base);
    }
}
