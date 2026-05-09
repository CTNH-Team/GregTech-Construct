package slimeknights.tconstruct.data;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.OrCondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;

public final class DynamicConditionSerializerRegistrar {
  private DynamicConditionSerializerRegistrar() {}

  public static void registerCommonSerializers() {
    tryRegister(OrCondition.Serializer.INSTANCE);
    tryRegister(AndCondition.Serializer.INSTANCE);
    tryRegister(ConfigEnabledCondition.SERIALIZER);
    tryRegister(TagFilledCondition.SERIALIZER);
  }

  private static void tryRegister(IConditionSerializer<?> serializer) {
    try {
      CraftingHelper.register(serializer);
    } catch (IllegalStateException exception) {
      String expectedMessage = "Duplicate recipe condition serializer: " + serializer.getID();
      if (!expectedMessage.equals(exception.getMessage())) {
        throw exception;
      }
    }
  }
}
