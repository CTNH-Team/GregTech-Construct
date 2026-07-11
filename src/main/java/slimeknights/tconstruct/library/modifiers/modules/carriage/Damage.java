package slimeknights.tconstruct.library.modifiers.modules.carriage;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.exception.DamageException;
import slimeknights.tconstruct.library.tools.helper.AttributeEntry;
import slimeknights.tconstruct.shared.TinkerAttributes;

import java.util.Arrays;

/**
 * 6-phase damage pipeline ported from TCAE.
 * <p>
 * Phases: RAW -> REDUCED -> STRENGTH_ABSORBED -> ARMOR_ABSORBED -> BLOCKED -> FINAL
 * <p>
 * Attributes array: [armor, toughness, strength, preReduction, postReduction, protection]
 */
public class Damage {
    private static final Attribute[] ATTRIBUTES = new Attribute[]{
        Attributes.ARMOR,
        Attributes.ARMOR_TOUGHNESS,
        TinkerAttributes.ARMOR_STRENGTH.get(),
        TinkerAttributes.PRE_REDUCTION.get(),
        TinkerAttributes.POST_REDUCTION.get(),
    };
    public static final double STRENGTH_ABSORBING_BASE = 0.8;
    public static final double STRENGTH_ABSORBING_UNIT = 4;

    private final float[] attributes;
    private final float[] damages;

    public Damage() {
        attributes = new float[]{0, 0, 0, 0, 0, 1};
        damages = new float[]{0, 0, 0, 0, 0, 0};
    }

    public Damage(Damage src) {
        this.attributes = Arrays.copyOf(src.attributes, 6);
        this.damages = Arrays.copyOf(src.damages, 6);
    }

    public Damage(DamageSource src, LivingEntity living, float raw) {
        attributes = new float[]{0, 0, 0, 0, 0, 1};
        damages = new float[]{raw, 0, 0, 0, 0, 0};
        read(0, src, living);
    }

    public Damage(float armor, float armorToughness, DamageSource src, LivingEntity living, float raw) {
        attributes = new float[]{armor, armorToughness, 0, 0, 0, 1};
        damages = new float[]{raw, 0, 0, 0, 0, 0};
        read(2, src, living);
    }

    public Damage(float a, float b, float c, float d, float e, float f, float raw) {
        attributes = new float[]{a, b, c, d, e, f};
        damages = new float[]{raw, 0, 0, 0, 0, 0};
    }

    public Damage accept(int from, float... values) {
        if (from > 5 || from < 0)
            return this;

        int ceiling = Math.min(6, values.length + from);
        System.arraycopy(values, 0, attributes, from, ceiling - from);
        return this;
    }

    public Damage read(int from, AttributeEntry entry) {
        for (int i = from; i < 6; i++) {
            attributes[i] = entry.get(i);
        }
        return this;
    }

    public Damage write(int from, AttributeEntry entry) {
        for (int i = from; i < 6; i++) {
            entry.set(i, attributes[i]);
        }
        return this;
    }

    public Damage read(int from, DamageSource src, LivingEntity living) {
        if (from > 5 || from < 0)
            return this;

        for (int i = from; i < 5; i++) {
            if (i < 3 && src.is(DamageTypeTags.BYPASSES_ARMOR))
                continue;
            if (i == 3 && src.is(TinkerTags.DamageTypes.BYPASSES_REDUCTION))
                continue;
            if (i == 4 && src.is(TinkerTags.DamageTypes.BYPASSES_BLOCKING))
                continue;
            var instance = living.getAttribute(ATTRIBUTES[i]);
            attributes[i] = instance != null ? (float) instance.getValue() : 0;
        }
        // protection multiplier: check bypass before reading
        if (!src.is(TinkerTags.DamageTypes.BYPASSES_PROTECTION)) {
            var protInstance = living.getAttribute(TinkerAttributes.ARMOR_PROTECTION.get());
            attributes[5] = protInstance != null ? (float) protInstance.getValue() : 1.0F;
        }
        return this;
    }

    public float get(Phase phase) {
        return damages[phase.index];
    }

    public float[] get() {
        return Arrays.copyOf(damages, 6);
    }

    public Damage set(Phase phase, float value) {
        damages[phase.index] = value;
        return this;
    }

    public Damage fallThrough(Phase phase, float value) {
        for (int i = phase.index; i < 6; i++) {
            damages[i] = value;
        }
        return this;
    }

    public float act() {
        return act(new Factor());
    }

    public float act(Factor factor) {
        factor.toughnessFactor = Math.max(2.0F, 2.0F + (attributes[1] / 4.0F));
        damages[1] = Math.max(0, damages[0] - attributes[3]);
        factor.effectiveStrength = Math.max(0.0F, attributes[2] - damages[1] / factor.toughnessFactor);
        damages[2] = damages[1] * (float) Math.pow(STRENGTH_ABSORBING_BASE, factor.effectiveStrength / STRENGTH_ABSORBING_UNIT);
        factor.effectiveArmor = Math.max((attributes[0] + attributes[1]) / 5.0F, Math.min(attributes[0], attributes[0] + factor.effectiveStrength - damages[2] / factor.toughnessFactor));
        damages[3] = damages[2] * Math.max(0.2F, 1.0F - factor.effectiveArmor * 0.04F);
        damages[4] = Math.max(0, damages[3] - attributes[4]);
        damages[5] = damages[4] * attributes[5];
        return damages[5];
    }

