package slimeknights.tconstruct.plugin.emi.partbuilder;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.recipe.partbuilder.IDisplayPartBuilderRecipe;
import slimeknights.tconstruct.library.tools.layout.Patterns;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.List;

public final class PartBuilderEmiRecipe extends TConstructEmiRecipe {
  private static final String KEY_COST = TConstruct.makeTranslationKey("emi", "part_builder.cost");

  private final IDisplayPartBuilderRecipe recipe;
  private final EmiIngredient material;
  private final EmiIngredient patternItem;
  private final PatternEmiIngredient pattern;
  private final EmiIngredient output;

  public PartBuilderEmiRecipe(ResourceLocation id, IDisplayPartBuilderRecipe recipe) {
    this(id, recipe, TConstructEmiRecipe.itemIngredient(recipe.getMaterialItems()),
        TConstructEmiRecipe.itemIngredient(recipe.getPatternItems()), new PatternEmiIngredient(recipe.getPattern()),
        TConstructEmiRecipe.itemIngredient(recipe.getResultItems()));
  }

  private PartBuilderEmiRecipe(ResourceLocation id, IDisplayPartBuilderRecipe recipe, EmiIngredient material,
                               EmiIngredient patternItem, PatternEmiIngredient pattern, EmiIngredient output) {
    super(id, EMIConstants.PART_BUILDER, List.of(material, patternItem, pattern), List.of(),
        TConstructEmiRecipe.itemOutputs(recipe.getResultItems()));
    this.recipe = recipe;
    this.material = material;
    this.patternItem = patternItem;
    this.pattern = pattern;
    this.output = output;
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addBackground(widgets);
    EmiRenderHelper.slot(widgets, patternItem, 4, 16);
    EmiRenderHelper.slot(widgets, material, 25, 16);
    EmiRenderHelper.slot(widgets, pattern, 46, 16);
    EmiRenderHelper.slot(widgets, output, 96, 15).recipeContext(this);

    MaterialVariant variant = recipe.getMaterial();
    if (!variant.isEmpty()) {
      Component name = MaterialTooltipCache.getColoredDisplayName(variant.getVariant());
      widgets.addText(name, 3, 2, -1, true);
      widgets.addText(Component.translatable(KEY_COST, recipe.getCost()), 3, 35, 0xFF808080, false);
    } else if (recipe.getMaterialItems().isEmpty()) {
      widgets.addDrawable(25, 16, 16, 16,
          (graphics, x, y, delta) -> GuiUtil.renderPattern(graphics, Patterns.INGOT, 0, 0));
    }
  }
}
