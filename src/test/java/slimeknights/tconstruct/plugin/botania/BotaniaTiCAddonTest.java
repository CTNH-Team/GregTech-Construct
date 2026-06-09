package slimeknights.tconstruct.plugin.botania;

import net.minecraftforge.common.crafting.CraftingHelper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.plugin.botania.recipe.TerrasteelHelmetPlatingIngredient;
import slimeknights.tconstruct.test.BaseMcTest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BotaniaTiCAddonTest extends BaseMcTest {
  @Test
  void ingredientSerializerRegistrationIsIdempotent() throws ReflectiveOperationException {
    boolean wasRegistered = isIngredientSerializerRegistered();
    setIngredientSerializerRegistered(false);
    try (MockedStatic<CraftingHelper> craftingHelper = Mockito.mockStatic(CraftingHelper.class)) {
      TerrasteelHelmetPlatingIngredient.register();
      TerrasteelHelmetPlatingIngredient.register();

      craftingHelper.verify(
        () -> CraftingHelper.register(
          TerrasteelHelmetPlatingIngredient.Serializer.ID,
          TerrasteelHelmetPlatingIngredient.Serializer.INSTANCE),
        Mockito.times(1));
    } finally {
      setIngredientSerializerRegistered(wasRegistered);
    }
  }

  @Test
  void dynamicRecipeProviderRegistrationDoesNotRegisterGlobalIngredientSerializers() {
    BotaniaTiCAddon addon = Mockito.mock(BotaniaTiCAddon.class, Mockito.CALLS_REAL_METHODS);
    List<String> providers = new ArrayList<>();
    DynamicProviderRegistrar registrar = (name, factory) -> providers.add(name);

    try (MockedStatic<CraftingHelper> craftingHelper = Mockito.mockStatic(CraftingHelper.class)) {
      addon.registerDynamicRecipeProviders(registrar);
      addon.registerDynamicRecipeProviders(registrar);

      craftingHelper.verifyNoInteractions();
    }

    assertThat(providers).containsExactly(
      "BotaniaModifierRecipeProvider",
      "BotaniaMaterialRecipeProvider",
      "BotaniaModifierRecipeProvider",
      "BotaniaMaterialRecipeProvider");
  }

  private static boolean isIngredientSerializerRegistered() throws ReflectiveOperationException {
    Field field = registeredField();
    return field.getBoolean(null);
  }

  private static void setIngredientSerializerRegistered(boolean registered) throws ReflectiveOperationException {
    Field field = registeredField();
    field.setBoolean(null, registered);
  }

  private static Field registeredField() throws NoSuchFieldException {
    Field field = TerrasteelHelmetPlatingIngredient.class.getDeclaredField("registered");
    field.setAccessible(true);
    return field;
  }
}
