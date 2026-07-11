package slimeknights.tconstruct.library.modifiers.modules.parameter;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.modules.carriage.Damage;
import slimeknights.tconstruct.library.modifiers.modules.carriage.IContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

public record DamageInfo(@Nullable List<Field> expected) implements ParameterProvider {
    public enum Field { RAW, REDUCED, STRENGTH_ABSORBED, ARMOR_ABSORBED, BLOCKED, FINAL }

    private static final List<Field> ALL = List.of(Field.values());
    private static final Loadable<Field> FIELD_LOADABLE = StringLoadable.DEFAULT.flatXmap(
        s -> Field.valueOf(s.toUpperCase()), f -> f.name().toLowerCase());
    private static final Loadable<List<Field>> FIELD_LIST = FIELD_LOADABLE.list(0);

    public static final RecordLoadable<DamageInfo> LOADER = RecordLoadable.create(
        FIELD_LIST.nullableField("expected", DamageInfo::expected),
        DamageInfo::new
    );

    @Override public String type() { return "damage"; }
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
        Damage damage = context != null ? context.damage() : null;
        List<Field> fields = expected != null ? expected : ALL;
        if (fields.isEmpty()) return;
        boolean persist = isPersistent();
        if (damage == null) {
            for (Field ignored : fields) {
                if (isFinalizer && !persist) p.push(() -> 0, false);
                else p.push(() -> 0, persist);
            }
            return;
        }
        if (fields.size() >= 3) {
            float[] all = damage.get();
            for (Field f : fields) {
                if (isFinalizer && !persist) {
                    p.push(() -> 0, false);
                } else {
                    int idx = f.ordinal();
                    p.push(() -> all[idx], persist);
                }
            }
        } else {
            float[] all = damage.get();
            for (Field f : fields) {
                if (isFinalizer && !persist) {
                    p.push(() -> 0, false);
                } else {
                    int idx = f.ordinal();
                    p.push(() -> all[idx], persist);
                }
            }
        }
    }
}
