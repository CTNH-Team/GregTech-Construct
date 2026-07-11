package slimeknights.tconstruct.plugin.emi.partbuilder;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class PatternEmiIngredient implements EmiIngredient {
  private final Pattern pattern;
  private long amount;
  private float chance;

  PatternEmiIngredient(Pattern pattern) {
    this(pattern, 1, 1);
  }

  private PatternEmiIngredient(Pattern pattern, long amount, float chance) {
    this.pattern = pattern;
    this.amount = amount;
    this.chance = chance;
  }

  @Override
  public List<EmiStack> getEmiStacks() {
    return List.of(EmiStack.EMPTY);
  }

  @Override
  public boolean isEmpty() {
    return pattern == null;
  }

  @Override
  public EmiIngredient copy() {
    return new PatternEmiIngredient(pattern, amount, chance);
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
    if (pattern != null) {
      GuiUtil.renderPattern(graphics, pattern, x, y);
    }
  }

  @Override
  public List<ClientTooltipComponent> getTooltip() {
    if (pattern == null) {
      return List.of();
    }
    List<Component> tooltip = new ArrayList<>();
    tooltip.add(pattern.getDisplayName());
    if (EmiRenderHelper.advancedTooltips()) {
      tooltip.add(Component.literal(pattern.toString()).withStyle(ChatFormatting.DARK_GRAY));
    }
    return EmiRenderHelper.tooltip(tooltip);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof PatternEmiIngredient other)) {
      return false;
    }
    return amount == other.amount && Float.compare(chance, other.chance) == 0 && Objects.equals(pattern, other.pattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pattern, amount, chance);
  }
}
