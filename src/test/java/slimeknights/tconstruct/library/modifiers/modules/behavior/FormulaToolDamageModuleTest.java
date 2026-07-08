package slimeknights.tconstruct.library.modifiers.modules.behavior;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
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

class FormulaToolDamageModuleTest extends BaseMcTest {
  private static final ResourceLocation FORMULA = TConstruct.getResource("test/tool_damage");
  private static final DummyToolStack TOOL = new TestToolStack();
  private final Map<ResourceLocation,IFormula> previousFormulas = FormulaManager.getAll();
  private final Map<ResourceLocation,String> previousRawJson = FormulaManager.getAllRawJson();

  @AfterEach
  void restoreFormulaManager() {
    FormulaManager.applySync(previousFormulas, previousRawJson);
  }

  @Test
  void missingFormulaLeavesDamageUnchanged() {
    FormulaManager.applySync(Map.of(), Map.of());

    int damage = FormulaToolDamageModule.formula(FORMULA)
      .onDamageTool(TOOL, new ModifierEntry(ModifierIds.cushion, 1), 7, null);

    assertThat(damage).isEqualTo(7);
  }

  @Test
  void formulaReceivesModifierLevelAndDamageAmount() {
    double[][] inputs = new double[1][];
    FormulaManager.applySync(Map.of(FORMULA, formula(values -> {
      inputs[0] = values;
      return 2.0;
    })), Map.of(FORMULA, "{}"));

    int damage = FormulaToolDamageModule.formula(FORMULA)
      .onDamageTool(TOOL, new ModifierEntry(ModifierIds.cushion, 3), 5, null);

    assertThat(damage).isEqualTo(2);
    assertThat(inputs[0]).containsExactly(3.0, 5.0, 100.0, 75.0);
  }

  @Test
  void formulaDamageIsClampedAtZero() {
    FormulaManager.applySync(Map.of(FORMULA, formula(values -> -4.0)), Map.of(FORMULA, "{}"));

    int damage = FormulaToolDamageModule.formula(FORMULA)
      .onDamageTool(TOOL, new ModifierEntry(ModifierIds.cushion, 1), 5, null);

    assertThat(damage).isZero();
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
