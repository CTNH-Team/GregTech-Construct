package slimeknights.tconstruct.library.modifiers.hook.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.modules.capacity.OverslimeModule;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.shared.AchievementEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface ArmorDamageStatsModifierHook {
  void addArmorDamageStats(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, ArmorDamageStats stats);

  record AllMerger(Collection<ArmorDamageStatsModifierHook> modules) implements ArmorDamageStatsModifierHook {
    @Override
    public void addArmorDamageStats(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, ArmorDamageStats stats) {
      for (ArmorDamageStatsModifierHook module : modules) {
        module.addArmorDamageStats(tool, modifier, context, slotType, source, stats);
      }
    }
  }

  final class ArmorDamageStats {
    private float armorStrength;
    private float preReduction;
    private float postReduction;
    private float armorProtection;
    private float armorAbsorptionCap;
    private float highestArmorStrength;
    private float highestPreReduction;
    private float highestPostReduction;
    private float highestArmorProtection;
    private float highestArmorAbsorptionCap;
    private final float originalDamage;
    private float damageLimit = Float.POSITIVE_INFINITY;
    private final List<DamageLimitEntry> damageLimits = new ArrayList<>();

    public ArmorDamageStats(float armorStrength, float preReduction, float postReduction, float armorProtection) {
      this(armorStrength, preReduction, postReduction, armorProtection, 0);
    }

    public ArmorDamageStats(float armorStrength, float preReduction, float postReduction, float armorProtection, float originalDamage) {
      this.armorStrength = armorStrength;
      this.preReduction = preReduction;
      this.postReduction = postReduction;
      this.armorProtection = armorProtection;
      this.originalDamage = originalDamage;
    }

    public float originalDamage() {
      return originalDamage;
    }

    public float armorStrength() {
      return armorStrength + highestArmorStrength;
    }

    public float preReduction() {
      return preReduction + highestPreReduction;
    }

    public float postReduction() {
      return postReduction + highestPostReduction;
    }

    public float armorProtection() {
      return armorProtection + highestArmorProtection;
    }

    public float armorAbsorptionCap() {
      return armorAbsorptionCap + highestArmorAbsorptionCap;
    }

    public void add(ArmorDamageStat stat, float value) {
      switch (stat) {
        case ARMOR_STRENGTH -> armorStrength += value;
        case PRE_REDUCTION -> preReduction += value;
        case POST_REDUCTION, DAMAGE_BLOCK -> postReduction += value;
        case ARMOR_PROTECTION -> armorProtection += value;
        case ARMOR_ABSORPTION_CAP -> armorAbsorptionCap += value;
      }
    }

    public void addHighest(ArmorDamageStat stat, float value) {
      switch (stat) {
        case ARMOR_STRENGTH -> highestArmorStrength = Math.max(highestArmorStrength, value);
        case PRE_REDUCTION -> highestPreReduction = Math.max(highestPreReduction, value);
        case POST_REDUCTION, DAMAGE_BLOCK -> highestPostReduction = Math.max(highestPostReduction, value);
        case ARMOR_PROTECTION -> highestArmorProtection = Math.max(highestArmorProtection, value);
        case ARMOR_ABSORPTION_CAP -> highestArmorAbsorptionCap = Math.max(highestArmorAbsorptionCap, value);
      }
    }

    public void addDamageLimit(IToolStackView tool, EquipmentSlot slotType, float level, float limit, ResourceLocation perArmorRatioFormula,
                               ResourceLocation armorDamageFormula, ResourceLocation overshieldDamageFormula) {
      if (limit <= 0) {
        return;
      }
      damageLimit = Math.min(damageLimit, limit);
      damageLimits.add(new DamageLimitEntry(tool, slotType, level, perArmorRatioFormula, armorDamageFormula, overshieldDamageFormula));
    }

    public boolean hasDamageLimit() {
      return !damageLimits.isEmpty();
    }

    public float applyDamageLimit(LivingEntity entity, float damage) {
      if (damage <= damageLimit || damageLimits.isEmpty()) {
        return damage;
      }
      float overflow = damage - damageLimit;
      double totalLevel = damageLimits.stream().mapToDouble(DamageLimitEntry::level).sum();
      float ratio = getPerArmorRatio(damageLimits.get(0).perArmorRatioFormula(), damageLimits.size());
      entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), Sounds.DAMAGE_LIMIT.getSound(), SoundSource.AMBIENT, 1.0F, 1.0F);
      if (originalDamage > 0 && overflow / originalDamage >= 0.8f && entity instanceof ServerPlayer player) {
        AchievementEvents.grantAdvancement(player, TConstruct.getResource("combat/damage_limit"));
      }
      for (DamageLimitEntry entry : damageLimits) {
        entry.apply(entity, totalLevel, originalDamage, overflow, ratio);
      }
      return damageLimit;
    }

    private static float getPerArmorRatio(ResourceLocation formula, int armorCount) {
      IFormula resolved = FormulaManager.getOrNull(formula);
      if (resolved == null) {
        return 1f / armorCount;
      }
      return Math.max(0, (float)resolved.accept(armorCount));
    }

    private static int randomRound(double value) {
      if (value <= 0) {
        return 0;
      }
      int whole = Mth.floor(value);
      return whole + (value - whole > TConstruct.RANDOM.nextDouble() ? 1 : 0);
    }

    private record DamageLimitEntry(IToolStackView tool, EquipmentSlot slotType, float level, ResourceLocation perArmorRatioFormula,
                                    ResourceLocation armorDamageFormula, ResourceLocation overshieldDamageFormula) {
      private void apply(LivingEntity entity, double totalLevel, float originalDamage, float overflow, float ratio) {
        IFormula armorDamage = FormulaManager.getOrNull(armorDamageFormula);
        if (armorDamage != null) {
          int currentDurability = Math.max(0, tool.getCurrentDurability());
          ToolDamageUtil.damageAnimated(tool, randomRound(armorDamage.accept(currentDurability, totalLevel, originalDamage, overflow) * ratio), entity, slotType);
        }

        IFormula overshieldDamage = FormulaManager.getOrNull(overshieldDamageFormula);
        if (overshieldDamage != null) {
          int overslime = OverslimeModule.INSTANCE.getAmount(tool);
          int amount = Math.min(overslime, randomRound(overshieldDamage.accept(overslime, totalLevel, originalDamage, overflow) * ratio));
          if (amount > 0) {
            OverslimeModule.INSTANCE.removeAmount(tool, amount);
          }
        }
      }
    }
  }

  enum ArmorDamageStat {
    ARMOR_STRENGTH,
    PRE_REDUCTION,
    POST_REDUCTION,
    DAMAGE_BLOCK,
    ARMOR_PROTECTION,
    ARMOR_ABSORPTION_CAP
  }
}
