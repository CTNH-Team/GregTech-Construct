package slimeknights.tconstruct.library.modifiers.modules.parameter;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.modules.carriage.IContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;

import javax.annotation.Nullable;
import java.util.List;

public record DurabilityInfo(@Nullable List<Field> expected) implements ParameterProvider {
    public enum Field { CURRENT_DURABILITY, DURABILITY }

    private static final List<Field> ALL = List.of(Field.values());
    private static final Loadable<Field> FIELD_LOADABLE = StringLoadable.DEFAULT.flatXmap(
        s -> Field.valueOf(s.toUpperCase()), f -> f.name().toLowerCase());
    private static final Loadable<List<Field>> FIELD_LIST = FIELD_LOADABLE.list(0);

    public static final RecordLoadable<DurabilityInfo> LOADER = RecordLoadable.create(
        FIELD_LIST.nullableField("expected", DurabilityInfo::expected),
        DurabilityInfo::new
    );

    @Override public String type() { return "durability"; }
    @Override public boolean isPersistent() { return false; }

    @Override
    public int fieldCount(int current) {
        List<Field> fields = expected != null ? expected : ALL;
        return fields.isEmpty() ? current : current + fields.size();
    }

    @Override
    public void pushTo(Parameter p, IToolStackView tool, ModifierEntry modifier,
                       @Nullable IContext context,
                       boolean isFinalizer) {
        List<Field> fields = expected != null ? expected : ALL;
        if (fields.isEmpty()) return;
        boolean persist = isPersistent();
        for (Field f : fields) {
            if (isFinalizer && !persist) {
                p.push(() -> 0, false);
            } else switch (f) {
                case CURRENT_DURABILITY -> p.push(() -> tool.getCurrentDurability(), persist);
                case DURABILITY -> p.push(() -> tool.getStats().getInt(ToolStats.DURABILITY), persist);
            }
        }
    }
}
