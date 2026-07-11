package slimeknights.tconstruct.library.modifiers.modules.carriage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import oftenoviour.util.formula.IFormula;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;

public record DamageTool(boolean direct, IFormula formula) implements Carriage {
    public static final RecordLoadable<DamageTool> LOADER = RecordLoadable.create(
        BooleanLoadable.INSTANCE.defaultField("direct", false, DamageTool::direct),
        FormulaUtil.FORMULA.requiredField("formula", DamageTool::formula),
        DamageTool::new
    );

    @Override public String type() { return "damage_tool"; }
    @Override public int defaultParamCount() { return 0; }

    @Override
    public void loop(IToolStackView tool, ModifierEntry modifier,
                     @Nullable IContext context, double[] parentParams,
                     @Nullable AccumulatorHandler handler) {
        LivingEntity holder = null;
        ItemStack stack = null;
        if (context != null) {
            holder = context.holder();
            stack = context.stack();
        }
        float dmg = FormulaUtil.accept(formula, 0, parentParams);
        int real = (int) dmg;
        int finalDmg = real + (dmg - real > TConstruct.RANDOM.nextDouble() ? 1 : 0);
        if (finalDmg > 0) {
            if (direct)
                ToolDamageUtil.directDamage(tool, finalDmg, holder, stack);
            else
                ToolDamageUtil.damage(tool, finalDmg, holder, stack);
        }
    }
}
