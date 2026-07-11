package slimeknights.tconstruct.plugin.emi.entity;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.melting.EmiMeltingFuelHandler;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.List;

public final class EntityMeltingEmiRecipe extends TConstructEmiRecipe {
  private static final String KEY_PER_HEARTS =
      TConstruct.makeTranslationKey("emi", "entity_melting.per_hearts");
  private static final Component TOOLTIP_PER_HEART =
      Component.translatable(TConstruct.makeTranslationKey("emi", "entity_melting.per_heart"))
          .withStyle(ChatFormatting.GRAY);

  private final EntityEmiIngredient entity;
  private final FluidStack output;
  private final int damage;
  private final EmiIngredient fuel;

  public EntityMeltingEmiRecipe(ResourceLocation id, EntityMeltingRecipe recipe) {
    this(id, recipe.getIngredient(), recipe.getOutput(), recipe.getDamage());
  }

  public EntityMeltingEmiRecipe(ResourceLocation id, EntityIngredient ingredient, FluidStack output, int damage) {
    this(id, new EntityEmiIngredient(ingredient, 32), output, damage,
        TConstructEmiRecipe.fluids(EmiMeltingFuelHandler.getUsableFuels(1)));
  }

  private EntityMeltingEmiRecipe(ResourceLocation id, EntityEmiIngredient entity, FluidStack output,
                                 int damage, EmiIngredient fuel) {
    super(id, EMIConstants.ENTITY_MELTING, List.of(entity), List.of(fuel),
        TConstructEmiRecipe.fluidOutputs(List.of(output)));
    this.entity = entity;
    this.output = output;
    this.damage = damage;
    this.fuel = fuel;
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addBackground(widgets);
    widgets.addDrawable(19, 11, 32, 32,
        (graphics, mouseX, mouseY, delta) -> entity.render(graphics, 0, 0, delta))
        .tooltip((mouseX, mouseY) -> entity.getTooltip());

    var outputTank = EmiRenderHelper.tank(widgets, TConstructEmiRecipe.fluidIngredient(output),
        115, 11, 16, 32, FluidValues.INGOT * 2).recipeContext(this);
    for (Component tooltip : outputTooltip(output, damage)) {
      outputTank.appendTooltip(tooltip);
    }
    EmiRenderHelper.tank(widgets, fuel, 75, 43, 16, 16, 1).catalyst(true);
    EmiRenderHelper.tankOverlay(widgets, EMIConstants.MELTING_TEXTURE, 75, 43, 16, 16, 150, 74);
    widgets.addAnimatedTexture(EMIConstants.MELTING_TEXTURE, 71, 21, 24, 17, 150, 41,
        200, true, false, false);

    String damageText = Float.toString(damage / 2f);
    widgets.addText(Component.literal(damageText), 84 - Minecraft.getInstance().font.width(damageText),
        8, 0xFFFF0000, false);
  }

  private static List<Component> outputTooltip(FluidStack fluid, int damage) {
    List<Component> tooltip = new ArrayList<>();
    FluidTooltipHandler.appendMaterial(fluid, tooltip);
    tooltip.add(damage == 2
        ? TOOLTIP_PER_HEART
        : Component.translatable(KEY_PER_HEARTS, damage / 2f).withStyle(ChatFormatting.GRAY));
    return tooltip;
  }
}
