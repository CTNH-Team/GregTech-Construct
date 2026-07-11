package slimeknights.tconstruct.library.modifiers.modules.behavior;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
import slimeknights.tconstruct.library.modifiers.modules.capacity.StatCapacityBarManager;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormulaCapacityRegenerateModuleTest extends BaseMcTest {
  private static final ResourceLocation REGENERATE = TConstruct.getResource("test/capacity_regen/regenerate");
  private static final ResourceLocation CONSUME = TConstruct.getResource("test/capacity_regen/consume");
  private static final ResourceLocation COOLDOWN = TConstruct.getResource("test/capacity_regen/cooldown");
  private static final ModifierId TEST_MODIFIER_ID = new ModifierId("test", "capacity_regen_bar");
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
  void duraConsumeFormulaReceivesAmountCapacityGainOrdering() {
    double[][] consumeInputs = new double[1][];
    FormulaManager.applySync(Map.of(
      REGENERATE, formula(values -> 2.0),
      CONSUME, formula(values -> {
        consumeInputs[0] = values;
        return 0.0;
      }),
      COOLDOWN, formula(values -> 0.0)
    ), Map.of(REGENERATE, "{}", CONSUME, "{}", COOLDOWN, "{}"));
    when(level.isClientSide()).thenReturn(false);
    when(level.getGameTime()).thenReturn(40L);

    TestCapacityBar bar = new TestCapacityBar(40, 20);
    bindBar(new TestCapacityModifier(bar));
    StatCapacityBarManager.register(TEST_MODIFIER_ID, bar);

    new FormulaCapacityRegenerateModule(TEST_MODIFIER_ID, REGENERATE, CONSUME, 1, COOLDOWN, null, slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ANY_TOOL)
      .onInventoryTick(tool, new ModifierEntry(ModifierIds.hardening, 3), level, holder, 0, false, true, Items.AIR.getDefaultInstance());

    assertThat(consumeInputs[0]).containsExactly(3.0, 20.0, 40.0, 2.0);
  }

  @Test
  void regeneratesOutsideTheCorrectEquipmentSlot() {
    FormulaManager.applySync(Map.of(
      REGENERATE, formula(values -> 2.0),
      CONSUME, formula(values -> 0.0),
      COOLDOWN, formula(values -> 0.0)
    ), Map.of(REGENERATE, "{}", CONSUME, "{}", COOLDOWN, "{}"));
    when(level.isClientSide()).thenReturn(false);
    when(level.getGameTime()).thenReturn(40L);

    TestCapacityBar bar = new TestCapacityBar(40, 20);
    bindBar(new TestCapacityModifier(bar));
    StatCapacityBarManager.register(TEST_MODIFIER_ID, bar);

    new FormulaCapacityRegenerateModule(TEST_MODIFIER_ID, REGENERATE, CONSUME, 1, COOLDOWN, null, slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ANY_TOOL)
      .onInventoryTick(tool, new ModifierEntry(ModifierIds.hardening, 1), level, holder, 0, false, false, Items.AIR.getDefaultInstance());

    assertThat(bar.amount).isEqualTo(22);
  }

  @Test
  void consumesDurabilityWhenTheCostEqualsTheRemainingAmount() {
    FormulaManager.applySync(Map.of(
      REGENERATE, formula(values -> 2.0),
      CONSUME, formula(values -> 2.0),
      COOLDOWN, formula(values -> 0.0)
    ), Map.of(REGENERATE, "{}", CONSUME, "{}", COOLDOWN, "{}"));
    when(level.isClientSide()).thenReturn(false);
    when(level.getGameTime()).thenReturn(40L);

    TestCapacityBar bar = new TestCapacityBar(40, 20);
    bindBar(new TestCapacityModifier(bar));
    StatCapacityBarManager.register(TEST_MODIFIER_ID, bar);
    TestToolStack depleted = new TestToolStack(2);

    new FormulaCapacityRegenerateModule(TEST_MODIFIER_ID, REGENERATE, CONSUME, 1, COOLDOWN, null, slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ANY_TOOL)
      .onInventoryTick(depleted, new ModifierEntry(ModifierIds.hardening, 1), level, holder, 0, false, true, Items.AIR.getDefaultInstance());

    assertThat(depleted.damage).isEqualTo(2);
    assertThat(bar.amount).isEqualTo(22);
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
    private final int durability;
    private int damage;

    private TestToolStack() {
      this(100);
    }

    private TestToolStack(int durability) {
      super(Items.AIR, ModifierNBT.EMPTY, new ModDataNBT());
      this.durability = durability;
    }

    @Override
    public StatsNBT getStats() {
      return StatsNBT.builder().set(ToolStats.DURABILITY, durability).build();
    }

    @Override
    public int getDamage() {
      return damage;
    }

    @Override
    public int getCurrentDurability() {
      return durability - damage;
    }

    @Override
    public void setDamage(int damage) {
      this.damage = damage;
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
      this.amount = amount;
    }
  }

  private static class TestCapacityModifier extends Modifier {
    private TestCapacityModifier(CapacityBarHook capacityBar) {
      super(ModuleHookMap.builder().addHook(capacityBar, ModifierHooks.CAPACITY_BAR).build());
    }
  }

  private static void bindBar(Modifier modifier) {
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
    } catch (ReflectiveOperationException exception) {
      throw new AssertionError(exception);
    }
  }

  @FunctionalInterface
  private interface FormulaBody {
    double accept(double[] values);
  }
}
