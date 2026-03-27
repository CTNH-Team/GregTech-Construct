package slimeknights.tconstruct.tools.modifiers.Botania;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.SingleLevelModifier;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import vazkii.botania.api.mana.ManaItemHandler;

public class ManaFixModifier extends SingleLevelModifier implements InventoryTickModifierHook {

  private static final int REPAIR_COST = 200;
  private static final int BROKEN_COST = 200000;

  public ManaFixModifier() {
  }
  @Override
  public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack)
  {
    if (world.isClientSide || !(holder instanceof Player player)) {
      return;
    }
    ToolStack toolStack = ToolStack.from(stack);
    
    // 先检查工具是否完全损坏，如果是则尝试用大量魔力完全修复
    if (toolStack.isBroken()) {
      if (ManaItemHandler.INSTANCE.requestManaExactForTool(stack, player, BROKEN_COST, true)) {
        toolStack.setDamage(0);
        toolStack.updateStack(stack);
      }
    }
    // 否则如果工具有损伤但未完全损坏，尝试用少量魔力修复 1 点耐久
    else if (toolStack.getDamage() > 0) {
      if (ManaItemHandler.INSTANCE.requestManaExactForTool(stack, player, REPAIR_COST, false)) {
        toolStack.setDamage(toolStack.getDamage() - 1);
        toolStack.updateStack(stack);
      }
    }
  }
}