    public float actWith(Factor factor) {
        damages[1] = Math.max(0, damages[0] - attributes[3]);
        damages[2] = damages[1] * (float) Math.pow(STRENGTH_ABSORBING_BASE, factor.effectiveStrength / STRENGTH_ABSORBING_UNIT);
        damages[3] = damages[2] * Math.max(0.2F, 1.0F - factor.effectiveArmor * 0.04F);
        damages[4] = Math.max(0, damages[3] - attributes[4]);
        damages[5] = damages[4] * attributes[5];
        return damages[5];
    }

    public float actFrom(Phase from, Factor factor) {
        try {
            switch (from) {
                case RAW:
                    factor.toughnessFactor = Math.max(2.0F, 2.0F + (attributes[1] / 4.0F));
                    damages[1] = Math.max(0, damages[0] - attributes[3]);
                case REDUCED:
                    factor.effectiveStrength = Math.max(0.0F, attributes[2] - damages[1] / factor.toughnessFactor);
                    damages[2] = damages[1] * (float) Math.pow(STRENGTH_ABSORBING_BASE, factor.effectiveStrength / STRENGTH_ABSORBING_UNIT);
                case STRENGTH_ABSORBED:
                    factor.effectiveArmor = Math.max((attributes[0] + attributes[1]) / 5.0F, Math.min(attributes[0], attributes[0] + factor.effectiveStrength - damages[2] / factor.toughnessFactor));
                    damages[3] = damages[2] * Math.max(0.2F, 1.0F - factor.effectiveArmor * 0.04F);
                case ARMOR_ABSORBED:
                    damages[4] = Math.max(0, damages[3] - attributes[4]);
                case BLOCKED:
                    damages[5] = damages[4] * attributes[5];
                case FINAL:
            }
        } catch (Exception e) {
            throw new DamageException("Invalid Damage Factor during invoke of Damage.actFrom");
        }
        return damages[5];
    }

    public float actFromWith(Phase from, Factor factor) {
        try {
            switch (from) {
                case RAW:
                    damages[1] = Math.max(0, damages[0] - attributes[3]);
                case REDUCED:
                    damages[2] = damages[1] * (float) Math.pow(STRENGTH_ABSORBING_BASE, factor.effectiveStrength / STRENGTH_ABSORBING_UNIT);
                case STRENGTH_ABSORBED:
                    damages[3] = damages[2] * Math.max(0.2F, 1.0F - factor.effectiveArmor * 0.04F);
                case ARMOR_ABSORBED:
                    damages[4] = Math.max(0, damages[3] - attributes[4]);
                case BLOCKED:
                    damages[5] = damages[4] * attributes[5];
                case FINAL:
            }
        } catch (Exception e) {
            throw new DamageException("Invalid Damage Factor during invoke of Damage.actFromWith");
        }
        return damages[5];
    }

    public float revert(Phase from, Phase to, float damage, Factor factor) {
        try {
            switch (from) {
                case FINAL:
                    if (to == Phase.FINAL) return damage;
                    damage = damage / attributes[5];
                case BLOCKED:
                    if (to == Phase.BLOCKED) return damage;
                    damage = damage + attributes[4];
                case ARMOR_ABSORBED:
                    if (to == Phase.ARMOR_ABSORBED) return damage;
                    damage = damage / (1.0F - Math.min(factor.armorAbsorptionCap, factor.effectiveArmor * 0.04F));
                case STRENGTH_ABSORBED:
                    if (to == Phase.STRENGTH_ABSORBED) return damage;
                    damage = damage / (float) Math.pow(STRENGTH_ABSORBING_BASE, factor.effectiveStrength / STRENGTH_ABSORBING_UNIT);
                case REDUCED:
                    if (to == Phase.REDUCED) return damage;
                    damage = damage + attributes[3];
                case RAW:
                    if (to == Phase.RAW) return damage;
            }
        } catch (Exception e) {
            throw new DamageException("Invalid Damage Factor during invoke of Damage.revert");
        }
        return -1.0F;
    }

    public static class Factor {
        public float toughnessFactor;
        public float effectiveStrength;
        public float effectiveArmor;
        public float armorAbsorptionCap;

        public Factor() {
            toughnessFactor = 0.0F;
            effectiveStrength = 0.0F;
            effectiveArmor = 0.0F;
            armorAbsorptionCap = 0.8F;
        }

        public void clear() {
            toughnessFactor = 0.0F;
            effectiveStrength = 0.0F;
            effectiveArmor = 0.0F;
            armorAbsorptionCap = 0.8F;
        }
    }

    public enum Phase {
        RAW(0),
        REDUCED(1),
        STRENGTH_ABSORBED(2),
        ARMOR_ABSORBED(3),
        BLOCKED(4),
        FINAL(5);

        private final int index;

        Phase(int i) {
            index = i;
        }
    }
}
