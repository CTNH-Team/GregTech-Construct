package slimeknights.tconstruct.plugin.sophisticated.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.controller.IControllableStorage;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import slimeknights.mantle.inventory.WrapperSlot;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;
import slimeknights.tconstruct.tables.menu.module.SideInventoryContainer;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Draws Sophisticated locked slot ghosts on station side inventories, replicating
 * {@code StorageScreenBase.renderSlotBackground} and {@code drawStackOverlay} one to one:
 * an empty slot with a memorized slot filter renders the locked item with a translucent
 * grey overlay on top. Slot memory is a Sophisticated Core feature, so any adjacent
 * {@link IControllableStorage} (storage blocks and backpack blocks alike) is supported.
 */
public class SophisticatedLockedSlotRenderer implements BaseTabbedScreen.IStationSlotOverlay {
  /** Sophisticated Core's GUI controls texture, the overlay at (77, 0) is a semi-transparent grey. */
  private static final ResourceLocation GUI_CONTROLS = ResourceLocation.tryBuild("sophisticatedcore", "textures/gui/gui_controls.png");
  private static final int OVERLAY_U = 77;
  private static final int OVERLAY_V = 0;

  @Nullable
  private IStorageWrapper wrapper;

  @Override
  public void init(BaseTabbedScreen<?, ?> screen) {
    // the side inventory tile is the adjacent container, which may be any Sophisticated storage
    SideInventoryContainer<?> sideInventory = screen.getMenu().getSubContainer(SideInventoryContainer.class);
    this.wrapper = sideInventory != null && sideInventory.getTile() instanceof IControllableStorage storage ? storage.getStorageWrapper() : null;
  }

  @Override
  public void render(GuiGraphics graphics, Slot slot) {
    IStorageWrapper wrapper = this.wrapper;
    // like StorageScreenBase, the locked ghost only draws over empty slots
    if (wrapper == null || slot.hasItem()) {
      return;
    }
    // the overlay is invoked with the parent menu's wrapper slot, whose index is the menu-wide
    // slot id; unwrap it so the storage memory is read by the side inventory's own slot index
    if (slot instanceof WrapperSlot wrapperSlot) {
      slot = wrapperSlot.parent;
    }
    Optional<ItemStack> lockedStack = wrapper.getSettingsHandler()
      .getTypeCategory(MemorySettingsCategory.class)
      .getSlotFilterStack(slot.getSlotIndex(), false);
    if (lockedStack.isEmpty()) {
      return;
    }
    ItemStack stack = lockedStack.get();
    if (stack.isEmpty()) {
      return;
    }

    // renderSlotBackground: the locked item on the empty slot
    graphics.renderItem(stack, slot.x, slot.y);
    // drawStackOverlay: semi-transparent grey over the item
    graphics.pose().pushPose();
    RenderSystem.enableBlend();
    RenderSystem.disableDepthTest();
    graphics.blit(GUI_CONTROLS, slot.x, slot.y, OVERLAY_U, OVERLAY_V, 16, 16);
    RenderSystem.enableDepthTest();
    RenderSystem.disableBlend();
    graphics.pose().popPose();
  }
}
