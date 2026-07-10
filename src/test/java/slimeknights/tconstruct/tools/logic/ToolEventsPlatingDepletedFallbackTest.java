package slimeknights.tconstruct.tools.logic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.RegistryObject;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.hook.armor.ShareDamageModifierHook.ShareDamageContext;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStat;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaArmorStatModule;
import slimeknights.tconstruct.library.modifiers.modules.behavior.ToolDamageCapacityModule;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageHandler;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.shared.TinkerAttributes;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ToolEventsPlatingDepletedFallbackTest extends BaseMcTest {
  private static final ModifierId TEST_MODIFIER_ID = new ModifierId("test", "plating_depleted_fallback");
  private static final ModifierId TEST_CAPACITY_ID = new ModifierId("test", "damage_limit_capacity");
  private static final ResourceLocation PRE_DAMAGE = TConstruct.getResource("plating/pre_damage");
  private static final ResourceLocation TOOL_DAMAGE = TConstruct.getResource("plating/tool_damage");
  private static final ResourceLocation DAMAGE_CAPACITY_PRE = TConstruct.getResource("plating/damage_capacity_pre");
  private static final ResourceLocation DAMAGE_CAPACITY = TConstruct.getResource("plating/damage_capacity");
  private static final ResourceLocation FROZEN_PRE_REDUCTION = TConstruct.getResource("test/frozen_pre_reduction");
  private static final ResourceLocation FROZEN_POST_REDUCTION = TConstruct.getResource("test/frozen_post_reduction");
  private static final ResourceLocation FROZEN_PROTECTION = TConstruct.getResource("test/frozen_protection");
  private static final ResourceLocation DAMAGE_LIMIT_CONDITION = TConstruct.getResource("test/damage_limit/condition");
  private static final ResourceLocation DAMAGE_LIMIT_CAP = TConstruct.getResource("test/damage_limit/cap");
  private static final ResourceLocation DAMAGE_LIMIT_RATIO = TConstruct.getResource("test/damage_limit/ratio");
  private static final ResourceLocation DAMAGE_LIMIT_ARMOR = TConstruct.getResource("test/damage_limit/armor");
  private static final ResourceLocation DAMAGE_LIMIT_CAPACITY = TConstruct.getResource("test/damage_limit/capacity");
  private final Map<ResourceLocation,IFormula> previousFormulas = FormulaManager.getAll();
  private final Map<ResourceLocation,String> previousRawJson = FormulaManager.getAllRawJson();

  @AfterEach
  void restoreFormulaManager() {
    FormulaManager.applySync(previousFormulas, previousRawJson);
    ToolDamageHandler.clearPendingDamageForTests();
    PlayerPersistentDataCache.resetDataGetter();
  }

  @Test
  void secondHitAfterPlatingDepletesDamagesUnderlyingArmor() throws Exception {
    FormulaManager.applySync(Map.of(
      PRE_DAMAGE, formula(values -> values[3] > 0 ? values[1] - 1 : values[1]),
      TOOL_DAMAGE, formula(values -> Math.max(0, values[1] - values[3])),
      DAMAGE_CAPACITY_PRE, formula(values -> values[3] > 0 ? 1 : 0),
      DAMAGE_CAPACITY, formula(values -> Math.min(values[1], values[3]))
    ), Map.of(
      PRE_DAMAGE, "{}",
      TOOL_DAMAGE, "{}",
      DAMAGE_CAPACITY_PRE, "{}",
      DAMAGE_CAPACITY, "{}"
    ));

    bindAttribute(TinkerAttributes.ARMOR_STRENGTH, Attributes.ARMOR);
    bindAttribute(TinkerAttributes.PRE_REDUCTION, Attributes.ARMOR);
    bindAttribute(TinkerAttributes.ARMOR_PROTECTION, Attributes.ARMOR);

    ToolStack tool = mock(ToolStack.class);
    TestCapacityBar capacityBar = new TestCapacityBar(10, 1);
    bind(new TestPlatingModifier(capacityBar));

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.is(TinkerTags.Items.MODIFIABLE)).thenReturn(true);

    Player player = mock(Player.class);
    Level level = mock(Level.class);
    DamageSource source = mock(DamageSource.class);
    @SuppressWarnings("unchecked")
    EntityType<Player> type = (EntityType<Player>) mock(EntityType.class);

    when(player.level()).thenReturn(level);
    when(level.getEntitiesOfClass(Mockito.eq(net.minecraft.world.entity.LivingEntity.class), any(AABB.class), any())).thenReturn(java.util.List.of());
    when(player.getArmorValue()).thenReturn(10);
    when(player.getAttributeValue(any(Attribute.class))).thenReturn(0d);
    when(player.getBoundingBox()).thenReturn(new AABB(0, 0, 0, 1, 2, 1));
    when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(player.getItemBySlot(Mockito.argThat(slot -> slot != EquipmentSlot.CHEST))).thenReturn(ItemStack.EMPTY);
    Mockito.doReturn(type).when(player).getType();
    when(type.is(any())).thenReturn(false);
    when(source.is(any(TagKey.class))).thenReturn(false);
    when(source.is(any(ResourceKey.class))).thenReturn(false);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    AtomicInteger damage = new AtomicInteger();
    when(tool.getDamage()).thenAnswer(invocation -> damage.get());
    when(tool.getCurrentDurability()).thenAnswer(invocation -> 100 - damage.get());
    Mockito.doAnswer(invocation -> {
      damage.set(invocation.getArgument(0));
      return null;
    }).when(tool).setDamage(Mockito.anyInt());
    when(tool.getModifiers()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1));
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.getModifierLevel(TEST_MODIFIER_ID)).thenReturn(1);
    when(tool.isBroken()).thenReturn(false);
    when(tool.isUnbreakable()).thenReturn(false);
    when(tool.hasTag(any())).thenAnswer(invocation -> invocation.getArgument(0) == slimeknights.tconstruct.common.TinkerTags.Items.DURABILITY);
    when(tool.getItem()).thenReturn(net.minecraft.world.item.Items.AIR);
    PlayerPersistentDataCache.setDataGetter(ignored -> new ModDataNBT());

    try (MockedStatic<EnchantmentHelper> enchantments = Mockito.mockStatic(EnchantmentHelper.class);
         MockedStatic<ToolStack> toolStacks = Mockito.mockStatic(ToolStack.class)) {
      enchantments.when(() -> EnchantmentHelper.getDamageProtection(player.getArmorSlots(), source)).thenReturn(0);
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);

      net.minecraftforge.event.entity.living.LivingHurtEvent firstEvent = new net.minecraftforge.event.entity.living.LivingHurtEvent(player, source, 8f);
      ToolEvents.livingHurt(firstEvent);
      assertThat(firstEvent.getAmount()).isEqualTo(6.08f);
      ToolDamageHandler.flushPendingDamage();
      assertThat(capacityBar.amount).isZero();
      assertThat(damage.get()).isZero();

      net.minecraftforge.event.entity.living.LivingHurtEvent secondEvent = new net.minecraftforge.event.entity.living.LivingHurtEvent(player, source, 8f);
      ToolEvents.livingHurt(secondEvent);
      assertThat(secondEvent.getAmount()).isEqualTo(6.08f);
      ToolDamageHandler.flushPendingDamage();
      assertThat(damage.get()).isEqualTo(1);
    }
  }

  @Test
  void guardingDoesNotScanNearbyPlayersForNonPlayerProtectedEntities() throws Exception {
    LivingEntity protectedEntity = mock(LivingEntity.class);
    Level level = mock(Level.class);
    when(protectedEntity.level()).thenReturn(level);
    when(protectedEntity.getPassengers()).thenReturn(java.util.List.of());

    Method shareDamage = ToolEvents.class.getDeclaredMethod("shareDamageWithNearbyGuardians", LivingEntity.class, DamageSource.class, float.class);
    shareDamage.setAccessible(true);
    float shared = (float)shareDamage.invoke(null, protectedEntity, mock(DamageSource.class), 4f);

    assertThat(shared).isZero();
    verify(level, never()).getEntitiesOfClass(Mockito.eq(LivingEntity.class), any(AABB.class), any());
    verify(level, never()).players();
  }

  @Test
  void guardingHealthGroundBackfillsRawDamageUsingFrozenGuardianArmor() throws Exception {
    bindAttribute(TinkerAttributes.ARMOR_STRENGTH, Attributes.ATTACK_DAMAGE);
    bindAttribute(TinkerAttributes.PRE_REDUCTION, Attributes.LUCK);
    bindAttribute(TinkerAttributes.ARMOR_PROTECTION, Attributes.MOVEMENT_SPEED);

    LivingEntity guardian = mock(LivingEntity.class);
    LivingEntity protectedEntity = mock(LivingEntity.class);
    DamageSource source = mock(DamageSource.class);
    Level level = mock(Level.class);
    EntityType<?> type = mock(EntityType.class);
    when(guardian.getHealth()).thenReturn(17f);
    when(guardian.getArmorValue()).thenReturn(10);
    when(guardian.getAttributeValue(any(Attribute.class))).thenAnswer(invocation -> {
      Attribute attribute = invocation.getArgument(0);
      if (attribute == Attributes.ATTACK_DAMAGE) {
        return 5d;
      }
      if (attribute == Attributes.LUCK) {
        return 1d;
      }
      return 0d;
    });
    when(guardian.getType()).thenReturn((EntityType)type);
    when(type.is(any())).thenReturn(false);
    when(guardian.getItemBySlot(any())).thenReturn(ItemStack.EMPTY);
    when(protectedEntity.level()).thenReturn(level);
    when(source.is(any(TagKey.class))).thenReturn(false);
    when(source.is(any(ResourceKey.class))).thenReturn(false);
    AtomicReference<net.minecraftforge.event.entity.living.LivingHurtEvent> guardianHurt = new AtomicReference<>();
    Mockito.doAnswer(invocation -> {
      var event = new net.minecraftforge.event.entity.living.LivingHurtEvent(guardian, source, invocation.getArgument(1));
      guardianHurt.set(event);
      ToolEvents.livingHurt(event);
      return true;
    }).when(guardian).hurt(any(DamageSource.class), Mockito.anyFloat());

    Class<?> transferClass = Class.forName("slimeknights.tconstruct.tools.logic.ToolEvents$GuardingTransfer");
    var constructor = transferClass.getDeclaredConstructor();
    constructor.setAccessible(true);
    ShareDamageContext transfer = (ShareDamageContext)constructor.newInstance();
    transfer.add(0.5f, 0, 10, 1);

    Method apply = transferClass.getDeclaredMethod("apply", LivingEntity.class, LivingEntity.class, DamageSource.class, float.class);
    apply.setAccessible(true);
    Object result = apply.invoke(transfer, guardian, protectedEntity, source, 20f);
    Method sharedRawDamage = result.getClass().getDeclaredMethod("sharedRawDamage");
    sharedRawDamage.setAccessible(true);

    assertThat((float)sharedRawDamage.invoke(result)).isCloseTo(7.6086955f, org.assertj.core.data.Offset.offset(0.0001f));
    assertThat(guardianHurt.get()).isNotNull();
    assertThat(CombatRules.getDamageAfterAbsorb(guardianHurt.get().getAmount(), 10, 0)).isCloseTo(7f, org.assertj.core.data.Offset.offset(0.0001f));
  }

  @Test
  void guardingHealthGroundTracksActualAppliedFinalDamage() throws Exception {
    LivingEntity guardian = mock(LivingEntity.class);
    DamageSource source = mock(DamageSource.class);
    when(guardian.getHealth()).thenReturn(17f);
    when(source.is(any(TagKey.class))).thenReturn(false);

    Class<?> contextClass = Class.forName("slimeknights.tconstruct.tools.logic.ToolEvents$GuardianHurtContext");
    var constructor = contextClass.getDeclaredConstructor(LivingEntity.class, float.class, float.class);
    constructor.setAccessible(true);
    Object context = constructor.newInstance(guardian, 12f, 10f);
    Method applyToEvent = contextClass.getDeclaredMethod(
      "applyToEvent",
      net.minecraftforge.event.entity.living.LivingHurtEvent.class,
      float.class,
      float.class,
      float.class,
      int.class,
      float.class,
      float.class,
      slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStats.class);
    applyToEvent.setAccessible(true);
    var event = new net.minecraftforge.event.entity.living.LivingHurtEvent(guardian, source, 12f);
    applyToEvent.invoke(context, event, 12f, 0f, 0f, 0, 0f, 20f, null);

    Method appliedFinalDamage = contextClass.getDeclaredMethod("appliedFinalDamage");
    appliedFinalDamage.setAccessible(true);
    assertThat((float)appliedFinalDamage.invoke(context)).isEqualTo(7f);
  }

  @Test
  void guardingKeepsRawShareWhenFrozenGuardianArmorAbsorbsIt() throws Exception {
    bindAttribute(TinkerAttributes.ARMOR_STRENGTH, Attributes.ATTACK_DAMAGE);
    bindAttribute(TinkerAttributes.PRE_REDUCTION, Attributes.LUCK);
    bindAttribute(TinkerAttributes.ARMOR_PROTECTION, Attributes.MOVEMENT_SPEED);

    LivingEntity guardian = mock(LivingEntity.class);
    LivingEntity protectedEntity = mock(LivingEntity.class);
    DamageSource source = mock(DamageSource.class);
    when(guardian.getHealth()).thenReturn(20f);
    when(guardian.getAttributeValue(any(Attribute.class))).thenAnswer(invocation -> invocation.getArgument(0) == Attributes.LUCK ? 20d : 0d);
    when(guardian.getItemBySlot(any())).thenReturn(ItemStack.EMPTY);
    when(source.is(any(TagKey.class))).thenReturn(false);
    when(source.is(any(ResourceKey.class))).thenReturn(false);

    Class<?> transferClass = Class.forName("slimeknights.tconstruct.tools.logic.ToolEvents$GuardingTransfer");
    var constructor = transferClass.getDeclaredConstructor();
    constructor.setAccessible(true);
    ShareDamageContext transfer = (ShareDamageContext)constructor.newInstance();
    transfer.add(0.5f, 0, 10, 1);

    Method apply = transferClass.getDeclaredMethod("apply", LivingEntity.class, LivingEntity.class, DamageSource.class, float.class);
    apply.setAccessible(true);
    Object result = apply.invoke(transfer, guardian, protectedEntity, source, 20f);
    Method sharedRawDamage = result.getClass().getDeclaredMethod("sharedRawDamage");
    sharedRawDamage.setAccessible(true);

    assertThat((float)sharedRawDamage.invoke(result)).isEqualTo(10f);
    verify(guardian, never()).hurt(any(DamageSource.class), Mockito.anyFloat());
  }

  @Test
  void guardingFreezesCapacityBackedArmorAttributeModules() throws Exception {
    FormulaManager.applySync(Map.of(
      FROZEN_PRE_REDUCTION, formula(values -> 2),
      FROZEN_PROTECTION, formula(values -> values[3] / values[2])
    ), Map.of(FROZEN_PRE_REDUCTION, "{}", FROZEN_PROTECTION, "{}"));
    bindAttribute(TinkerAttributes.ARMOR_STRENGTH, Attributes.ATTACK_DAMAGE);
    bindAttribute(TinkerAttributes.PRE_REDUCTION, Attributes.LUCK);
    bindAttribute(TinkerAttributes.ARMOR_PROTECTION, Attributes.MOVEMENT_SPEED);

    TestCapacityBar capacityBar = new TestCapacityBar(10, 5);
    bind(new TestFrozenArmorAttributeModifier(capacityBar));
    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.isBroken()).thenReturn(false);

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.is(TinkerTags.Items.MODIFIABLE)).thenReturn(true);

    LivingEntity guardian = mock(LivingEntity.class);
    LivingEntity protectedEntity = mock(LivingEntity.class);
    DamageSource source = mock(DamageSource.class);
    @SuppressWarnings("unchecked")
    EntityType<LivingEntity> type = (EntityType<LivingEntity>)mock(EntityType.class);
    when(guardian.getHealth()).thenReturn(20f);
    when(guardian.getArmorValue()).thenReturn(0);
    when(guardian.getAttributeValue(any(Attribute.class))).thenReturn(0d);
    Mockito.doReturn(type).when(guardian).getType();
    when(type.is(any())).thenReturn(false);
    when(guardian.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(guardian.getItemBySlot(Mockito.argThat(slot -> slot != EquipmentSlot.CHEST))).thenReturn(ItemStack.EMPTY);
    when(source.is(any(TagKey.class))).thenReturn(false);
    when(source.is(any(ResourceKey.class))).thenReturn(false);
    AtomicReference<Float> guardianDamage = new AtomicReference<>();
    Mockito.doAnswer(invocation -> {
      float damage = invocation.getArgument(1);
      guardianDamage.set(damage);
      var event = new net.minecraftforge.event.entity.living.LivingHurtEvent(guardian, source, damage);
      ToolEvents.livingHurt(event);
      guardianDamage.set(event.getAmount());
      return true;
    }).when(guardian).hurt(any(DamageSource.class), Mockito.anyFloat());

    try (MockedStatic<EnchantmentHelper> enchantments = Mockito.mockStatic(EnchantmentHelper.class);
         MockedStatic<ToolStack> toolStacks = Mockito.mockStatic(ToolStack.class)) {
      enchantments.when(() -> EnchantmentHelper.getDamageProtection(guardian.getArmorSlots(), source)).thenReturn(0);
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);

      Class<?> transferClass = Class.forName("slimeknights.tconstruct.tools.logic.ToolEvents$GuardingTransfer");
      var constructor = transferClass.getDeclaredConstructor();
      constructor.setAccessible(true);
      ShareDamageContext transfer = (ShareDamageContext)constructor.newInstance();
      transfer.add(0.5f, 0, 10, 1);
      Method apply = transferClass.getDeclaredMethod("apply", LivingEntity.class, LivingEntity.class, DamageSource.class, float.class);
      apply.setAccessible(true);
      apply.invoke(transfer, guardian, protectedEntity, source, 20f);

      assertThat(guardianDamage.get()).isEqualTo(4f);
    }
  }

  @Test
  void guardingFrozenProfileHonorsTcaeBypassTags() throws Exception {
    FormulaManager.applySync(Map.of(
      FROZEN_PRE_REDUCTION, formula(values -> 2),
      FROZEN_POST_REDUCTION, formula(values -> 1),
      FROZEN_PROTECTION, formula(values -> 0.5)
    ), Map.of(
      FROZEN_PRE_REDUCTION, "{}",
      FROZEN_POST_REDUCTION, "{}",
      FROZEN_PROTECTION, "{}"
    ));
    bindAttribute(TinkerAttributes.ARMOR_STRENGTH, Attributes.ATTACK_DAMAGE);
    bindAttribute(TinkerAttributes.PRE_REDUCTION, Attributes.LUCK);
    bindAttribute(TinkerAttributes.ARMOR_PROTECTION, Attributes.MOVEMENT_SPEED);

    bind(new TestFrozenArmorAttributeModifier(new TestCapacityBar(10, 5)));
    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.isBroken()).thenReturn(false);

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.is(TinkerTags.Items.MODIFIABLE)).thenReturn(true);

    LivingEntity guardian = mock(LivingEntity.class);
    DamageSource source = mock(DamageSource.class);
    @SuppressWarnings("unchecked")
    EntityType<LivingEntity> type = (EntityType<LivingEntity>)mock(EntityType.class);
    when(guardian.getArmorValue()).thenReturn(0);
    when(guardian.getAttributeValue(any(Attribute.class))).thenReturn(0d);
    Mockito.doReturn(type).when(guardian).getType();
    when(type.is(any())).thenReturn(false);
    when(guardian.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(guardian.getItemBySlot(Mockito.argThat(slot -> slot != EquipmentSlot.CHEST))).thenReturn(ItemStack.EMPTY);
    when(source.is(any(TagKey.class))).thenAnswer(invocation -> {
      TagKey<?> tag = invocation.getArgument(0);
      return tag == TinkerTags.DamageTypes.BYPASSES_REDUCTION
        || tag == TinkerTags.DamageTypes.BYPASSES_BLOCKING
        || tag == TinkerTags.DamageTypes.BYPASSES_PROTECTION;
    });
    when(source.is(any(ResourceKey.class))).thenReturn(false);

    try (MockedStatic<ToolStack> toolStacks = Mockito.mockStatic(ToolStack.class)) {
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);
      Class<?> profileClass = Class.forName("slimeknights.tconstruct.tools.logic.ToolEvents$GuardianDamageProfile");
      Method capture = profileClass.getDeclaredMethod("capture", LivingEntity.class, DamageSource.class, float.class);
      capture.setAccessible(true);
      Object profile = capture.invoke(null, guardian, source, 8f);
      Method apply = profileClass.getDeclaredMethod("apply", float.class);
      apply.setAccessible(true);

      assertThat((float)apply.invoke(profile, 8f)).isEqualTo(8f);
    }
  }

  @Test
  void guardingStillRunsDamageLimitCommonHook() throws Exception {
    bindAttribute(TinkerAttributes.ARMOR_STRENGTH, Attributes.ATTACK_DAMAGE);
    bindAttribute(TinkerAttributes.PRE_REDUCTION, Attributes.LUCK);
    bindAttribute(TinkerAttributes.ARMOR_PROTECTION, Attributes.MOVEMENT_SPEED);
    FormulaManager.applySync(Map.of(
      DAMAGE_LIMIT_CONDITION, formula(values -> 1),
      DAMAGE_LIMIT_CAP, formula(values -> 2),
      DAMAGE_LIMIT_RATIO, formula(values -> 1),
      DAMAGE_LIMIT_ARMOR, formula(values -> 0),
      DAMAGE_LIMIT_CAPACITY, formula(values -> 0)
    ), Map.of(
      DAMAGE_LIMIT_CONDITION, "{}",
      DAMAGE_LIMIT_CAP, "{}",
      DAMAGE_LIMIT_RATIO, "{}",
      DAMAGE_LIMIT_ARMOR, "{}",
      DAMAGE_LIMIT_CAPACITY, "{}"
    ));
    bind(new TestDamageLimitModifier());
    bind(new TestCapacityModifier(new TestCapacityBar(100, 100)), TEST_CAPACITY_ID);

    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.getModifierLevel(TEST_CAPACITY_ID)).thenReturn(1);
    when(tool.isBroken()).thenReturn(false);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    when(tool.getCurrentDurability()).thenReturn(100);
    when(tool.getPersistentData()).thenReturn(new slimeknights.tconstruct.library.tools.nbt.ToolDataNBT());

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.is(TinkerTags.Items.MODIFIABLE)).thenReturn(true);

    LivingEntity guardian = mock(LivingEntity.class);
    DamageSource source = mock(DamageSource.class);
    Level level = mock(Level.class);
    @SuppressWarnings("unchecked")
    EntityType<LivingEntity> type = (EntityType<LivingEntity>)mock(EntityType.class);
    when(guardian.getHealth()).thenReturn(20f);
    when(guardian.level()).thenReturn(level);
    when(guardian.getArmorValue()).thenReturn(0);
    when(guardian.getAttributeValue(any(Attribute.class))).thenReturn(0d);
    Mockito.doReturn(type).when(guardian).getType();
    when(type.is(any())).thenReturn(false);
    when(guardian.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(guardian.getItemBySlot(Mockito.argThat(slot -> slot != EquipmentSlot.CHEST))).thenReturn(ItemStack.EMPTY);
    when(source.is(any(TagKey.class))).thenReturn(false);
    when(source.is(any(ResourceKey.class))).thenReturn(false);
    AtomicReference<Float> guardianDamage = new AtomicReference<>();
    Mockito.doAnswer(invocation -> {
      var event = new net.minecraftforge.event.entity.living.LivingHurtEvent(guardian, source, invocation.getArgument(1));
      ToolEvents.livingHurt(event);
      guardianDamage.set(event.getAmount());
      return true;
    }).when(guardian).hurt(any(DamageSource.class), Mockito.anyFloat());

    try (MockedStatic<EnchantmentHelper> enchantments = Mockito.mockStatic(EnchantmentHelper.class);
         MockedStatic<ToolStack> toolStacks = Mockito.mockStatic(ToolStack.class)) {
      enchantments.when(() -> EnchantmentHelper.getDamageProtection(guardian.getArmorSlots(), source)).thenReturn(0);
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);

      Class<?> contextClass = Class.forName("slimeknights.tconstruct.tools.logic.ToolEvents$GuardianHurtContext");
      var constructor = contextClass.getDeclaredConstructor(LivingEntity.class, float.class, float.class);
      constructor.setAccessible(true);
      Object context = constructor.newInstance(guardian, 8f, 0f);
      Method hurtGuardian = ToolEvents.class.getDeclaredMethod("hurtGuardian", LivingEntity.class, DamageSource.class, contextClass);
      hurtGuardian.setAccessible(true);
      hurtGuardian.invoke(null, guardian, source, context);

      assertThat(guardianDamage.get()).isEqualTo(2f);
    }
  }

  @Test
  void guardingDamageLimitStillAppliesToArmorBypassingDamage() throws Exception {
    bindAttribute(TinkerAttributes.ARMOR_STRENGTH, Attributes.ATTACK_DAMAGE);
    bindAttribute(TinkerAttributes.PRE_REDUCTION, Attributes.LUCK);
    bindAttribute(TinkerAttributes.ARMOR_PROTECTION, Attributes.MOVEMENT_SPEED);
    FormulaManager.applySync(Map.of(
      DAMAGE_LIMIT_CONDITION, formula(values -> 1),
      DAMAGE_LIMIT_CAP, formula(values -> 2),
      DAMAGE_LIMIT_RATIO, formula(values -> 1),
      DAMAGE_LIMIT_ARMOR, formula(values -> 0),
      DAMAGE_LIMIT_CAPACITY, formula(values -> 0)
    ), Map.of(
      DAMAGE_LIMIT_CONDITION, "{}",
      DAMAGE_LIMIT_CAP, "{}",
      DAMAGE_LIMIT_RATIO, "{}",
      DAMAGE_LIMIT_ARMOR, "{}",
      DAMAGE_LIMIT_CAPACITY, "{}"
    ));
    bind(new TestDamageLimitModifier());
    bind(new TestCapacityModifier(new TestCapacityBar(100, 100)), TEST_CAPACITY_ID);

    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.getModifierLevel(TEST_CAPACITY_ID)).thenReturn(1);
    when(tool.isBroken()).thenReturn(false);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    when(tool.getCurrentDurability()).thenReturn(100);
    when(tool.getPersistentData()).thenReturn(new slimeknights.tconstruct.library.tools.nbt.ToolDataNBT());

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.is(TinkerTags.Items.MODIFIABLE)).thenReturn(true);

    LivingEntity guardian = mock(LivingEntity.class);
    DamageSource source = mock(DamageSource.class);
    Level level = mock(Level.class);
    @SuppressWarnings("unchecked")
    EntityType<LivingEntity> type = (EntityType<LivingEntity>)mock(EntityType.class);
    when(guardian.getHealth()).thenReturn(20f);
    when(guardian.level()).thenReturn(level);
    when(guardian.getArmorValue()).thenReturn(0);
    when(guardian.getAttributeValue(any(Attribute.class))).thenReturn(0d);
    Mockito.doReturn(type).when(guardian).getType();
    when(type.is(any())).thenReturn(false);
    when(guardian.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(guardian.getItemBySlot(Mockito.argThat(slot -> slot != EquipmentSlot.CHEST))).thenReturn(ItemStack.EMPTY);
    when(source.is(any(TagKey.class))).thenAnswer(invocation -> invocation.getArgument(0) == DamageTypeTags.BYPASSES_ARMOR);
    when(source.is(any(ResourceKey.class))).thenReturn(false);
    AtomicReference<Float> guardianDamage = new AtomicReference<>();
    Mockito.doAnswer(invocation -> {
      var event = new net.minecraftforge.event.entity.living.LivingHurtEvent(guardian, source, invocation.getArgument(1));
      ToolEvents.livingHurt(event);
      guardianDamage.set(event.getAmount());
      return true;
    }).when(guardian).hurt(any(DamageSource.class), Mockito.anyFloat());

    try (MockedStatic<EnchantmentHelper> enchantments = Mockito.mockStatic(EnchantmentHelper.class);
         MockedStatic<ToolStack> toolStacks = Mockito.mockStatic(ToolStack.class)) {
      enchantments.when(() -> EnchantmentHelper.getDamageProtection(guardian.getArmorSlots(), source)).thenReturn(0);
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);

      Class<?> contextClass = Class.forName("slimeknights.tconstruct.tools.logic.ToolEvents$GuardianHurtContext");
      var constructor = contextClass.getDeclaredConstructor(LivingEntity.class, float.class, float.class);
      constructor.setAccessible(true);
      Object context = constructor.newInstance(guardian, 8f, 0f);
      Method hurtGuardian = ToolEvents.class.getDeclaredMethod("hurtGuardian", LivingEntity.class, DamageSource.class, contextClass);
      hurtGuardian.setAccessible(true);
      hurtGuardian.invoke(null, guardian, source, context);

      assertThat(guardianDamage.get()).isEqualTo(2f);
    }
  }

  @Test
  void damageLimitReadsAndConsumesConfiguredCapacityBar() {
    FormulaManager.applySync(Map.of(
      DAMAGE_LIMIT_CONDITION, formula(values -> values[3] > 90 ? 1 : 0),
      DAMAGE_LIMIT_CAP, formula(values -> 2),
      DAMAGE_LIMIT_RATIO, formula(values -> 1),
      DAMAGE_LIMIT_ARMOR, formula(values -> 0),
      DAMAGE_LIMIT_CAPACITY, formula(values -> 7)
    ), Map.of(
      DAMAGE_LIMIT_CONDITION, "{}",
      DAMAGE_LIMIT_CAP, "{}",
      DAMAGE_LIMIT_RATIO, "{}",
      DAMAGE_LIMIT_ARMOR, "{}",
      DAMAGE_LIMIT_CAPACITY, "{}"
    ));
    TestCapacityBar capacityBar = new TestCapacityBar(100, 95);
    bind(new TestCapacityModifier(capacityBar), TEST_CAPACITY_ID);

    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierLevel(TEST_CAPACITY_ID)).thenReturn(1);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    when(tool.getCurrentDurability()).thenReturn(1);
    LivingEntity guardian = mock(LivingEntity.class);
    Level level = mock(Level.class);
    when(guardian.level()).thenReturn(level);

    var module = slimeknights.tconstruct.library.modifiers.modules.armor.FormulaDamageLimitModule.limit(
      DAMAGE_LIMIT_CAP,
      DAMAGE_LIMIT_CONDITION,
      DAMAGE_LIMIT_RATIO,
      DAMAGE_LIMIT_ARMOR,
      DAMAGE_LIMIT_CAPACITY,
      TEST_CAPACITY_ID);
    var stats = new slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStats(0, 0, 0, 0, 8f);
    module.addArmorDamageStats(tool, new ModifierEntry(TEST_MODIFIER_ID, 1), mock(slimeknights.tconstruct.library.tools.context.EquipmentContext.class),
      EquipmentSlot.CHEST, mock(DamageSource.class), stats);

    assertThat(stats.hasDamageLimit()).isTrue();
    assertThat(stats.applyDamageLimit(guardian, 8f)).isEqualTo(2f);
    assertThat(capacityBar.amount).isEqualTo(88);
    verify(tool, never()).setDamage(Mockito.anyInt());
  }

  @Test
  void livingHurtAppliesDamageLimitWithoutOtherArmorExtensionStats() throws Exception {
    PlayerPersistentDataCache.setDataGetter(ignored -> new ModDataNBT());
    bindAttribute(TinkerAttributes.ARMOR_STRENGTH, Attributes.ATTACK_DAMAGE);
    bindAttribute(TinkerAttributes.PRE_REDUCTION, Attributes.LUCK);
    bindAttribute(TinkerAttributes.ARMOR_PROTECTION, Attributes.MOVEMENT_SPEED);
    FormulaManager.applySync(Map.of(
      DAMAGE_LIMIT_CONDITION, formula(values -> 1),
      DAMAGE_LIMIT_CAP, formula(values -> 2),
      DAMAGE_LIMIT_RATIO, formula(values -> 1),
      DAMAGE_LIMIT_ARMOR, formula(values -> 0),
      DAMAGE_LIMIT_CAPACITY, formula(values -> 0)
    ), Map.of(
      DAMAGE_LIMIT_CONDITION, "{}",
      DAMAGE_LIMIT_CAP, "{}",
      DAMAGE_LIMIT_RATIO, "{}",
      DAMAGE_LIMIT_ARMOR, "{}",
      DAMAGE_LIMIT_CAPACITY, "{}"
    ));
    bind(new TestDamageLimitModifier());
    bind(new TestCapacityModifier(new TestCapacityBar(100, 100)), TEST_CAPACITY_ID);

    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.withModifier(TEST_MODIFIER_ID, 1).getModifiers());
    when(tool.getModifierLevel(TEST_CAPACITY_ID)).thenReturn(1);
    when(tool.isBroken()).thenReturn(false);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    when(tool.getCurrentDurability()).thenReturn(100);
    when(tool.getPersistentData()).thenReturn(new slimeknights.tconstruct.library.tools.nbt.ToolDataNBT());

    ItemStack chest = mock(ItemStack.class);
    when(chest.isEmpty()).thenReturn(false);
    when(chest.is(TinkerTags.Items.MODIFIABLE)).thenReturn(true);

    Player player = mock(Player.class);
    Level level = mock(Level.class);
    DamageSource source = mock(DamageSource.class);
    @SuppressWarnings("unchecked")
    EntityType<Player> type = (EntityType<Player>)mock(EntityType.class);
    when(player.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);
    Mockito.doReturn(java.util.List.of(player)).when(level).players();
    when(player.getPassengers()).thenReturn(java.util.List.of());
    when(player.getBoundingBox()).thenReturn(new AABB(0, 0, 0, 1, 2, 1));
    when(player.getArmorValue()).thenReturn(0);
    when(player.getAttributeValue(any(Attribute.class))).thenReturn(0d);
    Mockito.doReturn(type).when(player).getType();
    when(type.is(any())).thenReturn(false);
    when(player.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);
    when(player.getItemBySlot(Mockito.argThat(slot -> slot != EquipmentSlot.CHEST))).thenReturn(ItemStack.EMPTY);
    when(source.is(any(TagKey.class))).thenReturn(false);
    when(source.is(any(ResourceKey.class))).thenReturn(false);

    try (MockedStatic<EnchantmentHelper> enchantments = Mockito.mockStatic(EnchantmentHelper.class);
         MockedStatic<ToolStack> toolStacks = Mockito.mockStatic(ToolStack.class)) {
      enchantments.when(() -> EnchantmentHelper.getDamageProtection(player.getArmorSlots(), source)).thenReturn(0);
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);

      var event = new net.minecraftforge.event.entity.living.LivingHurtEvent(player, source, 8f);
      ToolEvents.livingHurt(event);

      assertThat(event.getAmount()).isEqualTo(2f);
    }
  }

  @Test
  void damageLimitArmorCarrierDirectlyDamagesCreativeGuardianLikeTcae() {
    FormulaManager.applySync(Map.of(
      DAMAGE_LIMIT_CONDITION, formula(values -> 1),
      DAMAGE_LIMIT_CAP, formula(values -> 2),
      DAMAGE_LIMIT_RATIO, formula(values -> 1),
      DAMAGE_LIMIT_ARMOR, formula(values -> 3),
      DAMAGE_LIMIT_CAPACITY, formula(values -> 0)
    ), Map.of(
      DAMAGE_LIMIT_CONDITION, "{}",
      DAMAGE_LIMIT_CAP, "{}",
      DAMAGE_LIMIT_RATIO, "{}",
      DAMAGE_LIMIT_ARMOR, "{}",
      DAMAGE_LIMIT_CAPACITY, "{}"
    ));
    bind(new TestCapacityModifier(new TestCapacityBar(100, 100)), TEST_CAPACITY_ID);

    ToolStack tool = mock(ToolStack.class);
    when(tool.getModifierLevel(TEST_CAPACITY_ID)).thenReturn(1);
    when(tool.getStats()).thenReturn(StatsNBT.builder().set(ToolStats.DURABILITY, 100f).build());
    when(tool.getCurrentDurability()).thenReturn(100);
    when(tool.getDamage()).thenReturn(0);
    when(tool.getPersistentData()).thenReturn(new slimeknights.tconstruct.library.tools.nbt.ToolDataNBT());
    when(tool.getModifierList()).thenReturn(ModifierNBT.EMPTY.getModifiers());
    when(tool.hasTag(TinkerTags.Items.DURABILITY)).thenReturn(true);

    Player guardian = mock(Player.class);
    Level level = mock(Level.class);
    ItemStack chest = mock(ItemStack.class);
    when(guardian.level()).thenReturn(level);
    when(guardian.isCreative()).thenReturn(true);
    when(guardian.getItemBySlot(EquipmentSlot.CHEST)).thenReturn(chest);

    var module = slimeknights.tconstruct.library.modifiers.modules.armor.FormulaDamageLimitModule.limit(
      DAMAGE_LIMIT_CAP,
      DAMAGE_LIMIT_CONDITION,
      DAMAGE_LIMIT_RATIO,
      DAMAGE_LIMIT_ARMOR,
      DAMAGE_LIMIT_CAPACITY,
      TEST_CAPACITY_ID);
    var stats = new slimeknights.tconstruct.library.modifiers.hook.armor.ArmorDamageStatsModifierHook.ArmorDamageStats(0, 0, 0, 0, 8f);
    module.addArmorDamageStats(tool, new ModifierEntry(TEST_MODIFIER_ID, 1), mock(slimeknights.tconstruct.library.tools.context.EquipmentContext.class),
      EquipmentSlot.CHEST, mock(DamageSource.class), stats);
    stats.applyDamageLimit(guardian, 8f);

    verify(tool).setDamage(3);
  }

  private static void bindAttribute(RegistryObject<Attribute> object, Attribute value) throws Exception {
    Field valueField = RegistryObject.class.getDeclaredField("value");
    valueField.setAccessible(true);
    valueField.set(object, value);
  }

  private static Modifier bind(Modifier modifier) {
    return bind(modifier, TEST_MODIFIER_ID);
  }

  private static Modifier bind(Modifier modifier, ModifierId id) {
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

  private static class TestPlatingModifier extends Modifier {
    private TestPlatingModifier(CapacityBarHook capacityBar) {
      super(ModuleHookMap.builder()
        .addHook(capacityBar, ModifierHooks.CAPACITY_BAR)
        .addHook(ToolDamageCapacityModule.of(PRE_DAMAGE, DAMAGE_CAPACITY_PRE, 3125), ModifierHooks.TOOL_DAMAGE)
        .addHook(ToolDamageCapacityModule.of(TOOL_DAMAGE, DAMAGE_CAPACITY, 100), ModifierHooks.TOOL_DAMAGE)
        .build());
    }
  }

  private static class TestFrozenArmorAttributeModifier extends Modifier {
    private TestFrozenArmorAttributeModifier(CapacityBarHook capacityBar) {
      super(ModuleHookMap.builder()
        .addHook(capacityBar, ModifierHooks.CAPACITY_BAR)
        .addHook(FormulaArmorStatModule.stat(ArmorDamageStat.PRE_REDUCTION, FROZEN_PRE_REDUCTION), ModifierHooks.ARMOR_DAMAGE_STATS)
        .addHook(FormulaArmorStatModule.stat(ArmorDamageStat.POST_REDUCTION, FROZEN_POST_REDUCTION), ModifierHooks.ARMOR_DAMAGE_STATS)
        .addHook(FormulaArmorStatModule.stat(ArmorDamageStat.ARMOR_PROTECTION, FROZEN_PROTECTION), ModifierHooks.ARMOR_DAMAGE_STATS)
        .build());
    }
  }

  private static class TestDamageLimitModifier extends Modifier {
    private TestDamageLimitModifier() {
      super(ModuleHookMap.builder()
        .addModule(slimeknights.tconstruct.library.modifiers.modules.armor.FormulaDamageLimitModule.limit(
          DAMAGE_LIMIT_CAP,
          DAMAGE_LIMIT_CONDITION,
          DAMAGE_LIMIT_RATIO,
          DAMAGE_LIMIT_ARMOR,
          DAMAGE_LIMIT_CAPACITY,
          TEST_CAPACITY_ID))
        .build());
    }
  }

  private static class TestCapacityModifier extends Modifier {
    private TestCapacityModifier(CapacityBarHook capacityBar) {
      super(ModuleHookMap.builder().addHook(capacityBar, ModifierHooks.CAPACITY_BAR).build());
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
      this.amount = Math.max(0, Math.min(amount, capacity));
    }
  }

  @FunctionalInterface
  private interface FormulaBody {
    double accept(double[] values);
  }
}
