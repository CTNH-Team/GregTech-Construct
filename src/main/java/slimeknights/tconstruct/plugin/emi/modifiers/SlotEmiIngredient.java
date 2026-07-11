package slimeknights.tconstruct.plugin.emi.modifiers;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import slimeknights.mantle.client.model.NBTKeyModel;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.SlotType.SlotCount;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class SlotEmiIngredient implements EmiIngredient {
  private static final String KEY_SLOT = TConstruct.makeTranslationKey("emi", "modifiers.slot");
  private static final String KEY_SLOTS = TConstruct.makeTranslationKey("emi", "modifiers.slots");
  private static final String KEY_ID = TConstruct.makeTranslationKey("emi", "modifier_slot.id");
  private static final List<Component> TEXT_FREE = List.of(TConstruct.makeTranslation("emi", "modifiers.free"));
  private static final Map<SlotType,TextureAtlasSprite> SLOT_SPRITES = new HashMap<>();

  @Nullable
  private final SlotCount slots;
  private long amount;
  private float chance;

  SlotEmiIngredient(@Nullable SlotCount slots) {
    this(slots, 1, 1);
  }

  private SlotEmiIngredient(@Nullable SlotCount slots, long amount, float chance) {
    this.slots = slots;
    this.amount = amount;
    this.chance = chance;
  }

  @Override
  public List<EmiStack> getEmiStacks() {
    return List.of(EmiStack.EMPTY);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public EmiIngredient copy() {
    return new SlotEmiIngredient(slots, amount, chance);
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
    if (slots != null && slots.count() > 0) {
      String text = Integer.toString(slots.count());
      graphics.drawString(Minecraft.getInstance().font, text,
          x + 9 - Minecraft.getInstance().font.width(text), y + 5, 0xFF808080, false);
    }
    graphics.blit(x + 8, y, 0, 16, 16, SLOT_SPRITES.computeIfAbsent(SlotCount.type(slots), SlotEmiIngredient::loadSprite));
  }

  @Override
  public List<ClientTooltipComponent> getTooltip() {
    if (slots == null || slots.count() <= 0) {
      return EmiRenderHelper.tooltip(TEXT_FREE);
    }
    SlotType type = slots.type();
    Component text = slots.count() == 1
        ? Component.translatable(KEY_SLOT, type.getDisplayName())
        : Component.translatable(KEY_SLOTS, slots.count(), type.getDisplayName());
    if (EmiRenderHelper.advancedTooltips()) {
      return EmiRenderHelper.tooltip(List.of(text,
          Component.translatable(KEY_ID, type.getName()).withStyle(ChatFormatting.DARK_GRAY)));
    }
    return EmiRenderHelper.tooltip(List.of(text));
  }

  @SuppressWarnings("removal")
  private static TextureAtlasSprite loadSprite(@Nullable SlotType slotType) {
    Minecraft minecraft = Minecraft.getInstance();
    ModelManager modelManager = minecraft.getModelManager();
    BakedModel model = minecraft.getItemRenderer().getItemModelShaper().getItemModel(TinkerModifiers.creativeSlotItem.get());
    if (model != null && model.getOverrides() instanceof NBTKeyModel.Overrides overrides) {
      Material material = overrides.getTexture(slotType == null ? "slotless" : slotType.getName());
      return modelManager.getAtlas(material.atlasLocation()).getSprite(material.texture());
    }
    return modelManager.getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(MissingTextureAtlasSprite.getLocation());
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof SlotEmiIngredient other)) {
      return false;
    }
    return amount == other.amount && Float.compare(chance, other.chance) == 0 && Objects.equals(slots, other.slots);
  }

  @Override
  public int hashCode() {
    return Objects.hash(slots, amount, chance);
  }
}
