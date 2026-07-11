package slimeknights.tconstruct.library.modifiers.hook.armor;

import slimeknights.tconstruct.library.modifiers.modules.carriage.Carriage;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface CarrierHook {
    @Nullable
    List<Carriage> getCarriages(String name);

    record AllMerger(Collection<CarrierHook> modules) implements CarrierHook {
        @Override
        public List<Carriage> getCarriages(String name) {
            for (var m : modules) {
                var c = m.getCarriages(name);
                if (c != null) return c;
            }
            return null;
        }
    }
}
