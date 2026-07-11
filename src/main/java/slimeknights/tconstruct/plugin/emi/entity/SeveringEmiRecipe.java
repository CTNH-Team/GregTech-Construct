package slimeknights.tconstruct.plugin.emi.entity;

import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.List;

public final class SeveringEmiRecipe extends TConstructEmiRecipe {
  private final EntityEmiIngredient entity;
  private final SeveringRecipe recipe;

  public SeveringEmiRecipe(ResourceLocation id, SeveringRecipe recipe) {
    this(id, recipe, new EntityEmiIngredient(recipe.getIngredient(), 32));
  }

  private SeveringEmiRecipe(ResourceLocation id, SeveringRecipe recipe, EntityEmiIngredient entity) {
    super(id, EMIConstants.SEVERING, List.of(entity), List.of(),
        TConstructEmiRecipe.itemOutputs(List.of(recipe.getOutput())));
    this.entity = entity;
    this.recipe = recipe;
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addBackground(widgets);
    widgets.addDrawable(3, 3, 32, 32,
        (graphics, mouseX, mouseY, delta) -> entity.render(graphics, 0, 0, delta))
        .tooltip((mouseX, mouseY) -> entity.getTooltip());
    EmiRenderHelper.slot(widgets, TConstructEmiRecipe.itemIngredient(recipe.getOutput()), 76, 11)
        .recipeContext(this);
  }
}
