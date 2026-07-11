package slimeknights.tconstruct.library.modifiers.modules.carriage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AccumulatorHandler {
    private final Set<IAccumulatable<?>> met = new LinkedHashSet<>(7);
    private final List<IAccumulatorManager.IAccumulatorStack<?>> accList = new ArrayList<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean tryAdd(IAccumulatable<?> aee) {
        if (!met.add(aee)) return false;
        var mgr = (IAccumulatorManager) aee.getManager();
        accList.add(mgr.add(aee));
        return true;
    }

    public void applyAll() {
        for (var acc : accList)
            acc.apply();
        accList.clear();
    }
}
