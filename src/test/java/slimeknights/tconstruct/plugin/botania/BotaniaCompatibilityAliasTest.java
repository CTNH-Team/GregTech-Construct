package slimeknights.tconstruct.plugin.botania;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.plugin.botania.material.BotaniaMaterialIds;
import slimeknights.tconstruct.plugin.botania.modifier.BotaniaModifierIds;
import slimeknights.tconstruct.plugin.botania.modifier.BotaniaModifiers;
import slimeknights.tconstruct.test.BaseMcTest;
import slimeknights.tconstruct.tools.TinkerModifiers;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("removal")
class BotaniaCompatibilityAliasTest extends BaseMcTest {
  @Test
  void botaniaMaterialIdsKeepStableTconstructLocations() {
    assertThat(BotaniaMaterialIds.manaSteel.toString()).isEqualTo("tconstruct:manasteel");
    assertThat(BotaniaMaterialIds.terraSteel.toString()).isEqualTo("tconstruct:terrasteel");
  }

  @Test
  void botaniaModifierIdsKeepStableTconstructLocations() {
    assertThat(BotaniaModifierIds.manafix.toString()).isEqualTo("tconstruct:manafix");
    assertThat(BotaniaModifierIds.terrarecover.toString()).isEqualTo("tconstruct:terrarecover");
  }

  @Test
  void legacyStaticModifiersForwardToBotaniaModifiers() {
    assertThat(TinkerModifiers.manafix).isSameAs(BotaniaModifiers.manafix);
    assertThat(TinkerModifiers.terrarecover).isSameAs(BotaniaModifiers.terrarecover);
  }
}
