package ac.grim.grimac.predictionengine;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.data.attribute.ValuedAttribute;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.enums.FluidTag;
import ac.grim.grimac.utils.enums.Pose;
import ac.grim.grimac.utils.latency.CompensatedEntities;
import ac.grim.grimac.utils.math.GrimMath;
import ac.grim.grimac.utils.math.Vector3dm;
import ac.grim.grimac.utils.nmsutil.BlockProperties;
import ac.grim.grimac.utils.nmsutil.CheckIfChunksLoaded;
import ac.grim.grimac.utils.nmsutil.Collisions;
import ac.grim.grimac.utils.nmsutil.FluidTypeFlowing;
import ac.grim.grimac.utils.nmsutil.GetBoundingBox;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;

import java.util.Optional;

public final class PlayerBaseTick {

    public static boolean canEnterPose(GrimPlayer player, Pose pose, double x, double y, double z) {
        return Collisions.isEmpty(player, getBoundingBoxForPose(player, pose, x, y, z).expand(-1.0E-7D));
    }

    private static SimpleCollisionBox getBoundingBoxForPose(GrimPlayer player, Pose pose, double x, double y, double z) {
        final float scale = (float) player.compensatedEntities.self.getAttributeValue(Attributes.SCALE);
        final float width = pose.width * scale;
        final float height = pose.height * scale;
        float radius = width / 2.0F;
        return new SimpleCollisionBox(x - radius, y, z - radius, x + radius, y + height, z + radius, false);
    }

