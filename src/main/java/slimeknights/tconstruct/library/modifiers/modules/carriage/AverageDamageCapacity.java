package slimeknights.tconstruct.library.modifiers.modules.carriage;

import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.modules.capacity.StatCapacityBarManager;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import oftenoviour.util.formula.IFormula;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record AverageDamageCapacity(ModifierId id, IFormula accumulator, IFormula finalizer, IFormula shareRatio)
        implements Carriage, IAccumulatable<AverageDamageCapacity.Accumulator> {

    public static final RecordLoadable<AverageDamageCapacity> LOADER = RecordLoadable.create(
        ModifierId.PARSER.requiredField("id", AverageDamageCapacity::id),
        FormulaUtil.FORMULA.nullableField("accumulator", AverageDamageCapacity::accumulator),
        FormulaUtil.FORMULA.requiredField("finalizer", AverageDamageCapacity::finalizer),
        FormulaUtil.FORMULA.nullableField("per_armor_ratio", AverageDamageCapacity::shareRatio),
        AverageDamageCapacity::new
    );

    public static class Accumulator implements IAccumulator {
        private boolean init;
        private boolean finalized;

        double[] parameter;
        private ModifierId id;

        private float value = 0;
        private final List<IToolStackView> armors = new ArrayList<>();

        IFormula formula;
        IFormula shareFormula;

        @Override public void apply() {
            if (!init || !finalized) return;
            var overshield = StatCapacityBarManager.getCapacityBar(id);
            if (overshield == null) return;

            value = FormulaUtil.accept(formula, value, parameter);
            float ratio = shareFormula != null ? FormulaUtil.accept(shareFormula, 1, armors.size()) : 1.0f / armors.size();
            if (value <= 0 || ratio <= 0) return;

            if (armors.isEmpty()) return;
            float perArmor = value * ratio;

            if (perArmor > 0) {
                int real = (int) perArmor;
                for (var armor : armors) {
                    int before = overshield.getAmount(armor);
                    int finalDmg = real + (perArmor - real > TConstruct.RANDOM.nextDouble() ? 1 : 0);
                    overshield.setAmount(armor, ModifierEntry.EMPTY, before - finalDmg);
                }
            }
        }
    }

    private static class Stack extends AbstractAccumulatorManager.AbstractAccumulatorStack<Accumulator> {
        @Override public void add() { stack.push(new Accumulator()); }
    }

    private static final IAccumulatorManager<Accumulator> MANAGER = new AbstractAccumulatorManager<>() {
        @Override public IAccumulatorStack<Accumulator> add(IAccumulatable<Accumulator> key) {
            var s = map.computeIfAbsent(key, k -> new Stack());
            s.add();
            return s;
        }
        @Override public Accumulator current(IAccumulatable<Accumulator> key) { return map.get(key).current(); }
    };

    @Override public String type() { return "average_damage_capacity"; }
    @Override public int defaultParamCount() { return 1; }
    @Override public IAccumulatorManager<Accumulator> getManager() { return MANAGER; }

    @Override
    public void loop(IToolStackView tool, ModifierEntry modifier,
                     @Nullable IContext context, double[] parentParams,
                     @Nullable AccumulatorHandler handler) {
        if (handler == null) return;
        if (handler.tryAdd(this)) {
            capture(parentParams.length);
        }
        var acc = MANAGER.current(this);
        acc.armors.add(tool);

        if (accumulator == null) return;
        acc.parameter[0] = acc.value;
        System.arraycopy(parentParams, 0, acc.parameter, 1, parentParams.length);
        acc.value = FormulaUtil.accept(accumulator, acc.value, acc.parameter);
    }

    @Override
    public void finalize(double[] parentParams) {
        var acc = MANAGER.current(this);
        acc.finalized = true;
        acc.parameter[0] = acc.value;
        acc.parameter = new double[defaultParamCount() + parentParams.length];
        System.arraycopy(parentParams, 0, acc.parameter, 1, parentParams.length);
    }

    private void capture(int extraLen) {
        var acc = MANAGER.current(this);

        acc.init = true;
        acc.id = this.id;

        acc.parameter = new double[1 + extraLen];

        acc.formula = finalizer;
        acc.shareFormula = shareRatio;
    }
}
