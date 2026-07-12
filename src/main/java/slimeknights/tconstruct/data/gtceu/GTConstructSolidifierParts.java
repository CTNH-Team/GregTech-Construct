package slimeknights.tconstruct.data.gtceu;

import java.util.List;

final class GTConstructSolidifierParts {
  private GTConstructSolidifierParts() {}

  static List<GTConstructRecipes.SolidifierPart> create() {
    return TinkersPartScanner.scan(GTConstructMaterialSupport.INSTANCE);
  }
}
