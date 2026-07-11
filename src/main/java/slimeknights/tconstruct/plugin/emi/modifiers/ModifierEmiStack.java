package slimeknights.tconstruct.plugin.emi.modifiers;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.client.modifiers.ModifierIconManager;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;
import slimeknights.tconstruct.tools.item.ModifierCrystalItem;

import java.util.ArrayList;
import java.util.List;

public final class ModifierEmiStack extends EmiStack {
  private static final String WRAPPER_KEY = "emi.tconstruct.modifier_ingredient";

  private final ModifierEntry entry;

  public ModifierEmiStack(ModifierEntry entry) {
    this.entry = entry;
    amount = entry.getLevel();
  }

  @Override
  public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
    if (!isEmpty() && (flags & RENDER_ICON) != 0) {
      ModifierIconManager.renderIcon(graphics, entry.getModifier(), x, y, 100, 16);
    }
  }

  @Override
  public EmiStack copy() {
    ModifierEmiStack copy = new ModifierEmiStack(entry);
    copy.amount = amount;
    copy.chance = chance;
    copy.comparison = comparison;
    copy.setRemainder(getRemainder().copy());
    return copy;
  }

  @Override
  public boolean isEmpty() {
    return !entry.isBound();
  }

  @Override
  public CompoundTag getNbt() {
    return null;
  }

  @Override
  public Object getKey() {
    return entry.getId();
  }

  @Override
  public net.minecraft.resources.ResourceLocation getId() {
    return entry.getId();
  }

  @Override
  public ItemStack getItemStack() {
    if (ModifierManager.isInTag(entry.getId(), TinkerTags.Modifiers.EXTRACT_MODIFIER_BLACKLIST)) {
      return ItemStack.EMPTY;
    }
    return ModifierCrystalItem.withModifier(entry.getId());
  }

  @Override
  public List<Component> getTooltipText() {
    if (isEmpty()) {
      return List.of();
    }
    List<Component> tooltip = new ArrayList<>();
    tooltip.add(entry.getDisplayName());
    tooltip.addAll(entry.getModifier().getDescriptionList(entry.getLevel()));
    return tooltip;
  }

  @Override
  public List<ClientTooltipComponent> getTooltip() {
    if (isEmpty()) {
      return List.of();
    }
    List<Component> tooltip = new ArrayList<>();
    tooltip.add(Component.translatable(WRAPPER_KEY,
        Component.translatable(entry.getModifier().getTranslationKey())));
    tooltip.addAll(entry.getModifier().getDescriptionList(entry.getLevel()));
    if (EmiRenderHelper.advancedTooltips()) {
      tooltip.add(Component.literal(entry.getId().toString()).withStyle(ChatFormatting.DARK_GRAY));
    }
    return EmiRenderHelper.tooltip(tooltip);
  }

  @Override
  public Component getName() {
    return isEmpty() ? Component.empty() : entry.getDisplayName();
  }
}
