package slimeknights.tconstruct.tools.modifiers.Botania;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InventoryTickModifierHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;

public class TerraRecoverModifier extends Modifier implements InventoryTickModifierHook {
    private static final int MANA = 200;
    private static final int HEAL_INTERVAL = 40;

    public TerraRecoverModifier() {
    }
    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level level, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack itemStack) {
        if (level.isClientSide || !(holder instanceof Player player)) return;
        boolean success =false;
        if (level.getGameTime() % HEAL_INTERVAL == 0 && player.getHealth() < player.getMaxHealth()) {

            for (ItemStack stack : ManaItemHandler.instance().getManaAccesories(player)) {
                if (!stack.isEmpty() && stack.getItem() instanceof ManaItem manaItem
                        && manaItem.canReceiveManaFromItem(stack)) {
                    success = ManaItemHandler.instance().dispatchManaExact(stack, player, MANA * modifier.getLevel(), true);
                    if (success) break;
                }
            }
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.getItem() instanceof ManaItem manaItem
                        && manaItem.canReceiveManaFromItem(stack)) {
                    success = ManaItemHandler.instance().dispatchManaExact(stack, player, MANA * modifier.getLevel(), true);
                    if (success) break;
                }
            }if (success) {
                player.heal(1.0F * modifier.getLevel());
            }
        }
    }
}