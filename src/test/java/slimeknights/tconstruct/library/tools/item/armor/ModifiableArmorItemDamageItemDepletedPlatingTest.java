package slimeknights.tconstruct.library.tools.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.modifiers.modules.behavior.ToolDamageCapacityModule;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class ModifiableArmorItemDamageItemDepletedPlatingTest extends BaseMcTest {
  private static final ResourceLocation DAMAGE_FORMULA = TConstruct.getResource("test/plating_depleted/damage_item_damage");
  private static final ResourceLocation CAPACITY_FORMULA = TConstruct.getResource("test/plating_depleted/damage_item_capacity");
  private static final ModifierId TEST_MODIFIER_ID = new ModifierId("test", "damage_item_plating_depleted");
  private final Map<ResourceLocation,IFormula> previousFormulas = FormulaManager.getAll();
  private final Map<ResourceLocation,String> previousRawJson = FormulaManager.getAllRawJson();

  @AfterEach
  void restoreFormulaManager() {
    FormulaManager.applySync(previousFormulas, previousRawJson);
  }

  @Test
  void damageItemStillDamagesArmorWhenPlatingIsDepleted() {
    FormulaManager.applySync(Map.of(
      DAMAGE_FORMULA, formula(values -> values[1]),
      CAPACITY_FORMULA, formula(values -> 0.0)
    ), Map.of(DAMAGE_FORMULA, "{}", CAPACITY_FORMULA, "{}"));

    bind(new TestPlatingModifier());

    ModifiableArmorItem armor = Mockito.mock(ModifiableArmorItem.class, Mockito.CALLS_REAL_METHODS);
    when(armor.canBeDepleted()).thenReturn(true);

    ItemStack stack = mock(ItemStack.class);
    AtomicBoolean broke = new AtomicBoolean(false);
    AtomicInteger damage = new AtomicInteger();
    ToolStack tool = mock(ToolStack.class);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    when(tool.getDamage()).thenAnswer(invocation -> damage.get());
    when(tool.getCurrentDurability()).thenAnswer(invocation -> 100 - damage.get());
    doAnswer(invocation -> {
      damage.set(invocation.getArgument(0));
      return null;
    }).when(tool).setDamage(Mockito.anyInt());
    when(tool.hasTag(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0) == TinkerTags.Items.DURABILITY);
    when(tool.getModifiers()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1));
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.getModifier(TEST_MODIFIER_ID)).thenReturn(new ModifierEntry(ModifierManager.INSTANCE.getValue(TEST_MODIFIER_ID), 1));
    when(tool.isBroken()).thenReturn(false);
    when(tool.isUnbreakable()).thenReturn(false);

    try (MockedStatic<ToolStack> toolStacks = Mockito.mockStatic(ToolStack.class)) {
      toolStacks.when(() -> ToolStack.from(stack)).thenReturn(tool);

      int result = armor.damageItem(stack, 1, mock(LivingEntity.class), living -> broke.set(true));

      assertThat(result).isZero();
      assertThat(damage.get()).isEqualTo(1);
      assertThat(broke.get()).isFalse();
    }
  }

  private static Modifier bind(Modifier modifier) {
    try {
      Method setId = Modifier.class.getDeclaredMethod("setId", ModifierId.class);
      setId.setAccessible(true);
      setId.invoke(modifier, TEST_MODIFIER_ID);

      Field staticModifiers = ModifierManager.class.getDeclaredField("staticModifiers");
      staticModifiers.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<ModifierId,Modifier> modifiers = (Map<ModifierId,Modifier>) staticModifiers.get(ModifierManager.INSTANCE);
      modifiers.put(TEST_MODIFIER_ID, modifier);

      Field dynamicModifiersLoaded = ModifierManager.class.getDeclaredField("dynamicModifiersLoaded");
      dynamicModifiersLoaded.setAccessible(true);
      dynamicModifiersLoaded.setBoolean(ModifierManager.INSTANCE, true);
      return modifier;
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }

  private static IFormula formula(FormulaBody body) {
    return new IFormula() {
      @Override
      public double accept(double... values) {
        return body.accept(values);
      }

      @Override
      public int expectInputSize() {
        return 4;
      }
    };
  }

  private static class TestPlatingModifier extends Modifier {
    private TestPlatingModifier() {
      super(ModuleHookMap.builder()
        .addHook(new ZeroBar(), ModifierHooks.CAPACITY_BAR)
        .addHook(ToolDamageCapacityModule.of(DAMAGE_FORMULA, CAPACITY_FORMULA, 100), ModifierHooks.TOOL_DAMAGE)
        .build());
    }
  }

  private static class ZeroBar implements CapacityBarHook {
    @Override
    public int getAmount(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool) {
      return 0;
    }

    @Override
    public int getCapacity(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry entry) {
      return 10;
    }

    @Override
    public void setAmount(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry entry, int amount) {}
  }

  @FunctionalInterface
  private interface FormulaBody {
    double accept(double[] values);
  }
}
