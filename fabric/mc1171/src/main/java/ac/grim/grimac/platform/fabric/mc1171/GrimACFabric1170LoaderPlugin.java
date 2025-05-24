package ac.grim.grimac.platform.fabric.mc1171;

import ac.grim.grimac.platform.api.PlatformServer;
import ac.grim.grimac.platform.api.manager.ParserDescriptorFactory;
import ac.grim.grimac.platform.fabric.GrimACFabricLoaderPlugin;
import ac.grim.grimac.platform.fabric.command.FabricPlayerSelectorParser;
import ac.grim.grimac.platform.fabric.manager.FabricParserDescriptorFactory;
import ac.grim.grimac.platform.fabric.mc1171.player.Fabric1170PlatformPlayer;
import ac.grim.grimac.platform.fabric.mc1161.Fabric1140PlatformServer;
import ac.grim.grimac.platform.fabric.mc1161.command.Fabric1161PlayerSelectorAdapter;
import ac.grim.grimac.platform.fabric.mc1161.player.Fabric1161PlatformInventory;
import ac.grim.grimac.platform.fabric.mc1171.entity.Fabric1170GrimEntity;
import ac.grim.grimac.platform.fabric.mc1161.util.convert.Fabric1140ConversionUtil;
import ac.grim.grimac.platform.fabric.mc1161.util.convert.Fabric1161MessageUtil;
import ac.grim.grimac.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.grim.grimac.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.grim.grimac.platform.fabric.utils.message.IFabricMessageUtil;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class GrimACFabric1170LoaderPlugin extends GrimACFabricLoaderPlugin {

    public GrimACFabric1170LoaderPlugin() {
        this(   new FabricParserDescriptorFactory(
                        new FabricPlayerSelectorParser<>(Fabric1161PlayerSelectorAdapter::new)
                ),
                new FabricPlatformPlayerFactory(
                        Fabric1170PlatformPlayer::new,
                        Fabric1170GrimEntity::new,
                        Fabric1161PlatformInventory::new
                ),
                new Fabric1140PlatformServer(),
                new Fabric1161MessageUtil(),
                new Fabric1140ConversionUtil()
        );
    }

    protected GrimACFabric1170LoaderPlugin(ParserDescriptorFactory parserDescriptorFactory,
                                           FabricPlatformPlayerFactory playerFactory,
                                           PlatformServer platformServer,
                                           IFabricMessageUtil fabricMessageUtil,
                                           IFabricConversionUtil fabricConversionUtil) {
        super(
                parserDescriptorFactory,
                playerFactory,
                platformServer,
                fabricMessageUtil,
                fabricConversionUtil
        );
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_1_17_1;
    }
}
