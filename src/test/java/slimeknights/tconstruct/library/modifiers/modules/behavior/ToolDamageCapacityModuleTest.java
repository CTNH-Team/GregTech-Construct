package slimeknights.tconstruct.library.modifiers.modules.behavior;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.DummyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ToolDamageCapacityModuleTest extends BaseMcTest {
  private static final ResourceLocation DAMAGE_FORMULA = TConstruct.getResource("test/tool_damage_capacity/damage");
  private static final ResourceLocation CAPACITY_FORMULA = TConstruct.getResource("test/tool_damage_capacity/capacity");
  private static final ModifierId TEST_MODIFIER_ID = new ModifierId("test", "tool_damage_capacity");
  private final Map<ResourceLocation,IFormula> previousFormulas = FormulaManager.getAll();
  private final Map<ResourceLocation,String> previousRawJson = FormulaManager.getAllRawJson();

  @AfterEach
  void restoreFormulaManager() {
    FormulaManager.applySync(previousFormulas, previousRawJson);
  }

  @Test
  void damageFormulaUsesPreConsumptionBarAmount() {
    FormulaManager.applySync(Map.of(
      DAMAGE_FORMULA, formula(values -> Math.max(0, values[1] - values[3])),
      CAPACITY_FORMULA, formula(values -> Math.min(values[1], values[3]))
    ), Map.of(
      DAMAGE_FORMULA, "{}",
      CAPACITY_FORMULA, "{}"
    ));

    TestCapacityBar capacityBar = new TestCapacityBar(10, 3);
    ModifierEntry modifier = new ModifierEntry(bind(new TestCapacityModifier(capacityBar)), 1);

    int damage = ToolDamageCapacityModule.of(DAMAGE_FORMULA, CAPACITY_FORMULA, 100)
      .onDamageTool(new TestToolStack(), modifier, 5, null);

    assertThat(damage).isEqualTo(2);
    assertThat(capacityBar.amount).isZero();
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

  private static class TestToolStack extends DummyToolStack {
    private TestToolStack() {
      super(Items.AIR, ModifierNBT.EMPTY, new ModDataNBT());
    }

    @Override
    public StatsNBT getStats() {
      return StatsNBT.builder().set(ToolStats.DURABILITY, 100).build();
    }

    @Override
    public int getCurrentDurability() {
      return 75;
    }
  }

  private static class TestCapacityBar implements CapacityBarHook {
    private final int capacity;
    private int amount;

    private TestCapacityBar(int capacity, int amount) {
      this.capacity = capacity;
      this.amount = amount;
    }

    @Override
    public int getAmount(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool) {
      return amount;
    }

    @Override
    public int getCapacity(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry entry) {
      return capacity;
    }

    @Override
    public void setAmount(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry entry, int amount) {
      this.amount = Math.max(0, Math.min(amount, capacity));
    }
  }

  private static class TestCapacityModifier extends Modifier {
    private TestCapacityModifier(CapacityBarHook capacityBar) {
      super(ModuleHookMap.builder().addHook(capacityBar, ModifierHooks.CAPACITY_BAR).build());
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

  @FunctionalInterface
  private interface FormulaBody {
    double accept(double[] values);
  }
}
