package ze.yamatoimplant;

import com.mojang.brigadier.Command;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = YamatoImplant.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class YamatoCommands {

    private static final ResourceLocation YAMATO_ADVANCEMENT =
            new ResourceLocation(YamatoImplant.MOD_ID, "yamato_awakened");

    private YamatoCommands() {}

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("yamatoimplant")
                        .then(Commands.literal("testar_efeitos")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    YamatoEffects.play(player);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("Efeitos da conquista da Yamato reproduzidos."),
                                            false
                                    );
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("testar_conquista")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> grantAdvancement(context.getSource().getPlayerOrException())))
        );
    }

    private static int grantAdvancement(ServerPlayer player) {
        Advancement advancement = player.getServer().getAdvancements().getAdvancement(YAMATO_ADVANCEMENT);
        if (advancement == null) {
            player.sendSystemMessage(Component.literal("A conquista da Yamato não foi encontrada."));
            return 0;
        }

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            player.sendSystemMessage(Component.literal("Você já desbloqueou essa conquista."));
            return 0;
        }

        ArrayList<String> remainingCriteria = new ArrayList<>();
        progress.getRemainingCriteria().forEach(remainingCriteria::add);
        for (String criterion : remainingCriteria) {
            player.getAdvancements().award(advancement, criterion);
        }
        return Command.SINGLE_SUCCESS;
    }
}