    public static void doBaseTick(GrimPlayer player) {
        // Keep track of basetick stuff
        player.baseTickAddition = new Vector3dm();
        player.baseTickWaterPushing = new Vector3dm();

        if (player.isFlying && player.isSneaking && !player.inVehicle()) {
            Vector3dm flyingShift = new Vector3dm(0, player.flySpeed * -3, 0);
            player.baseTickAddVector(flyingShift);
            player.trackBaseTickAddition(flyingShift);
        }

        updateInWaterStateAndDoFluidPushing(player);
        updateFluidOnEyes(player);
        updateSwimming(player);

        // If in lava, fall distance is multiplied by 0.5
        if (player.wasTouchingLava)
            player.fallDistance *= 0.5;

        // You cannot crouch while flying, only shift - could be specific to 1.14?
        // pre-1.13 clients don't have this code
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13) && player.wasTouchingWater && player.isSneaking && !player.isFlying && !player.inVehicle()) {
            Vector3dm waterPushVector = new Vector3dm(0, -0.04f, 0);
            player.baseTickAddVector(waterPushVector);
            player.trackBaseTickAddition(waterPushVector);
        }

        player.lastPose = player.pose;

        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_13_2)) {
            // 1.13.2 and below logic: If crouching, then slow movement, simple!
            player.isSlowMovement = player.isSneaking;
        } else {
            player.isSlowMovement =
                    !player.wasFlying && !player.isSwimming && canEnterPose(player, Pose.CROUCHING, player.lastX, player.lastY, player.lastZ)
                            && (player.wasSneaking || !player.isInBed && !canEnterPose(player, Pose.STANDING, player.lastX, player.lastY, player.lastZ)) ||
                            // If the player is in the swimming pose
                            // Or if the player is not gliding, and the player's pose is fall flying
                            // and the player is not touching water (yes, this also can override the gliding slowness)
                            ((player.pose == Pose.SWIMMING || (!player.isGliding && player.pose == Pose.FALL_FLYING)) && !player.wasTouchingWater);

            // Mojang also accidentally left this in with 1.14-1.14.4
            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14) && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_14_4)) {
                player.isSlowMovement |= player.isSneaking;
            }
        }

        if (player.inVehicle()) player.isSlowMovement = false;

        // Players in boats don't care about being in blocks
        if (!player.inVehicle()) {
            moveTowardsClosestSpace(player, player.lastX - (player.boundingBox.maxX - player.boundingBox.minX) * 0.35, player.lastZ + (player.boundingBox.maxZ - player.boundingBox.minZ) * 0.35);
            moveTowardsClosestSpace(player, player.lastX - (player.boundingBox.maxX - player.boundingBox.minX) * 0.35, player.lastZ - (player.boundingBox.maxZ - player.boundingBox.minZ) * 0.35);
            moveTowardsClosestSpace(player, player.lastX + (player.boundingBox.maxX - player.boundingBox.minX) * 0.35, player.lastZ - (player.boundingBox.maxZ - player.boundingBox.minZ) * 0.35);
            moveTowardsClosestSpace(player, player.lastX + (player.boundingBox.maxX - player.boundingBox.minX) * 0.35, player.lastZ + (player.boundingBox.maxZ - player.boundingBox.minZ) * 0.35);
        }

        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_14)) {
            updatePlayerSize(player);
        }
    }

    // 1.16 eye in water is a tick behind
    // 1.15 eye in water is the most recent result
    private static void updateFluidOnEyes(GrimPlayer player) {
        player.wasEyeInWater = player.isEyeInFluid(FluidTag.WATER);
        player.fluidOnEyes = null;

        double d0 = player.lastY + player.getEyeHeight() - 0.1111111119389534D;

        final PacketEntity riding = player.compensatedEntities.self.getRiding();
        if (riding != null && riding.isBoat() && !player.vehicleData.boatUnderwater && player.boundingBox.maxY >= d0 && player.boundingBox.minY <= d0) {
            return;
        }

        double d1 = (float) Math.floor(d0) + player.compensatedWorld.getWaterFluidLevelAt(player.lastX, d0, player.lastZ);
        if (d1 > d0) {
            player.fluidOnEyes = FluidTag.WATER;
            if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_15_2))
                player.wasEyeInWater = true;
            return;
        }

        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_15_2))
            player.wasEyeInWater = false;

        d1 = (float) Math.floor(d0) + player.compensatedWorld.getWaterFluidLevelAt(player.lastX, d0, player.lastZ);
        if (d1 > d0) {
            player.fluidOnEyes = FluidTag.LAVA;
        }
    }

    private static void updateInWaterStateAndDoFluidPushing(GrimPlayer player) {
        updateInWaterStateAndDoWaterCurrentPushing(player);
        final double multiplier = player.dimensionType.isUltraWarm() ? 0.007 : 0.0023333333333333335;
        // 1.15 and below clients use block collisions to check for being in lava
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_16))
            player.wasTouchingLava = updateFluidHeightAndDoFluidPushing(player, FluidTag.LAVA, multiplier);
            // 1.13 and below clients use this stupid method to check if in lava
        else if (player.getClientVersion().isOlderThan(ClientVersion.V_1_14)) {
            SimpleCollisionBox playerBox = player.boundingBox.copy().expand(-0.1F, -0.4F, -0.1F);
            player.wasTouchingLava = player.compensatedWorld.containsLava(playerBox);
        }
    }

    public static void updatePowderSnow(GrimPlayer player) {
        // Pre-1.17 clients don't have powder snow and therefore don't desync
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_16_4)) return;

        final ValuedAttribute playerSpeed = player.compensatedEntities.self.getAttribute(Attributes.MOVEMENT_SPEED).orElseThrow();

        // Might be null after respawn?
        final Optional<WrapperPlayServerUpdateAttributes.Property> property = playerSpeed.property();
        if (property.isEmpty()) return;

        // The client first desync's this attribute
        property.get().getModifiers().removeIf(modifier -> modifier.getUUID().equals(CompensatedEntities.SNOW_MODIFIER_UUID) || modifier.getName().getKey().equals("powder_snow"));
        playerSpeed.recalculate();

        // And then re-adds it using purely what the server has sent it
        StateType type = BlockProperties.getOnPos(player, player.mainSupportingBlockData, new Vector3d(player.x, player.y, player.z));

        if (!type.isAir()) {
            int i = player.powderSnowFrozenTicks;
            if (i > 0) {
                int ticksToFreeze = 140;
                // Remember, floats are not commutative, we must do it in the client's specific order
                float percentFrozen = (float) Math.min(i, ticksToFreeze) / (float) ticksToFreeze;
                float percentFrozenReducedToSpeed = -0.05F * percentFrozen;

                property.get().getModifiers().add(new WrapperPlayServerUpdateAttributes.PropertyModifier(CompensatedEntities.SNOW_MODIFIER_UUID, percentFrozenReducedToSpeed, WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION));
                playerSpeed.recalculate();
            }
        }
    }

    // 1.14
    public static void updatePlayerPose(GrimPlayer player) {
        if (canEnterPose(player, Pose.SWIMMING, player.x, player.y, player.z)) {
            Pose pose;
            if (player.isGliding) {
                pose = Pose.FALL_FLYING;
            } else if (player.isInBed) {
                pose = Pose.SLEEPING;
            } else if (player.isSwimming) {
                pose = Pose.SWIMMING;
            } else if (player.isRiptidePose) {
                pose = Pose.SPIN_ATTACK;
            } else if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) && player.getClientVersion().isOlderThan(ClientVersion.V_1_14) && player.isSneaking) {
                pose = Pose.NINE_CROUCHING;
            } else if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14) && player.isSneaking && !player.isFlying) {
                pose = Pose.CROUCHING;
            } else {
                pose = Pose.STANDING;
            }

            // I'm not too sure about this code, but it appears like this is only a 1.14+ feature
            // In my testing this seems good but still don't have full confidence for versions like 1.13
            if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14) &&
                    !player.inVehicle() && !canEnterPose(player, pose, player.x, player.y, player.z)) {
                if (canEnterPose(player, Pose.CROUCHING, player.x, player.y, player.z)) {
                    pose = Pose.CROUCHING;
                } else {
                    pose = Pose.SWIMMING;
                }
            }

            player.pose = pose;
            player.boundingBox = getBoundingBoxForPose(player, player.pose, player.x, player.y, player.z);
        }
    }

    // 1.13 and below
    private static void updatePlayerSize(GrimPlayer player) {
        Pose pose;
        if (player.isGliding) {
            pose = Pose.FALL_FLYING;
        } else if (player.isInBed) {
            pose = Pose.SLEEPING;
        } else if (!player.isSwimming && !player.isRiptidePose) {
            if (player.isSneaking && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) {
                pose = Pose.NINE_CROUCHING;
            } else {
                pose = Pose.STANDING;
            }
        } else {
            pose = Pose.SWIMMING;
        }

        // 1.13 actually compares widths and heights etc. but this should also work.
        if (pose != player.pose) {
            Pose oldPose = player.pose;
            player.pose = pose;

            SimpleCollisionBox box = GetBoundingBox.getCollisionBoxForPlayer(player, player.lastX, player.lastY, player.lastZ);
            boolean collides = !Collisions.isEmpty(player, box);

            if (collides) {
                // Revert, the player does not have room to enter this new pose
                player.pose = oldPose;
                return;
            }
        }

        player.boundingBox = GetBoundingBox.getCollisionBoxForPlayer(player, player.lastX, player.lastY, player.lastZ);
    }

    private static void updateSwimming(GrimPlayer player) {
        // This doesn't seem like the right place for determining swimming, but it's fine for now
        if (player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_12_2)) {
            player.isSwimming = false;
        } else if (player.isFlying) {
            player.isSwimming = false;
        } else {
            if (player.inVehicle()) {
                player.isSwimming = false;
            } else if (player.isSwimming) {
                player.isSwimming = player.lastSprinting && player.wasTouchingWater;
            } else {
                // Requirement added in 1.17 to fix player glitching between two swimming states
                // while swimming with feet in air and eyes in water
                boolean feetInWater = player.getClientVersion().isOlderThan(ClientVersion.V_1_17)
                        || player.compensatedWorld.getWaterFluidLevelAt(player.lastX, player.lastY, player.lastZ) > 0;
                player.isSwimming = player.lastSprinting && player.wasEyeInWater && player.wasTouchingWater && feetInWater;
            }
        }
    }

    private static void moveTowardsClosestSpace(GrimPlayer player, double xPosition, double zPosition) {
        double movementThreshold = player.getMovementThreshold();
        player.boundingBox = player.boundingBox.expand(movementThreshold, 0, movementThreshold); // 0.03... thanks mojang!
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14)) {
            moveTowardsClosestSpaceModern(player, xPosition, zPosition);
        } else {
            moveTowardsClosestSpaceLegacy(player, xPosition, zPosition);
        }
        player.boundingBox = player.boundingBox.expand(-movementThreshold, 0, -movementThreshold);
    }

    // Mojang is incompetent and this will push the player out a lot when using elytras
    private static void moveTowardsClosestSpaceLegacy(GrimPlayer player, double x, double z) {
        int floorX = GrimMath.floor(x);
        int floorZ = GrimMath.floor(z);
        int floorY = GrimMath.floor(player.lastY + 0.5);

        double d0 = x - floorX;
        double d1 = z - floorZ;

        boolean suffocates;

        if (player.isSwimming) {
            SimpleCollisionBox blockPos = new SimpleCollisionBox(floorX, floorY, floorZ, floorX + 1.0, floorY + 1, floorZ + 1.0, false).expand(-1.0E-7);
            suffocates = Collisions.suffocatesAt(player, blockPos);
        } else {
            suffocates = !clearAbove(player, floorX, floorY, floorZ);
        }

        if (suffocates) {
            int i = -1;
            double d2 = 9999.0D;
            if (clearAbove(player, floorX - 1, floorY, floorZ) && d0 < d2) {
                d2 = d0;
                i = 0;
            }

            if (clearAbove(player, floorX + 1, floorY, floorZ) && 1.0D - d0 < d2) {
                d2 = 1.0D - d0;
                i = 1;
            }

            if (clearAbove(player, floorX, floorY, floorZ - 1) && d1 < d2) {
                d2 = d1;
                i = 4;
            }

            if (clearAbove(player, floorX, floorY, floorZ + 1) && 1.0D - d1 < d2) {
                i = 5;
            }

            if (i == 0) { // Negative X
                player.uncertaintyHandler.xNegativeUncertainty -= 0.1;
                player.uncertaintyHandler.xPositiveUncertainty += 0.1;
                player.pointThreeEstimator.setPushing(true);
            }

            if (i == 1) { // Positive X
                player.uncertaintyHandler.xNegativeUncertainty -= 0.1;
                player.uncertaintyHandler.xPositiveUncertainty += 0.1;
                player.pointThreeEstimator.setPushing(true);
            }

            if (i == 4) { // Negative Z
                player.uncertaintyHandler.zNegativeUncertainty -= 0.1;
                player.uncertaintyHandler.zPositiveUncertainty += 0.1;
                player.pointThreeEstimator.setPushing(true);
            }

            if (i == 5) { // Positive Z
                player.uncertaintyHandler.zNegativeUncertainty -= 0.1;
                player.uncertaintyHandler.zPositiveUncertainty += 0.1;
                player.pointThreeEstimator.setPushing(true);
            }
        }
    }

    // 1.14+
    private static void moveTowardsClosestSpaceModern(GrimPlayer player, double xPosition, double zPosition) {
        int blockX = (int) Math.floor(xPosition);
        int blockZ = (int) Math.floor(zPosition);

        if (!suffocatesAt(player, blockX, blockZ)) {
            return;
        }

        double relativeXMovement = xPosition - blockX;
        double relativeZMovement = zPosition - blockZ;
        BlockFace direction = null;
        double lowestValue = Double.MAX_VALUE;
        for (BlockFace direction2 : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH}) {
            double d6;
            double d7 = direction2 == BlockFace.WEST || direction2 == BlockFace.EAST ? relativeXMovement : relativeZMovement;
            d6 = direction2 == BlockFace.EAST || direction2 == BlockFace.SOUTH ? 1.0 - d7 : d7;
            // d7 and d6 flip the movement direction based on desired movement direction
            boolean doesSuffocate = switch (direction2) {
                case EAST -> suffocatesAt(player, blockX + 1, blockZ);
                case WEST -> suffocatesAt(player, blockX - 1, blockZ);
                case NORTH -> suffocatesAt(player, blockX, blockZ - 1);
                default -> suffocatesAt(player, blockX, blockZ + 1);
            };

            if (d6 >= lowestValue || doesSuffocate) continue;
            lowestValue = d6;
            direction = direction2;
        }
        if (direction != null) {
            if (direction == BlockFace.WEST || direction == BlockFace.EAST) {
                player.uncertaintyHandler.xPositiveUncertainty += 0.15;
                player.uncertaintyHandler.xNegativeUncertainty -= 0.15;
                player.pointThreeEstimator.setPushing(true);
            } else {
                player.uncertaintyHandler.zPositiveUncertainty += 0.15;
                player.uncertaintyHandler.zNegativeUncertainty -= 0.15;
                player.pointThreeEstimator.setPushing(true);
            }
        }
    }

    public static void updateInWaterStateAndDoWaterCurrentPushing(GrimPlayer player) {
        final PacketEntity riding = player.compensatedEntities.self.getRiding();
        player.wasTouchingWater = updateFluidHeightAndDoFluidPushing(player, FluidTag.WATER, 0.014) && !(riding != null && riding.isBoat());
        if (player.wasTouchingWater)
            player.fallDistance = 0;
    }

    private static boolean updateFluidHeightAndDoFluidPushing(GrimPlayer player, FluidTag tag, double multiplier) {
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_13)) {
            return updateFluidHeightAndDoFluidPushingModern(player, tag, multiplier);
        }

        return updateFluidHeightAndDoFluidPushingLegacy(player, tag, multiplier);
    }

    private static boolean updateFluidHeightAndDoFluidPushingLegacy(GrimPlayer player, FluidTag tag, double multiplier) {
        SimpleCollisionBox aABB = player.boundingBox.copy().expand(0, -0.4, 0).expand(-0.001);

        int floorX = GrimMath.floor(aABB.minX);
        int ceilX = GrimMath.ceil(aABB.maxX);
        int floorY = GrimMath.floor(aABB.minY);
        int ceilY = GrimMath.ceil(aABB.maxY);
        int floorZ = GrimMath.floor(aABB.minZ);
        int ceilZ = GrimMath.ceil(aABB.maxZ);
        if (CheckIfChunksLoaded.isChunksUnloadedAt(player, floorX, floorY, floorZ, ceilX, ceilY, ceilZ)) {
            return false;
        }

        boolean hasPushed = false;
        Vector3dm vec3 = new Vector3dm();

        for (int x = floorX; x < ceilX; ++x) {
            for (int y = floorY; y < ceilY; ++y) {
                for (int z = floorZ; z < ceilZ; ++z) {
                    double fluidHeight;
                    if (tag == FluidTag.WATER) {
                        fluidHeight = player.compensatedWorld.getWaterFluidLevelAt(x, y, z);
                    } else {
                        fluidHeight = player.compensatedWorld.getLavaFluidLevelAt(x, y, z);
                    }

                    if (fluidHeight == 0)
                        continue;

                    double d0 = (float) (y + 1) - fluidHeight;

                    if (!player.isFlying && ceilY >= d0) {
                        hasPushed = true;
                        vec3.add(FluidTypeFlowing.getFlow(player, x, y, z));
                    }
                }
            }
        }

        // all clients using legacy fluid pushing are not pushed by lava
        if (tag == FluidTag.WATER && vec3.lengthSquared() > 0.0) {
            vec3.normalize();
            vec3.multiply(multiplier);
            player.baseTickAddWaterPushing(vec3);
            player.baseTickAddVector(vec3);
        }

        return hasPushed;
    }

    private static boolean updateFluidHeightAndDoFluidPushingModern(GrimPlayer player, FluidTag tag, double multiplier) {
        SimpleCollisionBox aABB = player.boundingBox.copy().expand(-0.001);

        int floorX = GrimMath.floor(aABB.minX);
        int ceilX = GrimMath.ceil(aABB.maxX);
        int floorY = GrimMath.floor(aABB.minY);
        int ceilY = GrimMath.ceil(aABB.maxY);
        int floorZ = GrimMath.floor(aABB.minZ);
        int ceilZ = GrimMath.ceil(aABB.maxZ);
        if (CheckIfChunksLoaded.isChunksUnloadedAt(player, floorX, floorY, floorZ, ceilX, ceilY, ceilZ)) {
            return false;
        }
        double d2 = 0.0;
        boolean hasTouched = false;
        Vector3dm vec3 = new Vector3dm();
        int n7 = 0;

        for (int x = floorX; x < ceilX; ++x) {
            for (int y = floorY; y < ceilY; ++y) {
                for (int z = floorZ; z < ceilZ; ++z) {
                    double fluidHeightToWorld;

                    double fluidHeight;
                    if (tag == FluidTag.WATER) {
                        fluidHeight = player.compensatedWorld.getWaterFluidLevelAt(x, y, z);
                    } else {
                        fluidHeight = player.compensatedWorld.getLavaFluidLevelAt(x, y, z);
                    }

                    if (player.getClientVersion().isOlderThan(ClientVersion.V_1_14))
                        fluidHeight = Math.min(fluidHeight, 8 / 9D);

                    if (fluidHeight == 0 || (fluidHeightToWorld = y + fluidHeight) < aABB.minY)
                        continue;

                    hasTouched = true;
                    d2 = Math.max(fluidHeightToWorld - aABB.minY, d2);

                    if (!player.isFlying) {
                        Vector3dm vec32 = FluidTypeFlowing.getFlow(player, x, y, z);
                        if (d2 < 0.4) {
                            vec32 = vec32.multiply(d2);
                        }
                        vec3 = vec3.add(vec32);
                        ++n7;
                    }
                }
            }
        }

        if (vec3.lengthSquared() > 0.0) {
            if (n7 > 0) {
                vec3 = vec3.multiply(1.0 / n7);
            }

            if (player.inVehicle()) {
                // This is a riding entity, normalize it for some reason.
                vec3 = vec3.normalize();
            }

            // If the player is using 1.16+ - 1.15 and below don't have lava pushing
            if (tag != FluidTag.LAVA || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_16)) {
                vec3 = vec3.multiply(multiplier);
                // Store the vector before handling 0.003, so knockback can use it
                // However, do this after the multiplier, so that we don't have to recompute it
                player.baseTickAddWaterPushing(vec3);
                if (Math.abs(player.clientVelocity.getX()) < 0.003 && Math.abs(player.clientVelocity.getZ()) < 0.003 && vec3.length() < 0.0045000000000000005D) {
                    vec3 = vec3.normalize().multiply(0.0045000000000000005);
                }

                player.baseTickAddVector(vec3);
            }
        }

        if (tag == FluidTag.LAVA) {
            player.slightlyTouchingLava = hasTouched && d2 <= 0.4D;
        }

        if (tag == FluidTag.WATER) {
            player.slightlyTouchingWater = hasTouched && d2 <= 0.4D;
        }

        return hasTouched;
    }

    private static boolean suffocatesAt(GrimPlayer player, int x, int z) {
        SimpleCollisionBox axisAlignedBB = new SimpleCollisionBox(x, player.boundingBox.minY, z, x + 1.0, player.boundingBox.maxY, z + 1.0, false).expand(-1.0E-7);
        return Collisions.suffocatesAt(player, axisAlignedBB);
    }

    private static boolean clearAbove(GrimPlayer player, int x, int y, int z) {
        return !Collisions.doesBlockSuffocate(player, x, y, z) && !Collisions.doesBlockSuffocate(player, x, y + 1, z);
    }
}
