package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
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
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStat;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStats;
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
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FormulaArmorStatModuleTest extends BaseMcTest {
  private static final ResourceLocation FORMULA = TConstruct.getResource("test/armor_stat");
  private static final DummyToolStack TOOL = new TestToolStack();
  private static final DamageSource DAMAGE = null;
  private static final ModifierId TEST_MODIFIER_ID = new ModifierId("test", "formula_armor_stat");
  private final Map<ResourceLocation,IFormula> previousFormulas = FormulaManager.getAll();
  private final Map<ResourceLocation,String> previousRawJson = FormulaManager.getAllRawJson();

  @AfterEach
  void restoreFormulaManager() {
    FormulaManager.applySync(previousFormulas, previousRawJson);
  }

  @Test
  void missingFormulaDoesNotChangeStats() {
    FormulaManager.applySync(Map.of(), Map.of());
    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0);

    FormulaArmorStatModule.stat(ArmorDamageStat.POST_REDUCTION, FORMULA)
      .addArmorDamageStats(TOOL, new ModifierEntry(ModifierIds.crystalLattice, 2), null, null, DAMAGE, stats);

    assertThat(stats.postReduction()).isZero();
  }

  @Test
  void formulaReceivesModifierLevelAndAddsArmorStat() {
    double[][] inputs = new double[1][];
    FormulaManager.applySync(Map.of(FORMULA, formula(values -> {
      inputs[0] = values;
      return 0.375;
    })), Map.of(FORMULA, "{}"));
    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0.25f, 0);

    FormulaArmorStatModule.stat(ArmorDamageStat.POST_REDUCTION, FORMULA)
      .addArmorDamageStats(TOOL, bind(new TestCapacityModifier(new TestCapacityBar(40, 20)), 3), null, null, DAMAGE, stats);

    assertThat(stats.postReduction()).isEqualTo(0.625f);
    assertThat(inputs[0]).containsExactly(0.25, 3.0, 40.0, 20.0);
  }

  @Test
  void platingStyleStatTargetsArmorProtection() {
    FormulaManager.applySync(Map.of(FORMULA, formula(values -> 0.15)), Map.of(FORMULA, "{}"));
    ArmorDamageStats stats = new ArmorDamageStats(0, 0.4f, 0.2f, 0.25f);

    FormulaArmorStatModule.stat(ArmorDamageStat.ARMOR_PROTECTION, FORMULA)
      .addArmorDamageStats(TOOL, bind(new TestCapacityModifier(new TestCapacityBar(40, 20)), 2), null, null, DAMAGE, stats);

    assertThat(stats.armorProtection()).isEqualTo(0.40f);
    assertThat(stats.preReduction()).isEqualTo(0.4f);
    assertThat(stats.postReduction()).isEqualTo(0.2f);
  }

  @Test
  void crystalLatticeStyleStatTargetsPreReduction() {
    FormulaManager.applySync(Map.of(FORMULA, formula(values -> 0.125)), Map.of(FORMULA, "{}"));
    ArmorDamageStats stats = new ArmorDamageStats(0, 0.3f, 0.2f, 0.1f);

    FormulaArmorStatModule.stat(ArmorDamageStat.PRE_REDUCTION, FORMULA)
      .addArmorDamageStats(TOOL, bind(new TestCapacityModifier(new TestCapacityBar(25, 12)), 1), null, null, DAMAGE, stats);

    assertThat(stats.preReduction()).isEqualTo(0.425f);
    assertThat(stats.postReduction()).isEqualTo(0.2f);
    assertThat(stats.armorProtection()).isEqualTo(0.1f);
  }

  private static IFormula formula(FormulaBody body) {
    return new IFormula() {
      @Override
      public double accept(double... values) {
        return body.accept(Arrays.copyOf(values, values.length));
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
    private final int amount;

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
    public void setAmount(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry entry, int amount) {}
  }

  private static class TestCapacityModifier extends Modifier {
    private TestCapacityModifier(CapacityBarHook capacityBar) {
      super(ModuleHookMap.builder().addHook(capacityBar, ModifierHooks.CAPACITY_BAR).build());
    }
  }

  private static ModifierEntry bind(Modifier modifier, int level) {
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
      return new ModifierEntry(modifier, level);
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }

  @FunctionalInterface
  private interface FormulaBody {
    double accept(double[] values);
  }
}
