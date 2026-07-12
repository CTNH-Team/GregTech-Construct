package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
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
import slimeknights.tconstruct.library.modifiers.hook.armor.ShareDamageModifierHook.ShareDamageContext;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.nbt.DummyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.logic.GuardingCache;
import slimeknights.tconstruct.tools.logic.GuardingRuntimeHooks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FormulaGuardingModuleTest extends BaseMcTest {
  private static final ResourceLocation DISTANCE = TConstruct.getResource("test/guarding/distance");
  private static final ResourceLocation SHARE = TConstruct.getResource("test/guarding/share");
  private static final ResourceLocation PROTECTION = TConstruct.getResource("test/guarding/protection");
  private static final ModifierId TEST_MODIFIER_ID = new ModifierId("test", "guarding_bar");
  private final Map<ResourceLocation,IFormula> previousFormulas = FormulaManager.getAll();
  private final Map<ResourceLocation,String> previousRawJson = FormulaManager.getAllRawJson();

  @AfterEach
  void restoreFormulaManager() {
    FormulaManager.applySync(previousFormulas, previousRawJson);
    GuardingCache.clearForTests();
  }

  @Test
  void shareAndProtectionFormulasUseDefaultInputsWithoutParameterProvider() {
    double[][] shareInputs = new double[1][];
    double[][] protectionInputs = new double[1][];
    FormulaManager.applySync(Map.of(
      DISTANCE, formula(values -> 1.0),
      SHARE, formula(values -> {
        shareInputs[0] = values;
        return 0.5;
      }),
      PROTECTION, formula(values -> {
        protectionInputs[0] = values;
        return 0.25;
      })
    ), Map.of(DISTANCE, "{}", SHARE, "{}", PROTECTION, "{}"));

    TestToolStack tool = new TestToolStack();

    LivingEntity guardian = mock(LivingEntity.class);
    LivingEntity protectedEntity = mock(LivingEntity.class);
    DamageSource source = mock(DamageSource.class);
    when(guardian.getHealth()).thenReturn(20f);
    when(guardian.distanceTo(protectedEntity)).thenReturn(2.0f);
    RecordingShareContext context = new RecordingShareContext();
    boolean claimed = FormulaGuardingModule.guarding(DISTANCE, SHARE, PROTECTION)
      .collectShareDamage(tool, guardingEntry(2), guardian, EquipmentSlot.CHEST, protectedEntity, source, context);

    assertThat(claimed).isTrue();
    assertThat(context.shareRatio).isEqualTo(0.5f);
    assertThat(context.extraProtection).isEqualTo(0.25f);
    assertThat(context.distanceFactor).isEqualTo(1f);
    assertThat(shareInputs[0]).containsExactly(0.0, 2.0, 1.0, 0.0);
    assertThat(protectionInputs[0]).containsExactly(0.0, 2.0, 1.0, 0.0);
  }

  @Test
  void guardingWithoutPlatingDoesNotFallbackToDurability() {
    double[][] shareInputs = new double[1][];
    FormulaManager.applySync(Map.of(
      DISTANCE, formula(values -> 1.0),
      SHARE, formula(values -> {
        shareInputs[0] = values;
        return 0.5;
      }),
      PROTECTION, formula(values -> 0.0)
    ), Map.of(DISTANCE, "{}", SHARE, "{}", PROTECTION, "{}"));

    TestToolStack tool = new TestToolStack();
    LivingEntity guardian = mock(LivingEntity.class);
    LivingEntity protectedEntity = mock(LivingEntity.class);
    DamageSource source = mock(DamageSource.class);
    when(guardian.getHealth()).thenReturn(20f);
    when(guardian.distanceTo(protectedEntity)).thenReturn(2.0f);

    RecordingShareContext context = new RecordingShareContext();
    boolean claimed = FormulaGuardingModule.guarding(DISTANCE, SHARE, PROTECTION)
      .collectShareDamage(tool, guardingEntry(2), guardian, EquipmentSlot.CHEST, protectedEntity, source, context);

    assertThat(claimed).isTrue();
    assertThat(context.called).isTrue();
    assertThat(shareInputs[0]).containsExactly(0.0, 2.0, 1.0, 0.0);
  }

  @Test
  void sourceContainsAdvancementTriggersForSharedFateAndSacrifice() throws Exception {
    String source = Files.readString(Path.of("src/main/java/slimeknights/tconstruct/tools/logic/ToolEvents.java"));
    assertThat(source).contains("combat/shared_fate");
    assertThat(source).contains("combat/sacrifice");
  }

  @Test
  void guardedPlayerWithSameModifierIsRejectedAsProtectedEntity() {
    FormulaManager.applySync(Map.of(
      DISTANCE, formula(values -> 1.0),
      SHARE, formula(values -> 0.5),
      PROTECTION, formula(values -> 0.0)
    ), Map.of(DISTANCE, "{}", SHARE, "{}", PROTECTION, "{}"));

    TestToolStack tool = new TestToolStack();

    Player protectedPlayer = mock(Player.class);
    LivingEntity guardian = mock(LivingEntity.class);
    DamageSource source = mock(DamageSource.class);
    when(guardian.getHealth()).thenReturn(20f);
    when(guardian.distanceTo(protectedPlayer)).thenReturn(2.0f);

    GuardingCache.addHook(java.util.UUID.randomUUID(), TEST_MODIFIER_ID);
    when(protectedPlayer.getUUID()).thenReturn(java.util.UUID.randomUUID());
    GuardingCache.addHook(protectedPlayer.getUUID(), TEST_MODIFIER_ID);

    RecordingShareContext context = new RecordingShareContext();
    boolean claimed = FormulaGuardingModule.guarding(DISTANCE, SHARE, PROTECTION)
      .collectShareDamage(tool, new ModifierEntry(TEST_MODIFIER_ID, 2), guardian, EquipmentSlot.CHEST, protectedPlayer, source, context);

    assertThat(claimed).isFalse();
    assertThat(context.called).isFalse();
  }

  @Test
  void equipmentChangeOnlyTracksRealItemSwap() {
    TestToolStack tool = new TestToolStack();
    ModifierEntry entry = new ModifierEntry(TEST_MODIFIER_ID, 1);
    Player player = mock(Player.class);
    net.minecraft.world.level.Level level = mock(net.minecraft.world.level.Level.class);
    when(player.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);

    EquipmentChangeContext sameItemContext = mock(EquipmentChangeContext.class);
    when(sameItemContext.getEntity()).thenReturn(player);
    when(sameItemContext.getLevel()).thenReturn(level);
    when(sameItemContext.getReplacementTool()).thenReturn(tool);
    FormulaGuardingModule.guarding(DISTANCE, SHARE, PROTECTION).onEquip(tool, entry, sameItemContext);
    assertThat(GuardingCache.hasHook(player.getUUID(), TEST_MODIFIER_ID)).isFalse();

    EquipmentChangeContext changedContext = mock(EquipmentChangeContext.class);
    when(changedContext.getEntity()).thenReturn(player);
    when(changedContext.getLevel()).thenReturn(level);
    when(changedContext.getReplacementTool()).thenReturn(null);
    FormulaGuardingModule.guarding(DISTANCE, SHARE, PROTECTION).onEquip(tool, entry, changedContext);
    assertThat(GuardingCache.hasHook(player.getUUID(), TEST_MODIFIER_ID)).isTrue();
  }

  @Test
  void loginRegistrationAndUnequipTearDownDoNotLeaveStaleGuardingState() {
    TestToolStack tool = new TestToolStack();
    Player player = mock(Player.class);
    net.minecraft.world.level.Level level = mock(net.minecraft.world.level.Level.class);
    EntityType<?> type = mock(EntityType.class);
    when(player.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);
    when(player.getType()).thenReturn((EntityType) type);
    when(type.is(any())).thenReturn(false);
    when(player.getItemBySlot(any())).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.LEGS)).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.FEET)).thenReturn(ItemStack.EMPTY);

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.is(org.mockito.ArgumentMatchers.eq(slimeknights.tconstruct.common.TinkerTags.Items.MODIFIABLE))).thenReturn(true);
    when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);

    TestGuardingModifier modifier = new TestGuardingModifier();
    bindBar(modifier, TEST_MODIFIER_ID);
    ToolStack loginTool = mock(ToolStack.class);
    ModifierNBT modifiers = ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1);
    when(loginTool.getModifierList()).thenReturn(modifiers.getModifiers());
    when(loginTool.isBroken()).thenReturn(false);
    try (org.mockito.MockedStatic<slimeknights.tconstruct.library.tools.nbt.ToolStack> toolStacks = org.mockito.Mockito.mockStatic(slimeknights.tconstruct.library.tools.nbt.ToolStack.class)) {
      toolStacks.when(() -> slimeknights.tconstruct.library.tools.nbt.ToolStack.from(chest)).thenReturn(loginTool);
      GuardingRuntimeHooks.onPlayerLoggedIn(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(player));
    }

    assertThat(GuardingCache.hasHook(player.getUUID(), TEST_MODIFIER_ID)).isTrue();

    EquipmentChangeContext changedContext = mock(EquipmentChangeContext.class);
    when(changedContext.getEntity()).thenReturn(player);
    when(changedContext.getLevel()).thenReturn(level);
    when(changedContext.getReplacementTool()).thenReturn(null);
    FormulaGuardingModule.guarding(DISTANCE, SHARE, PROTECTION).onUnequip(tool, new ModifierEntry(TEST_MODIFIER_ID, 1), changedContext);

    assertThat(GuardingCache.hasHook(player.getUUID(), TEST_MODIFIER_ID)).isFalse();
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

  private static class RecordingShareContext implements ShareDamageContext {
    private boolean called;
    private float shareRatio;
    private float extraProtection;
    private float healthGround;
    private float distanceFactor;

    @Override
    public void add(float shareRatio, float extraProtection, float healthGround, float distanceFactor) {
      this.called = true;
      this.shareRatio = shareRatio;
      this.extraProtection = extraProtection;
      this.healthGround = healthGround;
      this.distanceFactor = distanceFactor;
    }

    @Override
    public void add(float shareRatio, float extraProtection, float healthGround, float distanceFactor, int color) {
      add(shareRatio, extraProtection, healthGround, distanceFactor);
    }
  }

  private static class TestGuardingModifier extends Modifier {
    private TestGuardingModifier() {
      super(ModuleHookMap.builder().addModule(FormulaGuardingModule.guarding(DISTANCE, SHARE, PROTECTION)).build());
    }
  }

  private static ModifierEntry guardingEntry(int level) {
    bindBar(new TestGuardingModifier(), TEST_MODIFIER_ID);
    return new ModifierEntry(TEST_MODIFIER_ID, level);
  }

  private static void bindBar(Modifier modifier, ModifierId id) {
    try {
      Method setId = Modifier.class.getDeclaredMethod("setId", ModifierId.class);
      setId.setAccessible(true);
      setId.invoke(modifier, id);

      Field staticModifiers = ModifierManager.class.getDeclaredField("staticModifiers");
      staticModifiers.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<ModifierId,Modifier> modifiers = (Map<ModifierId,Modifier>) staticModifiers.get(ModifierManager.INSTANCE);
      modifiers.put(id, modifier);

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
