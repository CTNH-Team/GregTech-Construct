package slimeknights.tconstruct.library.modifiers.modules.carriage;

import net.minecraft.util.Mth;
import slimeknights.tconstruct.library.tools.helper.ArmorUtil;

public class EventDamage extends Damage {
    private final float resistancePoint;
    private final float cap;

    public EventDamage(float point, float cap) {
        super();
        this.resistancePoint = point;
        this.cap = cap;
    }

    public EventDamage(EventDamage src) {
        super(src);
        this.resistancePoint = src.resistancePoint;
        this.cap = src.cap;
    }

    public EventDamage(Damage src, float point, float cap) {
        super(src);
        this.resistancePoint = point;
        this.cap = cap;
    }

    public float getFinalDamage() {
        return ArmorUtil.getDamageAfterMagicAbsorb(get(Phase.FINAL), resistancePoint, cap);
    }

    public float revert(Phase to, float damage, Factor factor) {
        float ef = getEnchantFactor();
        if (ef <= 0) return -1.0F;
        return super.revert(Phase.FINAL, to, damage / ef, factor);
    }

    public float revertEnchant(float damage) {
        float ef = getEnchantFactor();
        return ef > 0 ? damage / ef : -1.0F;
    }

    private float getEnchantFactor() {
        return (1.0F - Mth.clamp(resistancePoint, -20.0F, cap) / 25.0F);
    }
}
