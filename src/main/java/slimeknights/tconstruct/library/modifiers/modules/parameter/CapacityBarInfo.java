package slimeknights.tconstruct.library.modifiers.modules.parameter;

import net.minecraft.world.entity.LivingEntity;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.modifiers.modules.capacity.StatCapacityBarManager;
import slimeknights.tconstruct.library.modifiers.modules.carriage.Damage;
import slimeknights.tconstruct.library.modifiers.modules.carriage.IContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

public record CapacityBarInfo(ModifierId id, @Nullable List<Field> expected) implements ParameterProvider {
    public enum Field { CAPACITY, AMOUNT }

    private static final List<Field> ALL = List.of(Field.values());
    private static final Loadable<Field> FIELD_LOADABLE = StringLoadable.DEFAULT.flatXmap(
        s -> Field.valueOf(s.toUpperCase()), f -> f.name().toLowerCase());
    private static final Loadable<List<Field>> FIELD_LIST = FIELD_LOADABLE.list(0);

    public static final RecordLoadable<CapacityBarInfo> LOADER = RecordLoadable.create(
        ModifierId.PARSER.requiredField("id", CapacityBarInfo::id),
        FIELD_LIST.nullableField("expected", CapacityBarInfo::expected),
        CapacityBarInfo::new
    );

    @Override public String type() { return "capacity_bar"; }
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
        LivingEntity holder = null;
        Damage damage = null;
        List<Field> fields = expected != null ? expected : ALL;
        if (fields.isEmpty()) return;
        boolean persist = isPersistent();
        CapacityBarHook overshield = StatCapacityBarManager.getCapacityBar(id);
        for (Field f : fields) {
            if (isFinalizer && !persist) {
                p.push(() -> 0, false);
            } else if (overshield == null) {
                p.push(() -> 0, persist);
            } else switch (f) {
                case CAPACITY -> p.push(() -> overshield.getCapacity(tool, modifier), persist);
                case AMOUNT -> p.push(() -> overshield.getAmount(tool), persist);
            }
        }
    }
}
