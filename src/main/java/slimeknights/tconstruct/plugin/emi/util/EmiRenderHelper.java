package slimeknights.tconstruct.plugin.emi.util;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class EmiRenderHelper {
  private EmiRenderHelper() {}

  public static SlotWidget slot(WidgetHolder widgets, EmiIngredient ingredient, int x, int y) {
    return widgets.addSlot(ingredient, x - 1, y - 1).drawBack(false);
  }

  public static SlotWidget bareSlot(WidgetHolder widgets, EmiIngredient ingredient, int x, int y) {
    return slot(widgets, ingredient, x, y);
  }

  public static SlotWidget slotWithBackground(WidgetHolder widgets, EmiIngredient ingredient, int x, int y) {
    return widgets.addSlot(ingredient, x - 1, y - 1).drawBack(true);
  }

  public static SlotWidget largeSlotWithBackground(WidgetHolder widgets, EmiIngredient ingredient, int x, int y) {
    return widgets.addSlot(ingredient, x - 5, y - 5).large(true).drawBack(true);
  }

  public static TankWidget tank(WidgetHolder widgets, EmiIngredient ingredient, int x, int y, int width, int height,
                                int capacity) {
    return (TankWidget)widgets.addTank(ingredient, x - 1, y - 1, width + 2, height + 2, capacity).drawBack(false);
  }

  public static void tankOverlay(WidgetHolder widgets, ResourceLocation texture, int x, int y, int width, int height,
                                 int u, int v) {
    widgets.addTexture(texture, x, y, width, height, u, v, width, height, 256, 256);
  }

  public static List<ClientTooltipComponent> tooltip(List<Component> components) {
    return components.stream()
        .map(Component::getVisualOrderText)
        .map(ClientTooltipComponent::create)
        .toList();
  }

  public static ClientTooltipComponent tooltip(Component component) {
    return ClientTooltipComponent.create(component.getVisualOrderText());
  }

  public static boolean advancedTooltips() {
    return Minecraft.getInstance().options.advancedItemTooltips;
  }

  public static int cycleIndex(int size) {
    if (size <= 1) {
      return 0;
    }
    return Math.floorMod((int)(System.currentTimeMillis() / 1000L), size);
  }
}
