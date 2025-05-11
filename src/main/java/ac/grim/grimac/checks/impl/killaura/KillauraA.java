package ac.grim.grimac.checks.impl.killaura;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.packet.Packet;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "KillauraA")
public class KillauraA extends Check implements PacketCheck {
    public KillauraA(final GrimPlayer player) {
        super(player);
    }

    private boolean sentAnimation = player.getClientVersion().isNewerThan(ClientVersion.V_1_8);
    private boolean cancelHits;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (Packet.isAnimation(event)) {
            sentAnimation = true;
        } else if (Packet.isAttack(event)) {

            if (player.getClientVersion().isOlderThan(ClientVersion.V_1_9)
                    && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9))
                return;

            if (!sentAnimation && shouldModifyPackets()) {
                flagAndAlert();
                if (cancelHits) event.setCancelled(true);
            }

            sentAnimation = false;
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.cancelHits = configManager.getBooleanElse("KillauraA.cancelHits", true);
    }
}
