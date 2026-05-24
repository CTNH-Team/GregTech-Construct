package slimeknights.tconstruct.plugin.botania.modifier;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierId;

public final class BotaniaModifierIds {
  public static final ModifierId manafix = id("manafix");
  public static final ModifierId terrarecover = id("terrarecover");

  private BotaniaModifierIds() {}

  private static ModifierId id(String name) {
    return new ModifierId(TConstruct.MOD_ID, name);
  }
}
