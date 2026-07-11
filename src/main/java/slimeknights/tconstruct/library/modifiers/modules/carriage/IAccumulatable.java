package slimeknights.tconstruct.library.modifiers.modules.carriage;

import slimeknights.tconstruct.library.modifiers.ModifierId;

import javax.annotation.Nullable;

public interface IAccumulatable<T extends IAccumulator> {
    IAccumulatorManager<T> getManager();
}
