package slimeknights.tconstruct.library.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TooltipUtilSetBonusTest extends BaseMcTest {
  private static final ModifierId BASE = ModifierFixture.TEST_1;
  private static final ModifierId BONUS = ModifierFixture.TEST_2;
  private static final ModifierId UNUSED_BASE = new ModifierId("test", "unused_base");

  private Player player;

  @BeforeEach
  void setUp() {
    ModifierFixture.init();
    ModifierSetBonusHelper.clearForTesting();
    ModifierSetBonusHelper.register(BASE, BONUS);
    player = Mockito.mock(Player.class);
    Level level = Mockito.mock(Level.class);
    Mockito.when(level.registryAccess()).thenReturn(RegistryAccess.EMPTY);
    Mockito.when(player.level()).thenReturn(level);
  }

  @AfterEach
  void tearDown() {
    ModifierSetBonusHelper.clearForTesting();
  }

  @Test
  void equippedArmorShowsBaseModifierProgressWhenSetIsPartial() {
    ItemStack shownStack = modifiableStackWithModifier(BASE, 1);
    equip(EquipmentSlot.HEAD, shownStack);
    equip(EquipmentSlot.CHEST, modifiableStackWithModifier(BASE, 1));
    equip(EquipmentSlot.LEGS, modifiableStackWithModifier(BASE, 1));
    equip(EquipmentSlot.FEET, modifiableStackWithModifier(new ModifierId("test", "other"), 1));

    List<Component> tooltip = modifierTooltip(shownStack, BASE, 1);

    assertThat(tooltip).extracting(Component::getString)
                       .containsExactly("modifier.test.modifier_1 I (3/4)");
  }

  @Test
  void equippedArmorShowsBonusModifierNameWhenSetIsFull() {
    ItemStack shownStack = modifiableStackWithModifier(BASE, 1);
    equip(EquipmentSlot.HEAD, shownStack);
    equip(EquipmentSlot.CHEST, modifiableStackWithModifier(BASE, 1));
    equip(EquipmentSlot.LEGS, modifiableStackWithModifier(BASE, 1));
    equip(EquipmentSlot.FEET, modifiableStackWithModifier(BASE, 1));

    List<Component> tooltip = modifierTooltip(shownStack, BASE, 1);

    assertThat(tooltip).extracting(Component::getString)
                       .containsExactly("modifier.test.modifier_2 I (4/4)");
  }

  @Test
  @Disabled("Advanced tooltips read Forge client config, which is not loaded in this unit test harness")
  void advancedTooltipUsesBonusModifierIdWhenSetIsFull() {
    ItemStack shownStack = modifiableStackWithModifier(BASE, 1);
    equip(EquipmentSlot.HEAD, shownStack);
    equip(EquipmentSlot.CHEST, modifiableStackWithModifier(BASE, 1));
    equip(EquipmentSlot.LEGS, modifiableStackWithModifier(BASE, 1));
    equip(EquipmentSlot.FEET, modifiableStackWithModifier(BASE, 1));

    List<Component> tooltip = modifierTooltip(shownStack, BASE, 1, TooltipFlag.Default.ADVANCED);

    assertThat(tooltip).extracting(Component::getString)
                       .containsExactly("modifier.test.modifier_2 I (4/4) (test:modifier_2)");
  }

  @Test
  void singleArmorPieceWithHighModifierLevelStillShowsOneOfFourProgress() {
    ItemStack shownStack = modifiableStackWithModifier(BASE, 4);
    equip(EquipmentSlot.HEAD, shownStack);

    List<Component> tooltip = modifierTooltip(shownStack, BASE, 4);

    assertThat(tooltip).extracting(Component::getString)
                       .containsExactly("modifier.test.modifier_1 IV (1/4)");
  }

  @Test
  void unequippedStackKeepsOrdinaryModifierName() {
    ItemStack shownStack = modifiableStackWithModifier(BASE, 1);
    equip(EquipmentSlot.HEAD, modifiableStackWithModifier(BASE, 1));
    equip(EquipmentSlot.CHEST, modifiableStackWithModifier(BASE, 1));
    equip(EquipmentSlot.LEGS, modifiableStackWithModifier(BASE, 1));
    equip(EquipmentSlot.FEET, modifiableStackWithModifier(BASE, 1));

    List<Component> tooltip = modifierTooltip(shownStack, BASE, 1);

    assertThat(tooltip).extracting(Component::getString)
                       .containsExactly("modifier.test.modifier_1 I");
  }

  @Test
  void unregisteredModifierKeepsOrdinaryNameWhenEquipped() {
    ModifierSetBonusHelper.clearForTesting();
    ModifierSetBonusHelper.register(UNUSED_BASE, BASE);
    ModifierId ordinary = BONUS;
    ItemStack shownStack = modifiableStackWithModifier(ordinary, 1);
    equip(EquipmentSlot.HEAD, shownStack);
    equip(EquipmentSlot.CHEST, modifiableStackWithModifier(ordinary, 1));
    equip(EquipmentSlot.LEGS, modifiableStackWithModifier(ordinary, 1));
    equip(EquipmentSlot.FEET, modifiableStackWithModifier(ordinary, 1));

    List<Component> tooltip = modifierTooltip(shownStack, ordinary, 1);

    assertThat(tooltip).extracting(Component::getString)
                       .containsExactly("modifier.test.modifier_2 I");
  }

  private List<Component> modifierTooltip(ItemStack stack, ModifierId modifier, int level) {
    return modifierTooltip(stack, modifier, level, TooltipFlag.Default.NORMAL);
  }

  private List<Component> modifierTooltip(ItemStack stack, ModifierId modifier, int level, TooltipFlag flag) {
    List<Component> tooltip = new ArrayList<>();
    IToolStackView tool = Mockito.mock(IToolStackView.class);
    Mockito.when(tool.getModifierList()).thenReturn(List.of(new ModifierEntry(modifier, level)));
    TooltipUtil.addModifierNames(stack, tool, player, tooltip, flag);
    return tooltip;
  }

  private void equip(EquipmentSlot slot, ItemStack stack) {
    Mockito.when(player.getItemBySlot(slot)).thenReturn(stack);
  }

  @SuppressWarnings("unchecked")
  private static ItemStack modifiableStackWithModifier(ModifierId modifier, int level) {
    ItemStack stack = Mockito.mock(ItemStack.class);
    CompoundTag entry = new CompoundTag();
    entry.putString(ModifierEntry.TAG_MODIFIER, modifier.toString());
    entry.putInt(ModifierEntry.TAG_LEVEL, level);
    ListTag modifiers = new ListTag();
    modifiers.add(entry);
    CompoundTag tag = new CompoundTag();
    tag.put(ToolStack.TAG_MODIFIERS, modifiers);
    Mockito.when(stack.isEmpty()).thenReturn(false);
    Mockito.when(stack.is((TagKey<Item>)TinkerTags.Items.MODIFIABLE)).thenReturn(true);
    Mockito.when(stack.getTag()).thenReturn(tag);
    return stack;
  }
}
