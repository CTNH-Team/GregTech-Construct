package slimeknights.tconstruct.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import slimeknights.tconstruct.library.tools.part.IRepairKitItem;
import slimeknights.tconstruct.tables.recipe.CraftingTableRepairKitRecipe;

@Mixin(value = CraftingTableRepairKitRecipe.class, remap = false)
public abstract class CraftingTableRepairKitRecipeMixin implements Recipe<CraftingContainer>
{
	public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingContainer container)
	{
		NonNullList<ItemStack> result = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

		for(int i = 0; i < result.size(); ++i)
		{
			ItemStack item = container.getItem(i);

			if (item.getItem() instanceof IRepairKitItem && item.hasCraftingRemainingItem())
			{
				result.set(i, item.getCraftingRemainingItem());
			}
		}

		return result;
	}
}
