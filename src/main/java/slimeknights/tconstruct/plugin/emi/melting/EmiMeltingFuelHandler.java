package slimeknights.tconstruct.plugin.emi.melting;

import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuel;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class EmiMeltingFuelHandler {
  public static final EmiIngredient SOLID_FUELS = TConstructEmiRecipe.itemIngredient(List.of(
      new ItemStack(Items.COAL), new ItemStack(Items.CHARCOAL), new ItemStack(Blocks.OAK_LOG),
      new ItemStack(Blocks.OAK_PLANKS), new ItemStack(Items.BLAZE_ROD)));

  private static List<FuelTier> fuelLookup = List.of();

  private EmiMeltingFuelHandler() {}

  public static void setFuels(List<MeltingFuel> fuels) {
    List<MeltingFuel> sorted = new ArrayList<>(fuels);
    sorted.sort(Comparator.comparingInt(MeltingFuel::getTemperature));
    fuelLookup = sorted.stream()
        .mapToInt(MeltingFuel::getTemperature)
        .distinct()
        .mapToObj(temperature -> new FuelTier(temperature, sorted.stream()
            .filter(fuel -> fuel.getTemperature() >= temperature)
            .flatMap(fuel -> fuel.getInputs().stream())
            .toList()))
        .toList();
  }

  public static List<FluidStack> getUsableFuels(int temperature) {
    for (FuelTier tier : fuelLookup) {
      if (temperature <= tier.temperature()) {
        return tier.fluids();
      }
    }
    return List.of();
  }

  private record FuelTier(int temperature, List<FluidStack> fluids) {}
}
