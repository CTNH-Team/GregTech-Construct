/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.modifier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.plugin.create.tag.CreateItemTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class CreateCrushingModifier extends NoLevelsModifier implements ProcessLootModifierHook, ToolStatsModifierHook {

    private final Cache<Item, Optional<AbstractCrushingRecipe>> recipeCache = CacheBuilder.newBuilder()
            .maximumSize(64)
            .build();
    private final ItemStackHandler inventory = new ItemStackHandler(1);

    public CreateCrushingModifier() {
        RecipeCacheInvalidator.addReloadListener(client -> {
            if (!client) {
                recipeCache.invalidateAll();
            }
        });
    }

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.PROCESS_LOOT, ModifierHooks.TOOL_STATS);
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> generatedLoot,
                            LootContext context) {
        if (generatedLoot.isEmpty()) {
            return;
        }

        Level level = context.getLevel();
        ToolDamageUtil.damage(tool, 5 * generatedLoot.size(), null, null);
        List<ItemStack> crushedLoot = new ArrayList<>();
        for (ItemStack originalStack : generatedLoot) {
            crushedLoot.addAll(crushItem(originalStack.copy(), level));
        }

        generatedLoot.clear();
        generatedLoot.addAll(crushedLoot);
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        builder.multiplier(ToolStats.MINING_SPEED, 0.75F);
        builder.multiplier(ToolStats.USE_ITEM_SPEED, 0.75F);
    }

    private List<ItemStack> crushItem(ItemStack stack, Level level) {
        if (stack.is(CreateItemTags.CRUSHING_BLACKLIST)) {
            return List.of(stack);
        }
        AbstractCrushingRecipe recipe = findCachedRecipe(stack, level);
        if (recipe == null) {
            return List.of(stack);
        }

        inventory.setStackInSlot(0, stack);
        List<ItemStack> results = recipe.rollResults();
        results.removeIf(ItemStack::isEmpty);
        results.forEach(result -> result.setCount(result.getCount() * stack.getCount()));
        return results;
    }

    @Nullable
    private AbstractCrushingRecipe findCachedRecipe(ItemStack stack, Level level) {
        if (stack.hasTag()) {
            return findRecipe(stack, level).orElse(null);
        }
        try {
            return recipeCache.get(stack.getItem(), () -> findRecipe(stack, level)).orElse(null);
        } catch (ExecutionException ignored) {
            return null;
        }
    }

    private Optional<AbstractCrushingRecipe> findRecipe(ItemStack stack, Level level) {
        inventory.setStackInSlot(0, stack);
        RecipeWrapper wrapper = new RecipeWrapper(inventory);
        Optional<AbstractCrushingRecipe> crushingRecipe = AllRecipeTypes.CRUSHING.find(wrapper, level);
        if (crushingRecipe.isPresent()) {
            return crushingRecipe;
        }
        return AllRecipeTypes.MILLING.find(wrapper, level);
    }
}
