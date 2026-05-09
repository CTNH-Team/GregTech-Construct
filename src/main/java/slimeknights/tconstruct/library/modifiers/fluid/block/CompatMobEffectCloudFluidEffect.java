package slimeknights.tconstruct.library.modifiers.fluid.block;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.fluid.CompatFluidMobEffect;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.TimeAction;

import java.util.List;

/** Cloud effect variant that stores mob effects by ID for compat-only datagen entries. */
public record CompatMobEffectCloudFluidEffect(List<CompatFluidMobEffect> effects) implements FluidEffect<FluidEffectContext.Block> {
  public static final RecordLoadable<CompatMobEffectCloudFluidEffect> LOADER = RecordLoadable.create(
    CompatFluidMobEffect.LOADABLE.list(1).requiredField("effects", CompatMobEffectCloudFluidEffect::effects),
    CompatMobEffectCloudFluidEffect::new);

  @Override
  public RecordLoadable<CompatMobEffectCloudFluidEffect> getLoader() {
    return LOADER;
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel level, FluidEffectContext.Block context, FluidAction action) {
    if (!context.isOffsetReplaceable()) {
      return 0;
    }
    float scale = level.value();
    if (action.execute()) {
      AreaEffectCloud cloud = MobEffectCloudFluidEffect.makeCloud(context);
      boolean hasEffects = false;
      for (CompatFluidMobEffect effect : effects) {
        int time = (int) (effect.time() * scale);
        if (time > 10) {
          var instance = effect.effectWithTime(time);
          if (instance != null) {
            cloud.addEffect(instance);
            hasEffects = true;
          }
        }
      }
      if (hasEffects) {
        context.getLevel().addFreshEntity(cloud);
      } else {
        cloud.discard();
        return 0;
      }
    }
    return scale;
  }

  @Override
  public Component getDescription(RegistryAccess registryAccess) {
    return FluidEffect.makeTranslation(
      getLoader(),
      effects.stream()
             .map(effect -> effect.getDisplayName(TimeAction.SET))
             .reduce(FluidEffect.MERGE_COMPONENT_LIST)
             .orElse(Component.empty()));
  }
}
