package slimeknights.tconstruct.library.tools.item;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModifiableGTToolItem extends ModifiableItem implements IGTTool {

    private final GTToolType gtToolType;

    public ModifiableGTToolItem(Properties properties, ToolDefinition tcToolDefinition) {
        super(properties, tcToolDefinition);
        String name = tcToolDefinition.getId().getPath();
        this.gtToolType = GTToolType.getTypes().get(name);
    }

    @Override
    public GTToolType getToolType() {
        return gtToolType;
    }

    @Override
    public Material getMaterial() {
        return null;
    }

    @Override
    public boolean isElectric() {
        return false;
    }

    @Override
    public int getElectricTier() {
        return 0;
    }

    @Override
    public IGTToolDefinition getToolStats() {
        return gtToolType.toolDefinition;
    }

    @Override
    public @Nullable SoundEntry getSound() {
        return gtToolType.soundEntry;
    }

    @Override
    public boolean playSoundOnBlockDestroy() {
        return gtToolType.playSoundOnBlockDestroy;
    }

    @Override
    public @Nullable ToolProperty getToolProperty() {
        return IGTTool.super.getToolProperty();
    }

    @Override
    public float getTotalToolSpeed(ItemStack stack) {
        if (ToolStack.from(stack).isBroken()) {
            return 0;
        }
        return ToolStack.from(stack).getStats().get(ToolStats.MINING_SPEED);
    }

    @Override
    public float getTotalAttackDamage(ItemStack stack) {
        return ToolStack.from(stack).getStats().get(ToolStats.ATTACK_DAMAGE);
    }

    @Override
    public float getTotalAttackSpeed(ItemStack stack) {
        return ToolStack.from(stack).getStats().get(ToolStats.ATTACK_SPEED);
    }

    @Override
    public int getTotalMaxDurability(ItemStack stack) {
        return super.getMaxDamage(stack);
    }

    @Override
    public int getTotalEnchantability(ItemStack stack) {
        return IGTTool.super.getTotalEnchantability(stack);
    }

    @Override
    public int getTotalHarvestLevel(ItemStack stack) {
        return IGTTool.super.getTotalHarvestLevel(stack);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        return super.onBlockStartBreak(stack, pos, player);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        return super.mineBlock(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
        return super.getAllEnchantments(stack);
    }

    @Override
    public Map<Enchantment, Integer> getDefaultEnchantments(ItemStack stack) {
        return IGTTool.super.getDefaultEnchantments(stack);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        return super.getEnchantmentLevel(stack, enchantment);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return super.canDisableShield(stack, shield, entity, attacker);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return super.shouldCauseBlockBreakReset(oldStack, newStack);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return super.hasCraftingRemainingItem(stack);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return super.getCraftingRemainingItem(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public int getDamage(ItemStack stack) {
        return super.getDamage(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return super.getMaxDamage(stack);
    }


    public void init() {
        IGTTool.super.definition$init();
    }

    @Override
    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ToolAction action) {
        return super.canPerformAction(stack, action);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (stack.getCount() == 1) {
            ToolStack tool = ToolStack.from(stack);
            InteractionHand hand = context.getHand();
            InteractionResult result = IGTTool.super.definition$onItemUseFirst(stack, context);
            if(result != InteractionResult.PASS) return result;
            if (shouldInteract(context.getPlayer(), tool, hand)) {
                for (ModifierEntry entry : tool.getModifierList()) {
                    result = entry.getHook(ModifierHooks.BLOCK_INTERACT).beforeBlockUse(tool, entry, context, InteractionSource.RIGHT_CLICK);
                    if (result.consumesAction()) {
                        return result;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = definition$use(world, player, hand);
        if(result.getResult().consumesAction()) return result;
        return super.use(world, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = IGTTool.super.definition$onItemUse(context);
        if(result != InteractionResult.PASS) return result;
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
    }

    @Override
    public boolean canApplyAtEnchantingTable(@NotNull ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void playCraftingSound(Player player, ItemStack stack) {
        IGTTool.super.playCraftingSound(player, stack);
    }

    @Override
    public void setLastCraftingSoundTime(ItemStack stack) {
        IGTTool.super.setLastCraftingSoundTime(stack);
    }

    @Override
    public boolean canPlaySound(ItemStack stack) {
        return IGTTool.super.canPlaySound(stack);
    }

    @Override
    public void playSound(Player player) {
        IGTTool.super.playSound(player);
    }

    @Override
    public ModularUI createUI(Player player, HeldItemUIFactory.HeldItemHolder holder) {
        return IGTTool.super.createUI(player, holder);
    }

    @Override
    public Set<GTToolType> getToolClasses(ItemStack stack) {
        return IGTTool.super.getToolClasses(stack);
    }

    @Override
    public Set<String> getToolClassNames(ItemStack stack) {
        return IGTTool.super.getToolClassNames(stack);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return super.initCapabilities(stack, nbt);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return super.isEnabled(enabledFeatures);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        return super.onDroppedByPlayer(item, player);
    }

    @Override
    public Component getHighlightTip(ItemStack item, Component displayName) {
        return super.getHighlightTip(item, displayName);
    }

    @Override
    public boolean isPiglinCurrency(ItemStack stack) {
        return super.isPiglinCurrency(stack);
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        return super.makesPiglinsNeutral(stack, wearer);
    }

    @Override
    public float getXpRepairRatio(ItemStack stack) {
        return super.getXpRepairRatio(stack);
    }

    @Override
    public @Nullable CompoundTag getShareTag(ItemStack stack) {
        return super.getShareTag(stack);
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt) {
        super.readShareTag(stack, nbt);
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return super.getEntityLifespan(itemStack, level);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        return super.onEntityItemUpdate(stack, entity);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return super.doesSneakBypassUse(stack, level, pos, player);
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) {
        return super.canEquip(stack, armorType, entity);
    }

    @Override
    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return super.getArmorTexture(stack, entity, slot, type);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return super.onEntitySwing(stack, entity);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return super.isDamaged(stack);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return super.getEnchantmentValue(stack);
    }

    @Override
    public @Nullable String getCreatorModId(ItemStack itemStack) {
        return super.getCreatorModId(itemStack);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return super.getBurnTime(itemStack, recipeType);
    }

    @Override
    public void onHorseArmorTick(ItemStack stack, Level level, Mob horse) {
        super.onHorseArmorTick(stack, level, horse);
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        super.onDestroyed(itemEntity, damageSource);
    }

    @Override
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return super.isEnderMask(stack, player, endermanEntity);
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return super.canElytraFly(stack, entity);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        return super.elytraFlightTick(stack, entity, flightTicks);
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return super.canWalkOnPowderedSnow(stack, wearer);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return super.isDamageable(stack);
    }

    @Override
    public @NotNull AABB getSweepHitBox(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity target) {
        return super.getSweepHitBox(stack, player, target);
    }

    @Override
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return super.getFoodProperties(stack, entity);
    }

    @Override
    public boolean canGrindstoneRepair(ItemStack stack) {
        return super.canGrindstoneRepair(stack);
    }

    @Override
    public Component getLocalizedName() {
        return super.getLocalizedName();
    }

    @Override
    public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
        return super.getStatInformation(tool, player, tooltips, key, tooltipFlag);
    }
}
