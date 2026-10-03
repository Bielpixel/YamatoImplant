package ze.yamatoimplant;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = YamatoImplant.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class YamatoEffects {

    private static final ResourceLocation YAMATO_ITEM =
            new ResourceLocation("invincible_dmc", "yamato_dmc5");
    private static final ResourceLocation YAMATO_ADVANCEMENT =
            new ResourceLocation(YamatoImplant.MOD_ID, "yamato_awakened");
    private static final Map<UUID, Integer> ACTIVE_EFFECTS = new HashMap<>();
    // The supplied OGG is 28.91 seconds at 20 game ticks per second.
    private static final int EFFECT_DURATION_TICKS = 578;
    private static final int ORIGINAL_EFFECT_TICKS = 60;
    private static final int PARTICLE_FRAME_INTERVAL_TICKS = 5;
    private static final int SPIRAL_CYCLE_TICKS = 100;

    private YamatoEffects() {}

    public static void play(ServerPlayer player) {
        ACTIVE_EFFECTS.put(player.getUUID(), 0);
        ServerLevel level = player.serverLevel();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        // DevilMineCraft plays its default Yamato theme for this player when the
        // obtain_any_yamato advancement is granted. Stop that track before ours.
        player.connection.send(new ClientboundStopSoundPacket(
                new ResourceLocation("invincible_dmc", "yamato"), SoundSource.PLAYERS));
        // Use the record slider: this pack has the general music slider nearly muted.
        level.playSound(null, x, y, z, ModSounds.YAMATO_AWAKENED.get(), SoundSource.RECORDS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.FLASH, x, y + 1.0, z, 1, 0.0, 0.0, 0.0, 0.0);
        level.sendParticles(ParticleTypes.PORTAL, x, y + 1.0, z, 320, 5.0, 2.8, 5.0, 0.42);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 1.0, z, 180, 4.0, 2.2, 4.0, 0.12);
        level.sendParticles(ParticleTypes.END_ROD, x, y + 1.0, z, 130, 3.5, 2.4, 3.5, 0.18);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 1.0, z, 110, 3.0, 2.0, 3.0, 0.16);
    }

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!YAMATO_ADVANCEMENT.equals(event.getAdvancement().getId())) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            play(player);
        }
    }

    @SubscribeEvent
    public static void detectExistingYamato(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 20 != 0) {
            return;
        }

        Item yamato = ForgeRegistries.ITEMS.getValue(YAMATO_ITEM);
        if (yamato == null || !player.getInventory().contains(new ItemStack(yamato))) {
            return;
        }

        Advancement advancement = player.getServer().getAdvancements().getAdvancement(YAMATO_ADVANCEMENT);
        if (advancement == null) {
            return;
        }

        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (!progress.isDone()) {
            for (String criterion : progress.getRemainingCriteria()) {
                player.getAdvancements().award(advancement, criterion);
            }
        }
    }

    @SubscribeEvent
    public static void animateOrbit(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE_EFFECTS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> iterator = ACTIVE_EFFECTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            int age = entry.getValue();
            if (player == null || age >= EFFECT_DURATION_TICKS) {
                iterator.remove();
                continue;
            }

            entry.setValue(age + 1);
            ServerLevel level = player.serverLevel();
            double centerX = player.getX();
            double centerY = player.getY();
            double centerZ = player.getZ();

            // Keep the original close orbit and burst for the opening three seconds.
            if (age < ORIGINAL_EFFECT_TICKS) {
                double originalPhase = age * 0.18;
                for (int i = 0; i < 16; i++) {
                    double angle = originalPhase + (Math.PI * 2.0 * i / 16.0);
                    double x = centerX + Math.cos(angle) * 1.35;
                    double z = centerZ + Math.sin(angle) * 1.35;
                    double y = centerY + 0.65 + Math.sin(angle * 2.0) * 0.35;
                    level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1,
                            0.0, 0.0, 0.0, 0.0);
                }

                for (int i = 0; i < 10; i++) {
                    double angle = -originalPhase + (Math.PI * 2.0 * i / 10.0);
                    double x = centerX + Math.cos(angle) * 0.72;
                    double z = centerZ + Math.sin(angle) * 0.72;
                    double y = centerY + 1.45 + Math.sin(angle + originalPhase) * 0.45;
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1,
                            0.0, 0.0, 0.0, 0.0);
                }

                if (age % PARTICLE_FRAME_INTERVAL_TICKS == 0) {
                    level.sendParticles(ParticleTypes.PORTAL, centerX, centerY + 1.0, centerZ,
                            28, 0.8, 1.0, 0.8, 0.18);
                }
            }

            if (age % PARTICLE_FRAME_INTERVAL_TICKS != 0) {
                continue;
            }

            double progress = (age % SPIRAL_CYCLE_TICKS) / (double) SPIRAL_CYCLE_TICKS;
            double phase = age * 0.16;
            Vec3 look = player.getLookAngle();
            double side = player.getMainArm() == HumanoidArm.RIGHT ? 1.0 : -1.0;
            if (player.getOffhandItem().is(ForgeRegistries.ITEMS.getValue(YAMATO_ITEM))) {
                side *= -1.0;
            }
            double handX = centerX - look.z * 0.42 * side + look.x * 0.28;
            double handY = centerY + 1.12;
            double handZ = centerZ + look.x * 0.42 * side + look.z * 0.28;

            // Wide blue-white helix spirals inward toward the sword hand.
            double outerRadius = 5.2 * (1.0 - progress) + 0.18;
            for (int i = 0; i < 16; i++) {
                double angle = phase + (Math.PI * 2.0 * i / 16.0);
                double x = handX + Math.cos(angle) * outerRadius;
                double z = handZ + Math.sin(angle) * outerRadius;
                double y = handY + Math.sin(angle * 2.0 + phase) * 1.4 * (1.0 - progress);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            }

            double innerRadius = 3.8 * (1.0 - progress) + 0.10;
            for (int i = 0; i < 12; i++) {
                double angle = -phase * 1.35 + (Math.PI * 2.0 * i / 12.0);
                double x = handX + Math.cos(angle) * innerRadius;
                double z = handZ + Math.sin(angle) * innerRadius;
                double y = handY + Math.sin(angle + phase) * 0.9 * (1.0 - progress);
                level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            }

            if (age % 30 == 0) {
                level.sendParticles(ParticleTypes.PORTAL, centerX, centerY + 1.0, centerZ,
                        90, 5.0, 2.8, 5.0, 0.24);
                for (int pillar = 0; pillar < 6; pillar++) {
                    double angle = phase + Math.PI * 2.0 * pillar / 6.0;
                    double radius = 4.8 * (1.0 - progress) + 0.5;
                    double x = centerX + Math.cos(angle) * radius;
                    double z = centerZ + Math.sin(angle) * radius;
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, centerY + 1.0, z,
                            12, 0.22, 2.4, 0.22, 0.04);
                }
            }
        }
    }
}
