/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.plugin.create.modifier.CreateCrushingModifier;
import slimeknights.tconstruct.plugin.create.modifier.CreateExtendoModifier;
import slimeknights.tconstruct.plugin.create.modifier.CreateModifierIds;
import slimeknights.tconstruct.plugin.create.modifier.CreateModifierProvider;
import slimeknights.tconstruct.plugin.create.modifier.CreateModifierRecipeProvider;
import slimeknights.tconstruct.plugin.create.tag.CreateBlockTagProvider;
import slimeknights.tconstruct.plugin.create.tag.CreateItemTagProvider;
import slimeknights.tconstruct.plugin.create.tag.CreateModifierTagProvider;

import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.DynamicRecipeProviderRegistrar;
import slimeknights.tconstruct.library.addon.DatagenTagProviderRegistrar;
import slimeknights.tconstruct.library.addon.ITiCAddon;
import slimeknights.tconstruct.library.addon.ITiCStaticModifierAddon;
import slimeknights.tconstruct.library.addon.TiCAddon;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;

@TiCAddon(modID = CreateTiCAddon.MOD_ID)
public class CreateTiCAddon implements ITiCAddon, ITiCStaticModifierAddon {

    public static final String MOD_ID = "create";

    /** Tooltip shown on blocks that can be heated by a Blaze Burner */
    private static final Component BLAZE_BURNER_TOOLTIP = TConstruct.makeTranslation("tooltip", "create.blaze_burner")
            .withStyle(ChatFormatting.GOLD);

    public CreateTiCAddon() {
        MinecraftForge.EVENT_BUS.addListener(CreateTiCAddon::onTooltip);
    }

    /** Adds the Blaze Burner heating tooltip to the seared melter */
    private static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() == TinkerSmeltery.searedMelter.get()) {
            event.getToolTip().add(BLAZE_BURNER_TOOLTIP);
        }
    }

    @Override
    public String addonModId() {
        return MOD_ID;
    }

    @Override
    public void registerStaticModifiers(StaticModifierRegistrar registrar) {
        registrar.register(CreateModifierIds.CRUSHING, CreateCrushingModifier.class);
        registrar.register(CreateModifierIds.EXTENDO, CreateExtendoModifier.class);
        registrar.register(CreateModifierIds.GOGGLES, NoLevelsModifier.class);
        registrar.register(CreateModifierIds.WRENCH, NoLevelsModifier.class);
        registrar.register(CreateModifierIds.DIVING_WEIGHTS, NoLevelsModifier.class);
    }

    @Override
    public void registerDynamicRecipeProviders(DynamicRecipeProviderRegistrar registrar) {
        registrar.addRecipeProvider(CreateModifierRecipeProvider.class);
    }

    @Override
    public void registerDynamicTinkeringProviders(DynamicProviderRegistrar registrar) {
        registrar.addDataProvider(CreateModifierProvider.class);
    }

    @Override
    public void registerDatagenTagProviders(DatagenTagProviderRegistrar registrar) {
        registrar.addBlockTags(CreateBlockTagProvider::addTags);
        registrar.addItemTags(CreateItemTagProvider::addTags);
        registrar.addModifierTags(CreateModifierTagProvider::addTags);
    }
}
