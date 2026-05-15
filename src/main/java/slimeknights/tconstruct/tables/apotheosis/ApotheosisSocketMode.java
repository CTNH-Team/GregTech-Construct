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
    return getDisplayedSocketGems(tool).stream().map(ApotheosisBridge.SocketGem::gem).toList();
  }

  public static int normalizeSelection(ItemStack tool, int selectedIndex) {
    List<ApotheosisBridge.SocketGem> gems = getDisplayedSocketGems(tool);
    if (selectedIndex < 0 || selectedIndex >= gems.size()) {
      return -1;
    }
    return selectedIndex;
  }

  public static ItemStack createResult(ItemStack tool, int selectedIndex) {
    return ApotheosisBridge.sockets().removeGem(tool, getRawSocketIndex(tool, selectedIndex));
  }

  public static ItemStack createExtractedGem(ItemStack tool, int selectedIndex) {
    return ApotheosisBridge.sockets().copyGem(tool, getRawSocketIndex(tool, selectedIndex));
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

  private static List<ApotheosisBridge.SocketGem> getDisplayedSocketGems(ItemStack tool) {
    return ApotheosisBridge.sockets().getSocketedGemData(tool);
  }

  private static int getRawSocketIndex(ItemStack tool, int selectedIndex) {
    int normalizedIndex = normalizeSelection(tool, selectedIndex);
    if (normalizedIndex < 0) {
      return -1;
    }
    return getDisplayedSocketGems(tool).get(normalizedIndex).rawSocketIndex();
  }
}
