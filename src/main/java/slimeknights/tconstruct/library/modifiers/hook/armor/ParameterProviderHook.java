package slimeknights.tconstruct.library.modifiers.hook.armor;

import slimeknights.tconstruct.library.modifiers.modules.parameter.ParameterProvider;

import java.util.Collection;
import java.util.List;

public interface ParameterProviderHook {
    void collectProviders(String name, List<ParameterProvider> out);

    record AllMerger(Collection<ParameterProviderHook> modules) implements ParameterProviderHook {
        @Override
        public void collectProviders(String name, List<ParameterProvider> out) {
            for (var m : modules)
                m.collectProviders(name, out);
        }
    }
}
