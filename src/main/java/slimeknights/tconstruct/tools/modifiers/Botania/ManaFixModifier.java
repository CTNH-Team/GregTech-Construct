package slimeknights.tconstruct.tools.modifiers.Botania;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import vazkii.botania.api.mana.ManaItemHandler;

public class ManaFixModifier extends Modifier implements InventoryTickModifierHook {

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
        int repairAmount = modifier.getLevel();
        
        if (toolStack.getDamage() > 0) {
          if (ManaItemHandler.INSTANCE.requestManaExactForTool(stack, player, REPAIR_COST * repairAmount, false)) {
                toolStack.setDamage(toolStack.getDamage() - repairAmount);
                toolStack.updateStack(stack);
            }
        }
        else if (toolStack.isBroken()) {
            if (ManaItemHandler.INSTANCE.requestManaExactForTool(stack, player, BROKEN_COST, true)) {
                toolStack.setDamage(0);
                toolStack.updateStack(stack);
            }
        }
    }
}
