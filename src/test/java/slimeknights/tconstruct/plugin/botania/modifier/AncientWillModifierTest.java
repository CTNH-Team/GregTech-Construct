package slimeknights.tconstruct.plugin.botania.modifier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.test.BaseMcTest;
import vazkii.botania.api.mana.ManaDiscountEvent;

import static org.assertj.core.api.Assertions.assertThat;

class AncientWillModifierTest extends BaseMcTest {
  private static final Offset<Float> TOLERANCE = Offset.strictOffset(0.0001F);

  private Player player;

  @BeforeEach
  void setUp() {
    player = Mockito.mock(Player.class);
    Mockito.when(player.getItemBySlot(Mockito.any(EquipmentSlot.class))).thenReturn(ItemStack.EMPTY);
  }

  @Test
  void manaDiscountIsAddedWhenTerrarecoverSetIsFull() {
    equip(EquipmentSlot.HEAD, 1);
    equip(EquipmentSlot.CHEST, 1);
    equip(EquipmentSlot.LEGS, 1);
    equip(EquipmentSlot.FEET, 1);
    ManaDiscountEvent event = new ManaDiscountEvent(player, 0.15F, ItemStack.EMPTY);

    AncientWillModifier.onManaDiscount(event);

    assertThat(event.getDiscount()).isCloseTo(0.35F, TOLERANCE);
  }

  @Test
  void manaDiscountIsNotAddedWhenTerrarecoverSetIsPartial() {
    equip(EquipmentSlot.HEAD, 1);
    equip(EquipmentSlot.CHEST, 1);
    equip(EquipmentSlot.LEGS, 1);
    ManaDiscountEvent event = new ManaDiscountEvent(player, 0.15F, ItemStack.EMPTY);

    AncientWillModifier.onManaDiscount(event);

    assertThat(event.getDiscount()).isCloseTo(0.15F, TOLERANCE);
  }

  @Test
  void singleHighLevelTerrarecoverPieceDoesNotActivateManaDiscount() {
    equip(EquipmentSlot.HEAD, 4);
    ManaDiscountEvent event = new ManaDiscountEvent(player, 0.15F, ItemStack.EMPTY);

    AncientWillModifier.onManaDiscount(event);

    assertThat(event.getDiscount()).isCloseTo(0.15F, TOLERANCE);
  }

  private void equip(EquipmentSlot slot, int level) {
    ItemStack stack = Mockito.mock(ItemStack.class);
    CompoundTag entry = new CompoundTag();
    entry.putString(ModifierEntry.TAG_MODIFIER, BotaniaModifierIds.terrarecover.toString());
    entry.putInt(ModifierEntry.TAG_LEVEL, level);
    ListTag modifiers = new ListTag();
    modifiers.add(entry);
    CompoundTag tag = new CompoundTag();
    tag.put(ToolStack.TAG_MODIFIERS, modifiers);
    Mockito.when(stack.isEmpty()).thenReturn(false);
    Mockito.when(stack.is(Mockito.<TagKey<Item>>any())).thenAnswer(invocation -> invocation.getArgument(0) == TinkerTags.Items.MODIFIABLE);
    Mockito.when(stack.getTag()).thenReturn(tag);
    Mockito.when(player.getItemBySlot(slot)).thenReturn(stack);
  }
}
