package slimeknights.tconstruct.library.tools.helper;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.shared.TinkerAttributes;

public class AttributeEntry {
    // armor, toughness, strength, preReduction, postReduction, protection
    protected final float[][] attributes = new float[3][6];

    public AttributeEntry() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 6; j++)
                attributes[i][j] = i == 2 ? 1 : 0;
        }
        attributes[0][5] = 1.0F;
    }

    public AttributeEntry read(DamageSource src, LivingEntity living) {
        if (!src.is(DamageTypeTags.BYPASSES_ARMOR)) {
            var armorInst = living.getAttribute(Attributes.ARMOR);
            attributes[0][0] = armorInst != null ? (float) armorInst.getValue() : 0;
            var toughnessInst = living.getAttribute(Attributes.ARMOR_TOUGHNESS);
            attributes[0][1] = toughnessInst != null ? (float) toughnessInst.getValue() : 0;
            var strengthInst = living.getAttribute(TinkerAttributes.ARMOR_STRENGTH.get());
            attributes[0][2] = strengthInst != null ? (float) strengthInst.getValue() : 0;
        }
        // preReduction
        if (!src.is(TinkerTags.DamageTypes.BYPASSES_REDUCTION)) {
            var preInst = living.getAttribute(TinkerAttributes.PRE_REDUCTION.get());
            attributes[0][3] = preInst != null ? (float) preInst.getValue() : 0;
        }
        // postReduction
        if (!src.is(TinkerTags.DamageTypes.BYPASSES_BLOCKING)) {
            var postInst = living.getAttribute(TinkerAttributes.POST_REDUCTION.get());
            attributes[0][4] = postInst != null ? (float) postInst.getValue() : 0;
        }
        // protection
        if (!src.is(TinkerTags.DamageTypes.BYPASSES_PROTECTION)) {
            var protInst = living.getAttribute(TinkerAttributes.ARMOR_PROTECTION.get());
            attributes[0][5] = protInst != null ? (float) protInst.getValue() : 1.0F;
        }

        return this;
    }

    public AttributeEntry accept(int from, float... values) {
        if (from > 5 || from < 0)
            return this;

        int ceiling = Math.min(6, values.length + from);
        System.arraycopy(values, 0, attributes[0], from, ceiling - from);
        return this;
    }

    public AttributeEntry act() {
        for (int j = 0; j < 5; j++) {
            if (attributes[0][j] < 0)
                attributes[0][j] = 0;
            if (attributes[1][j] < 0)
                attributes[1][j] = 0;
            if (attributes[2][j] < 0)
                attributes[2][j] = 0;

            attributes[0][j] *= (1 + attributes[1][j]) * attributes[2][j];
        }

        attributes[0][5] = (1 - Math.max(0, Math.min(1, (1 - attributes[0][5]) * (1 + attributes[1][5]) * attributes[2][5])));
        return this;
    }

    /** Unsafe for efficiency */
    public float get(int i, int j) {
        return attributes[i][j];
    }

    /** Unsafe for efficiency */
    public float get(int j) {
        return attributes[0][j];
    }

    public void set(int index, float value) {
        if (index < 0 || index > 5)
            return;

        attributes[0][index] += value;
    }

    public void setAt(int col, int index, float value) {
        if (col < 0 || col > 2 || index < 0 || index > 5)
            return;
        attributes[col][index] = value;
    }

    public void add(int index, float value) {
        if (index < 0 || index > 5)
            return;
        if (index == 5)
            value = -value;

        attributes[0][index] += value;
    }

    public void multiply_base(int index, float value) {
        if (index < 0 || index > 5)
            return;

        attributes[1][index] += value;
    }

    public void multiply_total(int index, float value) {
        if (index < 0 || index > 5)
            return;

        attributes[2][index] *= (1 + value);
    }
}
