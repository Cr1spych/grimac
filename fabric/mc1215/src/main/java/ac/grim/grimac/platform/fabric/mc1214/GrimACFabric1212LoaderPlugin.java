package ac.grim.grimac.platform.fabric.mc1214;

import ac.grim.grimac.platform.fabric.mc1214.command.Fabric1212PlayerSelectorAdapter;
import ac.grim.grimac.platform.fabric.command.FabricPlayerSelectorParser;
import ac.grim.grimac.platform.fabric.manager.FabricParserDescriptorFactory;
import ac.grim.grimac.platform.fabric.mc1194.GrimACFabric1190LoaderPlugin;
import ac.grim.grimac.platform.fabric.mc1194.entity.Fabric1194GrimEntity;
import ac.grim.grimac.platform.fabric.mc1194.player.Fabric1193PlatformInventory;
import ac.grim.grimac.platform.fabric.mc1205.Fabric1203PlatformServer;
import ac.grim.grimac.platform.fabric.mc1205.convert.Fabric1200MessageUtil;
import ac.grim.grimac.platform.fabric.mc1205.convert.Fabric1205ConversionUtil;
import ac.grim.grimac.platform.fabric.mc1214.player.Fabric1212PlatformPlayer;
import ac.grim.grimac.platform.fabric.mc1214.player.Fabric1215PlatformInventory;
import ac.grim.grimac.platform.fabric.player.FabricPlatformPlayerFactory;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class GrimACFabric1212LoaderPlugin extends GrimACFabric1190LoaderPlugin {

    public GrimACFabric1212LoaderPlugin() {
        super(
                new FabricParserDescriptorFactory(
                        new FabricPlayerSelectorParser<>(Fabric1212PlayerSelectorAdapter::new)
                ),
                new FabricPlatformPlayerFactory(
                        Fabric1212PlatformPlayer::new,
                        Fabric1194GrimEntity::new,
                        PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_4)
                            ? Fabric1215PlatformInventory::new : Fabric1193PlatformInventory::new
                ),
                new Fabric1203PlatformServer(),
                new Fabric1200MessageUtil(),
                new Fabric1205ConversionUtil()
        );
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_1_21_4;
    }
}
