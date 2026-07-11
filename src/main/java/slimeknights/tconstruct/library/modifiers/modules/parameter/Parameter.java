package slimeknights.tconstruct.library.modifiers.modules.parameter;

import slimeknights.tconstruct.library.exception.ParameterException;

import java.util.Arrays;
import java.util.function.DoubleSupplier;

public class Parameter {
    protected final double[] values;
    protected int current;
    protected int hasBeen;

    public Parameter(int length){
        values = new double[length];
        current = 0;
        hasBeen = 0;
    }

    public void push(DoubleSupplier method, boolean isPersistent){
        if (current >= values.length)
            throw new ParameterException("Parameter overflowed! Expect " + values.length + " values but there have been " + (current + 1) + " values");
        if (current < hasBeen) {
            if (isPersistent){
                current++;
                return;
            }
        }
        else hasBeen++;
        values[current++] = method.getAsDouble();
    }

    public double[] get(){
        if (hasBeen != values.length)
            throw new ParameterException("Parameter is incomplete! Expect " + values.length + " values but there are only " + hasBeen + " values");
        return Arrays.copyOf(values, hasBeen);
    }

    public boolean tryCopy(double[] target, int offset){
        if (hasBeen != values.length)
            return false;
        System.arraycopy(values, 0, target, offset, hasBeen);
        return true;
    }

    public void reset(){
        current = 0;
    }

    public void clean(){
        current = 0;
        hasBeen = 0;
    }
}
