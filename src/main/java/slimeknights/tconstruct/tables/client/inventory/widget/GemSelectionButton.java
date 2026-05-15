package slimeknights.tconstruct.tables.client.inventory.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.client.screen.ElementScreen;
import slimeknights.tconstruct.library.client.Icons;

public class GemSelectionButton extends Button {
  public static final int WIDTH = 18;
  public static final int HEIGHT = 18;

  private static final ElementScreen BUTTON_PRESSED_GUI = new ElementScreen(Icons.ICONS, 144, 216, WIDTH, HEIGHT, 256, 256);
  private static final ElementScreen BUTTON_NORMAL_GUI = new ElementScreen(Icons.ICONS, 180, 216, WIDTH, HEIGHT, 256, 256);
  private static final ElementScreen BUTTON_HOVER_GUI = new ElementScreen(Icons.ICONS, 216, 216, WIDTH, HEIGHT, 256, 256);

  private final ItemStack gem;
  private final int socketIndex;
  private boolean selected;

  public GemSelectionButton(int x, int y, int socketIndex, ItemStack gem, OnPress onPress) {
    super(x, y, WIDTH, HEIGHT, gem.getHoverName(), onPress, DEFAULT_NARRATION);
    this.gem = gem;
    this.socketIndex = socketIndex;
  }

  public ItemStack getGem() {
    return this.gem;
  }

  public int getSocketIndex() {
    return this.socketIndex;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isSelected() {
    return this.selected;
  }

  @Override
  protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    int x = getX();
    int y = getY();
    if (this.selected) {
      BUTTON_PRESSED_GUI.draw(graphics, x, y);
    } else if (this.isHovered) {
      BUTTON_HOVER_GUI.draw(graphics, x, y);
    } else {
      BUTTON_NORMAL_GUI.draw(graphics, x, y);
    }
    graphics.renderItem(this.gem, x + 1, y + 1);
  }
}
