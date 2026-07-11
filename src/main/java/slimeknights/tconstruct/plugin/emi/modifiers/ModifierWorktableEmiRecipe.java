package slimeknights.tconstruct.plugin.emi.modifiers;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.worktable.IModifierWorktableRecipe;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.List;

public final class ModifierWorktableEmiRecipe extends TConstructEmiRecipe {
  private final IModifierWorktableRecipe recipe;
  private final EmiIngredient tools;
  private final List<EmiIngredient> ingredients;
  private final ModifierEmiIngredient modifiers;

  public ModifierWorktableEmiRecipe(ResourceLocation id, IModifierWorktableRecipe recipe) {
    this(id, recipe, TConstructEmiRecipe.itemIngredient(recipe.getInputTools()), createInputs(recipe),
        new ModifierEmiIngredient(recipe.getModifierOptions(null), ModifierEmiIngredient.Style.ICON, 16));
  }

  private ModifierWorktableEmiRecipe(ResourceLocation id, IModifierWorktableRecipe recipe, EmiIngredient tools,
                                     List<EmiIngredient> ingredients, ModifierEmiIngredient modifiers) {
    super(id, EMIConstants.MODIFIER_WORKTABLE, inputs(recipe, tools, ingredients),
        catalysts(recipe, tools, modifiers), outputs(recipe));
    this.recipe = recipe;
    this.tools = tools;
    this.ingredients = ingredients;
    this.modifiers = modifiers;
  }

  private static List<EmiStack> outputs(IModifierWorktableRecipe recipe) {
    if (!recipe.isModifierOutput()) {
      return List.of();
    }
    return recipe.getModifierOptions(null).stream()
        .map(entry -> (EmiStack) new ModifierEmiStack(entry))
        .toList();
  }

  private static List<EmiIngredient> createInputs(IModifierWorktableRecipe recipe) {
    List<EmiIngredient> inputs = new ArrayList<>();
    for (int slot = 0; slot < Math.min(2, recipe.getInputCount()); slot++) {
      inputs.add(TConstructEmiRecipe.itemIngredient(recipe.getDisplayItems(slot)));
    }
    return inputs;
  }

  private static List<EmiIngredient> inputs(IModifierWorktableRecipe recipe, EmiIngredient tools,
                                            List<EmiIngredient> ingredients) {
    List<EmiIngredient> inputs = new ArrayList<>(ingredients);
    if (recipe.isToolInput()) {
      inputs.add(tools);
    }
    return inputs;
  }

  private static List<EmiIngredient> catalysts(IModifierWorktableRecipe recipe, EmiIngredient tools,
                                               ModifierEmiIngredient modifiers) {
    List<EmiIngredient> catalysts = new ArrayList<>();
    if (!recipe.isToolInput()) {
      catalysts.add(tools);
    }
    if (!recipe.isModifierOutput()) {
      catalysts.add(modifiers);
    }
    return catalysts;
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addBackground(widgets);
    if (recipe.getInputTools().isEmpty()) {
      widgets.addTexture(EMIConstants.TINKER_STATION_TEXTURE, 23, 16, 16, 16, 128, 0,
          16, 16, 256, 256);
    } else {
      var toolSlot = EmiRenderHelper.slot(widgets, tools, 23, 16);
      if (!recipe.isToolInput()) {
        toolSlot.catalyst(true);
      }
    }

    for (int slot = 0; slot < 2; slot++) {
      if (slot >= ingredients.size() || recipe.getDisplayItems(slot).isEmpty()) {
        widgets.addTexture(EMIConstants.TINKER_STATION_TEXTURE, 43 + slot * 18, 16,
            16, 16, slot == 0 ? 176 : 208, 0, 16, 16, 256, 256);
      } else {
        EmiRenderHelper.slot(widgets, ingredients.get(slot), 43 + slot * 18, 16);
      }
    }

    var modifierSlot = EmiRenderHelper.slot(widgets, modifiers, 82, 16);
    if (recipe.isModifierOutput()) {
      modifierSlot.recipeContext(this);
    } else {
      modifierSlot.catalyst(true);
    }
    widgets.addText(recipe.getTitle(), 3, 2, 0xFF404040, false);
    widgets.addTooltipText(List.of(recipe.getDescription(null)), 3, 2, 115, 10);
  }
}
