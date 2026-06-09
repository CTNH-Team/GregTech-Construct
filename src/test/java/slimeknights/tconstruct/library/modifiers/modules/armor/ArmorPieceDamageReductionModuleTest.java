package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.world.entity.EquipmentSlot;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.data.ModifierIds;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

class ArmorPieceDamageReductionModuleTest extends BaseMcTest {
  private static final Offset<Float> TOLERANCE = within(0.001f);

  @Test
  void reduceDamage_addsConfiguredResistancePerArmorPiece() {
    assertThat(ArmorPieceDamageReductionModule.reduceDamage(100, 0.25f, 0)).isEqualTo(100, TOLERANCE);
    assertThat(ArmorPieceDamageReductionModule.reduceDamage(100, 0.25f, 1)).isEqualTo(75, TOLERANCE);
    assertThat(ArmorPieceDamageReductionModule.reduceDamage(100, 0.25f, 2)).isEqualTo(50, TOLERANCE);
    assertThat(ArmorPieceDamageReductionModule.reduceDamage(100, 0.25f, 3)).isEqualTo(25, TOLERANCE);
    assertThat(ArmorPieceDamageReductionModule.reduceDamage(100, 0.25f, 4)).isEqualTo(0, TOLERANCE);
  }

  @Test
  void getResistance_clampsToFullArmorSetReduction() {
    assertThat(ArmorPieceDamageReductionModule.getResistance(0.25f, -1)).isEqualTo(0, TOLERANCE);
    assertThat(ArmorPieceDamageReductionModule.getResistance(0.25f, 1)).isEqualTo(0.25f, TOLERANCE);
    assertThat(ArmorPieceDamageReductionModule.getResistance(0.25f, 4)).isEqualTo(1, TOLERANCE);
    assertThat(ArmorPieceDamageReductionModule.getResistance(0.25f, 5)).isEqualTo(1, TOLERANCE);
  }

  @Test
  void countMatchingArmorPieces_countsEachArmorPieceOnceRegardlessOfModifierLevel() {
    ArmorPieceDamageReductionModule module = ArmorPieceDamageReductionModule.perPiece(DamageSourcePredicate.ANY, 0.25f);
    EquipmentContext context = Mockito.mock(EquipmentContext.class);
    IToolStackView boots = toolWithInsulationLevel(1);
    IToolStackView leggings = toolWithInsulationLevel(3);
    IToolStackView chestplate = toolWithInsulationLevel(5);
    IToolStackView mainHand = toolWithInsulationLevel(7);
    IToolStackView offHand = toolWithInsulationLevel(7);
    when(context.getValidTool(EquipmentSlot.FEET)).thenReturn(boots);
    when(context.getValidTool(EquipmentSlot.LEGS)).thenReturn(leggings);
    when(context.getValidTool(EquipmentSlot.CHEST)).thenReturn(chestplate);
    when(context.getValidTool(EquipmentSlot.HEAD)).thenReturn(null);
    when(context.getValidTool(EquipmentSlot.MAINHAND)).thenReturn(mainHand);
    when(context.getValidTool(EquipmentSlot.OFFHAND)).thenReturn(offHand);

    int count = module.countMatchingArmorPieces(context, new ModifierEntry(ModifierIds.insulation, 9));

    assertThat(count).isEqualTo(3);
  }

  private static IToolStackView toolWithInsulationLevel(int level) {
    IToolStackView tool = Mockito.mock(IToolStackView.class);
    when(tool.getModifiers()).thenReturn(new ModifierNBT(List.of(new ModifierEntry(ModifierIds.insulation, level))));
    return tool;
  }
}
