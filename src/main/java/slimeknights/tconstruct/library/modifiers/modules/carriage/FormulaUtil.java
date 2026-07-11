package slimeknights.tconstruct.library.modifiers.modules.carriage;

import net.minecraft.resources.ResourceLocation;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Ported from TCAE's FormulaUtil.
 * Provides a Loadable for formula fields and a null-safe evaluation helper.
 */
public final class FormulaUtil {
    private FormulaUtil() {}

    /** Loadable that resolves a formula string ID to an {@link IFormula} via {@link FormulaManager}. */
    public static final Loadable<IFormula> FORMULA = StringLoadable.DEFAULT.flatComap(
        s -> {
            if (s == null || s.isEmpty()) return null;
            ResourceLocation id = ResourceLocation.tryParse(s);
            return id != null ? FormulaManager.getOrNull(id) : null;
        },
        f -> ""
    );

    /** Null-safe formula evaluation. Returns {@code fallback} if the formula is null. */
    public static float accept(@Nullable IFormula formula, float fallback, double... inputs) {
        if (formula == null) return fallback;
        return (float) formula.accept(Arrays.copyOf(inputs, inputs.length));
    }
}
