package slimeknights.tconstruct.tables.client.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;

public class CraftingStationScreen extends BaseTabbedScreen<CraftingStationBlockEntity,CraftingStationContainerMenu> {
  private static final ResourceLocation CRAFTING_STATION_GUI = TConstruct.getResource("textures/gui/crafting_station.png");
  private static final ResourceLocation TOOL_SLOT_OVERLAY = TConstruct.getResource("textures/gui/tool_slot_overlay.png");

  public CraftingStationScreen(CraftingStationContainerMenu container, Inventory playerInventory, Component title) {
    super(container, playerInventory, title);
    this.imageHeight = 184;
    addChestSideInventory(playerInventory);
  }

  @Override
  protected void drawContainerName(GuiGraphics graphics) {
    graphics.drawString(this.font, this.getTitle(), 8, 6, 0x404040, false);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
    this.drawBackground(graphics, CRAFTING_STATION_GUI);
    // 在每个空工具槽中半透明绘制覆盖贴图
    RenderSystem.enableBlend();
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
    for (int i = 0; i < CraftingStationBlockEntity.TOOL_SLOT_COUNT; i++) {
      Slot slot = this.getMenu().getSlot(CraftingStationBlockEntity.TOOL_SLOT_START + i);
      if (!slot.hasItem()) {
        graphics.blit(TOOL_SLOT_OVERLAY, this.cornerX + slot.x, this.cornerY + slot.y, 0, 0, 16, 16, 16, 16);
      }
    }
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.disableBlend();
    super.renderBg(graphics, partialTicks, mouseX, mouseY);
  }
}
