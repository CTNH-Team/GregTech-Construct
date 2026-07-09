package slimeknights.tconstruct.tools.logic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.modifiers.modules.behavior.ToolDamageCapacityModule;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageHandler;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ToolEventsPlatingDepletedFallbackTest extends BaseMcTest {
  private static final ModifierId TEST_MODIFIER_ID = new ModifierId("test", "plating_depleted_fallback");
  private static final ResourceLocation PRE_DAMAGE = TConstruct.getResource("plating/pre_damage");
  private static final ResourceLocation TOOL_DAMAGE = TConstruct.getResource("plating/tool_damage");
  private static final ResourceLocation DAMAGE_CAPACITY_PRE = TConstruct.getResource("plating/damage_capacity_pre");
  private static final ResourceLocation DAMAGE_CAPACITY = TConstruct.getResource("plating/damage_capacity");
  private final Map<ResourceLocation,IFormula> previousFormulas = FormulaManager.getAll();
  private final Map<ResourceLocation,String> previousRawJson = FormulaManager.getAllRawJson();

  @AfterEach
  void restoreFormulaManager() {
    FormulaManager.applySync(previousFormulas, previousRawJson);
    ToolDamageHandler.clearPendingDamageForTests();
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

    try (MockedStatic<EnchantmentHelper> enchantments = Mockito.mockStatic(EnchantmentHelper.class);
         MockedStatic<ToolStack> toolStacks = Mockito.mockStatic(ToolStack.class)) {
      enchantments.when(() -> EnchantmentHelper.getDamageProtection(player.getArmorSlots(), source)).thenReturn(0);
      toolStacks.when(() -> ToolStack.from(chest)).thenReturn(tool);

      ToolEvents.livingHurt(new net.minecraftforge.event.entity.living.LivingHurtEvent(player, source, 8f));
      ToolDamageHandler.flushPendingDamage();
      assertThat(capacityBar.amount).isZero();
      int damageAfterFirstHit = damage.get();

      ToolEvents.livingHurt(new net.minecraftforge.event.entity.living.LivingHurtEvent(player, source, 8f));
      ToolDamageHandler.flushPendingDamage();
      assertThat(damage.get()).isGreaterThan(damageAfterFirstHit);
    }
  }

  private static void bindAttribute(RegistryObject<Attribute> object, Attribute value) throws Exception {
    Field valueField = RegistryObject.class.getDeclaredField("value");
    valueField.setAccessible(true);
    valueField.set(object, value);
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

  private static class TestPlatingModifier extends Modifier {
    private TestPlatingModifier(CapacityBarHook capacityBar) {
      super(ModuleHookMap.builder()
        .addHook(capacityBar, ModifierHooks.CAPACITY_BAR)
        .addHook(ToolDamageCapacityModule.of(PRE_DAMAGE, DAMAGE_CAPACITY_PRE, 3125), ModifierHooks.TOOL_DAMAGE)
        .addHook(ToolDamageCapacityModule.of(TOOL_DAMAGE, DAMAGE_CAPACITY, 100), ModifierHooks.TOOL_DAMAGE)
        .build());
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
