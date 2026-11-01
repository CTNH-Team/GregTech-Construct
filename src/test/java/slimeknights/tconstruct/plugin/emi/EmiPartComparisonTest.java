package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiComparisonDefaults;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.tconstruct.test.BaseMcTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EMI 的配方来源/用途索引默认只按物品 id 匹配(ComparisonHashStrategy 使用
 * EmiComparisonDefaults.get(item)),因此查询一个材料部件时会把该部件所有材料
 * 变体的配方一起返回。TConstructEmiFilterPlugin.registerPartComparisons 通过为
 * parts 标签下的部件注册 NBT 比较修复此问题;本测试在真实 EMI 类上验证该比较的
 * 效果,即同一部件不同材料 NBT 的堆叠不再相等,相同材料 NBT 仍相等。
 */
class EmiPartComparisonTest extends BaseMcTest {

  /** 构造一个带材料 NBT 的堆叠,模拟部件(如某材料的镐头) */
  private static EmiStack partStack(String material) {
    ItemStack stack = new ItemStack(Items.IRON_HELMET);
    stack.getOrCreateTag().putString("Material", material);
    return EmiStack.of(stack);
  }

  /** 未注册任何比较时,同物品不同 NBT 的堆叠相等 —— "查到所有部件"的根因 */
  @Test
  void stacksOfDifferentMaterialAreEqualByDefault() {
    assertThat(partStack("tconstruct:iron").isEqual(partStack("tconstruct:manyullyn"))).isTrue();
  }

  /** 注册 NBT 比较后,不同材料 NBT 不再相等,相同材料 NBT 仍相等 */
  @Test
  void registeredComparisonDistinguishesMaterialVariants() {
    Map<Object, Comparison> original = EmiComparisonDefaults.comparisons;
    try {
      EmiComparisonDefaults.comparisons = new HashMap<>();
      EmiComparisonDefaults.comparisons.put(Items.IRON_HELMET, Comparison.compareNbt());

      assertThat(partStack("tconstruct:iron").isEqual(partStack("tconstruct:manyullyn"))).isFalse();
      assertThat(partStack("tconstruct:iron").isEqual(partStack("tconstruct:iron"))).isTrue();
    } finally {
      EmiComparisonDefaults.comparisons = original;
    }
  }
}
