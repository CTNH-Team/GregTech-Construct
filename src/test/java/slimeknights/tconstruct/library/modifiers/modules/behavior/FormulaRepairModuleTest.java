package slimeknights.tconstruct.library.modifiers.modules.behavior;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormulaRepairModuleTest extends BaseMcTest {
  private static final ResourceLocation REPAIR = TConstruct.getResource("test/repair");
  private static final ResourceLocation COOLDOWN = TConstruct.getResource("test/cooldown");
  private final TestToolStack tool = new TestToolStack();
  private final Level level = mock(Level.class);
  private final LivingEntity holder = mock(LivingEntity.class);
  private final Map<ResourceLocation,IFormula> previousFormulas = FormulaManager.getAll();
  private final Map<ResourceLocation,String> previousRawJson = FormulaManager.getAllRawJson();

  @AfterEach
  void restoreFormulaManager() {
    FormulaManager.applySync(previousFormulas, previousRawJson);
  }

  @Test
  void missingFormulaDoesNotRepair() {
    FormulaManager.applySync(Map.of(), Map.of());
    tool.damage = 25;

    FormulaRepairModule.repair(REPAIR, COOLDOWN)
      .onInventoryTick(tool, new ModifierEntry(ModifierIds.hardening, 2), level, holder, 0, false, true, Items.AIR.getDefaultInstance());

    assertThat(tool.damage).isEqualTo(25);
  }

  @Test
  void formulaRepairsAndStoresCooldown() {
    double[][] repairInputs = new double[1][];
    double[][] cooldownInputs = new double[1][];
    FormulaManager.applySync(Map.of(
      REPAIR, formula(values -> {
        repairInputs[0] = values;
        return 3.0;
      }),
      COOLDOWN, formula(values -> {
        cooldownInputs[0] = values;
        return 7.0;
      })
    ), Map.of(REPAIR, "{}", COOLDOWN, "{}"));
    when(level.getGameTime()).thenReturn(40L);
    tool.damage = 25;

    FormulaRepairModule.repair(REPAIR, COOLDOWN)
      .onInventoryTick(tool, new ModifierEntry(ModifierIds.hardening, 2), level, holder, 0, false, true, Items.AIR.getDefaultInstance());

    assertThat(tool.damage).isEqualTo(22);
    assertThat(repairInputs[0]).containsExactly(2.0, 75.0, 100.0, 75.0);
    assertThat(cooldownInputs[0]).containsExactly(2.0, 78.0, 100.0, 75.0);
    assertThat(tool.getPersistentData().getCompound(ModifierIds.hardening).getLong("next_tick")).isEqualTo(47L);
    assertThat(tool.getPersistentData().getCompound(ModifierIds.hardening).getInt("last_amount")).isEqualTo(78);
  }

  @Test
  void cooldownPreventsRepair() {
    FormulaManager.applySync(Map.of(REPAIR, formula(values -> 3.0), COOLDOWN, formula(values -> 7.0)), Map.of(REPAIR, "{}", COOLDOWN, "{}"));
    when(level.getGameTime()).thenReturn(40L);
    tool.damage = 25;
    CompoundTag state = new CompoundTag();
    state.putLong("next_tick", 41L);
    tool.getPersistentData().put(ModifierIds.hardening, state);

    FormulaRepairModule.repair(REPAIR, COOLDOWN)
      .onInventoryTick(tool, new ModifierEntry(ModifierIds.hardening, 2), level, holder, 0, false, true, Items.AIR.getDefaultInstance());

    assertThat(tool.damage).isEqualTo(25);
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
    private int damage;

    private TestToolStack() {
      super(Items.AIR, ModifierNBT.EMPTY, new ModDataNBT());
    }

    @Override
    public StatsNBT getStats() {
      return StatsNBT.builder().set(ToolStats.DURABILITY, 100).build();
    }

    @Override
    public int getDamage() {
      return damage;
    }

    @Override
    public int getCurrentDurability() {
      return 100 - damage;
    }

    @Override
    public void setDamage(int damage) {
      this.damage = damage;
    }
  }

  @FunctionalInterface
  private interface FormulaBody {
    double accept(double[] values);
  }
}
