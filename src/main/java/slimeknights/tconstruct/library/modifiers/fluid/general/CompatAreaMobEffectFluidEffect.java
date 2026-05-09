package slimeknights.tconstruct.library.modifiers.fluid.general;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.fluid.CompatFluidMobEffect;
import slimeknights.tconstruct.library.modifiers.fluid.EffectLevel;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffect;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.GroupCost;
import slimeknights.tconstruct.library.modifiers.fluid.TimeAction;

/** Area mob effect variant that stores the effect by ID for compat-only datagen entries. */
public record CompatAreaMobEffectFluidEffect(CompatFluidMobEffect effect, TimeAction action, GroupCost groupCost) implements FluidEffect<FluidEffectContext> {
  public static final RecordLoadable<CompatAreaMobEffectFluidEffect> LOADER = RecordLoadable.create(
    CompatFluidMobEffect.LOADABLE.directField(CompatAreaMobEffectFluidEffect::effect),
    TimeAction.LOADABLE.requiredField("action", CompatAreaMobEffectFluidEffect::action),
    GroupCost.LOADABLE.requiredField("group_cost", CompatAreaMobEffectFluidEffect::groupCost),
    CompatAreaMobEffectFluidEffect::new);

  @Override
  public RecordLoadable<CompatAreaMobEffectFluidEffect> getLoader() {
    return LOADER;
  }

  @Override
  public float apply(FluidStack fluid, EffectLevel level, FluidEffectContext context, FluidAction action) {
    float used = 0;
    for (LivingEntity living : context.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(context.getBlockPos()))) {
      float localUsed = effect.apply(living, level, this.action, action);
      if (groupCost == GroupCost.SUM) {
        used += localUsed;
        level = level.subtract(localUsed);
      } else if (localUsed > used) {
        used = localUsed;
      }
    }
    return used;
  }

  @Override
  public Component getDescription(RegistryAccess registryAccess) {
    return effect.getDisplayName(action);
  }
}
