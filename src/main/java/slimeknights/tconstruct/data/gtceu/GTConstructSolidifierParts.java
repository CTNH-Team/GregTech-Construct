package slimeknights.tconstruct.data.gtceu;

import java.util.List;

final class GTConstructSolidifierParts {
  private GTConstructSolidifierParts() {}

  static List<GTConstructSolidifierRecipes.SolidifierPart> create() {
    return TinkersPartScanner.scan(GTConstructMaterialSupport.INSTANCE);
  }
}
