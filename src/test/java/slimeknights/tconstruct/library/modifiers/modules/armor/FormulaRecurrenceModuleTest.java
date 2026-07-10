package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStats;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
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
    FormulaRecurrenceModule.resetPersistentDataGetter();
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
      DAMAGE, formula(values -> values[0]),
      TICK, formula(values -> 0)
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    ModDataNBT entityData = new ModDataNBT();
    entityData.putFloat(KEY, 2);
    FormulaRecurrenceModule.setPersistentDataGetter(ignored -> entityData);
    Player player = player(entityData);
    DamageSource source = recurrenceSource();

    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0, 10);
    FormulaRecurrenceModule module = FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY);
    FormulaRecurrenceModule.beginDamageEvent(player);
    module.addArmorDamageStats(tool, new ModifierEntry(ModifierIds.recurrence, 2), EquipmentContext.withTool(player, tool, EquipmentSlot.CHEST), EquipmentSlot.CHEST, source, stats);
    FormulaRecurrenceModule.finishDamageEvent(stats);

    assertThat(stats.preReduction()).isEqualTo(2);
    assertThat(entityData.getFloat(KEY)).isEqualTo(1);
    assertThat(tool.getPersistentData().getFloat(KEY)).isZero();
    assertThat(armorInputs[0]).containsExactly(2.0, 0.0, 2.0, 8.0);
  }

  @Test
  void recurrenceAggregatesArmorLevelsBeforeApplyingEntityPersistentData() {
    double[][] damageInputs = new double[1][];
    FormulaManager.applySync(Map.of(
      ARMOR_STAT, formula(values -> values[0]),
      DAMAGE, formula(values -> {
        damageInputs[0] = values;
        return values[0] + values[1] + values[2] + values[3];
      }),
      TICK, formula(values -> 0)
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    ModDataNBT entityData = new ModDataNBT();
    entityData.putFloat(KEY, 2);
    FormulaRecurrenceModule.setPersistentDataGetter(ignored -> entityData);
    Player player = player(entityData);
    DamageSource source = recurrenceSource();
    TestToolStack secondTool = new TestToolStack();
    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0, 10);
    FormulaRecurrenceModule module = FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY);

    FormulaRecurrenceModule.beginDamageEvent(player);
    module.addArmorDamageStats(tool, new ModifierEntry(ModifierIds.recurrence, 2), EquipmentContext.withTool(player, tool, EquipmentSlot.CHEST), EquipmentSlot.CHEST, source, stats);
    module.addArmorDamageStats(secondTool, new ModifierEntry(ModifierIds.recurrence, 3), EquipmentContext.withTool(player, secondTool, EquipmentSlot.HEAD), EquipmentSlot.HEAD, source, stats);
    FormulaRecurrenceModule.finishDamageEvent(stats);

    assertThat(stats.preReduction()).isEqualTo(2);
    assertThat(entityData.getFloat(KEY)).isEqualTo(18);
    assertThat(damageInputs[0]).containsExactly(2.0, 3.0, 5.0, 8.0);
  }

  @Test
  void armorTickDecaysStoredReductionOnceForAllArmorLevels() {
    FormulaManager.applySync(Map.of(
      ARMOR_STAT, formula(values -> 0),
      DAMAGE, formula(values -> 0),
      TICK, formula(values -> 4.0)
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    ModDataNBT entityData = new ModDataNBT();
    entityData.putFloat(KEY, 5);
    entityData.putInt(KEY.withSuffix("_tick_offset"), 20);
    FormulaRecurrenceModule.setPersistentDataGetter(ignored -> entityData);
    Player holder = player(entityData);
    holder.tickCount = 20;

    FormulaRecurrenceModule module = FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY);
    module.onArmorTick(tool, new ModifierEntry(ModifierIds.recurrence, 2), EquipmentSlot.CHEST, holder);
    module.onArmorTick(new TestToolStack(), new ModifierEntry(ModifierIds.recurrence, 3), EquipmentSlot.HEAD, holder);
    FormulaRecurrenceModule.flushArmorTicks(holder);

    assertThat(entityData.getFloat(KEY)).isEqualTo(4);
  }

  @Test
  void damageToPersistentMarksIncrementalCacheForPlayerTooltipConsumption() {
    FormulaManager.applySync(Map.of(
      ARMOR_STAT, formula(values -> values[0]),
      DAMAGE, formula(values -> 6.0),
      TICK, formula(values -> values[0])
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    ModDataNBT entityData = new ModDataNBT();
    entityData.putFloat(KEY, 2);
    FormulaRecurrenceModule.setPersistentDataGetter(ignored -> entityData);
    DamageSource source = recurrenceSource();
    Player player = player(entityData);

    PlayerPersistentDataCache.setDataGetter(ignored -> entityData);
    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0, 10);
    FormulaRecurrenceModule module = FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY);
    FormulaRecurrenceModule.beginDamageEvent(player);
    module.addArmorDamageStats(tool, new ModifierEntry(ModifierIds.recurrence, 2), EquipmentContext.withTool(player, tool, EquipmentSlot.CHEST), EquipmentSlot.CHEST, source, stats);
    FormulaRecurrenceModule.finishDamageEvent(stats);

    assertThat(entityData.getFloat(KEY)).isEqualTo(6);
    entityData.putFloat(KEY, 6);
    entityData.putLong(ResourceLocation.tryParse(KEY + "_expiry"), 77L);
    PlayerPersistentDataCache.sync(TEST_PLAYER, entityData, packet -> PlayerPersistentDataCache.put(TEST_PLAYER, KEY.toString(), 6, 77L));
    assertThat(PlayerPersistentDataCache.get(TEST_PLAYER, KEY.toString(), 0)).isEqualTo(6);
  }

  @Test
  void acceptsTcaeEligibleDamageEvenWhenItBypassesArmor() {
    FormulaManager.applySync(Map.of(
      ARMOR_STAT, formula(values -> values[0]),
      DAMAGE, formula(values -> values[0]),
      TICK, formula(values -> 0)
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    ModDataNBT entityData = new ModDataNBT();
    entityData.putFloat(KEY, 2);
    FormulaRecurrenceModule.setPersistentDataGetter(ignored -> entityData);
    Player player = player(entityData);
    DamageSource source = mock(DamageSource.class);
    when(source.is(DamageTypeTags.BYPASSES_ARMOR)).thenReturn(true);
    when(source.is(TinkerTags.DamageTypes.MAGIC_PROTECTION)).thenReturn(true);

    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0, 10);
    FormulaRecurrenceModule module = FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY);
    FormulaRecurrenceModule.beginDamageEvent(player);
    module.addArmorDamageStats(tool, new ModifierEntry(ModifierIds.recurrence, 1), EquipmentContext.withTool(player, tool, EquipmentSlot.CHEST), EquipmentSlot.CHEST, source, stats);
    FormulaRecurrenceModule.finishDamageEvent(stats);

    assertThat(stats.preReduction()).isEqualTo(2);
    assertThat(entityData.getFloat(KEY)).isEqualTo(2);
  }

  @Test
  void ignoresDamageOutsideTcaeAllPredicate() {
    FormulaManager.applySync(Map.of(
      ARMOR_STAT, formula(values -> values[0]),
      DAMAGE, formula(values -> values[0]),
      TICK, formula(values -> 0)
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    ModDataNBT entityData = new ModDataNBT();
    entityData.putFloat(KEY, 2);
    FormulaRecurrenceModule.setPersistentDataGetter(ignored -> entityData);
    Player player = player(entityData);

    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0, 10);
    FormulaRecurrenceModule module = FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY);
    FormulaRecurrenceModule.beginDamageEvent(player);
    module.addArmorDamageStats(tool, new ModifierEntry(ModifierIds.recurrence, 1), EquipmentContext.withTool(player, tool, EquipmentSlot.CHEST), EquipmentSlot.CHEST, mock(DamageSource.class), stats);
    FormulaRecurrenceModule.finishDamageEvent(stats);

    assertThat(stats.preReduction()).isZero();
    assertThat(entityData.getFloat(KEY)).isEqualTo(2);
  }

  @Test
  void bypassMechanismStoresDamageWithoutApplyingPersistentReduction() {
    FormulaManager.applySync(Map.of(
      ARMOR_STAT, formula(values -> 0),
      DAMAGE, formula(values -> values[0] + values[3]),
      TICK, formula(values -> 0)
    ), Map.of(ARMOR_STAT, "{}", DAMAGE, "{}", TICK, "{}"));
    ModDataNBT entityData = new ModDataNBT();
    entityData.putFloat(KEY, 2);
    FormulaRecurrenceModule.setPersistentDataGetter(ignored -> entityData);
    Player player = player(entityData);
    DamageSource source = recurrenceSource();

    ArmorDamageStats stats = new ArmorDamageStats(0, 0, 0, 0, 8);
    FormulaRecurrenceModule module = FormulaRecurrenceModule.recurrence(ARMOR_STAT, DAMAGE, TICK, KEY);
    FormulaRecurrenceModule.beginDamageEvent(player);
    module.onDamageToPersistent(tool, new ModifierEntry(ModifierIds.recurrence, 2),
      EquipmentContext.withTool(player, tool, EquipmentSlot.CHEST), EquipmentSlot.CHEST, source, stats);
    FormulaRecurrenceModule.finishBypassDamageEvent(stats);

    assertThat(stats.preReduction()).isZero();
    assertThat(entityData.getFloat(KEY)).isEqualTo(10);
  }

  private static DamageSource recurrenceSource() {
    DamageSource source = mock(DamageSource.class);
    when(source.is(TinkerTags.DamageTypes.MELEE_PROTECTION)).thenReturn(true);
    when(source.isIndirect()).thenReturn(false);
    return source;
  }

  private static Player player(ModDataNBT data) {
    Player player = mock(Player.class);
    Level level = mock(Level.class);
    when(player.getUUID()).thenReturn(TEST_PLAYER);
    when(player.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);
    return player;
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
