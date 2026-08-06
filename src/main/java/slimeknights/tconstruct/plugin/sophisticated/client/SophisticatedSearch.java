package slimeknights.tconstruct.plugin.sophisticated.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;
import slimeknights.tconstruct.tables.client.inventory.CraftingStationScreen;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Search button for station side inventories styled after Sophisticated Core's {@code SearchBox}:
 * collapsed to a small grey button with the magnifying glass icon, expanding into an unbordered
 * edit box on click with a 200ms ease animation.
 */
public class SophisticatedSearch implements BaseTabbedScreen.IStationSearch {
  /** Sophisticated Core search box colors and sizes. */
  private static final int TEXT_COLOR = 0xBBBBBB;
  private static final int BG_COLOR = 0xFF777777;
  private static final int MAX_LENGTH = 50;
  private static final int COLLAPSED_WIDTH = 18;
  private static final int EXPANDED_WIDTH = 120;
  private static final int HEIGHT = 14;
  /** Animation duration in milliseconds, matching Sophisticated Core. */
  private static final float ANIMATION_TIME = 200.0f;

  /** Icons from Sophisticated Core's icons.png, referenced the same way its GUI code does. */
  private static final TextureBlitData MAGNIFYING_GLASS = new TextureBlitData(
    GuiHelper.ICONS, Dimension.SQUARE_256, new UV(96, 0), Dimension.SQUARE_16);
  private static final TextureBlitData BACKPACK_ICON = new TextureBlitData(
    GuiHelper.ICONS, Dimension.SQUARE_256, new UV(208, 0), Dimension.SQUARE_16);
  private static final TextureBlitData CHEST_ICON = new TextureBlitData(
    GuiHelper.ICONS, Dimension.SQUARE_256, new UV(224, 16), Dimension.SQUARE_16);

  /** Shift click result target button, left of the crafting grid */
  private static final int BUTTON_X = 10;
  private static final int BUTTON_Y = 27;
  private static final int BUTTON_SIZE = 16;

  private static final Component TOOLTIP_INTO_STORAGE = TConstruct.makeTranslation("gui", "crafting_station.shift_into_storage");
  private static final Component TOOLTIP_INTO_INVENTORY = TConstruct.makeTranslation("gui", "crafting_station.shift_into_inventory");

  @Nullable
  private EditBox searchBox;
  @Nullable
  private BaseTabbedScreen<?, ?> screen;
  private Predicate<ItemStack> stackFilter = stack -> true;
  private long lastFocusChangeTime;

  @Override
  public void init(BaseTabbedScreen<?, ?> screen) {
    this.screen = screen;
    if (searchBox == null) {
      searchBox = new EditBox(Minecraft.getInstance().font, 0, 0, COLLAPSED_WIDTH, HEIGHT, Component.empty());
      searchBox.setBordered(false);
      searchBox.setTextColor(TEXT_COLOR);
      searchBox.setTextColorUneditable(TEXT_COLOR);
      searchBox.setMaxLength(MAX_LENGTH);
      searchBox.setResponder(this::updateFilter);
    } else {
      // reset the search whenever another station opens
      searchBox.setValue("");
      searchBox.setFocused(false);
      lastFocusChangeTime = System.currentTimeMillis();
    }
  }

