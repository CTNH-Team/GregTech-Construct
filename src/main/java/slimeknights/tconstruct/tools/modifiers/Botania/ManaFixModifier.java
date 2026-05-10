package slimeknights.tconstruct.tools.modifiers.Botania;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import vazkii.botania.api.mana.ManaItemHandler;

public class ManaFixModifier extends Modifier implements InventoryTickModifierHook {

    private static final int REPAIR_COST = 200;

    public ManaFixModifier() {
    }
    @Override
    protected final void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack)
    {
        if (world.isClientSide || !(holder instanceof Player player)) {
            return;
        }
        int repairAmount = modifier.getLevel();
        
        if (tool.getDamage() > 0) {
          if (ManaItemHandler.INSTANCE.requestManaExactForTool(stack, player, REPAIR_COST * repairAmount, true)) {
                tool.setDamage(tool.getDamage() - Math.min(tool.getDamage(), repairAmount));
            }
        }
    }
}
