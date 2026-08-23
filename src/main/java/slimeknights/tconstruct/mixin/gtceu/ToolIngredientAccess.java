package slimeknights.tconstruct.mixin.gtceu;

import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.recipe.ingredient.ToolIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ToolIngredient.class)
/** Mixin bridge for the GT ingredient's otherwise private tool type. */
public interface ToolIngredientAccess {
  @Accessor(value = "toolType", remap = false)
  GTToolType tconstruct$getToolType();
}
