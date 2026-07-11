package slimeknights.tconstruct.library.recipe.modifiers;

import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;

import static org.assertj.core.api.Assertions.assertThat;

class ModifierRecipeLookupTest {
  @Test
  void modifierListIncludesRegistrationsAddedAfterFirstRead() {
    RecipeCacheInvalidator.reload(false);

    ModifierId initial = new ModifierId("test", "modifier_lookup_initial");
    ModifierId late = new ModifierId("test", "modifier_lookup_late");
    ModifierRecipeLookup.addRecipeModifier(null, new LazyModifier(initial));
    assertThat(ModifierRecipeLookup.getRecipeModifierList())
      .extracting(ModifierEntry::getId)
      .contains(initial);

    ModifierRecipeLookup.addRecipeModifier(null, new LazyModifier(late));

    assertThat(ModifierRecipeLookup.getRecipeModifierList())
      .extracting(ModifierEntry::getId)
      .contains(initial, late);
  }
}
