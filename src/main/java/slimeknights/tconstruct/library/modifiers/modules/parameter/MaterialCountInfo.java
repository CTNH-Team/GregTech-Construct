package slimeknights.tconstruct.library.modifiers.modules.parameter;

import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.modules.carriage.IContext;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

public record MaterialCountInfo(@Nullable List<Field> expected) implements ParameterProvider {
    public enum Field { COUNT }

    private static final List<Field> ALL = List.of(Field.values());
    private static final Loadable<Field> FIELD_LOADABLE = StringLoadable.DEFAULT.flatXmap(
        s -> Field.valueOf(s.toUpperCase()), f -> f.name().toLowerCase());
    private static final Loadable<List<Field>> FIELD_LIST = FIELD_LOADABLE.list(0);

    public static final RecordLoadable<MaterialCountInfo> LOADER = RecordLoadable.create(
        FIELD_LIST.nullableField("expected", MaterialCountInfo::expected),
        MaterialCountInfo::new
    );

    @Override public String type() { return "material_count"; }
    @Override public boolean isPersistent() { return true; }

    @Override
    public int fieldCount(int current) {
        List<Field> fields = expected != null ? expected : ALL;
        return fields.isEmpty() ? current : current + fields.size();
    }

    @Override
    public void pushTo(Parameter p, IToolStackView tool, ModifierEntry modifier,
                       @Nullable IContext context, boolean isFinalizer) {
        pushTo(p, (IToolContext)tool, modifier, context, isFinalizer);
    }

    @Override
    public void pushTo(Parameter p, IToolContext tool, ModifierEntry modifier,
                       @Nullable IContext context, boolean isFinalizer) {
        List<Field> fields = expected != null ? expected : ALL;
        if (fields.isEmpty()) return;
        boolean persist = isPersistent();
        p.push(() -> {
            var materials = tool.getMaterials();
            var ids = new HashSet<MaterialId>();
            for (var mv : materials.getList())
                ids.add(mv.getVariant().getId());
            return ids.size();
        }, persist);
    }
}
