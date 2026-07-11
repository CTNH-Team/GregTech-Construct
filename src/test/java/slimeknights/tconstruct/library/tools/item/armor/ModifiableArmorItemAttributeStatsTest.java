package slimeknights.tconstruct.library.tools.item.armor;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.ArmorDefinitions;
import slimeknights.tconstruct.tools.stats.ArmorStats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModifiableArmorItemAttributeStatsTest extends BaseMcTest {
  @Test
  void knightsChestplateAppliesNegativeMovementSpeedFromSpeedPenalty() throws Exception {
    ModifiableArmorItem armor = Mockito.mock(ModifiableArmorItem.class, Mockito.CALLS_REAL_METHODS);
    java.lang.reflect.Field typeField = net.minecraft.world.item.ArmorItem.class.getDeclaredField("type");
    typeField.setAccessible(true);
    typeField.set(armor, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE);
    java.lang.reflect.Field definitionField = ModifiableArmorItem.class.getDeclaredField("toolDefinition");
    definitionField.setAccessible(true);
    definitionField.set(armor, ArmorDefinitions.KNIGHTS.getArmorDefinition(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE));

    ToolStack tool = mock(ToolStack.class);
    when(tool.isBroken()).thenReturn(false);
    when(tool.getStats()).thenReturn(StatsNBT.builder()
      .set(ToolStats.ARMOR, 9f)
      .set(ToolStats.ARMOR_TOUGHNESS, 4f)
      .set(ArmorStats.SPEED_PENALTY, 0.15f)
      .build());
    when(tool.getModifierList()).thenReturn(java.util.List.of());

    Multimap<Attribute,AttributeModifier> attributes = armor.getAttributeModifiers(tool, EquipmentSlot.CHEST);

    assertThat(attributes.get(Attributes.MOVEMENT_SPEED))
      .anySatisfy(modifier -> {
        assertThat(modifier.getAmount()).isCloseTo(-0.15, org.assertj.core.data.Offset.offset(1.0E-5));
        assertThat(modifier.getOperation()).isEqualTo(AttributeModifier.Operation.MULTIPLY_TOTAL);
      });
  }
}
