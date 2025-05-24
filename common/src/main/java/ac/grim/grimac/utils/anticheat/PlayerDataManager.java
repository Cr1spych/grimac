package ac.grim.grimac.utils.anticheat;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.event.events.GrimJoinEvent;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.reflection.FloodgateUtil;
import ac.grim.grimac.utils.reflection.GeyserUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.player.User;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    public final Collection<User> exemptUsers = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<User, GrimPlayer> playerDataMap = new ConcurrentHashMap<>();

    @Nullable
    public GrimPlayer getPlayer(final @NonNull UUID uuid) {
        // Is it safe to interact with this, or is this internal PacketEvents code?
        Object channel = PacketEvents.getAPI().getProtocolManager().getChannel(uuid);
        User user = PacketEvents.getAPI().getProtocolManager().getUser(channel);
        return getPlayer(user);
    }

    @Nullable
    public GrimPlayer getPlayer(final @NonNull User user) {
        @Nullable GrimPlayer player = playerDataMap.get(user);
        if (player != null && player.platformPlayer != null && player.platformPlayer.isExternalPlayer())
            return null;
        return player;
    }

    public boolean shouldCheck(@NonNull User user) {
        if (exemptUsers.contains(user)) return false;
        if (!ChannelHelper.isOpen(user.getChannel())) return false;

        if (user.getUUID() != null) {
            // Geyser players don't have Java movement
            // Floodgate is the authentication system for Geyser on servers that use Geyser as a proxy instead of installing it as a plugin directly on the server
            if (GeyserUtil.isGeyserPlayer(user.getUUID()) || FloodgateUtil.isFloodgatePlayer(user.getUUID())) {
                exemptUsers.add(user);
                return false;
            }

            // Has exempt permission
            GrimPlayer grimPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(user);
            if (grimPlayer != null && grimPlayer.hasPermission("grim.exempt")) {
                exemptUsers.add(user);
                return false;
            }

            // Geyser formatted player string
            // This will never happen for Java players, as the first character in the 3rd group is always 4 (xxxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx)
            if (user.getUUID().toString().startsWith("00000000-0000-0000-0009")) {
                exemptUsers.add(user);
                return false;
            }
        }

        return true;
    }

    public void addUser(final @NonNull User user) {
        if (shouldCheck(user)) {
            GrimPlayer player = new GrimPlayer(user);
            playerDataMap.put(user, player);
            GrimAPI.INSTANCE.getEventBus().post(new GrimJoinEvent(player));
        }
    }

    public GrimPlayer remove(final @NonNull User user) {
        return playerDataMap.remove(user);
    }

    public Collection<GrimPlayer> getEntries() {
        return playerDataMap.values();
    }

    public int size() {
        return playerDataMap.size();
    }
}
