package slimeknights.tconstruct.tables.client.inventory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaternionf;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.item.ITinkerStationDisplay;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.TinkerTooltipFlags;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;
import slimeknights.tconstruct.tables.apotheosis.ApotheosisSocketMode;
import slimeknights.tconstruct.tables.client.inventory.module.InfoPanelScreen;
import slimeknights.tconstruct.tables.menu.TabbedContainerMenu;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Shared logic for the tinker station and the modifier worktable */
public abstract class ToolTableScreen<T extends BlockEntity, C extends TabbedContainerMenu<T>> extends BaseTabbedScreen<T,C> {
  private static final Component MODIFIERS_TEXT = TConstruct.makeTranslation("gui", "tinker_station.modifiers");
  private static final Component UPGRADES_TEXT = TConstruct.makeTranslation("gui", "tinker_station.upgrades");
  private static final Component TRAITS_TEXT = TConstruct.makeTranslation("gui", "tinker_station.traits");
  private static final String SOCKET_COUNT_KEY = TConstruct.makeTranslationKey("stat", "socket_count");
  private static final ResourceLocation ATTRIBUTESLIB_ARROW_DAMAGE = ResourceLocation.tryBuild("attributeslib", "arrow_damage");
  private static final ResourceLocation ATTRIBUTESLIB_ARROW_VELOCITY = ResourceLocation.tryBuild("attributeslib", "arrow_velocity");
  private static final ResourceLocation ATTRIBUTESLIB_DRAW_SPEED = ResourceLocation.tryBuild("attributeslib", "draw_speed");
  private static final ResourceLocation ATTRIBUTESLIB_MINING_SPEED = ResourceLocation.tryBuild("attributeslib", "mining_speed");

  private static final ResourceLocation ICON_TEXTURE = TConstruct.getResource("textures/gui/icons.png");

  /** Side panels, for tools and modifiers */
  protected final InfoPanelScreen<ToolTableScreen<T,C>,C> tinkerInfo;
  protected final InfoPanelScreen<ToolTableScreen<T,C>,C> modifierInfo;

  protected final Player player;
  @Nullable
  protected ArmorStand armorStandPreview;
  protected boolean enableArmorStandPreview = true;
  protected boolean clickedOnArmorStand = false;

  protected int armorStandX = 0;
  protected int armorStandY = 0;
  protected int armorStandScale = 10;
  protected float armorStandAngle = 0;
  protected double armorStandLastMouseX = -1;

  public ToolTableScreen(C c, Inventory playerInventory, Component title) {
    super(c, playerInventory, title);
    this.player = playerInventory.player;

    this.tinkerInfo = new InfoPanelScreen<>(this, c, playerInventory, title);
    this.tinkerInfo.setTextScale(8/9f);
    this.addModule(this.tinkerInfo);

    this.modifierInfo = new InfoPanelScreen<>(this, c, playerInventory, title);
    this.modifierInfo.setTextScale(7/9f);
    this.addModule(this.modifierInfo);
  }

  @Override
  protected void init() {
    super.init();
    if (enableArmorStandPreview) {
      assert this.minecraft != null;
      assert this.minecraft.level != null;
      this.armorStandPreview = new ArmorStand(this.minecraft.level, 0.0D, 0.0D, 0.0D);
      this.armorStandPreview.setNoBasePlate(true);
      this.armorStandPreview.setShowArms(true);
      this.armorStandPreview.yBodyRot = 210.0F;
      this.armorStandPreview.setXRot(25.0F);
      this.armorStandPreview.yHeadRot = this.armorStandPreview.getYRot();
      this.armorStandPreview.yHeadRotO = this.armorStandPreview.getYRot();
    }
  }

  /**
   * Renders the armor stand
   * @param graphics  Graphics instance
   */
  protected void renderArmorStand(GuiGraphics graphics) {
    if (this.armorStandPreview != null) {
      Quaternionf pose = new Quaternionf();
      SmithingScreen.ARMOR_STAND_ANGLE.rotateY(this.armorStandAngle, pose);
      InventoryScreen.renderEntityInInventory(graphics, this.armorStandX, this.armorStandY, this.armorStandScale, pose, null, this.armorStandPreview);

      graphics.blit(ICON_TEXTURE, armorStandX - 16, armorStandY - 16, 0, 184, 32, 32);
    }
  }

