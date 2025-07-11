package ac.grim.grimac.checks.impl.velocity;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import ac.grim.grimac.utils.data.VectorData;
import ac.grim.grimac.utils.data.VelocityData;
import ac.grim.grimac.utils.math.Vector3dm;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateValue;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.LinkedList;

@CheckData(name = "AntiExplosion", configName = "Explosion", setback = 10)
public class ExplosionHandler extends Check implements PostPredictionCheck {
    Deque<VelocityData> firstBreadMap = new LinkedList<>();

    VelocityData lastExplosionsKnownTaken = null;
    VelocityData firstBreadAddedExplosion = null;

    @Getter
    boolean explosionPointThree = false;

    double offsetToFlag;
    double setbackVL;

    public ExplosionHandler(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.EXPLOSION) {
            WrapperPlayServerExplosion explosion = new WrapperPlayServerExplosion(event);

            // Since 1.21.2, the server will instead send these changes via block change packets
            final boolean hasBlocks = PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_21_2);
            if (hasBlocks) {
                this.handleBlockExplosions(explosion);
            }

            Vector3d velocity = explosion.getKnockback();
            if (velocity != null && (velocity.x != 0 || velocity.y != 0 || velocity.z != 0)) {
                // No need to spam transactions
                if (!hasBlocks || explosion.getRecords().isEmpty()) player.sendTransaction();
                addPlayerExplosion(player.lastTransactionSent.get(), velocity);
                event.getTasksAfterSend().add(player::sendTransaction);
            }
        }
    }

    private void handleBlockExplosions(WrapperPlayServerExplosion explosion) {
        final @Nullable WrapperPlayServerExplosion.BlockInteraction blockInteraction = explosion.getBlockInteraction();
        final boolean shouldDestroy = blockInteraction != WrapperPlayServerExplosion.BlockInteraction.KEEP_BLOCKS;
        if (explosion.getRecords().isEmpty() || !shouldDestroy) {
            return;
        }

        player.sendTransaction();

        player.latencyUtils.addRealTimeTask(player.lastTransactionSent.get(), () -> {
            for (Vector3i record : explosion.getRecords()) {
                // Null OR not flip redstone blocks, then set to air
                if (blockInteraction != WrapperPlayServerExplosion.BlockInteraction.TRIGGER_BLOCKS) {
                    player.compensatedWorld.updateBlock(record.x, record.y, record.z, 0);
                } else {
                    // We need to flip redstone blocks, or do special things with other blocks
                    final WrappedBlockState state = player.compensatedWorld.getBlock(record);
                    final StateType type = state.getType();
                    if (BlockTags.CANDLES.contains(type) || BlockTags.CANDLE_CAKES.contains(type)) {
                        state.setLit(false);
                        continue;
                    } else if (type == StateTypes.BELL) {
                        // Does this affect anything? I don't know, I don't see anything that relies on whether a bell is ringing.
                        continue;
                    }

                    // Otherwise try and flip/open it.
                    final Object poweredValue = state.getInternalData().get(StateValue.POWERED);
                    final boolean canFlip = (poweredValue != null && !(Boolean) poweredValue) || type == StateTypes.LEVER;
                    if (canFlip) {
                        player.compensatedWorld.tickOpenable(record.x, record.y, record.z);
                    }
                }
            }
        });
    }

    public VelocityData getFutureExplosion() {
        // Chronologically in the future
        if (!firstBreadMap.isEmpty()) {
            return firstBreadMap.peek();
        }

        // Less in the future
        if (lastExplosionsKnownTaken != null) {
            return lastExplosionsKnownTaken;
        }

        // Uncertain, might be in the future
        if (player.firstBreadExplosion != null && player.likelyExplosions == null) {
            return player.firstBreadExplosion;
        } else if (player.likelyExplosions != null) { // Known to be in the present
            return player.likelyExplosions;
        }
        return null;
    }

    public boolean shouldIgnoreForPrediction(VectorData data) {
        if (data.isExplosion() && data.isFirstBreadExplosion()) {
            return player.firstBreadExplosion.offset > offsetToFlag;
        }
        return false;
    }

    public boolean wouldFlag() {
        return (player.likelyExplosions != null && player.likelyExplosions.offset > offsetToFlag) || (player.firstBreadExplosion != null && player.firstBreadExplosion.offset > offsetToFlag);
    }

    public void addPlayerExplosion(int breadOne, Vector3d explosion) {
        firstBreadMap.add(new VelocityData(-1, breadOne, player.getSetbackTeleportUtil().isSendingSetback, new Vector3dm(explosion.getX(), explosion.getY(), explosion.getZ())));
    }

    public void setPointThree(boolean isPointThree) {
        explosionPointThree = explosionPointThree || isPointThree;
    }

    public void handlePredictionAnalysis(double offset) {
        if (player.firstBreadExplosion != null) {
            player.firstBreadExplosion.offset = Math.min(player.firstBreadExplosion.offset, offset);
        }

        if (player.likelyExplosions != null) {
            player.likelyExplosions.offset = Math.min(player.likelyExplosions.offset, offset);
        }
    }

    public void forceExempt() {
        // Unsure explosion was taken
        if (player.firstBreadExplosion != null) {
            player.firstBreadExplosion.offset = 0;
        }

        if (player.likelyExplosions != null) {
            player.likelyExplosions.offset = 0;
        }
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        double offset = predictionComplete.getOffset();

        boolean wasZero = explosionPointThree;
        explosionPointThree = false;

        if (player.likelyExplosions == null && player.firstBreadExplosion == null) {
            firstBreadAddedExplosion = null;
            return;
        }

        // We must check to see if knockback has overridden this explosion
        // (Yes, I could make this very simple and exempt on kb, but that allows people to ignore most explosions)
        //
        // We do this by finding the minimum explosion transaction that could have been overridden
        // We then compare this against the maximum velocity transaction that could override
        //
        // If velocity is over transaction, exempt
        int minTrans = Math.min(player.likelyExplosions != null ? player.likelyExplosions.transaction : Integer.MAX_VALUE,
                player.firstBreadExplosion != null ? player.firstBreadExplosion.transaction : Integer.MAX_VALUE);
        int kbTrans = Math.max(player.likelyKB != null ? player.likelyKB.transaction : Integer.MIN_VALUE,
                player.firstBreadKB != null ? player.firstBreadKB.transaction : Integer.MIN_VALUE);

        if (player.predictedVelocity.isFirstBreadExplosion()) {
            firstBreadAddedExplosion = null;
            firstBreadMap.poll(); // Remove from map so we don't pull it again
        }

        if (wasZero || player.predictedVelocity.isExplosion() ||
                (minTrans < kbTrans)) {
            // Unsure knockback was taken
            if (player.firstBreadExplosion != null) {
                player.firstBreadExplosion.offset = Math.min(player.firstBreadExplosion.offset, offset);
            }

            if (player.likelyExplosions != null) {
                player.likelyExplosions.offset = Math.min(player.likelyExplosions.offset, offset);
            }
        }

        // 100% known kb was taken
        if (player.likelyExplosions != null && !player.compensatedEntities.self.isDead) {
            if (player.likelyExplosions.offset > offsetToFlag) {
                flagAndAlertWithSetback(player.likelyExplosions.offset == Integer.MAX_VALUE ? "ignored explosion" : "o: " + formatOffset(offset));
            } else {
                reward();
            }
        }
    }

    public VelocityData getPossibleExplosions(int lastTransaction, boolean isJustTesting) {
        handleTransactionPacket(lastTransaction);
        if (lastExplosionsKnownTaken == null)
            return null;

        VelocityData returnLastExplosion = lastExplosionsKnownTaken;
        if (!isJustTesting) {
            lastExplosionsKnownTaken = null;
        }
        return returnLastExplosion;
    }

    private void handleTransactionPacket(int transactionID) {
        VelocityData data = firstBreadMap.peek();
        while (data != null) {
            if (data.transaction == transactionID) { // First bread explosion
                if (lastExplosionsKnownTaken != null)
                    firstBreadAddedExplosion = new VelocityData(-1, data.transaction, data.isSetback, lastExplosionsKnownTaken.vector.clone().add(data.vector));
                else
                    firstBreadAddedExplosion = new VelocityData(-1, data.transaction, data.isSetback, data.vector);
                break; // All knockback after this will have not been applied
            } else if (data.transaction < transactionID) {
                if (lastExplosionsKnownTaken != null) {
                    lastExplosionsKnownTaken.vector.add(data.vector);
                } else {
                    lastExplosionsKnownTaken = new VelocityData(-1, data.transaction, data.isSetback, data.vector);
                }

                firstBreadAddedExplosion = null;
                firstBreadMap.poll();
                data = firstBreadMap.peek();
            } else { // We are too far ahead in the future
                break;
            }
        }
    }

    public VelocityData getFirstBreadAddedExplosion(int lastTransaction) {
        handleTransactionPacket(lastTransaction);
        return firstBreadAddedExplosion;
    }

    @Override
    public void onReload(ConfigManager config) {
        offsetToFlag = config.getDoubleElse("Explosion.threshold", 0.00001);
        setbackVL = config.getDoubleElse("Explosion.setbackvl", 10);
        if (setbackVL == -1) setbackVL = Double.MAX_VALUE;
    }

}
