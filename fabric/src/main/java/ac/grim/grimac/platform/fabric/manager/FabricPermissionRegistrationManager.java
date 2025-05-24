package ac.grim.grimac.platform.fabric.manager;

import ac.grim.grimac.platform.api.manager.PermissionRegistrationManager;
import ac.grim.grimac.platform.api.permissions.PermissionDefaultValue;
import ac.grim.grimac.platform.fabric.GrimACFabricLoaderPlugin;
import ac.grim.grimac.platform.fabric.sender.FabricSenderFactory;
import me.lucko.fabric.api.permissions.v0.Permissions;

public class FabricPermissionRegistrationManager implements PermissionRegistrationManager {

    FabricSenderFactory fabricSenderFactory = GrimACFabricLoaderPlugin.LOADER.getFabricSenderFactory();

    public FabricPermissionRegistrationManager() {
        registerPermission("grim.exempt", PermissionDefaultValue.FALSE);
        registerPermission("grim.nosetback", PermissionDefaultValue.FALSE);
        registerPermission("grim.nomodifypacket", PermissionDefaultValue.FALSE);
        registerPermission("grim.nosetback", PermissionDefaultValue.FALSE);
        registerPermission("grim.alerts.enable-on-join", PermissionDefaultValue.FALSE);
        registerPermission("grim.verbose.enable-on-join", PermissionDefaultValue.FALSE);
        registerPermission("grim.brand.enable-on-join", PermissionDefaultValue.FALSE);
        registerPermission("grim.alerts.enable-on-join.silent", PermissionDefaultValue.FALSE);
        registerPermission("grim.verbose.enable-on-join.silent", PermissionDefaultValue.FALSE);
        registerPermission("grim.brand.enable-on-join.silent", PermissionDefaultValue.FALSE);
    }

    @Override
    public void registerPermission(String name, PermissionDefaultValue defaultValue) {
        fabricSenderFactory.registerPermissionDefault(name, defaultValue);
        Permissions.check(GrimACFabricLoaderPlugin.FABRIC_SERVER.getCommandSource(), name);
    }
}
