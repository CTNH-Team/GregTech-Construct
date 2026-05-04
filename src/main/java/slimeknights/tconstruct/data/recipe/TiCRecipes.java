package slimeknights.tconstruct.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;

import java.util.function.Consumer;

/**
 * TiC 配方注册入口
 * 协调所有配方生成器，将配方注入动态数据包
 */
public class TiCRecipes {

    /**
     * 注册所有配方
     * 在 AddPackFindersEvent 中调用
     */
    public static void registerRecipes() {
        // 配方移除
        recipeRemoval();

        // 配方添加
        recipeAddition(TiCDynamicDataPack::addRecipe);
    }

    /**
     * 配方移除
     */
    private static void recipeRemoval() {
        // TODO: 实现配方移除逻辑
    }

    /**
     * 配方添加
     */
    private static void recipeAddition(Consumer<FinishedRecipe> consumer) {
        // Task 7 - 材料配方
        MaterialRecipeGenerator.register(consumer);

        // Task 8 - 工具配方
        ToolsRecipeGenerator.register(consumer);

        // TODO: Task 9 - 冶炼厂配方
        // SmelteryRecipeGenerator.register(consumer);

        // TODO: Task 10 - 修饰器配方
        // ModifierRecipeGenerator.register(consumer);
    }
}
