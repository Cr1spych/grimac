package ac.grim.grimac.checks;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.event.events.FlagEvent;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.player.PTF;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

// Class from https://github.com/Tecnio/AntiCheatBase/blob/master/src/main/java/me/tecnio/anticheat/check/Check.java
@Getter
public class Check extends GrimProcessor implements AbstractCheck {
    protected @NotNull final GrimPlayer player;

    public double violations;
    private double decay;
    private double setbackVL;

    private String checkName;
    private String configName;
    private String alternativeName;
    private String displayName;
    private String description;

    private boolean experimental;
    private @Setter boolean isEnabled;

    private boolean exemptPermission;
    private boolean noSetbackPermission;
    private boolean noModifyPacketPermission;
    private long lastViolationTime;

    public Check(final @NotNull GrimPlayer player) {
        this.player = Objects.requireNonNull(player);

        final CheckData checkData = this.getClass().getAnnotation(CheckData.class);
        if (checkData != null) {
            this.checkName = checkData.name();
            this.configName = checkData.configName();
            // Fall back to check name
            if (this.configName.equals("DEFAULT")) this.configName = this.checkName;
            this.decay = checkData.decay();
            this.setbackVL = checkData.setback();
            this.alternativeName = checkData.alternativeName();
            this.experimental = checkData.experimental();
            this.description = checkData.description();
            this.displayName = this.checkName;
        }

        reload();
    }

    public boolean shouldModifyPackets() {
        return isEnabled && !player.disableGrim && !player.noModifyPacketPermission && !exemptPermission;
    }

    public void updatePermissions() {
        if (player.platformPlayer == null || configName == null) return;
        GrimAPI.INSTANCE.getScheduler().getEntityScheduler().run(
                player.platformPlayer,
                GrimAPI.INSTANCE.getGrimPlugin(),
                () -> {
                    final String id = configName.toLowerCase();
                    exemptPermission = player.platformPlayer.hasPermission("grim.exempt." + id);
                    noSetbackPermission = player.platformPlayer.hasPermission("grim.nosetback." + id);
                    noModifyPacketPermission = player.platformPlayer.hasPermission("grim.nomodifypacket." + id);
                },
                () -> {}
        );
    }

    public final boolean flagAndAlert(String verbose) {
        if (flag(verbose)) {
            alert(verbose);
            return true;
        }
        return false;
    }

    public final boolean flagAndAlert() {
        return flagAndAlert("");
    }

    public final boolean flag() {
        return flag("");
    }

    public final boolean flag(String verbose) {
        if (player.disableGrim || (experimental && !player.isExperimentalChecks()) || exemptPermission)
            return false; // Avoid calling event if disabled

        FlagEvent event = new FlagEvent(player, this, verbose);
        GrimAPI.INSTANCE.getEventBus().post(event);
        if (event.isCancelled()) return false;

        player.punishmentManager.handleViolation(this);
        lastViolationTime = System.currentTimeMillis();
        violations++;
        return true;
    }

    public final boolean flagWithSetback() {
        return flagWithSetback("");
    }

    // отправка статуса игрока. и, да, я ебланище, мне лень писать нормальный метод
    public void sendTrustFactor() {
        PTF currentTf = player.trustFactor;
        if (player.hasPermission("grim.alerts"));
    }

    public final boolean flagWithSetback(String verbose) {
        if (flag(verbose)) {
            setbackIfAboveSetbackVL();
            return true;
        }
        return false;
    }

    public final boolean flagAndAlertWithSetback() {
        return flagAndAlertWithSetback("");
    }

    public final boolean flagAndAlertWithSetback(String verbose) {
        if (flagAndAlert(verbose)) {
            setbackIfAboveSetbackVL();
            return true;
        }
        return false;
    }

    public final void reward() {
        violations = Math.max(0, violations - decay);
    }

    @Override
    public void reload(ConfigManager configuration) {
        decay = configuration.getDoubleElse(configName + ".decay", decay);
        setbackVL = configuration.getDoubleElse(configName + ".setbackvl", setbackVL);
        displayName = configuration.getStringElse(configName + ".displayname", checkName);
        description = configuration.getStringElse(configName + ".description", description);

        if (setbackVL == -1) setbackVL = Double.MAX_VALUE;
        updatePermissions();
        onReload(configuration);
    }

    @Override
    public void onReload(ConfigManager config) {

    }

    public boolean alert(String verbose) {
        return player.punishmentManager.handleAlert(player, verbose, this);
    }

    public boolean setbackIfAboveSetbackVL() {
        if (shouldSetback()) {
            return player.getSetbackTeleportUtil().executeViolationSetback();
        }
        return false;
    }

    public boolean shouldSetback() {
        return !noSetbackPermission && violations > setbackVL;
    }

    public String formatOffset(double offset) {
        return offset > 0.001 ? String.format("%.5f", offset) : String.format("%.2E", offset);
    }

    public boolean isTransaction(PacketTypeCommon packetType) {
        return packetType == PacketType.Play.Client.PONG ||
                packetType == PacketType.Play.Client.WINDOW_CONFIRMATION;
    }

    public boolean isFlying(PacketTypeCommon packetType) {
        return WrapperPlayClientPlayerFlying.isFlying(packetType);
    }

    public boolean isUpdate(PacketTypeCommon packetType) {
        return isFlying(packetType)
                || packetType == PacketType.Play.Client.CLIENT_TICK_END
                || isTransaction(packetType);
    }

    public boolean isTickPacket(PacketTypeCommon packetType) {
        if (isTickPacketIncludingNonMovement(packetType)) {
            if (isFlying(packetType)) {
                return !player.packetStateData.lastPacketWasTeleport && !player.packetStateData.lastPacketWasOnePointSeventeenDuplicate;
            }
            return true;
        }
        return false;
    }

    public boolean isTickPacketIncludingNonMovement(PacketTypeCommon packetType) {
        // On 1.21.2+ fall back to the TICK_END packet IF the player did not send a movement packet for their tick
        // TickTimer checks to see if player did not send a tick end packet before new flying packet is sent
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)
                && !player.packetStateData.didSendMovementBeforeTickEnd) {
            if (packetType == PacketType.Play.Client.CLIENT_TICK_END) {
                return true;
            }
        }

        return isFlying(packetType);
    }
}
