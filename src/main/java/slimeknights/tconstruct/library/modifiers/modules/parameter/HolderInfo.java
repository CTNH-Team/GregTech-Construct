package slimeknights.tconstruct.library.modifiers.modules.parameter;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.modules.carriage.IContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

public record HolderInfo(@Nullable List<Field> expected) implements ParameterProvider {
    public enum Field { HEALTH, MAX_HEALTH }

    private static final List<Field> ALL = List.of(Field.values());
    private static final Loadable<Field> FIELD_LOADABLE = StringLoadable.DEFAULT.flatXmap(
        s -> Field.valueOf(s.toUpperCase()), f -> f.name().toLowerCase());
    private static final Loadable<List<Field>> FIELD_LIST = FIELD_LOADABLE.list(0);

    public static final RecordLoadable<HolderInfo> LOADER = RecordLoadable.create(
        FIELD_LIST.nullableField("expected", HolderInfo::expected),
        HolderInfo::new
    );

    @Override public String type() { return "holder"; }
    @Override public boolean isPersistent() { return true; }

    @Override
    public int fieldCount(int current) {
        List<Field> fields = expected != null ? expected : ALL;
        return fields.isEmpty() ? current : current + fields.size();
    }

    @Override
    public void pushTo(Parameter p, IToolStackView tool, ModifierEntry modifier,
                       @Nullable IContext context,
                       boolean isFinalizer) {
        LivingEntity holder = context != null ? context.holder() : null;
        List<Field> fields = expected != null ? expected : ALL;
        if (fields.isEmpty()) return;
        boolean persist = isPersistent();
        for (Field f : fields) {
            if (isFinalizer && !persist) {
                p.push(() -> 0, false);
            } else if (holder == null) {
                p.push(() -> 0, persist);
            } else switch (f) {
                case HEALTH -> p.push(holder::getHealth, persist);
                case MAX_HEALTH -> p.push(holder::getMaxHealth, persist);
            }
        }
    }
}
