package ze.yamatoimplant.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.registries.ForgeRegistries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
        targets = "com.p1nero.tcrcore.entity.custom.ornn.OrnnEntity",
        remap = false
)
public abstract class OrnnEntityMixin {

    @Inject(
            method = "initOffers",
            at = @At("TAIL"),
            remap = false
    )
    private void yamatoImplant$addTrade(CallbackInfo ci) {

        MerchantOffers offers = ((Merchant) (Object) this).getOffers();

        Item nightfallYamato = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("efn", "yamato_dmc4_in_sheath")
        );

        Item oracleFragment = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("tcrcore", "ancient_oracle_fragment")
        );

        Item dmc5Yamato = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("invincible_dmc", "yamato_dmc5")
        );

        if (nightfallYamato == null ||
                oracleFragment == null ||
                dmc5Yamato == null) {
            return;
        }

        offers.add(new MerchantOffer(
                new ItemStack(nightfallYamato, 1),
                new ItemStack(oracleFragment, 1),
                new ItemStack(dmc5Yamato, 1),
                142857,
                0,
                0.01F
        ));
    }
}