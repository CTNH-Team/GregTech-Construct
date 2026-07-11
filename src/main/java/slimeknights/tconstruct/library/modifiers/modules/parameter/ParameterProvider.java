package slimeknights.tconstruct.library.modifiers.modules.parameter;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.modules.carriage.IContext;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;

public interface ParameterProvider {
    ParameterProvider EMPTY = new ParameterProvider() {
        @Override public String type() { return ""; }
        @Override public int fieldCount(int current) { return current; }
        @Override public boolean isPersistent() { return false; }
    };

    String type();

    int fieldCount(int current);

    boolean isPersistent();

    default void pushTo(Parameter param, IToolStackView tool, ModifierEntry modifier,
                @Nullable IContext context, boolean isFinalizer) {}

    default void pushTo(Parameter param, IToolContext tool, ModifierEntry modifier,
                @Nullable IContext context, boolean isFinalizer) {}
}
