package slimeknights.tconstruct.library.modifiers.modules.armor;

import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ParameterProviderHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.parameter.ParameterProvider;
import slimeknights.tconstruct.library.modifiers.modules.parameter.ParameterProviderLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;

import java.util.List;

public record ParameterProviderModule(
    String name,
    List<ParameterProvider> providers
) implements ModifierModule, ParameterProviderHook {

    public ParameterProviderModule {
        providers = providers.stream().filter(p -> p != ParameterProvider.EMPTY).toList();
    }

    public static final RecordLoadable<ParameterProviderModule> LOADER = RecordLoadable.create(
        StringLoadable.DEFAULT.requiredField("name", ParameterProviderModule::name),
        ParameterProviderLoadable.INSTANCE.list(1).requiredField("providers", ParameterProviderModule::providers),
        ParameterProviderModule::new
    );

    private static final List<ModuleHook<?>> DEFAULT_HOOKS =
        HookProvider.defaultHooks(ModifierHooks.PARAMETER_PROVIDER);

    @Override public RecordLoadable<ParameterProviderModule> getLoader() { return LOADER; }
    @Override public List<ModuleHook<?>> getDefaultHooks() { return DEFAULT_HOOKS; }

    @Override
    public void collectProviders(String name, List<ParameterProvider> out) {
        if (this.name.equals(name))
            out.addAll(providers);
    }
}
