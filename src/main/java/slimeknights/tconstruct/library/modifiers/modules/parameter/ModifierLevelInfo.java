package slimeknights.tconstruct.library.modifiers.modules.parameter;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.modules.carriage.IContext;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;

import javax.annotation.Nullable;
import java.util.List;

public record ModifierLevelInfo(ModifierId id, @Nullable List<Field> expected) implements ParameterProvider {
    public enum Field { LEVEL }

    private static final List<Field> ALL = List.of(Field.values());
    private static final Loadable<Field> FIELD_LOADABLE = StringLoadable.DEFAULT.flatXmap(
        s -> Field.valueOf(s.toUpperCase()), f -> f.name().toLowerCase());
    private static final Loadable<List<Field>> FIELD_LIST = FIELD_LOADABLE.list(0);

    public static final RecordLoadable<ModifierLevelInfo> LOADER = RecordLoadable.create(
        ModifierId.PARSER.requiredField("id", ModifierLevelInfo::id),
        FIELD_LIST.nullableField("expected", ModifierLevelInfo::expected),
        ModifierLevelInfo::new
    );

    @Override public String type() { return "modifier_level"; }
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
        pushTo(p, (IToolContext)tool, modifier, context, isFinalizer);
    }

    @Override
    public void pushTo(Parameter p, IToolContext tool, ModifierEntry modifier,
                       @Nullable IContext context,
                       boolean isFinalizer) {
        List<Field> fields = expected != null ? expected : ALL;
        if (fields.isEmpty()) return;
        boolean persist = isPersistent();
        for (Field f : fields) {
            if (isFinalizer && !persist) {
                p.push(() -> 0, false);
            } else {
                var entries = tool.getModifierList();
                float level = 0;
                for (var entry : entries) {
                    if (entry.getId().equals(id)) {
                        level = entry.getEffectiveLevel();
                        break;
                    }
                }
                float lvl = level;
                p.push(() -> lvl, persist);
            }
        }
    }
}
