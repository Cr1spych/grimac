package ac.grim.grimac.predictionengine.predictions;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.VectorData;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.nmsutil.Collisions;
import ac.grim.grimac.utils.nmsutil.JumpPower;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;

import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;

public class PredictionEngineNormal extends PredictionEngine {

    public static void staticVectorEndOfTick(GrimPlayer player, Vector3dm vector) {
        double adjustedY = vector.getY();
        final OptionalInt levitation = player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.LEVITATION);
        if (levitation.isPresent()) {
            adjustedY += (0.05 * (levitation.getAsInt() + 1) - vector.getY()) * 0.2;
            // Reset fall distance with levitation
            player.fallDistance = 0;
        } else if (player.hasGravity) {
            adjustedY -= player.gravity;
        }

        vector.setX(vector.getX() * player.friction);
        vector.setY(adjustedY * 0.98F);
        vector.setZ(vector.getZ() * player.friction);
    }

    @Override
    public void addJumpsToPossibilities(GrimPlayer player, Set<VectorData> existingVelocities) {
        if (player.supportsEndTick() && !player.packetStateData.knownInput.jump()) {
            return;
        }

        for (VectorData vector : new HashSet<>(existingVelocities)) {
            Vector3dm jump = vector.vector.clone();

            if (!player.isFlying) {
                // Negative jump boost does not allow the player to leave the ground
                // Negative jump boost doesn't seem to work in water/lava
                // If the player didn't try to jump
                // And 0.03 didn't affect onGround status
                // The player cannot jump
                final OptionalInt jumpBoost = player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.JUMP_BOOST);
                if (((jumpBoost.isEmpty() || jumpBoost.getAsInt() >= 0) && player.onGround) || !player.lastOnGround)
                    return;

                JumpPower.jumpFromGround(player, jump);
            } else {
                jump.add(new Vector3dm(0, player.flySpeed * 3, 0));
                if (!player.wasFlying) {
                    Vector3dm edgeCaseJump = jump.clone();
                    JumpPower.jumpFromGround(player, edgeCaseJump);
                    existingVelocities.add(vector.returnNewModified(edgeCaseJump, VectorData.VectorType.Jump));
                }
            }

            existingVelocities.add(vector.returnNewModified(jump, VectorData.VectorType.Jump));
        }
    }

    @Override
    public void endOfTick(GrimPlayer player, double delta) {
        super.endOfTick(player, delta);

        boolean walkingOnPowderSnow = false;

        if (!player.inVehicle() && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_17) &&
                player.compensatedWorld.getBlockType(player.x, player.y, player.z) == StateTypes.POWDER_SNOW) {
            ItemStack boots = player.getInventory().getBoots();
            walkingOnPowderSnow = boots != null && boots.getType() == ItemTypes.LEATHER_BOOTS;
        }

        player.isClimbing = Collisions.onClimbable(player, player.x, player.y, player.z);

        // Force 1.13.2 and below players to have something to collide with horizontally to climb
        if (player.lastWasClimbing == 0 && (player.pointThreeEstimator.isNearClimbable() || player.isClimbing) && (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14)
                || !Collisions.isEmpty(player, player.boundingBox.copy().expand(
                player.clientVelocity.getX(), 0, player.clientVelocity.getZ()).expand(0.5, -SimpleCollisionBox.COLLISION_EPSILON, 0.5))) || walkingOnPowderSnow) {
            Vector3dm ladderVelocity = player.clientVelocity.clone().setY(0.2);
            staticVectorEndOfTick(player, ladderVelocity);
            player.lastWasClimbing = ladderVelocity.getY();
        }

        for (VectorData vector : player.getPossibleVelocitiesMinusKnockback()) {
            staticVectorEndOfTick(player, vector.vector);
        }
    }

    @Override
    public Vector3dm handleOnClimbable(Vector3dm vector, GrimPlayer player) {
        if (player.isClimbing) {
            // Reset fall distance when climbing
            player.fallDistance = 0;

            vector.setX(GrimMath.clamp(vector.getX(), -0.15F, 0.15F));
            vector.setZ(GrimMath.clamp(vector.getZ(), -0.15F, 0.15F));
            vector.setY(Math.max(vector.getY(), -0.15F));

            // Yes, this uses shifting not crouching
            if (vector.getY() < 0.0 && !(player.compensatedWorld.getBlockType(player.lastX, player.lastY, player.lastZ) == StateTypes.SCAFFOLDING) && player.isSneaking && !player.isFlying) {
                vector.setY(0.0);
            }
        }

        return vector;
    }
}
