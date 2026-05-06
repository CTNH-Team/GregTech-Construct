package slimeknights.tconstruct.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import slimeknights.tconstruct.data.material.MaterialDataGenerator;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;

import java.util.function.Consumer;

public class TiCRecipes {

    public static void registerRecipes() {
        // 材料数据（定义、属性、特性）必须在配方之前注册
        MaterialDataGenerator.register();

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

        // Task 9 - 冶炼厂配方
        SmelteryRecipeGenerator.register(consumer);

        // Task 10 - 修饰器配方
        ModifierRecipeGenerator.register(consumer);
    }
}
