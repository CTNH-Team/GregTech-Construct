package slimeknights.tconstruct.plugin.botania.modifier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialIds;
import slimeknights.tconstruct.test.BaseMcTest;
import vazkii.botania.api.mana.ManaDiscountEvent;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class AncientWillModifierTest extends BaseMcTest {
  private static final Offset<Float> TOLERANCE = Offset.strictOffset(0.0001F);

  private Player player;

  @BeforeEach
  void setUp() {
    player = Mockito.mock(Player.class);
    Mockito.when(player.getItemBySlot(Mockito.any(EquipmentSlot.class))).thenReturn(ItemStack.EMPTY);
  }

  @Test
  void manaDiscountIsAddedWhenTerrarecoverSetIsFull() {
    equip(EquipmentSlot.HEAD, 1);
    equip(EquipmentSlot.CHEST, 1);
    equip(EquipmentSlot.LEGS, 1);
    equip(EquipmentSlot.FEET, 1);
    ManaDiscountEvent event = new ManaDiscountEvent(player, 0.15F, ItemStack.EMPTY);

    TerraSetBonusModifier.onManaDiscount(event);

    assertThat(event.getDiscount()).isCloseTo(0.35F, TOLERANCE);
  }

  @Test
  void manaDiscountIsNotAddedWhenTerrarecoverSetIsPartial() {
    equip(EquipmentSlot.HEAD, 1);
    equip(EquipmentSlot.CHEST, 1);
    equip(EquipmentSlot.LEGS, 1);
    ManaDiscountEvent event = new ManaDiscountEvent(player, 0.15F, ItemStack.EMPTY);

    TerraSetBonusModifier.onManaDiscount(event);

    assertThat(event.getDiscount()).isCloseTo(0.15F, TOLERANCE);
  }

  @Test
  void singleHighLevelTerrarecoverPieceDoesNotActivateManaDiscount() {
    equip(EquipmentSlot.HEAD, 4);
    ManaDiscountEvent event = new ManaDiscountEvent(player, 0.15F, ItemStack.EMPTY);

    TerraSetBonusModifier.onManaDiscount(event);

    assertThat(event.getDiscount()).isCloseTo(0.15F, TOLERANCE);
  }

  @Test
  void terraSetBonusGeneratesFourManaPerTick() {
    assertThat(TerraSetBonusModifier.MANA_GENERATION).isEqualTo(4);
  }

  @Test
  void terrasteelHelmetPlatingRequiresHelmetWithTerrasteelPrimaryMaterial() {
    IToolContext tool = Mockito.mock(IToolContext.class);
    ArmorItem helmet = Mockito.mock(ArmorItem.class, Mockito.withSettings().extraInterfaces(IModifiable.class));
    Mockito.when(tool.getItem()).thenReturn(helmet);
    Mockito.when(helmet.getType()).thenReturn(ArmorItem.Type.HELMET);
    Mockito.when(tool.getMaterial(0)).thenReturn(MaterialVariant.of(BotaniaMaterialIds.terraSteel, ""));

    assertThat(AncientWillModifier.hasTerrasteelHelmetPlating(tool)).isTrue();

    Mockito.when(tool.getMaterial(0)).thenReturn(MaterialVariant.UNKNOWN);
    assertThat(AncientWillModifier.hasTerrasteelHelmetPlating(tool)).isFalse();

    Mockito.when(tool.getMaterial(0)).thenReturn(MaterialVariant.of(BotaniaMaterialIds.terraSteel, ""));
    Mockito.when(helmet.getType()).thenReturn(ArmorItem.Type.CHESTPLATE);
    assertThat(AncientWillModifier.hasTerrasteelHelmetPlating(tool)).isFalse();
  }

  @Test
  void terrasteelHelmetIngredientProvidesNonEmptyDisplay() {
    assertThat(AncientWillModifier.terrasteelHelmetIngredient().getItems()).isNotEmpty();
  }

  @Test
  void dharokMultiplierMatchesBotaniaFormula() {
    assertThat(AncientWillModifier.getDharokCritDamageMult(20F, 20F)).isCloseTo(1F, TOLERANCE);
    assertThat(AncientWillModifier.getDharokCritDamageMult(10F, 20F)).isCloseTo(1.25F, TOLERANCE);
    assertThat(AncientWillModifier.getDharokCritDamageMult(1F, 20F)).isCloseTo(1.475F, TOLERANCE);
  }

  @Test
  void willEffectsMatchBotaniaDurationsAndAmounts() {
    Player attacker = Mockito.mock(Player.class);
    LivingEntity target = Mockito.mock(LivingEntity.class);

    AncientWillModifier.applyEffects(EnumSet.of(
            AncientWillModifier.Will.AHRIM,
            AncientWillModifier.Will.GUTHAN,
            AncientWillModifier.Will.TORAG,
            AncientWillModifier.Will.KARIL), 8F, attacker, target);

    Mockito.verify(attacker).heal(2F);
    ArgumentCaptor<MobEffectInstance> effects = ArgumentCaptor.forClass(MobEffectInstance.class);
    Mockito.verify(target, Mockito.times(3)).addEffect(effects.capture());
    assertEffect(effects.getAllValues().get(0), MobEffects.WEAKNESS, 20, 1);
    assertEffect(effects.getAllValues().get(1), MobEffects.MOVEMENT_SLOWDOWN, 60, 1);
    assertEffect(effects.getAllValues().get(2), MobEffects.WITHER, 60, 1);
  }

  private static void assertEffect(MobEffectInstance instance, MobEffect effect, int duration, int amplifier) {
    assertThat(instance.getEffect()).isSameAs(effect);
    assertThat(instance.getDuration()).isEqualTo(duration);
    assertThat(instance.getAmplifier()).isEqualTo(amplifier);
  }

  private void equip(EquipmentSlot slot, int level) {
    ItemStack stack = Mockito.mock(ItemStack.class);
    CompoundTag entry = new CompoundTag();
    entry.putString(ModifierEntry.TAG_MODIFIER, BotaniaModifierIds.terrarecover.toString());
    entry.putInt(ModifierEntry.TAG_LEVEL, level);
    ListTag modifiers = new ListTag();
    modifiers.add(entry);
    CompoundTag tag = new CompoundTag();
    tag.put(ToolStack.TAG_MODIFIERS, modifiers);
    Mockito.when(stack.isEmpty()).thenReturn(false);
    Mockito.when(stack.is(Mockito.<TagKey<Item>>any())).thenAnswer(invocation -> invocation.getArgument(0) == TinkerTags.Items.MODIFIABLE);
    Mockito.when(stack.getTag()).thenReturn(tag);
    Mockito.when(player.getItemBySlot(slot)).thenReturn(stack);
  }
}
