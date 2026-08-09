package slimeknights.tconstruct.plugin.emi;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EMI 的 byId 配方索引对重复 id 会互相覆盖并在每次 reload 刷
 * "recipes loaded with the same id" 日志。EMIPlugin.recipeId 对同一原始 id
 * 展开出的多条显示配方做唯一化:首个保持数据包原始 id(与 JEI 时代显示一致),
 * 后续条目使用 / 开头合成 id(EMI 合成配方约定,不做 RecipeManager 校验),
 * 并追加材料名(如 /manyullyn)保持可读与唯一;无法提供材料时退回数字序号。
 */
class EmiRecipeIdTest extends BaseMcTest {
  private static final ResourceLocation BASE = new ResourceLocation("tconstruct", "smeltery/casting/filling/test");

  @Test
  void firstRecipeKeepsOriginalId() {
    EMIPlugin.RECIPE_ID_COUNTS.clear();
    try {
      assertThat(EMIPlugin.recipeId(BASE)).isEqualTo(BASE);
    } finally {
      EMIPlugin.RECIPE_ID_COUNTS.clear();
    }
  }

  @Test
  void expandedRecipesUseMaterialDiscriminator() {
    EMIPlugin.RECIPE_ID_COUNTS.clear();
    try {
      assertThat(EMIPlugin.recipeId(BASE, "manyullyn")).isEqualTo(BASE);
      // 展开条目使用 / 开头合成 id(EMI 合成配方约定,不做 RecipeManager 校验),保留材料名可读
      assertThat(EMIPlugin.recipeId(BASE, "iron"))
          .isEqualTo(new ResourceLocation("tconstruct", "/" + BASE.getPath() + "/iron"));
      assertThat(EMIPlugin.recipeId(BASE, "cobalt"))
          .isEqualTo(new ResourceLocation("tconstruct", "/" + BASE.getPath() + "/cobalt"));
    } finally {
      EMIPlugin.RECIPE_ID_COUNTS.clear();
    }
  }

  @Test
  void repeatsWithoutDiscriminatorUseNumericSuffix() {
    EMIPlugin.RECIPE_ID_COUNTS.clear();
    try {
      assertThat(EMIPlugin.recipeId(BASE)).isEqualTo(BASE);
      assertThat(EMIPlugin.recipeId(BASE))
          .isEqualTo(new ResourceLocation("tconstruct", "/" + BASE.getPath() + "/2"));
      assertThat(EMIPlugin.recipeId(BASE, ""))
          .isEqualTo(new ResourceLocation("tconstruct", "/" + BASE.getPath() + "/3"));
    } finally {
      EMIPlugin.RECIPE_ID_COUNTS.clear();
    }
  }
}
