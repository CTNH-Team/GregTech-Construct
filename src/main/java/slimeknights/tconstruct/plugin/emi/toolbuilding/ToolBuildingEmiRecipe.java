package slimeknights.tconstruct.plugin.emi.toolbuilding;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe.X_OFFSET;
import static slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe.Y_OFFSET;
import static slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe.SLOT_SIZE;

public final class ToolBuildingEmiRecipe extends TConstructEmiRecipe {
  private static final Component ANVIL_TOOLTIP =
      TConstruct.makeTranslation("emi", "tinkering.tool_building.anvil");

  private final ToolBuildingRecipe recipe;
  private final List<EmiIngredient> ingredients;
  private final EmiIngredient output;

  public ToolBuildingEmiRecipe(ResourceLocation id, ToolBuildingRecipe recipe) {
    this(id, recipe, createInputs(recipe), TConstructEmiRecipe.itemIngredient(recipe.getDisplayOutput()));
  }

  private ToolBuildingEmiRecipe(ResourceLocation id, ToolBuildingRecipe recipe,
                                List<EmiIngredient> ingredients, EmiIngredient output) {
    super(id, EMIConstants.TOOL_BUILDING, ingredients, List.of(),
        TConstructEmiRecipe.itemOutputs(recipe.getDisplayOutput()));
    this.recipe = recipe;
    this.ingredients = ingredients;
    this.output = output;
  }

  private static List<EmiIngredient> createInputs(ToolBuildingRecipe recipe) {
    Stream<List<ItemStack>> parts = recipe.getAllToolParts().stream();
    Stream<List<ItemStack>> extras = recipe.getExtraRequirements().stream()
        .map(Ingredient::getItems)
        .map(Arrays::asList);
    List<List<ItemStack>> stacks = new ArrayList<>(Stream.concat(parts, extras).toList());
    while (stacks.size() < recipe.getLayoutSlots().size()) {
      stacks.add(List.of());
    }
    return stacks.stream().map(TConstructEmiRecipe::itemIngredient).toList();
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addBackground(widgets);
    ItemStack preview = recipe.getOutput() instanceof IModifiableDisplay modifiable
        ? modifiable.getRenderTool()
        : recipe.getOutput().asItem().getDefaultInstance();
    widgets.addDrawable(5, 6, 70, 60, (graphics, x, y, delta) -> {
      graphics.pose().pushPose();
      graphics.pose().translate(0, 0.5, -100);
      graphics.pose().scale(3.7f, 3.7f, 1);
      graphics.renderItem(preview, 0, 0);
      graphics.pose().popPose();
    });
    widgets.addDrawable(5, 6, 70, 60, (graphics, x, y, delta) -> {
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      RenderSystem.setShaderColor(1, 1, 1, 0.82f);
      graphics.blit(EMIConstants.TINKER_STATION_TEXTURE, 0, 0, 122, 77, 70, 60, 256, 256);
      RenderSystem.setShaderColor(1, 1, 1, 1);
      RenderSystem.disableBlend();
      RenderSystem.enableDepthTest();
    });

    List<LayoutSlot> layoutSlots = recipe.getLayoutSlots();
    widgets.addDrawable(0, 0, getDisplayWidth(), getDisplayHeight(), (graphics, x, y, delta) -> {
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      RenderSystem.setShaderColor(1, 1, 1, 0.28f);
      for (LayoutSlot slot : layoutSlots) {
        graphics.blit(EMIConstants.TINKER_STATION_TEXTURE,
            slot.getX() + X_OFFSET - 1, slot.getY() + Y_OFFSET - 1,
            144, 59, SLOT_SIZE, SLOT_SIZE, 256, 256);
      }
      RenderSystem.setShaderColor(1, 1, 1, 1);
      for (LayoutSlot slot : layoutSlots) {
        graphics.blit(EMIConstants.TINKER_STATION_TEXTURE,
            slot.getX() + X_OFFSET - 1, slot.getY() + Y_OFFSET - 1,
            162, 59, SLOT_SIZE, SLOT_SIZE, 256, 256);
      }
      RenderSystem.disableBlend();
      RenderSystem.enableDepthTest();
    });
    for (int index = 0; index < layoutSlots.size(); index++) {
      LayoutSlot slot = layoutSlots.get(index);
      EmiRenderHelper.slot(widgets, ingredients.get(index),
          slot.getX() + X_OFFSET, slot.getY() + Y_OFFSET);
    }

    widgets.addTexture(EMIConstants.TINKER_STATION_TEXTURE, 103, 18, 26, 26,
        122, 77, 26, 26, 256, 256);
    EmiRenderHelper.largeSlotWithBackground(widgets, output, 108, 23).recipeContext(this);
    if (recipe.requiresAnvil()) {
      widgets.addTexture(EMIConstants.TINKER_STATION_TEXTURE, 76, 44, 16, 16, 128, 61,
          16, 16, 256, 256);
      widgets.addTooltipText(List.of(ANVIL_TOOLTIP), 76, 44, 16, 16);
    }
  }
}
