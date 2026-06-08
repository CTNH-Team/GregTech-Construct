package slimeknights.tconstruct.library.modifiers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ModifierSetBonusHelperTest extends BaseMcTest {
  private static final ModifierId BASE = new ModifierId("test", "set_base");
  private static final ModifierId BONUS = new ModifierId("test", "set_bonus");
  private static final ModifierId OTHER = new ModifierId("test", "other_modifier");

  private net.minecraft.world.entity.player.Player player;

  @BeforeEach
  void setUp() {
    ModifierSetBonusHelper.clearForTesting();
    player = Mockito.mock(net.minecraft.world.entity.player.Player.class);
    Mockito.when(player.getItemBySlot(Mockito.any(EquipmentSlot.class))).thenReturn(ItemStack.EMPTY);
  }

  @AfterEach
  void tearDown() {
    ModifierSetBonusHelper.clearForTesting();
  }

  @Test
  void registerRejectsDuplicateBaseModifier() {
    ModifierSetBonusHelper.register(BASE, BONUS);

    assertThatIllegalArgumentException()
      .isThrownBy(() -> ModifierSetBonusHelper.register(BASE, OTHER))
      .withMessageContaining(BASE.toString());
  }

  @Test
  void registerRequiresBothModifierIds() {
    assertThatNullPointerException()
      .isThrownBy(() -> ModifierSetBonusHelper.register(null, BONUS))
      .withMessageContaining("baseModifier");

    assertThatNullPointerException()
      .isThrownBy(() -> ModifierSetBonusHelper.register(BASE, null))
      .withMessageContaining("bonusDisplayModifier");
  }

  @Test
  void getBonusDisplayModifierReturnsRegisteredModifier() {
    ModifierSetBonusHelper.register(BASE, BONUS);

    assertThat(ModifierSetBonusHelper.getBonusDisplayModifier(BASE)).isEqualTo(BONUS);
    assertThat(ModifierSetBonusHelper.getBonusDisplayModifier(OTHER)).isNull();
  }

  @Test
  void countsOneEquippedArmorPiecePerSlot() {
    equip(EquipmentSlot.HEAD, BASE, 3);
    equip(EquipmentSlot.CHEST, BASE, 1);
    equip(EquipmentSlot.LEGS, OTHER, 1);
    equip(EquipmentSlot.FEET, BASE, 2);

    assertThat(ModifierSetBonusHelper.getEquippedSetCount(player, BASE)).isEqualTo(3);
    assertThat(ModifierSetBonusHelper.hasFullSet(player, BASE)).isFalse();
  }

  @Test
  void modifierLevelDoesNotCountAsMultipleArmorPieces() {
    equip(EquipmentSlot.HEAD, BASE, 4);

    assertThat(ModifierSetBonusHelper.getEquippedSetCount(player, BASE)).isEqualTo(1);
    assertThat(ModifierSetBonusHelper.hasFullSet(player, BASE)).isFalse();
  }

  @Test
  void vanillaArmorWithForgedModifierNbtDoesNotCount() {
    Mockito.when(player.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(stackWithModifier(BASE, 1, false));
    Mockito.when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(stackWithModifier(BASE, 1, false));
    Mockito.when(player.getItemBySlot(EquipmentSlot.LEGS)).thenReturn(stackWithModifier(BASE, 1, false));
    Mockito.when(player.getItemBySlot(EquipmentSlot.FEET)).thenReturn(stackWithModifier(BASE, 1, false));

    assertThat(ModifierSetBonusHelper.getEquippedSetCount(player, BASE)).isZero();
    assertThat(ModifierSetBonusHelper.hasFullSet(player, BASE)).isFalse();
  }

  @Test
  void hasFullSetRequiresAllFourArmorSlots() {
    equip(EquipmentSlot.HEAD, BASE, 1);
    equip(EquipmentSlot.CHEST, BASE, 1);
    equip(EquipmentSlot.LEGS, BASE, 1);
    equip(EquipmentSlot.FEET, BASE, 1);

    assertThat(ModifierSetBonusHelper.getEquippedSetCount(player, BASE)).isEqualTo(4);
    assertThat(ModifierSetBonusHelper.hasFullSet(player, BASE)).isTrue();
  }

  private void equip(EquipmentSlot slot, ModifierId modifier, int level) {
    ItemStack stack = stackWithModifier(modifier, level, true);
    Mockito.when(player.getItemBySlot(slot)).thenReturn(stack);
  }

  @SuppressWarnings("unchecked")
  private static ItemStack stackWithModifier(ModifierId modifier, int level, boolean modifiable) {
    ItemStack stack = modifiable ? Mockito.mock(ItemStack.class) : new ItemStack(Items.LEATHER_HELMET);
    CompoundTag tag = new CompoundTag();
    CompoundTag entry = new CompoundTag();
    entry.putString(ModifierEntry.TAG_MODIFIER, modifier.toString());
    entry.putInt(ModifierEntry.TAG_LEVEL, level);
    ListTag modifiers = new ListTag();
    modifiers.add(entry);
    tag.put(ToolStack.TAG_MODIFIERS, modifiers);
    if (modifiable) {
      Mockito.when(stack.isEmpty()).thenReturn(false);
      Mockito.when(stack.is((TagKey<Item>)TinkerTags.Items.MODIFIABLE)).thenReturn(true);
      Mockito.when(stack.getTag()).thenReturn(tag);
    } else {
      stack.setTag(tag);
    }
    return stack;
  }
}
