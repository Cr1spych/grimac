package ac.grim.grimac.utils.debug;

import ac.grim.grimac.player.GrimPlayer;

public class DebugUtil {

    public <T> void debug(T message) {
        System.out.println(message);
    }

    public void debugTo(GrimPlayer player, String message, boolean formatted) {
        if (formatted) {
            player.sendMessage("§7[DEBUG] §f" + message);
        } else {
            player.sendMessage(message);
        }
    }

    public void debugTo(GrimPlayer player, String message) {
        player.sendMessage("§7[DEBUG] §f" + message);
    }
}
