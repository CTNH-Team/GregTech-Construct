package slimeknights.tconstruct.plugin.botania.modifier;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import slimeknights.tconstruct.library.modifiers.ModifierSetBonusHelper;
import slimeknights.tconstruct.library.modifiers.impl.SingleLevelModifier;
import vazkii.botania.api.mana.ManaDiscountEvent;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.ManaTabletItem;
import vazkii.botania.common.item.equipment.bauble.BandOfManaItem;
import vazkii.botania.xplat.XplatAbstractions;

public class TerraSetBonusModifier extends SingleLevelModifier {
    static final int MANA_GENERATION = 4;
    private static final float MANA_DISCOUNT = 0.2F;

    public static boolean isActive(Player player) {
        return ModifierSetBonusHelper.hasFullSet(player, BotaniaModifierIds.terrarecover);
    }

    public static void onManaDiscount(ManaDiscountEvent event) {
        if (isActive(event.getEntityPlayer())) {
            event.setDiscount(event.getDiscount() + MANA_DISCOUNT);
        }
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level().isClientSide && isActive(player)) {
            generateMana(player, player.getItemBySlot(EquipmentSlot.HEAD));
        }
    }

    private static void generateMana(Player player, ItemStack source) {
        if (!generateMana(ManaItemHandler.INSTANCE.getManaItems(player), source)) {
            generateMana(ManaItemHandler.INSTANCE.getManaAccesories(player), source);
        }
    }

    private static boolean generateMana(Iterable<ItemStack> stacks, ItemStack source) {
        for (ItemStack stack : stacks) {
            ManaItem manaItem = getManaStorage(stack, source);
            if (manaItem != null) {
                manaItem.addMana(MANA_GENERATION);
                return true;
            }
        }
        return false;
    }

    private static ManaItem getManaStorage(ItemStack stack, ItemStack source) {
        if (stack == source || !(stack.getItem() instanceof ManaTabletItem || stack.getItem() instanceof BandOfManaItem)) {
            return null;
        }
        ManaItem manaItem = XplatAbstractions.INSTANCE.findManaItem(stack);
        if (manaItem == null || manaItem.getMana() >= manaItem.getMaxMana() || !manaItem.canReceiveManaFromItem(source)) {
            return null;
        }
        return manaItem;
    }
}
