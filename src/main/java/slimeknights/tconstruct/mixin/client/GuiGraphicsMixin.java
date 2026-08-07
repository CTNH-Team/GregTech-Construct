package slimeknights.tconstruct.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.library.client.StackCountFormatter;
import slimeknights.tconstruct.tables.client.inventory.BaseTabbedScreen;

/**
 * 槽位堆叠数量按 k/m/b 缩写，避免成千上万的数字溢出槽位；
 * 只作用于匠魂自己的表格界面 (BaseTabbedScreen 体系)，其他界面保持 vanilla 行为；
 * 按住 Shift 时显示未缩写的原始数量。
 * 只替换 vanilla 自动生成数量文本的路径 (传入 text 为 null 时)，
 * 其他 mod 显式传入的文本 (如精妙自己的特殊计数渲染) 不受影响。
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
  @Redirect(
    method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
    at = @At(value = "INVOKE", target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"))
  private String tconstruct$abbreviateStackCount(int count) {
    if (!(Minecraft.getInstance().screen instanceof BaseTabbedScreen)) {
      return String.valueOf(count);
    }
    return Screen.hasShiftDown() ? String.valueOf(count) : StackCountFormatter.abbreviate(count);
  }
}
