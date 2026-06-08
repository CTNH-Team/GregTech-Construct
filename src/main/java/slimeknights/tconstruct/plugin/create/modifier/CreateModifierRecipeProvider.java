/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.modifier;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.recipe.modifiers.adding.ModifierRecipeBuilder;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.function.Consumer;

public final class CreateModifierRecipeProvider extends BaseRecipeProvider {

    public CreateModifierRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    public String getName() {
        return "Tinkers' Construct Create Modifier Recipes";
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        addUpgradeRecipes(consumer);
        addSlotlessRecipes(consumer);
        addAbilityRecipes(consumer);
    }

    private void addUpgradeRecipes(Consumer<FinishedRecipe> consumer) {
        ModifierRecipeBuilder.modifier(CreateModifierIds.DIVING_WEIGHTS)
                .addInput(AllItems.ANDESITE_ALLOY)
                .addInput(AllItems.ANDESITE_ALLOY)
                .addInput(Items.IRON_NUGGET)
                .addInput(Items.IRON_NUGGET)
                .exactLevel(1)
                .setSlots(SlotType.UPGRADE, 1)
                .setTools(TinkerTags.Items.BOOTS)
                .disallowCrystal()
                .save(consumer, location("tools/modifiers/create/upgrades/diving"));

        ModifierRecipeBuilder.modifier(CreateModifierIds.EXTENDO)
                .addInput(AllItems.EXTENDO_GRIP)
                .addInput(AllItems.EXTENDO_GRIP)
                .addInput(AllBlocks.COGWHEEL)
                .addInput(AllBlocks.LARGE_COGWHEEL)
                .exactLevel(1)
                .setSlots(SlotType.UPGRADE, 1)
                .setTools(TinkerTags.Items.CHESTPLATES)
                .disallowCrystal()
                .save(consumer, location("tools/modifiers/create/upgrades/extendo"));
    }

    private void addSlotlessRecipes(Consumer<FinishedRecipe> consumer) {
        ModifierRecipeBuilder.modifier(CreateModifierIds.GOGGLES)
                .addInput(AllItems.GOGGLES)
                .exactLevel(1)
                .setTools(TinkerTags.Items.HELMETS)
                .disallowCrystal()
                .save(consumer, location("tools/modifiers/create/slotless/goggles"));
    }

    private void addAbilityRecipes(Consumer<FinishedRecipe> consumer) {
        ModifierRecipeBuilder.modifier(CreateModifierIds.WRENCH)
                .addInput(AllItems.WRENCH)
                .exactLevel(1)
                .setSlots(SlotType.ABILITY, 1)
                .setTools(TinkerTags.Items.HELD)
                .disallowCrystal()
                .save(consumer, location("tools/modifiers/create/ability/wrench"));

        ModifierRecipeBuilder.modifier(CreateModifierIds.CRUSHING)
                .addInput(AllBlocks.CRUSHING_WHEEL)
                .addInput(AllBlocks.CRUSHING_WHEEL)
                .addInput(Tags.Items.INGOTS)
                .addInput(AllBlocks.COGWHEEL)
                .addInput(AllBlocks.LARGE_COGWHEEL)
                .exactLevel(1)
                .setSlots(SlotType.ABILITY, 1)
                .setTools(Ingredient.of(TinkerTools.pickaxe))
                .disallowCrystal()
                .save(consumer, location("tools/modifiers/create/ability/crushing"));
    }
}
