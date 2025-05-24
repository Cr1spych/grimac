package ac.grim.grimac.platform.bukkit.initables;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.manager.init.start.AbstractTickEndEvent;
import ac.grim.grimac.platform.api.Platform;
import ac.grim.grimac.platform.bukkit.player.BukkitPlatformPlayer;
import ac.grim.grimac.platform.bukkit.utils.reflection.PaperUtils;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.lists.HookedListWrapper;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.reflection.Reflection;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

// Copied from: https://github.com/ThomasOM/Pledge/blob/master/src/main/java/dev/thomazz/pledge/inject/ServerInjector.java
@SuppressWarnings(value = {"unchecked", "deprecated"})
public class BukkitTickEndEvent extends AbstractTickEndEvent implements Listener {

    @Override
    public void start() {
        if (!super.shouldInjectEndTick()) {
            return;
        }
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_11_2) && !Boolean.getBoolean("paper.explicit-flush")) {
            LogUtil.warn("Reach.enable-post-packet=true but paper.explicit-flush=false, add \"-Dpaper.explicit-flush=true\" to your server's startup flags for fully functional extra reach accuracy.");
        }
        // this is necessary for folia
        if (GrimAPI.INSTANCE.getPlatform() == Platform.FOLIA) {
            PaperUtils.registerTickEndEvent(this, this::tickAllFoliaPlayers);
            return;
        }
        // if it fails to inject, try to use paper's event
        if (!injectWithReflection() && !PaperUtils.registerTickEndEvent(this, this::tickAllPlayers)) {
            LogUtil.error("Failed to inject into the end of tick event!");
        }
    }

    private void tickAllPlayers() {
        for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            if (player.disableGrim) continue;
            super.onEndOfTick(player);
        }
    }

    private void tickAllFoliaPlayers() {
        for (GrimPlayer player : GrimAPI.INSTANCE.getPlayerDataManager().getEntries()) {
            if (player.disableGrim) continue;
            if (player.platformPlayer == null) continue;
            Player p = ((BukkitPlatformPlayer) player.platformPlayer).getNative();
            if (!Bukkit.isOwnedByCurrentRegion(p)) continue;
            super.onEndOfTick(player);
        }
    }

    private boolean injectWithReflection() {
        // Inject so we can add the final transaction pre-flush event
        try {
            Object connection = SpigotReflectionUtil.getMinecraftServerConnectionInstance();

            Field connectionsList = Reflection.getField(connection.getClass(), List.class, 1);
            List<Object> endOfTickObject = (List<Object>) connectionsList.get(connection);

            // Use a list wrapper to check when the size method is called
            // Unsure why synchronized is needed because the object itself gets synchronized
            // but whatever.  At least plugins can't break it, I guess.
            //
            // Pledge injects into another list, so we should be safe injecting into this one
            List<?> wrapper = Collections.synchronizedList(new HookedListWrapper<>(endOfTickObject) {
                @Override
                public void onIterator() {
                    tickAllPlayers();
                }
            });

            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            unsafe.putObject(connection, unsafe.objectFieldOffset(connectionsList), wrapper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LogUtil.error("Failed to inject into the end of tick event via reflection", e);
            return false;
        }
        return true;
    }
}
