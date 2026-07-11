package slimeknights.tconstruct.library.modifiers.modules.armor;

import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.CarrierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.carriage.Carriage;
import slimeknights.tconstruct.library.modifiers.modules.carriage.CarriageLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;

import javax.annotation.Nullable;
import java.util.List;

public record CarrierModule(
    String name,
    List<Carriage> carriages
) implements ModifierModule, CarrierHook {

    public CarrierModule {
        carriages = carriages.stream().filter(c -> c != Carriage.EMPTY).toList();
    }

    public static final RecordLoadable<CarrierModule> LOADER = RecordLoadable.create(
        StringLoadable.DEFAULT.requiredField("name", CarrierModule::name),
        CarriageLoadable.INSTANCE.list(1).requiredField("carriages", CarrierModule::carriages),
        CarrierModule::new
    );

    private static final List<ModuleHook<?>> DEFAULT_HOOKS =
        HookProvider.defaultHooks(ModifierHooks.CARRIER);

    @Override public RecordLoadable<CarrierModule> getLoader() { return LOADER; }
    @Override public List<ModuleHook<?>> getDefaultHooks() { return DEFAULT_HOOKS; }

    @Override
    @Nullable
    public List<Carriage> getCarriages(String queryName) {
        return name.equals(queryName) ? carriages : null;
    }
}
