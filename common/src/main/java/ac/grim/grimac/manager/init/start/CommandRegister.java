package ac.grim.grimac.manager.init.start;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.command.SenderRequirement;
import ac.grim.grimac.command.commands.GrimAlerts;
import ac.grim.grimac.command.commands.GrimBrands;
import ac.grim.grimac.command.commands.GrimDebug;
import ac.grim.grimac.command.commands.GrimDump;
import ac.grim.grimac.command.commands.GrimHelp;
import ac.grim.grimac.command.commands.GrimLog;
import ac.grim.grimac.command.commands.GrimPerf;
import ac.grim.grimac.command.commands.GrimProfile;
import ac.grim.grimac.command.commands.GrimReload;
import ac.grim.grimac.command.commands.GrimSendAlert;
import ac.grim.grimac.command.commands.GrimSpectate;
import ac.grim.grimac.command.commands.GrimStopSpectating;
import ac.grim.grimac.command.commands.GrimVerbose;
import ac.grim.grimac.command.commands.GrimVersion;
import ac.grim.grimac.command.handler.GrimCommandFailureHandler;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.utils.anticheat.MessageUtil;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.processors.requirements.RequirementApplicable;
import org.incendo.cloud.processors.requirements.RequirementPostprocessor;
import org.incendo.cloud.processors.requirements.Requirements;

import java.util.function.Function;
import java.util.function.Supplier;

public class CommandRegister implements StartableInitable {

    public static final CloudKey<Requirements<Sender, SenderRequirement>>
            REQUIREMENT_KEY = CloudKey.of(
            "requirements",
            new TypeToken<>() {}
    );

    public static final RequirementApplicable.RequirementApplicableFactory<Sender,
            SenderRequirement> REQUIREMENT_FACTORY = RequirementApplicable.factory(REQUIREMENT_KEY);


    private static boolean commandsRegistered = false;
    private final Supplier<CommandManager<Sender>> commandManagerSupplier;

    public CommandRegister(Supplier<CommandManager<Sender>> commandManagerSupplier) {
        this.commandManagerSupplier = commandManagerSupplier;
    }

    // Public static method that can be called on platforms where command must be registered earlier than InitManager.load()
    public static void registerCommands(CommandManager<Sender> commandManager) {
        if (commandsRegistered) return;
        new GrimPerf().register(commandManager);
        new GrimDebug().register(commandManager);
        new GrimAlerts().register(commandManager);
        new GrimProfile().register(commandManager);
        new GrimSendAlert().register(commandManager);
        new GrimHelp().register(commandManager);
        new GrimReload().register(commandManager);
        new GrimSpectate().register(commandManager);
        new GrimStopSpectating().register(commandManager);
        new GrimLog().register(commandManager);
        new GrimVerbose().register(commandManager);
        new GrimVersion().register(commandManager);
        new GrimDump().register(commandManager);
        new GrimBrands().register(commandManager);

        final RequirementPostprocessor<Sender, SenderRequirement>
                senderRequirementPostprocessor = RequirementPostprocessor.of(
                REQUIREMENT_KEY,
                new GrimCommandFailureHandler()
        );
        commandManager.registerCommandPostProcessor(senderRequirementPostprocessor);
        registerExceptionHandler(commandManager, InvalidSyntaxException.class, e -> MessageUtil.miniMessage(e.correctSyntax()));
        commandsRegistered = true;
    }

    protected static <E extends Exception> void registerExceptionHandler(CommandManager<Sender> commandManager, Class<E> ex, Function<E, ComponentLike> toComponent) {
        commandManager.exceptionController().registerHandler(ex,
                (c) -> c.context().sender().sendMessage(toComponent.apply(c.exception()).asComponent().colorIfAbsent(NamedTextColor.RED))
        );
    }


    @Override
    public void start() {
        CommandManager<Sender> commandManager = commandManagerSupplier.get();
        registerCommands(commandManager);

        if (GrimAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("check-for-updates", true)) {
            GrimVersion.checkForUpdatesAsync(GrimAPI.INSTANCE.getPlatformServer().getConsoleSender());
        }
    }
}
