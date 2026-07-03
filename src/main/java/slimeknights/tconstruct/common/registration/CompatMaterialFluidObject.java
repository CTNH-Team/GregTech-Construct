package slimeknights.tconstruct.common.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import slimeknights.mantle.fluid.TextureFluidType;
import slimeknights.mantle.registration.object.FlowingFluidObject;

import javax.annotation.Nullable;

public class CompatMaterialFluidObject extends FlowingFluidObject<ForgeFlowingFluid> {
  public CompatMaterialFluidObject(String modId, String name, FluidType.Properties properties) {
    super(ResourceLocation.tryBuild(modId, name), name, () -> new TextureFluidType(properties), () -> missing(name), () -> missing(name), null);
  }

  private static ForgeFlowingFluid missing(String name) {
    throw new IllegalStateException("Compat material fluid is provided by tag only: " + name);
  }

  @Nullable
  @Override
  public Item getBucket() {
    return null;
  }

  @Override
  public Item asItem() {
    return Items.AIR;
  }

  @Nullable
  @Override
  public LiquidBlock getBlock() {
    return null;
  }
}
