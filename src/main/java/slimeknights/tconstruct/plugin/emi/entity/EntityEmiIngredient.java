package slimeknights.tconstruct.plugin.emi.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class EntityEmiIngredient implements EmiIngredient {
  private static final ResourceLocation MISSING = Mantle.getResource("textures/item/missingno.png");
  private static final Set<EntityType<?>> IGNORED_ENTITIES = new HashSet<>();

  private final List<EntityIngredient.EntityInput> display;
  private final List<EmiStack> eggs;
  private final int size;
  private final Map<EntityType<?>,Entity> entities = new HashMap<>();
  private long amount;
  private float chance;

  EntityEmiIngredient(EntityIngredient ingredient, int size) {
    this(ingredient.getDisplay(), TConstructEmiRecipe.itemOutputs(ingredient.getEggs()), size, 1, 1);
  }

  private EntityEmiIngredient(List<EntityIngredient.EntityInput> display, List<EmiStack> eggs, int size,
                              long amount, float chance) {
    this.display = List.copyOf(display);
    this.eggs = List.copyOf(eggs);
    this.size = size;
    this.amount = amount;
    this.chance = chance;
  }

  @Override
  public List<EmiStack> getEmiStacks() {
    return eggs.isEmpty() ? List.of(EmiStack.EMPTY) : eggs;
  }

  @Override
  public boolean isEmpty() {
    return display.isEmpty();
  }

  @Override
  public EmiIngredient copy() {
    return new EntityEmiIngredient(display, eggs, size, amount, chance);
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
    if (display.isEmpty()) {
      return;
    }
    EntityType<?> type = display.get(EmiRenderHelper.cycleIndex(display.size())).type();
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.level != null && !IGNORED_ENTITIES.contains(type)) {
      Entity entity = type == EntityType.PLAYER ? minecraft.player : entities.computeIfAbsent(type, key -> key.create(minecraft.level));
      if (entity instanceof LivingEntity living) {
        int scale = size / 2;
        if (entity.getBbHeight() > 2 || entity.getBbWidth() > 2) {
          scale = (int)(size / Math.max(entity.getBbHeight(), entity.getBbWidth()));
        }
        try {
          graphics.pose().pushPose();
          graphics.pose().translate(x, y, 0);
          InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, size / 2, size, scale, 0, 10, living);
          graphics.pose().popPose();
          return;
        } catch (Exception exception) {
          graphics.pose().popPose();
          Mantle.logger.error("Error drawing EMI entity {}", BuiltInRegistries.ENTITY_TYPE.getKey(type), exception);
          IGNORED_ENTITIES.add(type);
          entities.remove(type);
        }
      } else {
        IGNORED_ENTITIES.add(type);
        entities.remove(type);
      }
    }
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1, 1, 1, 1);
    int offset = (size - 16) / 2;
    graphics.blit(MISSING, x + offset, y + offset, 0, 0, 16, 16, 16, 16);
  }

  @Override
  public List<ClientTooltipComponent> getTooltip() {
    if (display.isEmpty()) {
      return List.of();
    }
    EntityType<?> type = display.get(EmiRenderHelper.cycleIndex(display.size())).type();
    List<net.minecraft.network.chat.Component> tooltip = new ArrayList<>();
    tooltip.add(type.getDescription());
    if (EmiRenderHelper.advancedTooltips()) {
      tooltip.add(net.minecraft.network.chat.Component.literal(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString())
          .withStyle(ChatFormatting.DARK_GRAY));
    }
    return EmiRenderHelper.tooltip(tooltip);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof EntityEmiIngredient other)) {
      return false;
    }
    return size == other.size && amount == other.amount && Float.compare(chance, other.chance) == 0
        && display.equals(other.display);
  }

  @Override
  public int hashCode() {
    return Objects.hash(display, size, amount, chance);
  }
}
