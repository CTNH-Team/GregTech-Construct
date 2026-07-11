package slimeknights.tconstruct.plugin.emi.modifiers;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModifierEmiRecipe extends TConstructEmiRecipe {
  private static final List<Component> TEXT_INCREMENTAL =
      Collections.singletonList(TConstruct.makeTranslation("emi", "modifiers.incremental"));
  private static final String KEY_MIN = TConstruct.makeTranslationKey("emi", "modifiers.level.min");
  private static final String KEY_MAX = TConstruct.makeTranslationKey("emi", "modifiers.level.max");
  private static final String KEY_RANGE = TConstruct.makeTranslationKey("emi", "modifiers.level.range");
  private static final String KEY_EXACT = TConstruct.makeTranslationKey("emi", "modifiers.level.exact");
  private static final int[] INPUT_X = {3, 25, 47, 43, 7};
  private static final int[] INPUT_Y = {33, 15, 33, 58, 58};

  private final IDisplayModifierRecipe recipe;
  private final List<EmiIngredient> ingredients;
  private final EmiIngredient toolBefore;
  private final EmiIngredient toolAfter;
  private final ModifierEmiIngredient modifier;
  private final SlotEmiIngredient slots;

  public ModifierEmiRecipe(ResourceLocation id, IDisplayModifierRecipe recipe) {
    this(id, recipe, createInputs(recipe), TConstructEmiRecipe.itemIngredient(recipe.getToolWithoutModifier()),
        TConstructEmiRecipe.itemIngredient(recipe.getToolWithModifier()),
        new ModifierEmiIngredient(recipe.getDisplayResult(), ModifierEmiIngredient.Style.NAME, 124),
        new SlotEmiIngredient(recipe.getSlots()));
  }

  private ModifierEmiRecipe(ResourceLocation id, IDisplayModifierRecipe recipe, List<EmiIngredient> ingredients,
                            EmiIngredient toolBefore, EmiIngredient toolAfter, ModifierEmiIngredient modifier,
                            SlotEmiIngredient slots) {
    super(id, EMIConstants.MODIFIERS, ingredients, List.of(toolBefore),
        outputs(recipe));
    this.recipe = recipe;
    this.ingredients = ingredients;
    this.toolBefore = toolBefore;
    this.toolAfter = toolAfter;
    this.modifier = modifier;
    this.slots = slots;
  }

  private static List<EmiStack> outputs(IDisplayModifierRecipe recipe) {
    List<EmiStack> outputs = new ArrayList<>();
    outputs.add(new ModifierEmiStack(recipe.getDisplayResult()));
    outputs.addAll(TConstructEmiRecipe.itemOutputs(recipe.getToolWithModifier()));
    return outputs;
  }

  private static List<EmiIngredient> createInputs(IDisplayModifierRecipe recipe) {
    List<EmiIngredient> inputs = new ArrayList<>();
    for (int slot = 0; slot < 5; slot++) {
      inputs.add(TConstructEmiRecipe.itemIngredient(recipe.getDisplayItems(slot)));
    }
    if (recipe.getSlots() != null) {
      inputs.add(new SlotEmiIngredient(recipe.getSlots()));
    }
    return inputs;
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addBackground(widgets);
    for (int slot = 0; slot < 5; slot++) {
      if (recipe.getDisplayItems(slot).isEmpty()) {
        widgets.addTexture(EMIConstants.TINKER_STATION_TEXTURE, INPUT_X[slot], INPUT_Y[slot],
            16, 16, 128 + slot * 16, 0, 16, 16, 256, 256);
      } else {
        EmiRenderHelper.slot(widgets, ingredients.get(slot), INPUT_X[slot], INPUT_Y[slot]);
      }
    }

    widgets.add(new ModifierNameSlotWidget(modifier, 3, 3)).drawBack(false).recipeContext(this);
    EmiRenderHelper.slot(widgets, toolBefore, 25, 38).catalyst(true);
    EmiRenderHelper.slot(widgets, toolAfter, 105, 34).recipeContext(this);

    ModifierEntry result = recipe.getDisplayResult();
    Component requirements = result.getHook(ModifierHooks.REQUIREMENTS).requirementsError(result);
    if (requirements != null) {
      widgets.addTexture(EMIConstants.TINKER_STATION_TEXTURE, 66, 58, 16, 16, 128, 17,
          16, 16, 256, 256);
      widgets.addTooltipText(List.of(requirements), 66, 58, 16, 16);
    }
    if (recipe.isIncremental()) {
      widgets.addTexture(EMIConstants.TINKER_STATION_TEXTURE, 83, 59, 16, 16, 128, 33,
          16, 16, 256, 256);
      widgets.addTooltipText(TEXT_INCREMENTAL, 83, 59, 16, 16);
    }

    widgets.addDrawable(102, 58, 24, 16,
        (graphics, x, y, delta) -> slots.render(graphics, 0, 0, delta))
        .tooltip((mouseX, mouseY) -> slots.getTooltip());

    Component levelText = levelText(recipe);
    if (levelText != null) {
      widgets.addText(levelText, 86 - Minecraft.getInstance().font.width(levelText) / 2,
          16, 0xFF808080, false);
    }
  }

  private static final class ModifierNameSlotWidget extends SlotWidget {
    private ModifierNameSlotWidget(EmiIngredient ingredient, int x, int y) {
      super(ingredient, x, y);
    }

    @Override
    public Bounds getBounds() {
      return new Bounds(x, y, 124, 10);
    }

    @Override
    public void drawStack(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
      getStack().render(graphics, x, y, delta);
    }
  }

  private static Component levelText(IDisplayModifierRecipe recipe) {
    Component variant = recipe.getVariant();
    if (variant != null) {
      return variant;
    }
    IntRange level = recipe.getLevel();
    int min = level.min();
    int max = level.max();
    if (min == 1) {
      return max < ModifierEntry.VALID_LEVEL.max() ? Component.translatable(KEY_MAX, max) : null;
    }
    if (min == max) {
      return Component.translatable(KEY_EXACT, min);
    }
    if (max == ModifierEntry.VALID_LEVEL.max()) {
      return Component.translatable(KEY_MIN, min);
    }
    return Component.translatable(KEY_RANGE, min, max);
  }
}
