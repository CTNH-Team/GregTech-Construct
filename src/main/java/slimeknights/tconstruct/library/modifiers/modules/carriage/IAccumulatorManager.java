package slimeknights.tconstruct.library.modifiers.modules.carriage;

public interface IAccumulatorManager<T extends IAccumulator> {
    IAccumulatorStack<T> add(IAccumulatable<T> key);
    T current(IAccumulatable<T> key);

    interface IAccumulatorStack<T extends IAccumulator> {
        void add();
        T current();
        void apply();
    }
}
