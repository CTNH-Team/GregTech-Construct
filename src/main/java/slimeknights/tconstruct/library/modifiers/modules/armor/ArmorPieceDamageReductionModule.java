package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ModifyDamageModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

public record ArmorPieceDamageReductionModule(IJsonPredicate<DamageSource> source, float percentPerPiece, ModifierCondition<IToolStackView> condition) implements ModifierModule, ModifyDamageModifierHook, TooltipModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ArmorPieceDamageReductionModule>defaultHooks(ModifierHooks.MODIFY_HURT, ModifierHooks.TOOLTIP);
  public static final RecordLoadable<ArmorPieceDamageReductionModule> LOADER = RecordLoadable.create(
    DamageSourcePredicate.LOADER.defaultField("damage_source", ArmorPieceDamageReductionModule::source),
    FloatLoadable.PERCENT.requiredField("percent_per_piece", ArmorPieceDamageReductionModule::percentPerPiece),
    ModifierCondition.TOOL_FIELD,
    ArmorPieceDamageReductionModule::new);

  public static ArmorPieceDamageReductionModule perPiece(IJsonPredicate<DamageSource> source, float percentPerPiece) {
    return new ArmorPieceDamageReductionModule(source, percentPerPiece, ModifierCondition.ANY_TOOL);
  }

  @Override
  public RecordLoadable<ArmorPieceDamageReductionModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
    if (!condition.matches(tool, modifier) || !this.source.matches(source) || !isFirstMatchingArmorSlot(context, modifier, slotType)) {
      return amount;
    }
    return reduceDamage(amount, percentPerPiece, countMatchingArmorPieces(context, modifier));
  }

  public static float reduceDamage(float amount, float percentPerPiece, int pieceCount) {
    return amount * (1 - getResistance(percentPerPiece, pieceCount));
  }

  public static float getResistance(float percentPerPiece, int pieceCount) {
    return Math.min(1, Math.max(0, pieceCount) * Math.max(0, percentPerPiece));
  }

  int countMatchingArmorPieces(EquipmentContext context, ModifierEntry modifier) {
    int count = 0;
    for (EquipmentSlot slot : ModifiableArmorMaterial.ARMOR_SLOTS) {
      if (hasMatchingModifier(context.getValidTool(slot), modifier)) {
        count++;
      }
    }
    return count;
  }

  private boolean isFirstMatchingArmorSlot(EquipmentContext context, ModifierEntry modifier, EquipmentSlot slotType) {
    for (EquipmentSlot slot : ModifiableArmorMaterial.ARMOR_SLOTS) {
      if (hasMatchingModifier(context.getValidTool(slot), modifier)) {
        return slot == slotType;
      }
    }
    return false;
  }

  private boolean hasMatchingModifier(@Nullable IToolStackView tool, ModifierEntry modifier) {
    if (tool == null || tool.isBroken()) {
      return false;
    }
    ModifierEntry sameModifier = tool.getModifiers().getEntry(modifier.getId());
    return sameModifier.getLevel() > 0 && condition.matches(tool, sameModifier);
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    if (condition.matches(tool, modifier)) {
      TooltipModifierHook.addPercentBoost(modifier.getModifier(), Component.translatable(modifier.getModifier().getTranslationKey() + ".resistance"), percentPerPiece, tooltip);
    }
  }
}
