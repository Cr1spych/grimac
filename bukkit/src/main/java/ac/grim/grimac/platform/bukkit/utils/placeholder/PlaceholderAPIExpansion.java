package ac.grim.grimac.platform.bukkit.utils.placeholder;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.player.GrimPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "grim";
    }

    public @NotNull String getAuthor() {
        return String.join(", ", GrimAPI.INSTANCE.getGrimPlugin().getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return GrimAPI.INSTANCE.getExternalAPI().getGrimVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        Set<String> staticReplacements = GrimAPI.INSTANCE.getExternalAPI().getStaticReplacements().keySet();
        Set<String> variableReplacements = GrimAPI.INSTANCE.getExternalAPI().getVariableReplacements().keySet();
        ArrayList<String> placeholders = new ArrayList<>(staticReplacements.size() + variableReplacements.size());
        for (String s : staticReplacements) {
            placeholders.add(s.equals("%grim_version%") ? s : "%grim_" + s.replaceAll("%", "") + "%");
        }
        for (String s : variableReplacements) {
            placeholders.add(s.equals("%player%") ? "%grim_player%" : "%grim_player_" + s.replaceAll("%", "") + "%");
        }
        return placeholders;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        for (Map.Entry<String, String> entry : GrimAPI.INSTANCE.getExternalAPI().getStaticReplacements().entrySet()) {
            String key = entry.getKey().equals("%grim_version%")
                    ? "version"
                    : entry.getKey().replaceAll("%", "");
            if (params.equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        if (offlinePlayer instanceof Player player) {
            GrimPlayer grimPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(player.getUniqueId());
            if (grimPlayer == null) return null;

            for (Map.Entry<String, Function<GrimUser, String>> entry : GrimAPI.INSTANCE.getExternalAPI().getVariableReplacements().entrySet()) {
                String key = entry.getKey().equals("%player%")
                        ? "player"
                        : "player_" + entry.getKey().replaceAll("%", "");
                if (params.equalsIgnoreCase(key)) {
                    return entry.getValue().apply(grimPlayer);
                }
            }
        }

        return null;
    }
}
