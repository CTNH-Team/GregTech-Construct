package slimeknights.tconstruct.data.recipe;

import net.minecraft.data.recipes.FinishedRecipe;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.data.recipe.IMaterialRecipeHelper;

import java.util.function.Consumer;

/**
 * 修饰器配方生成器
 * 运行时生成修饰器相关配方，替代 DataGen
 */
public class ModifierRecipeGenerator implements IMaterialRecipeHelper {

    /**
     * 注册所有修饰器配方
     * @param consumer 配方消费者（TiCDynamicDataPack::addRecipe）
     */
    public static void register(Consumer<FinishedRecipe> consumer) {
        ModifierRecipeGenerator generator = new ModifierRecipeGenerator();
        generator.addItemRecipes(consumer);
        generator.addModifierRecipes(consumer);
        generator.addTextureRecipes(consumer);
        generator.addHeadRecipes(consumer);
    }

    @Override
    public String getModId() {
        return TConstruct.MOD_ID;
    }

    private void addItemRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 ModifierRecipeProvider 迁移
    }

    private void addModifierRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 ModifierRecipeProvider 迁移
    }

    private void addTextureRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 ModifierRecipeProvider 迁移
    }

    private void addHeadRecipes(Consumer<FinishedRecipe> consumer) {
        // TODO: 从 ModifierRecipeProvider 迁移
    }
}
