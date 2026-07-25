package slimeknights.tconstruct.library.recipe.ingredient;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.assertj.core.api.Assertions.assertThat;

class MaterialToolIngredientTest extends BaseMcTest {
  private static final MaterialVariantId MATERIAL = MaterialVariantId.create("tconstruct", "test", "");

  @Test
  void matchesExplicitToolAtConfiguredMaterialIndex() {
    IModifiable selectedTool = Mockito.mock(IModifiable.class);
    Item selectedItem = Mockito.mock(Item.class, Mockito.withSettings().extraInterfaces(IModifiable.class));
    Mockito.when(selectedTool.asItem()).thenReturn(selectedItem);
    IToolContext tool = Mockito.mock(IToolContext.class);
    Mockito.when(tool.getItem()).thenReturn(selectedItem);
    Mockito.when(tool.getMaterial(1)).thenReturn(MaterialVariant.of(MATERIAL));
    MaterialToolIngredient ingredient = MaterialToolIngredient.builder(MATERIAL)
        .materialIndex(1)
        .tools(selectedTool)
        .build();

    assertThat(ingredient.test(tool)).isTrue();

    Mockito.when(tool.getMaterial(1)).thenReturn(MaterialVariant.UNKNOWN);
    assertThat(ingredient.test(tool)).isFalse();
  }

  @Test
  void matchesModifiableArmorByArmorType() {
    ArmorItem helmet = Mockito.mock(ArmorItem.class, Mockito.withSettings().extraInterfaces(IModifiable.class));
    IToolContext tool = Mockito.mock(IToolContext.class);
    Mockito.when(tool.getItem()).thenReturn(helmet);
    Mockito.when(helmet.getType()).thenReturn(ArmorItem.Type.HELMET);
    Mockito.when(tool.getMaterial(0)).thenReturn(MaterialVariant.of(MATERIAL));
    MaterialToolIngredient ingredient = MaterialToolIngredient.builder(MATERIAL)
        .armorTypes(ArmorItem.Type.HELMET)
        .build();

    assertThat(ingredient.test(tool)).isTrue();

    Mockito.when(helmet.getType()).thenReturn(ArmorItem.Type.CHESTPLATE);
    assertThat(ingredient.test(tool)).isFalse();
  }

  @Test
  void providesFallbackDisplayWhenNoSelectedToolsAreRegistered() {
    MaterialToolIngredient ingredient = MaterialToolIngredient.builder(MATERIAL)
        .armorTypes(ArmorItem.Type.HELMET)
        .build();

    assertThat(ingredient.isEmpty()).isFalse();
    assertThat(ingredient.getItems()).hasSize(1);
    assertThat(ingredient.getItems()[0].getItem()).isEqualTo(Blocks.BARRIER.asItem());
  }
}