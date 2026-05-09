package slimeknights.tconstruct.library.modifiers.fluid.general;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;

/** Set-block effect variant that stores the target block by ID for compat-only datagen entries. */
public record CompatSetBlockFluidEffect(ResourceLocation blockId) implements FluidEffect<FluidEffectContext> {
  public static final RecordLoadable<CompatSetBlockFluidEffect> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("block", CompatSetBlockFluidEffect::blockId),
    CompatSetBlockFluidEffect::new);

  @Override
  public RecordLoadable<CompatSetBlockFluidEffect> getLoader() {
    return LOADER;
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel level, FluidEffectContext context, FluidAction action) {
    Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(null);
    if (block == null) {
      return 0;
    }
    return new SetBlockFluidEffect(block).apply(fluid, level, context, action);
  }

  @Override
  public Component getDescription(RegistryAccess registryAccess) {
    Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(null);
    if (block == null) {
      return FluidEffect.makeTranslation(getLoader(), Component.literal(blockId.toString()));
    }
    BlockState state = block.defaultBlockState();
    return FluidEffect.makeTranslation(getLoader(), Component.translatable(state.getBlock().getDescriptionId()));
  }
}
