package slimeknights.tconstruct.plugin.botania.material;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

public final class BotaniaMaterialIds {
  public static final MaterialId manaSteel = id("manasteel");
  public static final MaterialId terraSteel = id("terrasteel");

  private BotaniaMaterialIds() {}

  private static MaterialId id(String name) {
    return new MaterialId(TConstruct.MOD_ID, name);
  }
}
