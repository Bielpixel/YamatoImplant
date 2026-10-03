package ze.yamatoimplant;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, YamatoImplant.MOD_ID);

    public static final RegistryObject<SoundEvent> YAMATO_AWAKENED = SOUND_EVENTS.register(
            "yamato_awakened",
            () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(YamatoImplant.MOD_ID, "yamato_awakened")
            )
    );

    private ModSounds() {}
}
