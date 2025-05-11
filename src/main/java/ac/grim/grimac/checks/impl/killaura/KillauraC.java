package ac.grim.grimac.checks.impl.killaura;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
// MultiactionsA
@CheckData(name = "KillauraC")
public class KillauraC extends Check implements PacketCheck {
    public KillauraC(GrimPlayer player) {
        super(player);
    }

    private double buffer, maxBuffer;
    private boolean cancelHits;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.packetStateData.isSlowedByUsingItem() && (player.packetStateData.lastSlotSelected == player.packetStateData.getSlowedByUsingItemSlot() || player.packetStateData.eatingHand == InteractionHand.OFF_HAND) && event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            if (new WrapperPlayClientInteractEntity(event).getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (shouldModifyPackets()) {
                    buffer++;
                } else {
                    buffer = 0;
                }
                if (buffer > maxBuffer) {
                    buffer = 0;
                    if (cancelHits) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @Override
    public void onReload(ConfigManager configManager) {
        this.maxBuffer = configManager.getDoubleElse("KillauraC.buffer", 1);
        this.cancelHits = configManager.getBooleanElse("KillauraC.cancelHits", true);
    }
}
