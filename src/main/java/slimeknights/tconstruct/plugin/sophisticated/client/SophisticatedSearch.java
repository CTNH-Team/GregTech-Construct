package slimeknights.tconstruct.plugin.sophisticated.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.controller.IControllableStorage;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;
import slimeknights.tconstruct.tables.client.inventory.module.SideInventoryScreen;
import slimeknights.tconstruct.tables.menu.module.SideInventoryContainer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Search box for station side inventories styled after Sophisticated Core's {@code SearchBox}:
 * collapsed to a small grey box with the magnifying glass glyph, expanding into an unbordered
 * edit box on click with a 200ms ease animation.
 */
public class SophisticatedSearch implements BaseTabbedScreen.IStationSearch {
  /** Sophisticated Core search box colors and sizes. */
  private static final int TEXT_COLOR = 0xBBBBBB;
  private static final int BG_COLOR = 0xFF777777;
  private static final int MAX_LENGTH = 50;
  private static final int COLLAPSED_WIDTH = 8;
  private static final int EXPANDED_WIDTH = 120;
  private static final int HEIGHT = 9;
  /** Animation duration in milliseconds, matching Sophisticated Core. */
  private static final float ANIMATION_TIME = 200.0f;

  /** Magnifying glass glyph rendered while collapsed, same as Sophisticated Core's SearchBox. */
  private static final String MAGNIFYING_GLASS = "\uD83D\uDD0D";
  /** Nudges the collapsed button slightly left and down within the side inventory panel. */
  private static final int OFFSET_X = -6;
  private static final int OFFSET_Y = 2;

  @Nullable
  private EditBox searchBox;
  @Nullable
  private BaseTabbedScreen<?, ?> screen;
  private Predicate<ItemStack> stackFilter = stack -> true;
  /** Side slots matching the active filter, slot -> visible order, rebuilt from the side inventory each frame. */
  private final Map<Slot, Integer> matches = new HashMap<>();
  /** Wrapper of the adjacent Sophisticated storage, provides the memorized items of locked slots. */
  @Nullable
  private IStorageWrapper wrapper;
  private long lastFocusChangeTime;

  @Override
  public void init(BaseTabbedScreen<?, ?> screen) {
    this.screen = screen;
    // the side inventory tile is the adjacent container, which may be any Sophisticated storage
    SideInventoryContainer<?> sideInventory = screen.getMenu().getSubContainer(SideInventoryContainer.class);
    this.wrapper = sideInventory != null && sideInventory.getTile() instanceof IControllableStorage storage ? storage.getStorageWrapper() : null;
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
    // without an active search every slot is visible, matching Sophisticated's storage screen
    if (!isFilterActive()) {
      return true;
    }
    return getMatchIndex(slot) >= 0;
  }

  @Override
  public boolean isFilterActive() {
    EditBox box = searchBox;
    return box != null && !box.getValue().isBlank();
  }

  @Override
  public void refreshMatches(AbstractContainerMenu menu, int slotCount) {
    matches.clear();
    if (!isFilterActive()) {
      return;
    }
    int order = 0;
    for (Slot slot : menu.slots) {
      // side slots are the first slotCount slots of the menu, same as SideInventoryScreen
      if (slot.getSlotIndex() >= slotCount) {
        continue;
      }
      ItemStack stack = slot.getItem();
      // an empty locked slot counts as its memorized item, like Sophisticated Core's slot ghosts
      if (stack.isEmpty() && this.wrapper != null) {
        stack = this.wrapper.getSettingsHandler()
          .getTypeCategory(MemorySettingsCategory.class)
          .getSlotFilterStack(slot.getSlotIndex(), false)
          .orElse(ItemStack.EMPTY);
      }
      if (stackFilter.test(stack)) {
        matches.put(slot, order++);
      }
    }
  }

  @Override
  public int getMatchIndex(Slot slot) {
    return matches.getOrDefault(slot, -1);
  }

