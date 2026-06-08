/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.mixin.tconstruct.create;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import slimeknights.tconstruct.plugin.create.CreateGoggleTooltipUtil;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;

import java.util.List;

@Mixin(CastingBlockEntity.class)
public class CastingBlockEntityGoggleTooltipMixin implements IHaveGoggleInformation {

    @Override
    public ItemStack getIcon(boolean isPlayerSneaking) {
        return new ItemStack(TinkerCommons.mightySmelting);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CastingBlockEntity casting = (CastingBlockEntity) (Object) this;
        boolean hasInfo = false;

        CreateGoggleTooltipUtil.addStats(tooltip);
        IFluidHandler fluidHandler = casting.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
        if (fluidHandler != null && !fluidHandler.getFluidInTank(0).isEmpty()) {
            CreateGoggleTooltipUtil.addFluidStats(tooltip);
            CreateGoggleTooltipUtil.addFluid(tooltip, fluidHandler.getFluidInTank(0));
            hasInfo = true;
        }

        if (casting.getCoolingTime() > 0) {
            CreateGoggleTooltipUtil.addOutput(tooltip, casting.getRecipeOutput());
            CreateGoggleTooltipUtil.addProgress(tooltip, casting.getTimer(), casting.getCoolingTime());
            hasInfo = true;
        }

        if (!hasInfo) {
            tooltip.remove(0);
        }
        return hasInfo;
    }
}
