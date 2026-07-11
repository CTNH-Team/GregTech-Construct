package slimeknights.tconstruct.library.modifiers.modules.carriage;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;

public interface Carriage {
    Carriage EMPTY = new Carriage() {
        @Override public String type() { return ""; }
        @Override public int defaultParamCount() { return 0; }
        @Override public void loop(IToolStackView tool, ModifierEntry modifier,
                                   @Nullable IContext context, double[] parentParams,
                                   @Nullable AccumulatorHandler handler) {}
        @Override public void finalize(double[] parentParams) {}
    };

    String type();

    int defaultParamCount();

    void loop(IToolStackView tool, ModifierEntry modifier,
              @Nullable IContext context, double[] parentParams,
              @Nullable AccumulatorHandler handler);

    default void finalize(double[] parentParams) {}
}
