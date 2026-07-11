package slimeknights.tconstruct.library.modifiers.modules.carriage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import oftenoviour.util.formula.IFormula;

import javax.annotation.Nullable;

public record HurtHolder(IFormula formula, IFormula accumulator, IFormula finalizer, @Nullable ResourceKey<DamageType> damageType)
        implements Carriage, IAccumulatable<HurtHolder.Accumulator> {

    public static final RecordLoadable<HurtHolder> LOADER = RecordLoadable.create(
        FormulaUtil.FORMULA.nullableField("formula", HurtHolder::formula),
        FormulaUtil.FORMULA.nullableField("accumulator", HurtHolder::accumulator),
        FormulaUtil.FORMULA.nullableField("finalizer", HurtHolder::finalizer),
        Loadables.DAMAGE_TYPE_KEY.nullableField("damage_type", HurtHolder::damageType),
        HurtHolder::new
    );

    public static class Accumulator implements IAccumulator {
        boolean init;
        private boolean finalized;

        LivingEntity holder;
        double[] parameter;

        float value = 0;

        IFormula formula;
        DamageSource source;

        @Override public void apply() {
            if (!init || !finalized) return;

            float dmg = FormulaUtil.accept(formula, 0, parameter);
            if (dmg <= 0) return;

            holder.hurt(source, dmg);
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

    @Override public String type() { return "hurt_holder"; }
    @Override public int defaultParamCount() { return 1; }
    @Override public IAccumulatorManager<Accumulator> getManager() { return MANAGER; }

    @Override
    public void loop(IToolStackView tool, ModifierEntry modifier,
                     @Nullable IContext context, double[] parentParams,
                     @Nullable AccumulatorHandler handler) {
        if (context == null) return;
        if (handler == null){
            float dmg = FormulaUtil.accept(accumulator, 0, parentParams);
            if (dmg > 0){
                var holder = context.holder();
                if (holder == null) return;
                holder.hurt(damageType == null ? holder.damageSources().generic() :
                        new DamageSource(holder.level().registryAccess()
                                .registryOrThrow(Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(damageType)), dmg);
            }
            return;
        }
        if (handler.tryAdd(this)) {
            capture(context, parentParams.length);
        }
        var acc = MANAGER.current(this);
        if (acc.holder == null) return;

        if (accumulator == null || finalizer == null) return;
        acc.parameter[0] = acc.value;
        System.arraycopy(parentParams, 0, acc.parameter, 1, parentParams.length);
        acc.value = FormulaUtil.accept(accumulator, acc.value, acc.parameter);
    }

    @Override
    public void finalize(double[] parentParams){
        if (finalizer == null) return;
        var acc = MANAGER.current(this);
        acc.finalized = true;
        acc.parameter = new double[1 + parentParams.length];
        acc.parameter[0] = acc.value;
        System.arraycopy(parentParams, 0, acc.parameter, 1, parentParams.length);
    }

    private void capture(IContext context, int length){
        var acc = MANAGER.current(this);
        if (acc.init) return;

        acc.holder = context.holder();
        if (acc.holder == null) return;
        if (finalizer == null) return;

        acc.init = true;
        acc.formula = finalizer;
        acc.parameter = new double[1 + length];
        acc.source = damageType == null ? acc.holder.damageSources().generic() :
                new DamageSource(acc.holder.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(damageType));
    }
}
