package slimeknights.tconstruct.plugin.polymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import slimeknights.tconstruct.library.addon.ITiCAddon;
import slimeknights.tconstruct.library.addon.TiCAddon;
import slimeknights.tconstruct.plugin.polymorph.client.CraftingStationRecipesWidget;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;
import slimeknights.tconstruct.tables.client.inventory.CraftingStationScreen;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;
import slimeknights.tconstruct.tables.menu.slot.PlayerSensitiveLazyResultSlot;

/**
 * Polymorph integration for the crafting station: registers the station as a recipe data
 * provider so Polymorph's selection widget appears and conflicting recipes can be resolved,
 * and routes the station's recipe lookups through the player's selection.
 */
@TiCAddon(modID = PolymorphTiCAddon.MOD_ID)
public class PolymorphTiCAddon implements ITiCAddon {
  public static final String MOD_ID = "polymorph";

  public PolymorphTiCAddon() {
    // resolve crafting conflicts by letting the player pick among matching recipes
    CraftingStationBlockEntity.recipeSelector = new PolymorphRecipeSelector();
    PolymorphApi.common().registerBlockEntity2RecipeData(blockEntity ->
      blockEntity instanceof CraftingStationBlockEntity station ? new CraftingStationRecipeData(station) : null);
    PolymorphApi.common().registerContainer2BlockEntity(menu ->
      menu instanceof CraftingStationContainerMenu station ? station.getTile() : null);
    // the result slot is not a vanilla ResultContainer, so anchor the widget to it manually
    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
      PolymorphApi.client().registerWidget(screen -> {
        if (screen instanceof CraftingStationScreen) {
          Slot resultSlot = findResultSlot(screen.getMenu());
          return resultSlot == null ? null : new CraftingStationRecipesWidget(screen, resultSlot);
        }
        return null;
      }));
  }

  @Override
  public String addonModId() {
    return MOD_ID;
  }

  /** Finds the station's result slot in the menu, identified by its slot type. */
  private static Slot findResultSlot(AbstractContainerMenu menu) {
    for (Slot slot : menu.slots) {
      if (slot instanceof PlayerSensitiveLazyResultSlot) {
        return slot;
      }
    }
    return null;
  }
}
