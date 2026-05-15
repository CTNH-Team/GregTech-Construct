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

  public record ButtonState(boolean visible, boolean enabled, String reasonKey) {}

  public static ButtonState buttonState(ItemStack tool, int inputCount) {
    if (tool.isEmpty() || inputCount <= 4) {
      return new ButtonState(false, false, "");
    }
    int sockets = ApotheosisBridge.sockets().getSocketCount(tool);
    if (sockets <= 0) {
      return new ButtonState(true, false, "no_sockets");
    }
    if (sockets > 5) {
      return new ButtonState(true, false, "too_many_sockets");
    }
    return new ButtonState(true, true, "");
  }

  @Deprecated(forRemoval = false)
  public static boolean canExtract(ItemStack tool, int inputCount) {
    ButtonState state = buttonState(tool, inputCount);
    return state.visible() && state.enabled() && ApotheosisBridge.sockets().hasSocketedGems(tool);
  }

  public static List<ApotheosisBridge.SocketGem> getVisibleSockets(ItemStack tool) {
    int sockets = Math.min(5, ApotheosisBridge.sockets().getSocketCount(tool));
    List<ApotheosisBridge.SocketGem> data = ApotheosisBridge.sockets().getSocketedGemData(tool);
    if (data.size() >= sockets) {
      return data.subList(0, sockets);
    }
    List<ApotheosisBridge.SocketGem> padded = new ArrayList<>(data);
    for (int i = data.size(); i < sockets; i++) {
      padded.add(ApotheosisBridge.SocketGem.empty(i));
    }
    return padded;
  }

  @Deprecated(forRemoval = false)
  public static List<ItemStack> getDisplayedGems(ItemStack tool) {
    return getVisibleSockets(tool).stream().filter(ApotheosisBridge.SocketGem::isFilled).map(ApotheosisBridge.SocketGem::gem).toList();
  }

  @Deprecated(forRemoval = false)
  public static int normalizeSelection(ItemStack tool, int selectedIndex) {
    List<ItemStack> gems = getDisplayedGems(tool);
    if (selectedIndex < 0 || selectedIndex >= gems.size()) {
      return -1;
    }
    return selectedIndex;
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

  public static ItemStack insertGem(ItemStack tool, int visibleSocketIndex, ItemStack gem) {
    int rawSocketIndex = getRawSocketIndex(tool, visibleSocketIndex);
    return ApotheosisBridge.sockets().insertGem(tool, rawSocketIndex, gem);
  }

  public static ItemStack removeGem(ItemStack tool, int visibleSocketIndex) {
    int rawSocketIndex = getRawSocketIndex(tool, visibleSocketIndex);
    return ApotheosisBridge.sockets().removeGem(tool, rawSocketIndex);
  }

  @Deprecated(forRemoval = false)
  public static ItemStack createResult(ItemStack tool, int selectedIndex) {
    int rawSocketIndex = getRawFilledSocketIndex(tool, selectedIndex);
    return ApotheosisBridge.sockets().removeGem(tool, rawSocketIndex);
  }

  @Deprecated(forRemoval = false)
  public static ItemStack createExtractedGem(ItemStack tool, int selectedIndex) {
    int rawSocketIndex = getRawFilledSocketIndex(tool, selectedIndex);
    return ApotheosisBridge.sockets().copyGem(tool, rawSocketIndex);
  }

  private static int getRawSocketIndex(ItemStack tool, int visibleSocketIndex) {
    List<ApotheosisBridge.SocketGem> sockets = getVisibleSockets(tool);
    if (visibleSocketIndex < 0 || visibleSocketIndex >= sockets.size()) {
      return -1;
    }
    return sockets.get(visibleSocketIndex).rawSocketIndex();
  }

  private static int getRawFilledSocketIndex(ItemStack tool, int selectedIndex) {
    int normalizedIndex = normalizeSelection(tool, selectedIndex);
    if (normalizedIndex < 0) {
      return -1;
    }
    return getVisibleSockets(tool).stream()
                                  .filter(ApotheosisBridge.SocketGem::isFilled)
                                  .toList()
                                  .get(normalizedIndex)
                                  .rawSocketIndex();
  }
}
