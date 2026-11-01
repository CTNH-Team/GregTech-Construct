package slimeknights.tconstruct.plugin.jecharacters;

import me.towdium.jecharacters.utils.Match;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import slimeknights.tconstruct.library.addon.ITiCAddon;
import slimeknights.tconstruct.library.addon.TiCAddon;
import slimeknights.tconstruct.plugin.sophisticated.client.SophisticatedSearch;

/**
 * Wires the workstation side inventory search box into JEC (Just Enough Characters):
 * item names are matched through JEC's pinyin aware matcher, so typing pinyin finds
 * CJK item names. Without Sophisticated the search box does not exist and the hook is
 * simply unused.
 */
@TiCAddon(modID = JecharactersTiCAddon.MOD_ID)
public class JecharactersTiCAddon implements ITiCAddon {
  public static final String MOD_ID = "jecharacters";

  public JecharactersTiCAddon() {
    // the search box is a client side Sophisticated feature
    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SophisticatedSearch.nameMatcher = JecharactersTiCAddon::matchesName);
  }

  /** JEC lowercases both sides, mirroring the plain contains fallback. */
  private static boolean matchesName(String name, String query) {
    return Match.contains(name, query, true);
  }

  @Override
  public String addonModId() {
    return MOD_ID;
  }
}
