package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "InventoryA")
public class InventoryA extends Check implements PacketCheck {
    public InventoryA(GrimPlayer player) {
        super(player);
    }

    private boolean hasInventoryOpen;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLOSE_WINDOW) {
            hasInventoryOpen = false;
        }

        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            hasInventoryOpen = true;
        }

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (hasInventoryOpen) {
                    flagAndAlert();
                    event.setCancelled(true);
                }
            }
        }
    }
}
