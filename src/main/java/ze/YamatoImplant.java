package ze.yamatoimplant;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(YamatoImplant.MOD_ID)
public class YamatoImplant
{
    public static final String MOD_ID = "yamato_implant";

    public static final Logger LOGGER = LogUtils.getLogger();

    public YamatoImplant(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        ModSounds.SOUND_EVENTS.register(modEventBus);
        LOGGER.info("Hello, world! This is YamatoImplant mod.");
    }
}
