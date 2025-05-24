package ac.grim.grimac.platform.bukkit.utils.anticheat;

import ac.grim.grimac.utils.anticheat.LogUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class MultiLibUtil {

    public final static Method externalPlayerMethod = getMethod(Player.class, "isExternalPlayer");
    private static final boolean IS_PRE_1_18 = PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_18);

    public static Method getMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    // TODO: cache external players for better performance, but this only matters for people using multi-lib
    public static boolean isExternalPlayer(Player player) {
        if (externalPlayerMethod == null || IS_PRE_1_18) return false;
        try {
            return (boolean) externalPlayerMethod.invoke(player);
        } catch (Exception e) {
            LogUtil.error("Failed to invoke external player method", e);
            return false;
        }
    }


}