 /**
  * Setup clickable areas and position for the armor stand
  * @param x         Stand X position
  * @param y         Stand Y position
  * @param scale     Stand size
  */
  protected void setupArmorStandPreview(int x, int y, int scale) {
    this.armorStandX = this.cornerX + x;
    this.armorStandY = this.cornerY + y;
    this.armorStandScale = scale;
  }

  /** Updates the item displayed on the armor stand */
  protected void updateArmorStandPreview(ItemStack stack) {
    if (this.armorStandPreview != null) {
      for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
        this.armorStandPreview.setItemSlot(equipmentslot, ItemStack.EMPTY);
      }

      if (!stack.isEmpty()) {
        ItemStack copy = stack.copy();
        Item item = stack.getItem();
        if (item instanceof ArmorItem armor) {
          this.armorStandPreview.setItemSlot(armor.getEquipmentSlot(), copy);
        } else {
          this.armorStandPreview.setItemSlot(EquipmentSlot.OFFHAND, copy);
        }
      }

    }
  }

  /** Updates the tool panel area */
  protected void updateToolPanel(LazyToolStack lazyToolStack) {
    ToolStack tool = lazyToolStack.getTool();
    if (tool.getItem() instanceof ITinkerStationDisplay display) {
      List<Component> tooltip = display.getStatInformation(tool, Minecraft.getInstance().player, new ArrayList<>(), SafeClientAccess.getTooltipKey(), TinkerTooltipFlags.TINKER_STATION);
      appendApotheosisSocketTooltipData(lazyToolStack.getStack(), tool, display, this.player, tooltip, true);
      tinkerInfo.setCaption(display.getLocalizedName());
      tinkerInfo.setText(tooltip);
    }
    else {
      ItemStack result = lazyToolStack.getStack();
      tinkerInfo.setCaption(result.getHoverName());
      List<Component> list = new ArrayList<>();
      result.getItem().appendHoverText(result, Minecraft.getInstance().level, list, Default.NORMAL);
      appendApotheosisSocketBonuses(result, list);
      tinkerInfo.setText(list);
    }
  }

  public static void appendApotheosisSocketTooltipData(ItemStack stack, IToolStackView tool, ITinkerStationDisplay display, @Nullable Player player, List<Component> tooltip, boolean includeBonusLines) {
    appendApotheosisSocketCount(stack, tooltip);
    applyApotheosisSocketStatOverrides(stack, tool, display, player, tooltip);
    if (includeBonusLines) {
      appendApotheosisSocketBonuses(stack, tooltip);
    }
  }

  public static void appendApotheosisSocketBonuses(ItemStack stack, List<Component> tooltip) {
    ApotheosisSocketMode.appendTooltip(stack, tooltip);
  }

  public static void appendApotheosisSocketCount(ItemStack stack, List<Component> tooltip) {
    int totalSocketCount = ApotheosisBridge.sockets().getSocketCount(stack);
    if (totalSocketCount > 0) {
      int filledSocketCount = (int) ApotheosisBridge.sockets().getSocketedGemData(stack).stream()
        .filter(ApotheosisBridge.SocketGem::isFilled)
        .count();
      tooltip.add(Component.translatable(SOCKET_COUNT_KEY)
        .append(Component.literal(filledSocketCount + "/" + totalSocketCount)
          .withStyle(style -> style.withColor(ToolStats.ATTACK_SPEED.getColor()))));
    }
  }

  public static void applyApotheosisSocketStatOverrides(ItemStack stack, IToolStackView tool, ITinkerStationDisplay display, @Nullable Player player, List<Component> tooltip) {
    EquipmentSlot slot = getDisplayAttributeSlot(display);
    Multimap<Attribute,AttributeModifier> gemModifiers = getSocketAttributeModifiers(stack, slot);
    if (gemModifiers.isEmpty()) {
      return;
    }

    replaceAttributeStatLine(tooltip, ToolStats.ATTACK_DAMAGE, tool.getStats().get(ToolStats.ATTACK_DAMAGE), getPlayerBaseAttribute(player, Attributes.ATTACK_DAMAGE), gemModifiers.get(Attributes.ATTACK_DAMAGE));
    replaceAttributeStatLine(tooltip, ToolStats.ATTACK_SPEED, tool.getStats().get(ToolStats.ATTACK_SPEED), getPlayerBaseAttribute(player, Attributes.ATTACK_SPEED), gemModifiers.get(Attributes.ATTACK_SPEED));
    replaceScaledMultiplierStatLine(tooltip, ToolStats.MINING_SPEED, tool.getStats().get(ToolStats.MINING_SPEED), findAttribute(ATTRIBUTESLIB_MINING_SPEED), gemModifiers);
    replaceScaledMultiplierStatLine(tooltip, ToolStats.DRAW_SPEED, tool.getStats().get(ToolStats.DRAW_SPEED), findAttribute(ATTRIBUTESLIB_DRAW_SPEED), gemModifiers);
    replaceScaledMultiplierStatLine(tooltip, ToolStats.VELOCITY, tool.getStats().get(ToolStats.VELOCITY), findAttribute(ATTRIBUTESLIB_ARROW_VELOCITY), gemModifiers);
    replaceScaledMultiplierStatLine(tooltip, ToolStats.PROJECTILE_DAMAGE, tool.getStats().get(ToolStats.PROJECTILE_DAMAGE), findAttribute(ATTRIBUTESLIB_ARROW_DAMAGE), gemModifiers);
    replaceAttributeStatLine(tooltip, ToolStats.ARMOR, tool.getStats().get(ToolStats.ARMOR), (float) Attributes.ARMOR.getDefaultValue(), gemModifiers.get(Attributes.ARMOR));
    replaceAttributeStatLine(tooltip, ToolStats.ARMOR_TOUGHNESS, tool.getStats().get(ToolStats.ARMOR_TOUGHNESS), (float) Attributes.ARMOR_TOUGHNESS.getDefaultValue(), gemModifiers.get(Attributes.ARMOR_TOUGHNESS));
    replaceScaledAttributeStatLine(tooltip, ToolStats.KNOCKBACK_RESISTANCE, tool.getStats().get(ToolStats.KNOCKBACK_RESISTANCE), (float) Attributes.KNOCKBACK_RESISTANCE.getDefaultValue(), 10f, gemModifiers.get(Attributes.KNOCKBACK_RESISTANCE));
  }

  private static void replaceAttributeStatLine(List<Component> tooltip, FloatToolStat stat, float displayedValueWithoutGems, float attributeBaseValue, Collection<AttributeModifier> modifiers) {
    if (modifiers.isEmpty()) {
      return;
    }
    replaceStatLine(tooltip, stat, stat.formatValue(applyAttributeModifiers(attributeBaseValue, displayedValueWithoutGems - attributeBaseValue, modifiers)));
  }

  private static void replaceScaledAttributeStatLine(List<Component> tooltip, FloatToolStat stat, float rawValueWithoutGems, float attributeBaseValue, float scale, Collection<AttributeModifier> modifiers) {
    if (modifiers.isEmpty()) {
      return;
    }
    replaceStatLine(tooltip, stat, stat.formatValue(applyAttributeModifiers(attributeBaseValue, rawValueWithoutGems - attributeBaseValue, modifiers) * scale));
  }

  private static void replaceScaledMultiplierStatLine(List<Component> tooltip, FloatToolStat stat, float baseValue, @Nullable Attribute attribute, Multimap<Attribute,AttributeModifier> modifiers) {
    if (attribute == null || !modifiers.containsKey(attribute)) {
      return;
    }
    float multiplier = applyAttributeModifiers((float) attribute.getDefaultValue(), 0, modifiers.get(attribute));
    replaceStatLine(tooltip, stat, stat.formatValue(baseValue * multiplier));
  }

  private static void replaceStatLine(List<Component> tooltip, IToolStat<?> stat, Component replacement) {
    String prefix = stat.getPrefix().getString();
    for (int i = 0; i < tooltip.size(); i++) {
      if (tooltip.get(i).getString().startsWith(prefix)) {
        tooltip.set(i, replacement);
        return;
      }
    }
  }

  private static Multimap<Attribute,AttributeModifier> getSocketAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
    Multimap<Attribute,AttributeModifier> modifiers = ArrayListMultimap.create();
    ApotheosisBridge.sockets().addAttributeModifiers(stack, slot, modifiers::put);
    return modifiers;
  }

  private static EquipmentSlot getDisplayAttributeSlot(ITinkerStationDisplay display) {
    if (display.asItem() instanceof ArmorItem armor) {
      return armor.getEquipmentSlot();
    }
    return EquipmentSlot.MAINHAND;
  }

  private static float getPlayerBaseAttribute(@Nullable Player player, Attribute attribute) {
    if (player != null) {
      return (float) player.getAttributeBaseValue(attribute);
    }
    return (float) attribute.getDefaultValue();
  }

  private static float applyAttributeModifiers(float attributeBaseValue, float coreAddition, Collection<AttributeModifier> modifiers) {
    double value = attributeBaseValue + coreAddition;
    for (AttributeModifier modifier : modifiers) {
      if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
        value += modifier.getAmount();
      }
    }

    double multipliedBaseValue = value;
    for (AttributeModifier modifier : modifiers) {
      if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
        multipliedBaseValue += attributeBaseValue * modifier.getAmount();
      }
    }

    for (AttributeModifier modifier : modifiers) {
      if (modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
        multipliedBaseValue *= 1.0D + modifier.getAmount();
      }
    }
    return (float) multipliedBaseValue;
  }

  @Nullable
  private static Attribute findAttribute(ResourceLocation id) {
    return net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(id);
  }

  /** Updates the modifier panel with relevant info */
  protected void updateModifierPanel(ToolStack tool) {
    RegistryAccess access = player.level().registryAccess();
    List<Component> modifierNames = new ArrayList<>();
    List<Component> modifierTooltip = new ArrayList<>();
    Component title;
    // control displays just traits, bit trickier to do
    if (hasControlDown()) {
      title = TRAITS_TEXT;
      Map<Modifier,Integer> upgrades = tool.getUpgrades().getModifiers().stream()
                                           .collect(Collectors.toMap(ModifierEntry::getModifier, ModifierEntry::getLevel, Integer::sum));
      for (ModifierEntry entry : tool.getModifierList()) {
        Modifier mod = entry.getModifier();
        if (mod.shouldDisplay(true)) {
          int level = entry.getLevel() - upgrades.getOrDefault(mod, 0);
          if (level > 0) {
            ModifierEntry trait = new ModifierEntry(entry.getModifier(), level);
            modifierNames.add(mod.getDisplayName(tool, trait, access));
            modifierTooltip.add(mod.getDescription(tool, trait));
          }
        }
      }
    } else {
      // shift is just upgrades/abilities, otherwise all
      List<ModifierEntry> modifiers;
      if (hasShiftDown()) {
        modifiers = tool.getUpgrades().getModifiers();
        title = UPGRADES_TEXT;
      } else {
        modifiers = tool.getModifierList();
        title = MODIFIERS_TEXT;
      }
      for (ModifierEntry entry : modifiers) {
        Modifier mod = entry.getModifier();
        if (mod.shouldDisplay(true)) {
          modifierNames.add(mod.getDisplayName(tool, entry, access));
          modifierTooltip.add(mod.getDescription(tool, entry));
        }
      }
    }

    modifierInfo.setCaption(title);
    modifierInfo.setText(modifierNames, modifierTooltip);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    int armorStandBoxW = this.armorStandScale + 30;
    int armorStandBoxH = this.armorStandScale * 2;
    int armorStandBoxX = this.armorStandX - armorStandBoxW / 2;
    int armorStandBoxY = this.armorStandY - armorStandBoxH + 5;
    this.clickedOnArmorStand = this.enableArmorStandPreview && GuiUtil.isHovered((int) mouseX, (int) mouseY, armorStandBoxX, armorStandBoxY, armorStandBoxW, armorStandBoxH);

    return super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int state) {
    this.clickedOnArmorStand = false;
    this.armorStandLastMouseX = -1;

    return super.mouseReleased(mouseX, mouseY, state);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double timeSinceLastClick, double unkowwn) {
    if (this.enableArmorStandPreview && this.clickedOnArmorStand && this.armorStandLastMouseX != -1) {
      this.armorStandAngle += (float) (mouseX - this.armorStandLastMouseX) / 10f;
    }
    this.armorStandLastMouseX = mouseX;

    return super.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick, unkowwn);
  }
}
