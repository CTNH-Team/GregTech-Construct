package slimeknights.tconstruct.library.modifiers.modules.carriage;

/**
 * Exposes FloatBuilder internal fields for the TCAE port.
 */
public interface IFloatBuilderExtra {
    float getBase();

    float getAdd();

    float getTotal();

    float getWeights();

    float getPercent();

    float getMultiply();

    void setBase(float f);

    void setAdd(float f);

    void setTotal(float f);

    void setWeights(float f);

    void setPercent(float f);

    void setMultiply(float f);
}
