package ac.grim.grimac.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.github.retrooper.packetevents.PacketEvents;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.viaversion.ViaVersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

@CommandAlias("grim|grimac")
public class GrimDump extends BaseCommand {

    @Subcommand("dump")
    @CommandPermission("grim.dump")
    public void onCommand(CommandSender sender) {
        if (link != null) {
            MessageUtil.sendMessage(sender, MessageUtil.miniMessage(GrimAPI.INSTANCE.getConfigManager().getConfig()
                    .getStringElse("upload-log", "%prefix% &fUploaded debug to: %url%")
                    .replace("%url%", link)));
            return;
        }
        // TODO: change this back to application/json once allowed
        GrimLog.sendLogAsync(sender, generateDump(), string -> link = string, "text/yaml");
    }

    private String link = null; // these links should not expire for a while

    // this will help for debugging & replicating issues
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
        versions.addProperty("implementation", Bukkit.getVersion());
        // properties
        JsonArray properties = new JsonArray();
        base.add("properties", properties);
        if (PAPER) properties.add("paper");
        if (FoliaScheduler.isFolia()) properties.add("folia");
        if (ViaVersionUtil.isAvailable()) properties.add("viaversion");
        // system
        JsonObject system = new JsonObject();
        base.add("system", system);
        system.addProperty("os", System.getProperty("os.name"));
        system.addProperty("java", System.getProperty("java.version"));
        // plugins
        JsonArray plugins = new JsonArray();
        base.add("plugins", plugins);
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            JsonObject pluginJson = new JsonObject();
            pluginJson.addProperty("enabled", plugin.isEnabled());
            pluginJson.addProperty("name", plugin.getName());
            pluginJson.addProperty("version", plugin.getDescription().getVersion());
            plugins.add(pluginJson);
        }
        return gson.toJson(base);
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final boolean PAPER = hasClass("com.destroystokyo.paper.PaperConfig")
            || hasClass("io.papermc.paper.configuration.Configuration");

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
