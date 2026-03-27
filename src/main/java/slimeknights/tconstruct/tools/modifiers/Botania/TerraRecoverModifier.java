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

public class TerraRecoverModifier extends Modifier implements InventoryTickModifierHook {
    private static final int MANA = 200;
    private static final int HEAL_INTERVAL = 40;

    public TerraRecoverModifier() {
    }
    @Override
    protected final void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.INVENTORY_TICK);
    }
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (level.isClientSide || !(holder instanceof Player player)) {
            return;
        }
        if (level.getGameTime() % HEAL_INTERVAL == 0 && player.getHealth() < player.getMaxHealth()) {
            if (ManaItemHandler.INSTANCE.requestManaExactForTool(stack, player, MANA * modifier.getLevel(), true)) {
                player.heal(1.0F * modifier.getLevel());
            }
        }
    }
}