  @Override
  public boolean shouldShowSlot(Slot slot) {
    ItemStack stack = slot.getItem();
    return stack.isEmpty() || stackFilter.test(stack);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    EditBox box = searchBox;
    BaseTabbedScreen<?, ?> currentScreen = screen;
    if (box == null || currentScreen == null) {
      return;
    }
    Rect2i area = currentScreen.getSideInventoryArea();
    if (area == null) {
      return;
    }

    // animate the width between collapsed and expanded with ease in/out cubic
    float progress = Math.min((System.currentTimeMillis() - lastFocusChangeTime) / ANIMATION_TIME, 1.0f);
    float eased = easeInOutCubic(progress);
    boolean expanded = box.isFocused() || !box.getValue().isEmpty();
    float width = expanded
                  ? COLLAPSED_WIDTH + (EXPANDED_WIDTH - COLLAPSED_WIDTH) * eased
                  : EXPANDED_WIDTH - (EXPANDED_WIDTH - COLLAPSED_WIDTH) * eased;

    // inside the side inventory panel, replacing its name bar
    int x = area.getX() + (area.getWidth() - (int)width) / 2;
    int y = area.getY() + 3;
    box.setX(x);
    box.setY(y);
    box.setWidth((int)width);

    graphics.fill(x - 4, y - 3, x + box.getWidth() + 4, y + HEIGHT + 3, BG_COLOR);
    if (width <= COLLAPSED_WIDTH + 1) {
      // collapsed: show the magnifying glass icon instead of the edit box
      GuiHelper.blit(graphics, x - 1, y - 1, MAGNIFYING_GLASS);
    } else {
      box.render(graphics, mouseX, mouseY, partialTick);
    }

    // shift click result target button on the crafting station, styled after Sophisticated buttons
    if (currentScreen instanceof CraftingStationScreen craftingScreen) {
      int bx = currentScreen.getCornerX() + BUTTON_X;
      int by = currentScreen.getCornerY() + BUTTON_Y;
      boolean intoStorage = craftingScreen.getMenu().shiftClickIntoStorage.get() != 0;
      GuiHelper.blit(graphics, bx, by, intoStorage ? GuiHelper.SMALL_BUTTON_HOVERED_BACKGROUND : GuiHelper.SMALL_BUTTON_BACKGROUND,
                     BUTTON_SIZE, BUTTON_SIZE);
      GuiHelper.blit(graphics, bx + 2, by + 2, intoStorage ? CHEST_ICON : BACKPACK_ICON);
      if (mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE) {
        graphics.renderTooltip(Minecraft.getInstance().font, intoStorage ? TOOLTIP_INTO_STORAGE : TOOLTIP_INTO_INVENTORY, mouseX, mouseY);
      }
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    BaseTabbedScreen<?, ?> currentScreen = screen;
    if (button == 0 && currentScreen instanceof CraftingStationScreen craftingScreen) {
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
    }
    EditBox box = searchBox;
    if (box == null) {
      return false;
    }
    boolean wasFocused = box.isFocused();
    if (box.mouseClicked(mouseX, mouseY, button)) {
      if (button == 1) {
        // right click clears the search, matching Sophisticated Core
        box.setValue("");
      } else if (!wasFocused) {
        // only start the expand animation when the box was not focused yet
        onFocusChanged(true);
      }
      return true;
    }
    if (box.isFocused()) {
      onFocusChanged(false);
    }
    box.setFocused(false);
    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    EditBox box = searchBox;
    return box != null && box.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    EditBox box = searchBox;
    return box != null && box.charTyped(codePoint, modifiers);
  }

  /** Marks the start of an expand/collapse animation. */
  private void onFocusChanged(boolean focused) {
    if (searchBox != null) {
      searchBox.setFocused(focused);
      lastFocusChangeTime = System.currentTimeMillis();
    }
  }

  /** Cubic ease in/out, matching Sophisticated Core's Easing. */
  private static float easeInOutCubic(float value) {
    return value < 0.5f ? 4.0f * value * value * value : 1.0f - (float)Math.pow(-2.0f * value + 2.0f, 3.0f) / 2.0f;
  }

  /** Builds the item name filter from the search text, matching Sophisticated Core's rules. */
  private void updateFilter(String text) {
    String trimmed = text.trim();
    if (trimmed.isEmpty()) {
      stackFilter = stack -> true;
      return;
    }
    List<Predicate<ItemStack>> predicates = new ArrayList<>();
    for (String word : trimmed.split(" ")) {
      if (!word.isEmpty()) {
        String lower = word.toLowerCase();
        predicates.add(stack -> stack.getHoverName().getString().toLowerCase().contains(lower));
      }
    }
    stackFilter = predicates.stream().reduce(Predicate::and).orElse(stack -> true);
  }
}
