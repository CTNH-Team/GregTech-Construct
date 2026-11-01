package slimeknights.tconstruct.common.registration;

import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.loading.LoadingModList;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.registration.deferred.FluidDeferredRegister;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.tconstruct.library.utils.Util;

import static slimeknights.tconstruct.fluids.block.BurningLiquidBlock.createBurning;

/** Extension of the fluid register to add a few common tinkers fluid behaviors. */
public class FluidDeferredRegisterExtension extends FluidDeferredRegister {
  private static final Logger LOGGER = Util.getLogger("FluidRegister");
  /** Mod that provides the molten material fluids for the built in compat metals. */
  private static final String COMPAT_PROVIDER = "gtceu";

  private final String modId;

  public FluidDeferredRegisterExtension(String modID) {
    super(modID);
    this.modId = modID;
  }

  /** Registers a slime fluid with slime-like behavior. Slows down the flow rate but doesn't reduce flow speed. */
  public Builder registerSlime(String name) {
    return register(name).tickRate(50);
  }

  /** Registers a fluid with stone-like behavior. Has even less flow distance than lava but flows a bit faster */
  public Builder registerStone(String name) {
    return register(name).levelDecreasePerBlock(2).slopeFindDistance(2).tickRate(40);
  }

  /** Registers a hot fluid with half flow rate. */
  public Builder registerMetal(String name) {
    return register(name).levelDecreasePerBlock(3).slopeFindDistance(3).tickRate(30);
  }

  /**
   * Registers a metal fluid supplied by another mod through the common tag.
   * <p>
   * When {@code providerModId} is loaded the other mod owns the fluid, so this only exposes a tag backed object and no
   * fluid is registered. When the provider is missing nothing would fill the common tag, which would leave recipes
   * producing an empty {@link net.minecraftforge.fluids.FluidStack}; in that case the fluid is registered locally so the
   * recipes keep working.
   *
   * @param name          Fluid name, also used as the common tag name
   * @param properties    Fluid type properties to use when registering locally
   * @param providerModId Mod expected to supply the fluid through the common tag
   * @return Fluid object, either tag backed or locally registered
   */
  public FlowingFluidObject<ForgeFlowingFluid> registerCompatMetal(String name, FluidType.Properties properties, String providerModId) {
    if (isModLoading(providerModId)) {
      return new CompatMaterialFluidObject(modId, name, properties);
    }
    LOGGER.debug("Compat metal fluid provider '{}' is missing, registering '{}' locally", providerModId, name);
    return registerMetal(name)
      .type(properties)
      .block(createBurning(MapColor.METAL, 12, 10, 5f))
      .bucket()
      .commonTag()
      .flowing();
  }

  /** Registers a metal fluid, defaulting to GTCEu as the provider. */
  public FlowingFluidObject<ForgeFlowingFluid> registerCompatMetal(String name, FluidType.Properties properties) {
    return registerCompatMetal(name, properties, COMPAT_PROVIDER);
  }

  /** Checks if the given mod will be loaded, safe to call while mods are still constructing. */
  private static boolean isModLoading(String modId) {
    LoadingModList loadingModList = LoadingModList.get();
    return loadingModList != null && loadingModList.getModFileById(modId) != null;
  }

  /** Registers a fluid with gem-like behavior. Flows just the one block */
  public Builder registerGem(String name) {
    return register(name).levelDecreasePerBlock(4).slopeFindDistance(4).tickRate(45);
  }

  /** Registers a fluid with glass-like behavior. Minimal flow distance */
  public Builder registerGlass(String name) {
    return register(name).levelDecreasePerBlock(8).slopeFindDistance(8).tickRate(30);
  }
}
