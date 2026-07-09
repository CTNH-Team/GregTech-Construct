package slimeknights.tconstruct.library.tools.helper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
import slimeknights.tconstruct.library.tools.nbt.DummyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ToolDamageUtilPlatingDepletedTest extends BaseMcTest {
  private static final ResourceLocation DAMAGE_FORMULA = TConstruct.getResource("test/plating_depleted/damage");
  private static final ResourceLocation CAPACITY_FORMULA = TConstruct.getResource("test/plating_depleted/capacity");
  private static final ModifierId TEST_MODIFIER_ID = new ModifierId("test", "plating_depleted");
  private final Map<ResourceLocation,IFormula> previousFormulas = FormulaManager.getAll();
  private final Map<ResourceLocation,String> previousRawJson = FormulaManager.getAllRawJson();

  @AfterEach
  void restoreFormulaManager() {
    FormulaManager.applySync(previousFormulas, previousRawJson);
  }

  @Test
  void depletedPlatingStillDamagesUnderlyingTool() {
    FormulaManager.applySync(Map.of(
      DAMAGE_FORMULA, formula(values -> values[1]),
      CAPACITY_FORMULA, formula(values -> 0.0)
    ), Map.of(DAMAGE_FORMULA, "{}", CAPACITY_FORMULA, "{}"));

    bind(new TestPlatingModifier());
    TestToolStack tool = new TestToolStack();

    boolean broke = ToolDamageUtil.damage(tool, 1, mock(LivingEntity.class), new ItemStack(Items.AIR));

    assertThat(broke).isFalse();
    assertThat(tool.getDamage()).isEqualTo(1);
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

  private static class TestToolStack extends DummyToolStack {
    private int damage;

    private TestToolStack() {
      super(Items.AIR, ModifierNBT.EMPTY, new ModDataNBT());
    }

    @Override
    public StatsNBT getStats() {
      return StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build();
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

    @Override
    public boolean hasTag(TagKey<Item> tag) {
      return tag == TinkerTags.Items.DURABILITY;
    }

    @Override
    public ModifierNBT getModifiers() {
      return ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1);
    }
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
