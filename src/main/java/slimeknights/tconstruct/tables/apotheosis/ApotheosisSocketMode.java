package slimeknights.tconstruct.tables.apotheosis;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.plugin.apotheosis.ApotheosisBridge;

import java.util.ArrayList;
import java.util.List;

/** Shared socket extraction helpers for anvil-sized tinkering stations. */
public final class ApotheosisSocketMode {
  private static final Component SOCKET_BONUS_HEADER = Component.translatable("stat.tconstruct.socket_bonus");

  private ApotheosisSocketMode() {}

  public static boolean canExtract(ItemStack tool, int inputCount) {
    return inputCount > 4 && ApotheosisBridge.sockets().hasSocketedGems(tool);
  }

  public static List<ItemStack> getDisplayedGems(ItemStack tool) {
    return ApotheosisBridge.sockets().getSocketedGems(tool);
  }

  public static int normalizeSelection(ItemStack tool, int selectedIndex) {
    List<ItemStack> gems = getDisplayedGems(tool);
    if (selectedIndex < 0 || selectedIndex >= gems.size()) {
      return -1;
    }
    return selectedIndex;
  }

  public static ItemStack createResult(ItemStack tool, int selectedIndex) {
    return ApotheosisBridge.sockets().removeGem(tool, normalizeSelection(tool, selectedIndex));
  }

  public static ItemStack createExtractedGem(ItemStack tool, int selectedIndex) {
    return ApotheosisBridge.sockets().copyGem(tool, normalizeSelection(tool, selectedIndex));
  }

  public static void appendTooltip(ItemStack tool, List<Component> tooltip) {
    List<Component> bridgeLines = new ArrayList<>();
    ApotheosisBridge.sockets().appendTooltip(tool, bridgeLines::add);
    if (bridgeLines.isEmpty()) {
      return;
    }
    tooltip.add(Component.empty());
    tooltip.add(SOCKET_BONUS_HEADER);
    tooltip.addAll(bridgeLines);
  }
}
