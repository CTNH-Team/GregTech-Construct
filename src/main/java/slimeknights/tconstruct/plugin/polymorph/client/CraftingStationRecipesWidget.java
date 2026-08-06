package slimeknights.tconstruct.plugin.polymorph.client;

import com.illusivesoulworks.polymorph.client.recipe.widget.PersistentRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;

/**
 * Polymorph's selection widget anchored to the crafting station's result slot, which is not
 * a vanilla {@code ResultContainer} so Polymorph's automatic result slot lookup misses it.
 * Must extend {@link PersistentRecipesWidget} so Polymorph sends the block entity listener
 * packet that registers the player for recipe list syncs. Module layouts shift {@code leftPos}
 * away from {@code cornerX}, so the x anchor compensates for that difference to keep the
 * button aligned with the station's shift click target button column.
 */
public class CraftingStationRecipesWidget extends PersistentRecipesWidget {
  private final Slot outputSlot;

  public CraftingStationRecipesWidget(AbstractContainerScreen<?> screen, Slot outputSlot) {
    super(screen);
    this.outputSlot = outputSlot;
  }

  @Override
  public Slot getOutputSlot() {
    return this.outputSlot;
  }

  @Override
  public int getXPos() {
    // Polymorph anchors to guiLeft, station buttons anchor to cornerX; compensate the difference
    if (this.containerScreen instanceof BaseTabbedScreen<?, ?> tabbed) {
      return tabbed.getCornerX() - this.containerScreen.getGuiLeft() + this.outputSlot.x;
    }
    return this.outputSlot.x;
  }

  @Override
  public int getYPos() {
    // the button sits just above the result slot
    return this.outputSlot.y - 22;
  }
}
