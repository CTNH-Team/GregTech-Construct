package slimeknights.tconstruct.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import slimeknights.mantle.recipe.data.ICommonRecipeHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.data.recipe.ISmelteryRecipeHelper;

import java.util.function.Consumer;

/**
 * 冶炼厂配方生成器
 * 运行时生成冶炼厂相关配方，替代 DataGen
 */
public class SmelteryRecipeGenerator implements ISmelteryRecipeHelper, ICommonRecipeHelper {

    /**
     * 注册所有冶炼厂配方
     * @param consumer 配方消费者（TiCDynamicDataPack::addRecipe）
     */
    public static void register(Consumer<FinishedRecipe> consumer) {
        SmelteryRecipeGenerator generator = new SmelteryRecipeGenerator();
        generator.addCraftingRecipes(consumer);
        generator.addSmelteryRecipes(consumer);
        generator.addFoundryRecipes(consumer);
        generator.addTagRecipes(consumer);
        generator.addMeltingRecipes(consumer);
        generator.addCastingRecipes(consumer);
        generator.addAlloyRecipes(consumer);
        generator.addEntityMeltingRecipes(consumer);
    }

    @Override
    public String getModId() {
        return TConstruct.MOD_ID;
    }

    private void addCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 SmelteryRecipeProvider 迁移
    }

    private void addSmelteryRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 SmelteryRecipeProvider 迁移
    }

    private void addFoundryRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 SmelteryRecipeProvider 迁移
    }

    private void addTagRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 SmelteryRecipeProvider 迁移
    }

    private void addMeltingRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 SmelteryRecipeProvider 迁移
    }

    private void addCastingRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 SmelteryRecipeProvider 迁移
    }

    private void addAlloyRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 SmelteryRecipeProvider 迁移
    }

    private void addEntityMeltingRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 SmelteryRecipeProvider 迁移
    }
}
