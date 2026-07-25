package slimeknights.tconstruct.plugin.botania;

import net.minecraftforge.common.crafting.CraftingHelper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import slimeknights.tconstruct.library.addon.DynamicRecipeProviderRegistrar;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BotaniaTiCAddonTest extends BaseMcTest {
  @Test
  void dynamicRecipeProviderRegistrationDoesNotRegisterGlobalIngredientSerializers() {
    BotaniaTiCAddon addon = Mockito.mock(BotaniaTiCAddon.class, Mockito.CALLS_REAL_METHODS);
    List<String> providers = new ArrayList<>();
    DynamicRecipeProviderRegistrar registrar = (name, writer) -> providers.add(name);

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
}
