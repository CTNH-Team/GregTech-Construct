package slimeknights.tconstruct.library.modifiers.modules.carriage;

import slimeknights.tconstruct.library.exception.RespchainException;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

abstract public class AbstractAccumulatorManager<T extends IAccumulator> implements IAccumulatorManager<T> {
    protected final Map<IAccumulatable<?>, IAccumulatorStack<T>> map;

    protected AbstractAccumulatorManager() { map = new HashMap<>(); }

    abstract public IAccumulatorStack<T> add(IAccumulatable<T> key);
    abstract public T current(IAccumulatable<T> key);

    abstract public static class AbstractAccumulatorStack<T extends IAccumulator> implements IAccumulatorStack<T> {
        protected final Stack<T> stack;

        protected AbstractAccumulatorStack() { stack = new Stack<>(); }

        abstract public void add();

        public T current() {
            if (stack.isEmpty())
                throw new RespchainException("No accumulator in manager.");
            return stack.peek();
        }

        public void apply() {
            if (!stack.isEmpty())
                stack.pop().apply();
        }
    }
}
