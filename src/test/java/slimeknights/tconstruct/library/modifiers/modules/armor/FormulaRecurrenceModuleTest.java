package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStats;
import slimeknights.tconstruct.library.tools.nbt.DummyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.logic.PlayerPersistentDataCache;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormulaRecurrenceModuleTest extends BaseMcTest {
  private static final ResourceLocation ARMOR_STAT = TConstruct.getResource("test/recurrence/stat");
  private static final ResourceLocation DAMAGE = TConstruct.getResource("test/recurrence/damage");
  private static final ResourceLocation TICK = TConstruct.getResource("test/recurrence/tick");
  private static final ResourceLocation KEY = TConstruct.getResource("test/recurrence");
  private final TestToolStack tool = new TestToolStack();
  private final Map<ResourceLocation,IFormula> previousFormulas = FormulaManager.getAll();
  private final Map<ResourceLocation,String> previousRawJson = FormulaManager.getAllRawJson();

  @AfterEach
  void restoreFormulaManager() {
    FormulaManager.applySync(previousFormulas, previousRawJson);
    PlayerPersistentDataCache.remove(TEST_PLAYER);
    PlayerPersistentDataCache.resetDataGetter();
  }

  private static final UUID TEST_PLAYER = UUID.randomUUID();

  @Test
  void storedReductionAppliesAndUpdatesFromDamage() {
    double[][] armorInputs = new double[1][];
    FormulaManager.applySync(Map.of(
      ARMOR_STAT, formula(values -> {
        armorInputs[0] = values;
        return 1.0;
      }),
      DAMAGE, formula(values -> 5.0),
      TICK, formula(values -> 0)
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    tool.getPersistentData().putFloat(KEY, 2);
    DamageSource source = mock(DamageSource.class);
    when(source.is(DamageTypeTags.BYPASSES_ARMOR)).thenReturn(false);

    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0, 10);
    FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY)
      .addArmorDamageStats(tool, new ModifierEntry(ModifierIds.recurrence, 2), null, EquipmentSlot.CHEST, source, stats);

    assertThat(stats.preReduction()).isEqualTo(2);
    assertThat(tool.getPersistentData().getFloat(KEY)).isEqualTo(1);
    assertThat(armorInputs[0]).containsExactly(2.0, 0.0, 2.0, 8.0);
  }

  @Test
  void armorTickDecaysStoredReduction() {
    FormulaManager.applySync(Map.of(
      ARMOR_STAT, formula(values -> 0),
      DAMAGE, formula(values -> 0),
      TICK, formula(values -> 4.0)
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    tool.getPersistentData().putFloat(KEY, 5);
    LivingEntity holder = mock(LivingEntity.class);
    holder.tickCount = 20;
    net.minecraft.world.level.Level level = mock(net.minecraft.world.level.Level.class);
    when(holder.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);

    FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY)
      .onArmorTick(tool, new ModifierEntry(ModifierIds.recurrence, 2), EquipmentSlot.CHEST, holder);

    assertThat(tool.getPersistentData().getFloat(KEY)).isEqualTo(4);
  }

  @Test
  void damageToPersistentUsesStoredPersistentValueInAccumulatorFormula() {
    double[][] damageInputs = new double[1][];
    FormulaManager.applySync(Map.of(
      ARMOR_STAT, formula(values -> 0.0),
      DAMAGE, formula(values -> {
        damageInputs[0] = values;
        return values[0] + values[1] + values[2] + values[3];
      }),
      TICK, formula(values -> 0.0)
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    tool.getPersistentData().putFloat(KEY, 2);
    DamageSource source = mock(DamageSource.class);
    when(source.is(DamageTypeTags.BYPASSES_ARMOR)).thenReturn(false);
    Player player = mock(Player.class);
    Level level = mock(Level.class);
    when(player.getUUID()).thenReturn(TEST_PLAYER);
    when(player.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);

    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0, 10);
    FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY)
      .onDamageToPersistent(tool, new ModifierEntry(ModifierIds.recurrence, 2), slimeknights.tconstruct.library.tools.context.EquipmentContext.withTool(player, tool, EquipmentSlot.CHEST), EquipmentSlot.CHEST, source, stats);

    assertThat(tool.getPersistentData().getFloat(KEY)).isEqualTo(16);
    assertThat(damageInputs[0]).containsExactly(2.0, 2.0, 2.0, 10.0);
  }

  @Test
  void damageToPersistentMarksIncrementalCacheForPlayerTooltipConsumption() {
    FormulaManager.applySync(Map.of(
      ARMOR_STAT, formula(values -> values[0]),
      DAMAGE, formula(values -> 6.0),
      TICK, formula(values -> values[0])
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    tool.getPersistentData().putFloat(KEY, 2);
    DamageSource source = mock(DamageSource.class);
    when(source.is(DamageTypeTags.BYPASSES_ARMOR)).thenReturn(false);
    Player player = mock(Player.class);
    Level level = mock(Level.class);
    when(player.getUUID()).thenReturn(TEST_PLAYER);
    when(player.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);
    PlayerPersistentDataCache.setDataGetter(ignored -> new ModDataNBT());

    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0, 10);
    FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY)
      .onDamageToPersistent(tool, new ModifierEntry(ModifierIds.recurrence, 2), slimeknights.tconstruct.library.tools.context.EquipmentContext.withTool(player, tool, EquipmentSlot.CHEST), EquipmentSlot.CHEST, source, stats);

    assertThat(tool.getPersistentData().getFloat(KEY)).isEqualTo(6);
    ModDataNBT entityData = new ModDataNBT();
    entityData.putFloat(KEY, 6);
    entityData.putLong(ResourceLocation.tryParse(KEY + "_expiry"), 77L);
    PlayerPersistentDataCache.sync(TEST_PLAYER, entityData, packet -> PlayerPersistentDataCache.put(TEST_PLAYER, KEY.toString(), 6, 77L));
    assertThat(PlayerPersistentDataCache.get(TEST_PLAYER, KEY.toString(), 0)).isEqualTo(6);
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
  }

  @FunctionalInterface
  private interface FormulaBody {
    double accept(double[] values);
  }
}
