package slimeknights.tconstruct.tools.logic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
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
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.AroundEntityTickModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.SelfTickModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaAreaEffectModule;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.common.TinkerTags;
import net.minecraft.nbt.CompoundTag;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AroundEntityTickHandlerTest extends BaseMcTest {
  private static final ModifierId TEST_MODIFIER_ID = new ModifierId("test", "totem_around_tick");
  private static final ResourceLocation RANGE = TConstruct.getResource("test/totem/range");
  private static final ResourceLocation ACCUM_DURATION = TConstruct.getResource("test/totem/accum_duration");
  private static final ResourceLocation ACCUM_LEVEL = TConstruct.getResource("test/totem/accum_level");
  private static final ResourceLocation FINAL_DURATION = TConstruct.getResource("test/totem/final_duration");
  private static final ResourceLocation FINAL_LEVEL = TConstruct.getResource("test/totem/final_level");

  @AfterEach
  void clearFormulaManager() {
    FormulaManager.applySync(Map.of(), Map.of());
    PlayerPersistentDataCache.resetDataGetter();
  }

  @Test
  void startPhaseRunsAroundEntityHookForNearbyLivingEntities() throws Exception {
    PlayerPersistentDataCache.setDataGetter(player -> new ModDataNBT());
    AtomicInteger startCalls = new AtomicInteger();
    bind(new TestAroundModifier(new AroundEntityTickModifierHook() {
      @Override
      public void onAroundEntityTickStart(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, Player player, LivingEntity target, double distance) {
        startCalls.incrementAndGet();
      }
    }));

    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.isBroken()).thenReturn(false);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    when(tool.getItem()).thenReturn(Items.IRON_CHESTPLATE);

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.getItem()).thenReturn(Items.IRON_CHESTPLATE);
    when(chest.is(org.mockito.ArgumentMatchers.eq(TinkerTags.Items.MODIFIABLE))).thenReturn(true);

    Player player = mock(Player.class);
    net.minecraft.world.level.Level level = mock(net.minecraft.world.level.Level.class);
    LivingEntity target = mock(LivingEntity.class);
    when(player.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);
    when(player.isSpectator()).thenReturn(false);
    when(player.isAlive()).thenReturn(true);
    when(player.getBoundingBox()).thenReturn(new AABB(0, 0, 0, 1, 2, 1));
    when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(player.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.LEGS)).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.FEET)).thenReturn(ItemStack.EMPTY);
    when(level.getEntitiesOfClass(org.mockito.Mockito.eq(LivingEntity.class), any(AABB.class), any())).thenReturn(List.of(target));
    when(target.isAlive()).thenReturn(true);
    when(target.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(target.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ItemStack.EMPTY);
    when(target.getItemBySlot(EquipmentSlot.LEGS)).thenReturn(ItemStack.EMPTY);
    when(target.getItemBySlot(EquipmentSlot.FEET)).thenReturn(ItemStack.EMPTY);
    when(player.distanceTo(target)).thenReturn(3.0f);

    try (org.mockito.MockedStatic<ToolStack> toolStacks = org.mockito.Mockito.mockStatic(ToolStack.class)) {
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);
      AroundEntityTickHandler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player));
    }

    assertThat(startCalls.get()).isEqualTo(1);
  }

  @Test
  void startPhaseAlsoRunsSelfAndArmorTickHooks() throws Exception {
    PlayerPersistentDataCache.setDataGetter(player -> new ModDataNBT());
    AtomicInteger selfCalls = new AtomicInteger();
    AtomicInteger armorCalls = new AtomicInteger();
    bind(new TestCompositeModifier(
      new SelfTickModifierHook() {
        @Override
        public void onSelfTick(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, LivingEntity entity) {
          selfCalls.incrementAndGet();
        }
      },
      new ArmorTickModifierHook() {
        @Override
        public void onArmorTick(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, LivingEntity entity) {
          armorCalls.incrementAndGet();
        }
      },
      new AroundEntityTickModifierHook() {}
    ));

    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.isBroken()).thenReturn(false);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    when(tool.getItem()).thenReturn(Items.IRON_CHESTPLATE);

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.getItem()).thenReturn(Items.IRON_CHESTPLATE);
    when(chest.is(org.mockito.ArgumentMatchers.eq(TinkerTags.Items.MODIFIABLE))).thenReturn(true);

    Player player = mock(Player.class);
    net.minecraft.world.level.Level level = mock(net.minecraft.world.level.Level.class);
    LivingEntity target = mock(LivingEntity.class);
    when(player.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);
    when(player.isSpectator()).thenReturn(false);
    when(player.isAlive()).thenReturn(true);
    when(player.getBoundingBox()).thenReturn(new AABB(0, 0, 0, 1, 2, 1));
    when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(player.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.LEGS)).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.FEET)).thenReturn(ItemStack.EMPTY);
    when(level.getEntitiesOfClass(org.mockito.Mockito.eq(LivingEntity.class), any(AABB.class), any())).thenReturn(List.of(target));
    when(target.isAlive()).thenReturn(true);
    when(target.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(target.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ItemStack.EMPTY);
    when(target.getItemBySlot(EquipmentSlot.LEGS)).thenReturn(ItemStack.EMPTY);
    when(target.getItemBySlot(EquipmentSlot.FEET)).thenReturn(ItemStack.EMPTY);
    when(player.distanceTo(target)).thenReturn(3.0f);

    try (org.mockito.MockedStatic<ToolStack> toolStacks = org.mockito.Mockito.mockStatic(ToolStack.class)) {
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);
      AroundEntityTickHandler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player));
    }

    assertThat(selfCalls.get()).isEqualTo(2);
    assertThat(armorCalls.get()).isEqualTo(2);
  }

  @Test
  void startPhaseRunsSelfAndArmorTickHooksWithoutNearbyTargets() throws Exception {
    PlayerPersistentDataCache.setDataGetter(player -> new ModDataNBT());
    AtomicInteger selfCalls = new AtomicInteger();
    AtomicInteger armorCalls = new AtomicInteger();
    bind(new TestCompositeModifier(
      new SelfTickModifierHook() {
        @Override
        public void onSelfTick(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, LivingEntity entity) {
          selfCalls.incrementAndGet();
        }
      },
      new ArmorTickModifierHook() {
        @Override
        public void onArmorTick(slimeknights.tconstruct.library.tools.nbt.IToolStackView tool, ModifierEntry modifier, EquipmentSlot slotType, LivingEntity entity) {
          armorCalls.incrementAndGet();
        }
      },
      new AroundEntityTickModifierHook() {}
    ));

    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.isBroken()).thenReturn(false);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    when(tool.getItem()).thenReturn(Items.IRON_CHESTPLATE);

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.getItem()).thenReturn(Items.IRON_CHESTPLATE);
    when(chest.is(org.mockito.ArgumentMatchers.eq(TinkerTags.Items.MODIFIABLE))).thenReturn(true);

    Player player = mock(Player.class);
    net.minecraft.world.level.Level level = mock(net.minecraft.world.level.Level.class);
    when(player.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);
    when(player.isSpectator()).thenReturn(false);
    when(player.isAlive()).thenReturn(true);
    when(player.getBoundingBox()).thenReturn(new AABB(0, 0, 0, 1, 2, 1));
    when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(player.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.LEGS)).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.FEET)).thenReturn(ItemStack.EMPTY);
    when(level.getEntitiesOfClass(org.mockito.Mockito.eq(LivingEntity.class), any(AABB.class), any())).thenReturn(List.of());

    try (org.mockito.MockedStatic<ToolStack> toolStacks = org.mockito.Mockito.mockStatic(ToolStack.class)) {
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);
      AroundEntityTickHandler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player));
    }

    assertThat(selfCalls.get()).isEqualTo(1);
    assertThat(armorCalls.get()).isEqualTo(1);
  }

  @Test
  void endPhaseFlushesAggregatedTamedEffect() throws Exception {
    PlayerPersistentDataCache.setDataGetter(player -> new ModDataNBT());
    FormulaManager.applySync(Map.of(
      RANGE, formula(values -> 8),
      ACCUM_DURATION, formula(values -> values[0] + 40),
      ACCUM_LEVEL, formula(values -> Math.max(values[0], 1)),
      FINAL_DURATION, formula(values -> values[0] + values[1]),
      FINAL_LEVEL, formula(values -> Math.max(values[0], values[1]))
    ), Map.of(RANGE, "{}", ACCUM_DURATION, "{}", ACCUM_LEVEL, "{}", FINAL_DURATION, "{}", FINAL_LEVEL, "{}"));

    bind(new TestAreaEffectModifier(FormulaAreaEffectModule.tamed(
      net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.GLOWING),
      RANGE,
      ACCUM_DURATION,
      ACCUM_LEVEL,
      FINAL_DURATION,
      FINAL_LEVEL
    )));

    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.isBroken()).thenReturn(false);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    when(tool.getItem()).thenReturn(Items.IRON_CHESTPLATE);

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.getItem()).thenReturn(Items.IRON_CHESTPLATE);
    when(chest.is(org.mockito.ArgumentMatchers.eq(TinkerTags.Items.MODIFIABLE))).thenReturn(true);

    Player player = mock(Player.class);
    net.minecraft.world.level.Level level = mock(net.minecraft.world.level.Level.class);
    TamableAnimal target = mock(TamableAnimal.class);
    CompoundTag persistent = new CompoundTag();
    when(player.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);
    when(player.isSpectator()).thenReturn(false);
    when(player.isAlive()).thenReturn(true);
    when(player.getUUID()).thenReturn(java.util.UUID.randomUUID());
    when(player.getBoundingBox()).thenReturn(new AABB(0, 0, 0, 1, 2, 1));
    when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(player.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.LEGS)).thenReturn(ItemStack.EMPTY);
    when(player.getItemBySlot(EquipmentSlot.FEET)).thenReturn(ItemStack.EMPTY);
    when(level.getEntitiesOfClass(org.mockito.Mockito.eq(LivingEntity.class), any(AABB.class), any())).thenReturn(List.of(target));
    when(target.isAlive()).thenReturn(true);
    when(target.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(ItemStack.EMPTY);
    when(target.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ItemStack.EMPTY);
    when(target.getItemBySlot(EquipmentSlot.LEGS)).thenReturn(ItemStack.EMPTY);
    when(target.getItemBySlot(EquipmentSlot.FEET)).thenReturn(ItemStack.EMPTY);
    when(target.getOwner()).thenReturn(player);
    when(target.getPersistentData()).thenReturn(persistent);
    when(player.distanceTo(target)).thenReturn(3.0f);

    try (org.mockito.MockedStatic<ToolStack> toolStacks = org.mockito.Mockito.mockStatic(ToolStack.class)) {
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);
      AroundEntityTickHandler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, player));
      AroundEntityTickHandler.onPlayerTick(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, player));
    }

    org.mockito.Mockito.verify(target).addEffect(org.mockito.ArgumentMatchers.argThat((MobEffectInstance effect) ->
      effect.getEffect() == MobEffects.GLOWING && effect.getDuration() == 40 && effect.getAmplifier() == 1));
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

  private static class TestAroundModifier extends Modifier {
    private TestAroundModifier(AroundEntityTickModifierHook hook) {
      super(ModuleHookMap.builder().addHook(hook, ModifierHooks.AROUND_ENTITY_TICK).build());
    }
  }

  private static class TestAreaEffectModifier extends Modifier {
    private TestAreaEffectModifier(FormulaAreaEffectModule module) {
      super(ModuleHookMap.builder().addModule(module).build());
    }
  }

  private static class TestCompositeModifier extends Modifier {
    private TestCompositeModifier(SelfTickModifierHook selfTick, ArmorTickModifierHook armorTick, AroundEntityTickModifierHook around) {
      super(ModuleHookMap.builder()
        .addHook(selfTick, ModifierHooks.SELF_TICK)
        .addHook(armorTick, ModifierHooks.ARMOR_TICK)
        .addHook(around, ModifierHooks.AROUND_ENTITY_TICK)
        .build());
    }
  }

  private static IFormula formula(java.util.function.Function<double[], Number> body) {
    return new IFormula() {
      @Override
      public double accept(double... values) {
        return body.apply(values).doubleValue();
      }

      @Override
      public int expectInputSize() {
        return 4;
      }
    };
  }
}
