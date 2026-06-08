/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create;

import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public final class CreateGoggleTooltipUtil {

    private CreateGoggleTooltipUtil() {}

    public static void addStats(List<Component> tooltip) {
        CreateLang.translate("tooltip.tconstruct.tinker_stats")
                .forGoggles(tooltip);
    }

    public static void addFluidStats(List<Component> tooltip) {
        CreateLang.translate("tooltip.tconstruct.tinker_stats.fluid")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
    }

    public static void addFluid(List<Component> tooltip, FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return;
        }
        CreateLang.fluidName(fluidStack)
                .add(CreateLang.text(" "))
                .style(ChatFormatting.GRAY)
                .add(CreateLang.number(fluidStack.getAmount())
                        .add(CreateLang.text("mB"))
                        .style(ChatFormatting.BLUE))
                .forGoggles(tooltip, 1);
    }

    public static void addProgress(List<Component> tooltip, int progress, int maxProgress) {
        if (maxProgress <= 0) {
            return;
        }
        CreateLang.translate("tooltip.tconstruct.progress")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        int percent = progress * 100 / maxProgress;
        CreateLang.number(percent)
                .add(CreateLang.text("%"))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
    }

    public static void addOutput(List<Component> tooltip, ItemStack output) {
        if (output.isEmpty()) {
            return;
        }
        CreateLang.translate("tooltip.tconstruct.output")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.itemName(output)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
    }
}
