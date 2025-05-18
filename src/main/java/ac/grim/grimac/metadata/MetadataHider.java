package ac.grim.grimac.metadata;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

@CheckData(name = "MetadataHider")
public class MetadataHider extends Check implements PacketCheck {
    boolean enable;
    boolean healthHider;
    boolean xpHider;
    boolean oxygenHider;
    boolean absorptionHider;
    boolean onlyForPlayers;

    public MetadataHider(GrimPlayer player) {
        super(player);
    }

    public void onPacketSend(PacketSendEvent event) {
        if (!this.player.disableGrim && !this.player.noModifyPacketPermission && this.enable && event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);
            int entityId = wrapper.getEntityId();
            if (event.getUser().getEntityId() != entityId) {
                PacketEntity packetEntity = this.player.compensatedEntities.getEntity(entityId);
                if (packetEntity != null && packetEntity.isLivingEntity()) {
                    List<EntityData> entityMetaData = wrapper.getEntityMetadata();
                    boolean shouldPush = false;
                    Random random = new Random();
                    Iterator iterator = entityMetaData.iterator();

                    while(true) {
                        while(iterator.hasNext()) {
                            EntityData data = (EntityData)iterator.next();
                            if (this.healthHider && data.getIndex() == MetadataIndex.HEALTH) {
                                float health = Float.parseFloat(String.valueOf(data.getValue()));
                                if (health > 0.0F) {
                                    float randomHealth = (float)(1 + random.nextInt(20));
                                    data.setValue(randomHealth);
                                    shouldPush = true;
                                }
                            } else if (this.xpHider && data.getIndex() == MetadataIndex.XP) {
                                this.setDynamicValue(data, 1000);
                                shouldPush = true;
                            }
                        }

                        if (shouldPush) {
                            this.push(event, wrapper.getEntityId(), entityMetaData);
                        }

                        return;
                    }
                }
            }
        }

    }

    void push(PacketSendEvent event, int entityId, List<EntityData> dataList) {
        event.setCancelled(true);
        WrapperPlayServerEntityMetadata metadata = new WrapperPlayServerEntityMetadata(entityId, dataList);
        ChannelHelper.runInEventLoop(this.player.user.getChannel(), () -> {
            this.player.user.sendPacketSilently(metadata);
        });
    }

    private void setDynamicValue(EntityData obj, int spoofValue) {
        Object value = obj.getValue();
        if (value instanceof Integer) {
            obj.setValue(spoofValue);
        } else if (value instanceof Short) {
            obj.setValue((short)spoofValue);
        } else if (value instanceof Byte) {
            obj.setValue((byte)spoofValue);
        } else if (value instanceof Long) {
            obj.setValue(spoofValue);
        } else if (value instanceof Float) {
            obj.setValue((float)spoofValue);
        } else if (value instanceof Double) {
            obj.setValue(spoofValue);
        }

    }

    public void onReload(ConfigManager config) {
        this.onlyForPlayers = config.getBooleanElse("visual.metadata-hider.onlyForPlayers", true);
        this.healthHider = config.getBooleanElse("Metadata.hideHealth", true);
        this.xpHider = config.getBooleanElse("Metadata.hideXp", true);
        this.enable = this.healthHider || this.xpHider || this.oxygenHider || this.absorptionHider;
    }
}
