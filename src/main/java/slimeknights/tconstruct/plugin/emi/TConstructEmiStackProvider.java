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
        return new EmiStackInteraction(TConstructEmiRecipe.fluidIngredient(location.fluid()));
      }
    }
    return EmiStackInteraction.EMPTY;
  }
}
