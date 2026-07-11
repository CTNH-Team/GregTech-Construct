package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public abstract class TConstructEmiRecipe implements EmiRecipe {
  private final ResourceLocation id;
  private final EMIConstants.TConstructEmiCategory category;
  private final List<EmiIngredient> inputs;
  private final List<EmiIngredient> catalysts;
  private final List<EmiStack> outputs;

  protected TConstructEmiRecipe(@Nullable ResourceLocation id, EMIConstants.TConstructEmiCategory category,
                                List<EmiIngredient> inputs, List<EmiIngredient> catalysts, List<EmiStack> outputs) {
    this.inputs = nonEmptyIngredients(inputs);
    this.catalysts = nonEmptyIngredients(catalysts);
    this.outputs = nonEmptyOutputs(outputs);
    this.id = id == null ? syntheticId(category, this.inputs, this.catalysts, this.outputs) : id;
    this.category = category;
  }

  public static EmiIngredient itemIngredient(ItemStack stack) {
    return EmiStack.of(stack);
  }

  public static EmiIngredient itemIngredient(List<ItemStack> stacks) {
    return EmiIngredient.of(stacks.stream()
        .filter(stack -> !stack.isEmpty())
        .map(EmiStack::of)
        .toList());
  }

  public static EmiIngredient fluidIngredient(FluidStack stack) {
    return fluidStack(stack);
  }

  private static EmiStack fluidStack(FluidStack stack) {
    if (stack.isEmpty()) {
      return EmiStack.EMPTY;
    }
    CompoundTag tag = stack.getTag();
    return EmiStack.of(stack.getFluid(), tag == null ? null : tag.copy(), stack.getAmount());
  }

  public static EmiIngredient fluids(List<FluidStack> stacks) {
    return EmiIngredient.of(stacks.stream()
        .filter(stack -> !stack.isEmpty())
        .map(TConstructEmiRecipe::fluidStack)
        .toList());
  }

  public static List<EmiIngredient> itemInputs(List<? extends ItemStack> stacks) {
    return stacks.stream()
        .filter(stack -> !stack.isEmpty())
        .map(TConstructEmiRecipe::itemIngredient)
        .toList();
  }

  public static List<EmiStack> itemOutputs(List<? extends ItemStack> stacks) {
    return stacks.stream()
        .filter(stack -> !stack.isEmpty())
        .map(EmiStack::of)
        .toList();
  }

  public static List<EmiStack> fluidOutputs(List<FluidStack> stacks) {
    return stacks.stream()
        .filter(stack -> !stack.isEmpty())
        .map(TConstructEmiRecipe::fluidStack)
        .toList();
  }

  protected final void addBackground(WidgetHolder widgets) {
    widgets.addTexture(category.getTexture(), 0, 0, getDisplayWidth(), getDisplayHeight(),
        category.getTextureU(), category.getTextureV(), getDisplayWidth(), getDisplayHeight(), 256, 256);
  }

  @Override
  public final EmiRecipeCategory getCategory() {
    return category;
  }

  @Override
  public final ResourceLocation getId() {
    return id;
  }

  @Override
  public final List<EmiIngredient> getInputs() {
    return inputs;
  }

  @Override
  public final List<EmiIngredient> getCatalysts() {
    return catalysts;
  }

  @Override
  public final List<EmiStack> getOutputs() {
    return outputs;
  }

  @Override
  public final int getDisplayWidth() {
    return category.getDisplayWidth();
  }

  @Override
  public final int getDisplayHeight() {
    return category.getDisplayHeight();
  }

  private static List<EmiIngredient> nonEmptyIngredients(List<EmiIngredient> ingredients) {
    return ingredients.stream()
        .filter(ingredient -> ingredient != null && !ingredient.isEmpty())
        .toList();
  }

  private static List<EmiStack> nonEmptyOutputs(List<EmiStack> outputs) {
    return outputs.stream()
        .filter(output -> output != null && !output.isEmpty())
        .toList();
  }

  private static ResourceLocation syntheticId(EmiRecipeCategory category, List<EmiIngredient> inputs,
                                              List<EmiIngredient> catalysts, List<EmiStack> outputs) {
    StringBuilder descriptor = new StringBuilder(category.getId().toString());
    appendIngredients(descriptor, inputs);
    appendIngredients(descriptor, catalysts);
    appendIngredients(descriptor, outputs);
    UUID uuid = UUID.nameUUIDFromBytes(descriptor.toString().getBytes(StandardCharsets.UTF_8));
    return TConstruct.getResource("/emi/" + category.getId().getPath() + "/" + uuid);
  }

  private static void appendIngredients(StringBuilder descriptor, List<? extends EmiIngredient> ingredients) {
    descriptor.append('|').append('[');
    for (EmiIngredient ingredient : ingredients) {
      descriptor.append(ingredient.getClass().getName())
          .append(':').append(ingredient.getAmount())
          .append(':').append(ingredient.getChance())
          .append('{');
      for (EmiStack stack : ingredient.getEmiStacks()) {
        descriptor.append(stack.getId())
            .append(':').append(stack.getAmount())
            .append(':').append(String.valueOf(stack.getNbt()))
            .append(';');
      }
      descriptor.append("};");
    }
    descriptor.append(']');
  }
}
