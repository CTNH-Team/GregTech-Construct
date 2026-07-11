package slimeknights.tconstruct.library.modifiers.modules.carriage;

import net.minecraft.resources.ResourceLocation;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.tconstruct.library.exception.FormulaException;

import javax.annotation.Nullable;
import java.util.Arrays;

public class FormulaUtil {
    private FormulaUtil() {}

    public static final Loadable<IFormula> FORMULA = StringLoadable.DEFAULT.flatComap(
            FormulaUtil::resolve,
            (f, err) -> ""
        );

    public static IFormula resolve(@Nullable String id){
        if (id == null)
            return null;
        return resolve(ResourceLocation.tryParse(id));
    }

    public static IFormula resolve(@Nullable ResourceLocation id) {
        if (id == null)
            return null;
        return FormulaManager.getOrNull(id);
    }

    /** @return defaultId if formula not found */
    public static IFormula get(ResourceLocation id, ResourceLocation defaultId) {
        if (id == null)
            return null;
        IFormula f = FormulaManager.getOrNull(id);
        return f != null ? f : FormulaManager.getOrNull(defaultId);
    }

    public static float accept(IFormula formula, float fallback, double... inputs) {
        if (formula == null) return fallback;
        return (float) formula.accept(Arrays.copyOf(inputs, inputs.length));
    }

    public static float accept(ResourceLocation id, float fallback, double... inputs) {
        if (id == null) return fallback;
        try {
            return accept(FormulaManager.getOrNull(id), fallback, inputs);
        } catch (Exception e){
            throw new FormulaException("Break while running formula " + id.toString());
        }
    }

    public static int evalInt(IFormula formula, int fallback, double... inputs) {
        if (formula == null) return fallback;
        return (int) Math.round(formula.accept(Arrays.copyOf(inputs, inputs.length)));
    }

    public static int evalInt(ResourceLocation id, int fallback, double... inputs) {
        if (id == null) return fallback;
        try {
            return evalInt(FormulaManager.getOrNull(id), fallback, inputs);
        } catch (Exception e){
            throw new FormulaException("Break while running formula " + id.toString());
        }
    }

    /** RecordLoadable helper: load formula field, resolve lazily. */
    @FunctionalInterface
    public interface FormulaSupplier {
        IFormula get();
    }

    public static FormulaSupplier lazy(ResourceLocation id) {
        return new FormulaSupplier() {
            private IFormula cache;
            @Override
            public IFormula get() {
                if (cache == null) cache = FormulaManager.getOrNull(id);
                return cache;
            }
        };
    }
}
