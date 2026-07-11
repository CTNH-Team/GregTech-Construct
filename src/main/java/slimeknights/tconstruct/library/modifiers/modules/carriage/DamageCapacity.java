package slimeknights.tconstruct.library.modifiers.modules.carriage;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.modules.capacity.StatCapacityBarManager;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import oftenoviour.util.formula.IFormula;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;

public record DamageCapacity(ModifierId id, IFormula formula) implements Carriage {
    public static final RecordLoadable<DamageCapacity> LOADER = RecordLoadable.create(
        ModifierId.PARSER.requiredField("id", DamageCapacity::id),
        FormulaUtil.FORMULA.requiredField("formula", DamageCapacity::formula),
        DamageCapacity::new
    );

    @Override public String type() { return "damage_capacity"; }
    @Override public int defaultParamCount() { return 0; }

    @Override
    public void loop(IToolStackView tool, ModifierEntry modifier,
                     @Nullable IContext context, double[] parentParams,
                     @Nullable AccumulatorHandler handler) {
        var overshield = StatCapacityBarManager.getCapacityBar(id);
        if (overshield == null) return;
        float dmg = FormulaUtil.accept(formula, 0, parentParams);
        int real = (int) dmg;
        int finalDmg = real + (dmg - real > TConstruct.RANDOM.nextDouble() ? 1 : 0);
        if (finalDmg > 0)
            overshield.setAmount(tool, modifier, overshield.getAmount(tool) - finalDmg);
    }
}