  @Override
  public int getMatchCount() {
    return matches.size();
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

    // animate the width between collapsed and expanded with ease in/out cubic, anchored to the
    // right edge of the side inventory panel like Sophisticated Core's SearchBox
    float progress = Math.min((System.currentTimeMillis() - lastFocusChangeTime) / ANIMATION_TIME, 1.0f);
    float eased = easeInOutCubic(progress);
    boolean expanded = box.isFocused() || !box.getValue().isEmpty();
    float width = expanded
                  ? COLLAPSED_WIDTH + (EXPANDED_WIDTH - COLLAPSED_WIDTH) * eased
                  : EXPANDED_WIDTH - (EXPANDED_WIDTH - COLLAPSED_WIDTH) * eased;
    int maximizedX = area.getX() + area.getWidth() - EXPANDED_WIDTH;
    int x = maximizedX + EXPANDED_WIDTH - (int)width + OFFSET_X;
    int y = area.getY() + 3 + OFFSET_Y;
    box.setX(x);
    box.setY(y);
    box.setWidth((int)width);

    // the background keeps the collapsed button size, expanding only towards the left
    graphics.fill(x, y, x + box.getWidth(), y + HEIGHT, BG_COLOR);
    box.render(graphics, mouseX, mouseY, partialTick);
    if (box.getValue().isEmpty() && !box.isFocused()) {
      // collapsed: draw the magnifying glass glyph instead of text, nudged a fraction left
      PoseStack pose = graphics.pose();
      pose.pushPose();
      pose.translate(-0.6f, 0.0f, 0.0f);
      graphics.drawCenteredString(Minecraft.getInstance().font, MAGNIFYING_GLASS, x + box.getWidth() / 2 + 1, y, TEXT_COLOR);
      pose.popPose();
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
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

  /** Builds the item filter from the search text, matching Sophisticated Core's rules. */
  private void updateFilter(String text) {
    String trimmed = text.trim();
    if (trimmed.isEmpty()) {
      stackFilter = stack -> true;
    }
    else if (trimmed.startsWith("@")) {
      // mod filter: items from a matching namespace, same as Sophisticated Core
      String modId = trimmed.substring(1).toLowerCase(Locale.ROOT);
      stackFilter = stack -> !stack.isEmpty() && ForgeRegistries.ITEMS.getKey(stack.getItem()) != null
                             && ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace().toLowerCase(Locale.ROOT).contains(modId);
    }
    else if (trimmed.startsWith("#")) {
      // tooltip filter: any tooltip line contains the search, same as Sophisticated Core
      String search = trimmed.substring(1).toLowerCase(Locale.ROOT);
      Player player = screen == null ? null : screen.getMinecraft().player;
      stackFilter = stack -> {
        if (stack.isEmpty()) {
          return false;
        }
        List<Component> tooltip = stack.getTooltipLines(player, TooltipFlag.Default.NORMAL);
        return tooltip.stream().anyMatch(line -> line.getString().toLowerCase(Locale.ROOT).contains(search));
      };
    }
    else {
      // item name filter: space separated words all have to match, same as Sophisticated Core
      List<Predicate<ItemStack>> predicates = new ArrayList<>();
      for (String word : trimmed.split(" ")) {
        if (!word.isEmpty()) {
          String lower = word.toLowerCase(Locale.ROOT);
          predicates.add(stack -> !stack.isEmpty() && stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(lower));
        }
      }
      stackFilter = predicates.stream().reduce(Predicate::and).orElse(stack -> true);
    }

    // reflow the side inventory slots into the matches and refresh the scroll range
    BaseTabbedScreen<?, ?> currentScreen = screen;
    if (currentScreen != null) {
      SideInventoryScreen<?, ?> sideInventory = currentScreen.getSideInventory();
      if (sideInventory != null) {
        sideInventory.onSearchChanged();
      }
    }
  }
}
