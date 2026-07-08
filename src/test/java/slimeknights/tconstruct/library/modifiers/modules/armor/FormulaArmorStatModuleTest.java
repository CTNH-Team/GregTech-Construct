package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Items;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStat;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStats;
import slimeknights.tconstruct.library.tools.nbt.DummyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FormulaArmorStatModuleTest extends BaseMcTest {
  private static final ResourceLocation FORMULA = TConstruct.getResource("test/armor_stat");
  private static final DummyToolStack TOOL = new TestToolStack();
  private static final DamageSource DAMAGE = null;
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
      .addArmorDamageStats(TOOL, new ModifierEntry(ModifierIds.plating, 3), null, null, DAMAGE, stats);

    assertThat(stats.postReduction()).isEqualTo(0.625f);
    assertThat(inputs[0]).containsExactly(0.25, 3.0, 100.0, 75.0);
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

  @FunctionalInterface
  private interface FormulaBody {
    double accept(double[] values);
  }
}
