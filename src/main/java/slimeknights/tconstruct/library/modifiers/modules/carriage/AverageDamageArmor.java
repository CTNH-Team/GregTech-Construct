package slimeknights.tconstruct.library.modifiers.modules.carriage;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import oftenoviour.util.formula.IFormula;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record AverageDamageArmor(boolean direct, IFormula accumulator, IFormula finalizer, IFormula shareRatio)
        implements Carriage, IAccumulatable<AverageDamageArmor.Accumulator> {

    public static final RecordLoadable<AverageDamageArmor> LOADER = RecordLoadable.create(
        BooleanLoadable.INSTANCE.defaultField("direct", false, AverageDamageArmor::direct),
        FormulaUtil.FORMULA.nullableField("accumulator", AverageDamageArmor::accumulator),
        FormulaUtil.FORMULA.requiredField("finalizer", AverageDamageArmor::finalizer),
        FormulaUtil.FORMULA.nullableField("per_armor_ratio", AverageDamageArmor::shareRatio),
        AverageDamageArmor::new
    );

    public static class Accumulator implements IAccumulator {
        private boolean init;

        private boolean finalized;
        private boolean direct;
        private IContext context;
        private double[] parameter;

        private float value = 0;
        private final List<IToolStackView> armors = new ArrayList<>();

        IFormula formula;
        IFormula shareFormula;

        @Override public void apply() {
            if (!init || !finalized || armors.isEmpty()) return;

            value = FormulaUtil.accept(formula, value, parameter);
            float ratio = shareFormula != null ? FormulaUtil.accept(shareFormula, 1, armors.size()) : 1.0f / armors.size();
            if (value <= 0 || ratio <= 0) return;

            float perArmor = value * ratio;

            if (perArmor > 0) {
                LivingEntity holder = null;
                ItemStack stack = null;
                if (context != null){
                    holder = context.holder();
                    stack = context.stack();
                }

                int real = (int) perArmor;
                for (var armor : armors) {
                    int finalDmg = real + (perArmor - real > TConstruct.RANDOM.nextDouble() ? 1 : 0);
                    if (direct)
                        ToolDamageUtil.directDamage(armor, finalDmg, holder, stack);
                    else
                        ToolDamageUtil.damage(armor, finalDmg, holder, stack);
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

    @Override public String type() { return "average_damage_armor"; }
    @Override public int defaultParamCount() { return 1; }
    @Override public IAccumulatorManager<Accumulator> getManager() { return MANAGER; }

    @Override
    public void loop(IToolStackView tool, ModifierEntry modifier,
                     @Nullable IContext context, double[] parentParams,
                     @Nullable AccumulatorHandler handler) {
        if (handler == null) return;
        if (handler.tryAdd(this)) {
            capture(parentParams.length, context);
        }
        var acc = MANAGER.current(this);
        acc.armors.add(tool);

        if (accumulator == null) return;
        acc.parameter[0] = acc.value;
        System.arraycopy(parentParams, 0, acc.parameter, 1, parentParams.length);
        acc.value = FormulaUtil.accept(accumulator, acc.value, acc.parameter);
    }

    @Override
    public void finalize(double[] parentParams){
        var acc = MANAGER.current(this);
        acc.finalized = true;
        acc.parameter[0] = acc.value;
        acc.parameter = new double[1 + parentParams.length];
        System.arraycopy(parentParams, 0, acc.parameter, 1, parentParams.length);
    }

    private void capture(int extraLen, IContext context){
        var acc = MANAGER.current(this);
        acc.init = true;

        acc.parameter = new double[1 + extraLen];

        acc.context = context;
        acc.direct = direct;
        acc.formula = finalizer;
        acc.shareFormula = shareRatio;
    }
}
