package slimeknights.tconstruct.plugin.botania.modifier;

import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

public final class BotaniaModifiers {
  public static final StaticModifier<?> manafix = new StaticModifier<>(BotaniaModifierIds.manafix);
  public static final StaticModifier<?> terrarecover = new StaticModifier<>(BotaniaModifierIds.terrarecover);

  private BotaniaModifiers() {}
}
