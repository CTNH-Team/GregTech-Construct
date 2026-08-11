package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialToolIngredient;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.List;

/**
 * Category icon rendering a modifiable tool whose main part is a given material.
 * The tool is built lazily on first use so the material registry has time to load;
 * before that the plain render tool is used as a fallback.
 */
public final class MaterialToolEmiStack extends EmiStack {
  private final MaterialToolIngredient ingredient;
  private final IModifiableDisplay fallback;
  /** Cached built stack, null until first use */
  private ItemStack stack;

  public MaterialToolEmiStack(MaterialToolIngredient ingredient, IModifiableDisplay fallback) {
    this.ingredient = ingredient;
    this.fallback = fallback;
    amount = 1;
  }

  /** Gets the tool stack, building it from the ingredient once the material data is available */
  private ItemStack getStack() {
    if (stack == null) {
      stack = fallback.getRenderTool();
      ItemStack[] items = ingredient.getItems();
      if (items.length > 0 && ToolStack.isInitialized(items[0])) {
        stack = items[0];
      }
    }
    return stack;
  }

  @Override
  public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
    if (!isEmpty() && (flags & RENDER_ICON) != 0) {
      graphics.renderItem(getStack(), x, y);
    }
  }

  @Override
  public EmiStack copy() {
    MaterialToolEmiStack copy = new MaterialToolEmiStack(ingredient, fallback);
    copy.amount = amount;
    copy.chance = chance;
    copy.comparison = comparison;
    copy.setRemainder(getRemainder().copy());
    return copy;
  }

  @Override
  public boolean isEmpty() {
    return getStack().isEmpty();
  }

  @Override
  public CompoundTag getNbt() {
    return null;
  }

  @Override
  public Object getKey() {
    return getStack().getItem();
  }

  @Override
  public ResourceLocation getId() {
    return ForgeRegistries.ITEMS.getKey(getStack().getItem());
  }

  @Override
  public ItemStack getItemStack() {
    return getStack().copy();
  }

  @Override
  public List<Component> getTooltipText() {
    return List.of(getStack().getHoverName());
  }

  @Override
  public List<ClientTooltipComponent> getTooltip() {
    return EmiRenderHelper.tooltip(getTooltipText());
  }

  @Override
  public Component getName() {
    return getStack().getHoverName();
  }
}
