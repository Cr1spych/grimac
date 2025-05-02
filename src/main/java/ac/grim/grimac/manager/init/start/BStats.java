package ac.grim.grimac.manager.init.start;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.manager.init.Initable;
import io.github.retrooper.packetevents.bstats.bukkit.Metrics;

public class BStats implements Initable {
    @Override
    public void start() {
        int pluginId = 12820; // <-- Replace with the id of your plugin!
        try {
            new Metrics(GrimAPI.INSTANCE.getPlugin(), pluginId);
        } catch (Exception ignored) {
        }
    }
}
