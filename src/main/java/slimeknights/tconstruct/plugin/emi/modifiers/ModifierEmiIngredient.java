package slimeknights.tconstruct.plugin.emi.modifiers;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import slimeknights.tconstruct.library.client.modifiers.ModifierIconManager;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class ModifierEmiIngredient implements EmiIngredient {
  private static final String WRAPPER_KEY = "emi.tconstruct.modifier_ingredient";

  enum Style {
    NAME,
    ICON
  }

  private final List<ModifierEntry> modifiers;
  private final Style style;
  private final int width;
  private long amount;
  private float chance;

  ModifierEmiIngredient(List<ModifierEntry> modifiers, Style style, int width) {
    this(modifiers, style, width, 1, 1);
  }

  ModifierEmiIngredient(ModifierEntry modifier, Style style, int width) {
    this(List.of(modifier), style, width);
  }

  private ModifierEmiIngredient(List<ModifierEntry> modifiers, Style style, int width, long amount, float chance) {
    this.modifiers = List.copyOf(modifiers);
    this.style = style;
    this.width = width;
    this.amount = amount;
    this.chance = chance;
  }

  @Override
  public List<EmiStack> getEmiStacks() {
    return modifiers.stream().map(entry -> (EmiStack) new ModifierEmiStack(entry)).toList();
  }

  @Override
  public boolean isEmpty() {
    return modifiers.isEmpty();
  }

  @Override
  public EmiIngredient copy() {
    return new ModifierEmiIngredient(modifiers, style, width, amount, chance);
  }

  @Override
  public long getAmount() {
    return amount;
  }

  @Override
  public EmiIngredient setAmount(long amount) {
    this.amount = amount;
    return this;
  }

  @Override
  public float getChance() {
    return chance;
  }

  @Override
  public EmiIngredient setChance(float chance) {
    this.chance = chance;
    return this;
  }

  @Override
  public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
    if (modifiers.isEmpty()) {
      return;
    }
    ModifierEntry entry = modifiers.get(EmiRenderHelper.cycleIndex(modifiers.size()));
    if (style == Style.ICON) {
      ModifierIconManager.renderIcon(graphics, entry.getModifier(), x, y, 100, 16);
    } else {
      Component name = entry.getDisplayName();
      int textX = x + (width - Minecraft.getInstance().font.width(name)) / 2;
      graphics.drawString(Minecraft.getInstance().font, name, textX, y + 1, -1, true);
    }
  }

  @Override
  public List<ClientTooltipComponent> getTooltip() {
    if (modifiers.isEmpty()) {
      return List.of();
    }
    ModifierEntry entry = modifiers.get(EmiRenderHelper.cycleIndex(modifiers.size()));
    List<Component> tooltip = new ArrayList<>();
    if (style == Style.ICON) {
      tooltip.add(Component.translatable(WRAPPER_KEY,
          Component.translatable(entry.getModifier().getTranslationKey())));
      tooltip.addAll(entry.getModifier().getDescriptionList());
    } else {
      tooltip.addAll(entry.getModifier().getDescriptionList(entry.getLevel()));
    }
    if (EmiRenderHelper.advancedTooltips()) {
      tooltip.add(Component.literal(entry.getId().toString()).withStyle(ChatFormatting.DARK_GRAY));
    }
    return EmiRenderHelper.tooltip(tooltip);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof ModifierEmiIngredient other)) {
      return false;
    }
    return width == other.width && amount == other.amount && Float.compare(chance, other.chance) == 0
        && modifiers.equals(other.modifiers) && style == other.style;
  }

  @Override
  public int hashCode() {
    return Objects.hash(modifiers, style, width, amount, chance);
  }
}
