package slimeknights.tconstruct.tables.client.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeknights.mantle.client.screen.ElementScreen;
import slimeknights.mantle.client.screen.ModuleScreen;
import slimeknights.mantle.client.screen.MultiModuleScreen;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.library.client.Icons;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;
import slimeknights.tconstruct.tables.client.inventory.module.SideInventoryScreen;
import slimeknights.tconstruct.tables.client.inventory.widget.TinkerTabsWidget;
import slimeknights.tconstruct.tables.menu.TabbedContainerMenu;
import slimeknights.tconstruct.tables.menu.module.SideInventoryContainer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class BaseTabbedScreen<TILE extends BlockEntity, CONTAINER extends TabbedContainerMenu<TILE>> extends MultiModuleScreen<CONTAINER> {
  protected static final Component COMPONENT_WARNING = TConstruct.makeTranslation("gui", "warning");
  protected static final Component COMPONENT_ERROR = TConstruct.makeTranslation("gui", "error");

  public static final ResourceLocation BLANK_BACK = TConstruct.getResource("textures/gui/blank.png");
  public static final ResourceLocation BLANK_BACK_PLUS_1 = TConstruct.getResource("textures/gui/blank_extra_row.png");

  /** Optional search UI for the side inventory, provided by a plugin. */
  @Nullable
  public static IStationSearch search;

  /** Hook for a plugin-provided side inventory search box. */
  public interface IStationSearch {
    /** Called each time a station screen initializes. */
    void init(BaseTabbedScreen<?, ?> screen);

    /** Tests whether the given side inventory slot is visible with the current search. */
    boolean shouldShowSlot(Slot slot);

    /** Renders the search box over the screen. */
    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    /** Mouse and keyboard input, returning true when the event was consumed. */
    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    boolean charTyped(char codePoint, int modifiers);
  }

  /** Optional button rendered over station screens, provided by a plugin. */
  @Nullable
  public static IStationButton stationButton;

  /** Hook for a plugin-provided button drawn over station screens. */
  public interface IStationButton {
    /** Called each time a station screen initializes. */
    void init(BaseTabbedScreen<?, ?> screen);

    /** Renders the button over the screen. */
    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    /** Mouse input, returning true when the event was consumed. */
    boolean mouseClicked(double mouseX, double mouseY, int button);
  }

  @Nullable
  protected final TILE tile;
  protected TinkerTabsWidget tabsScreen;

  public BaseTabbedScreen(CONTAINER container, Inventory playerInventory, Component title) {
    super(container, playerInventory, title);
    this.tile = container.getTile();
  }

  @Override
  protected void init() {
    super.init();

    this.tabsScreen = addRenderableWidget(new TinkerTabsWidget(this));
    if (search != null) {
      search.init(this);
    }
    if (stationButton != null) {
      stationButton.init(this);
    }
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);
    if (search != null) {
      search.render(graphics, mouseX, mouseY, partialTick);
    }
    if (stationButton != null) {
      stationButton.render(graphics, mouseX, mouseY, partialTick);
    }
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (search != null && search.keyPressed(keyCode, scanCode, modifiers)) {
      return true;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    if (search != null && search.charTyped(codePoint, modifiers)) {
      return true;
    }
    return super.charTyped(codePoint, modifiers);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (search != null && search.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    if (stationButton != null && stationButton.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Nullable
  public TILE getTileEntity() {
    return this.tile;
  }

  protected void drawIcon(GuiGraphics graphics, Slot slot, ElementScreen element) {
    RenderSystem.setShaderTexture(0, Icons.ICONS);
    element.draw(graphics, slot.x + this.cornerX - 1, slot.y + this.cornerY - 1);
  }

  protected void drawIconEmpty(GuiGraphics graphics, Slot slot, ElementScreen element) {
    if (slot.hasItem()) {
      return;
    }

    this.drawIcon(graphics, slot, element);
  }

  protected void drawIconEmpty(GuiGraphics graphics, Slot slot, Pattern pattern) {
    if (!slot.hasItem()) {
      GuiUtil.renderPattern(graphics, pattern, slot.x + this.cornerX, slot.y + this.cornerY);
    }
  }

  public void error(Component message) {
  }

  public void warning(Component message) {
  }

  public void updateDisplay() {
  }

  /** Adds the chest screen, returning true if it was added */
  protected boolean addChestSideInventory(Inventory inventory) {
    SideInventoryContainer<?> sideInventoryContainer = getMenu().getSubContainer(SideInventoryContainer.class);
    if (sideInventoryContainer != null) {
      // no title if missing one
      Component sideInventoryName = Component.empty();
      BlockEntity te = sideInventoryContainer.getTile();
      if (te instanceof MenuProvider) {
        sideInventoryName = Objects.requireNonNullElse(((MenuProvider)te).getDisplayName(), Component.empty());
      }

      this.addModule(new SideInventoryScreen<>(this, sideInventoryContainer, inventory, sideInventoryName, sideInventoryContainer.getSlotCount(), sideInventoryContainer.getColumns()));
      return true;
    }
    return false;
  }

  /** Returns the side inventory module area, or null when this station has no side inventory. */
  @Nullable
  public Rect2i getSideInventoryArea() {
    for (ModuleScreen<?, ?> module : this.modules) {
      if (module instanceof SideInventoryScreen) {
        return module.getArea();
      }
    }
    return null;
  }

  /** Content origin X, exposed for plugin widgets */
  public int getCornerX() {
    return this.cornerX;
  }

  /** Content origin Y, exposed for plugin widgets */
  public int getCornerY() {
    return this.cornerY;
  }

  @Override
  public List<Rect2i> getModuleAreas() {
    List<Rect2i> areas = super.getModuleAreas();
    if (tabsScreen != null) {
      areas.add(tabsScreen.getArea());
    } else {
      TConstruct.LOG.error("Someone is trying to access module areas before the screen is initialized. This usually indicates an EMI integration incorrectly implementing its API. Report this issue to EMI.");
    }
    return areas;
  }

  @Override
  protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
    return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton)
      && !tabsScreen.isMouseOver(mouseX, mouseY);
  }
}
