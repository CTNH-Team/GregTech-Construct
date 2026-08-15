package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.minecraft.client.gui.screens.Screen;
import slimeknights.tconstruct.smeltery.client.screen.IScreenWithFluidTank;

public final class TConstructEmiStackProvider implements EmiStackProvider<Screen> {
  @Override
  public EmiStackInteraction getStackAt(Screen screen, int mouseX, int mouseY) {
    if (screen instanceof IScreenWithFluidTank fluidScreen) {
      IScreenWithFluidTank.FluidLocation location = fluidScreen.getFluidUnderMouse(mouseX, mouseY);
      if (location != null && !location.fluid().isEmpty()) {
        // non-clickable: clicking a tank fluid is TiC's own interaction (move fluid to bottom / fill held item).
        // A clickable interaction makes EMI's MouseMixin intercept the press before the screen's mouseClicked
        // and start an EMI drag (or open the recipe on quick click), so the fluid can no longer be swapped.
        return new EmiStackInteraction(TConstructEmiRecipe.fluidIngredient(location.fluid()), null, false);
      }
    }
    return EmiStackInteraction.EMPTY;
  }
}
