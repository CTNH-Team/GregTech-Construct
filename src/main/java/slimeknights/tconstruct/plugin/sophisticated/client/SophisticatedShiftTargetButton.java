package slimeknights.tconstruct.plugin.sophisticated.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;
import slimeknights.tconstruct.tables.client.inventory.CraftingStationScreen;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Toggle button below the crafting result slot selecting where shift clicking the result places
 * it. Styled after Sophisticated Backpacks' {@code SHIFT_CLICK_TARGET} button, reusing its icon
 * UVs, with Tinkers' own tooltips.
 */
public class SophisticatedShiftTargetButton implements BaseTabbedScreen.IStationButton {
  /** Shift click result target icons, same UVs as Sophisticated Backpacks' SHIFT_CLICK_TARGET. */
  private static final TextureBlitData INTO_STORAGE_ICON = new TextureBlitData(
    GuiHelper.ICONS, Dimension.SQUARE_256, new UV(32, 48), Dimension.SQUARE_16);
  private static final TextureBlitData INTO_INVENTORY_ICON = new TextureBlitData(
    GuiHelper.ICONS, Dimension.SQUARE_256, new UV(48, 48), Dimension.SQUARE_16);

  /** Button position, directly below the crafting result slot */
  private static final int BUTTON_X = 124;
  private static final int BUTTON_Y = 58;
  private static final int BUTTON_SIZE = 18;

  /** Tooltips describing the shift click result target. */
  private static final Component TOOLTIP_INTO_STORAGE = TConstruct.makeTranslation("gui", "crafting_station.shift_into_storage");
  private static final Component TOOLTIP_INTO_INVENTORY = TConstruct.makeTranslation("gui", "crafting_station.shift_into_inventory");

  @Nullable
  private BaseTabbedScreen<?, ?> screen;

  @Override
  public void init(BaseTabbedScreen<?, ?> screen) {
    this.screen = screen;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    BaseTabbedScreen<?, ?> currentScreen = screen;
    if (!(currentScreen instanceof CraftingStationScreen craftingScreen)) {
      return;
    }
    int bx = currentScreen.getCornerX() + BUTTON_X;
    int by = currentScreen.getCornerY() + BUTTON_Y;
    boolean intoStorage = craftingScreen.getMenu().shiftClickIntoStorage.get() != 0;
    GuiHelper.blit(graphics, bx, by, intoStorage ? GuiHelper.DEFAULT_BUTTON_HOVERED_BACKGROUND : GuiHelper.DEFAULT_BUTTON_BACKGROUND,
                   BUTTON_SIZE, BUTTON_SIZE);
    GuiHelper.blit(graphics, bx + 1, by + 1, intoStorage ? INTO_STORAGE_ICON : INTO_INVENTORY_ICON);
    if (mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE) {
      graphics.renderTooltip(Minecraft.getInstance().font,
                             List.of(intoStorage ? TOOLTIP_INTO_STORAGE : TOOLTIP_INTO_INVENTORY),
                             Optional.empty(), mouseX, mouseY);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    BaseTabbedScreen<?, ?> currentScreen = screen;
    if (button != 0 || !(currentScreen instanceof CraftingStationScreen craftingScreen)) {
      return false;
    }
    int bx = currentScreen.getCornerX() + BUTTON_X;
    int by = currentScreen.getCornerY() + BUTTON_Y;
    if (mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player != null) {
        minecraft.player.connection.send(new ServerboundContainerButtonClickPacket(craftingScreen.getMenu().containerId,
                                                                                  CraftingStationContainerMenu.SHIFT_CLICK_TARGET_BUTTON));
      }
      return true;
    }
    return false;
  }
}
