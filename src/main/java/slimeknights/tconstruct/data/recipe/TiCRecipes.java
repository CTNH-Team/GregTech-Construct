package slimeknights.tconstruct.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import slimeknights.tconstruct.data.material.MaterialDataGenerator;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;

import java.util.function.Consumer;

public class TiCRecipes {
    public static void registerRecipes() {
        registerAllRecipes(TiCDynamicDataPack::addRecipe);
    }

    private static void registerAllRecipes(Consumer<FinishedRecipe> consumer) {
        // 材料数据（定义、属性、特性）必须在配方之前注册
        MaterialDataGenerator.register();

        // 配方添加
        recipeAddition(consumer);
    }

    /**
     * 配方添加
     */
    private static void recipeAddition(Consumer<FinishedRecipe> consumer) {
        CommonRecipeGenerator.register(consumer);
        TableRecipeGenerator.register(consumer);
        GadgetRecipeGenerator.register(consumer);
        WorldRecipeGenerator.register(consumer);

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
