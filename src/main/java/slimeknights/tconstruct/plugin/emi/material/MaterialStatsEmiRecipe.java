package slimeknights.tconstruct.plugin.emi.material;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolPartsHook;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.plugin.emi.EMIConstants;
import slimeknights.tconstruct.plugin.emi.TConstructEmiRecipe;
import slimeknights.tconstruct.plugin.emi.util.EmiRenderHelper;
import slimeknights.tconstruct.tools.TinkerToolParts;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

public class MaterialStatsEmiRecipe extends TConstructEmiRecipe {
  @Getter
  private final IMaterial material;
  @Getter
  private final MaterialId materialId;
  @Getter
  private final List<MaterialStatsId> statIds;
  private final FluidStack fluid;
  private final List<ItemStack> parts;

  public MaterialStatsEmiRecipe(EMIConstants.TConstructEmiCategory category, IMaterial material,
                               List<MaterialStatsId> statIds, @Nullable TagKey<Item> partTag,
                               @Nullable RecipeManager recipeManager) {
    this(category, material, statIds, partTag, recipeManager,
        findFluid(material.getIdentifier()), null);
  }

  protected MaterialStatsEmiRecipe(EMIConstants.TConstructEmiCategory category, IMaterial material,
                                   List<MaterialStatsId> statIds, @Nullable TagKey<Item> partTag,
                                   @Nullable RecipeManager recipeManager, FluidStack fluid,
                                   @Nullable List<ItemStack> customParts) {
    super(syntheticId(category.getId(), material.getIdentifier()), category,
        createInputs(material, partTag, recipeManager, fluid, customParts), List.of(), List.of());
    this.material = material;
    this.materialId = material.getIdentifier();
    this.statIds = List.copyOf(statIds);
    this.fluid = fluid.isEmpty() ? FluidStack.EMPTY : fluid.copy();
    this.parts = customParts == null ? getInputParts(materialId, partTag) : List.copyOf(customParts);
  }

  public static ResourceLocation syntheticId(ResourceLocation categoryId, MaterialId material) {
    return TConstruct.getResource("/emi/" + categoryId.getPath() + "/material/"
        + material.getNamespace() + "/" + material.getPath());
  }

  private static List<EmiIngredient> createInputs(IMaterial material, @Nullable TagKey<Item> partTag,
                                                  @Nullable RecipeManager recipeManager, FluidStack fluid,
                                                  @Nullable List<ItemStack> customParts) {
    List<EmiIngredient> inputs = new ArrayList<>();
    inputs.add(itemIngredient(getRepairStacks(material.getIdentifier(), recipeManager)));
    inputs.add(fluidIngredient(fluid));
    inputs.add(itemIngredient(customParts == null ? getInputParts(material.getIdentifier(), partTag) : customParts));
    return inputs;
  }

  private static List<ItemStack> getRepairStacks(MaterialId materialId, @Nullable RecipeManager recipeManager) {
    MaterialVariantId variant = materialId;
    List<ItemStack> repairStacks = recipeManager == null
        ? List.of()
        : RecipeHelper.getUIRecipes(recipeManager, TinkerRecipeTypes.MATERIAL.get(), MaterialRecipe.class,
            recipe -> variant.matchesVariant(recipe.getMaterial())).stream()
            .flatMap(recipe -> Arrays.stream(recipe.getIngredient().getItems()))
            .map(ItemStack::copy)
            .toList();
    if (repairStacks.isEmpty()) {
      return List.of(TinkerToolParts.repairKit.get().withMaterialForDisplay(variant));
    }
    return repairStacks;
  }

  protected static FluidStack findFluid(MaterialVariantId material) {
    return MaterialCastingLookup.getCastingFluids(material).stream()
        .flatMap(recipe -> recipe.getFluids().stream())
        .findFirst()
        .map(FluidStack::copy)
        .orElse(FluidStack.EMPTY);
  }

  protected static List<ItemStack> getInputParts(MaterialId materialId, @Nullable TagKey<Item> tag) {
    if (tag == null) {
      return List.of();
    }
    Set<Item> seen = new HashSet<>();
    return RegistryHelper.getTagValueStream(tag)
        .filter(item -> item instanceof IModifiable)
        .map(item -> ((IModifiable)item).getToolDefinition())
        .map(ToolPartsHook::parts)
        .flatMap(List::stream)
        .filter(part -> part.canUseMaterial(materialId))
        .map(part -> part.withMaterial(materialId))
        .filter(part -> seen.add(part.getItem()))
        .sorted(Comparator.comparing(stack -> ForgeRegistries.ITEMS.getKey(stack.getItem())))
        .toList();
  }

  public FluidStack getFluidStack() {
    return fluid.isEmpty() ? FluidStack.EMPTY : fluid.copy();
  }

  public List<ItemStack> getParts() {
    return parts.stream().map(ItemStack::copy).toList();
  }

  public Optional<IMaterialStats> getStats(MaterialStatsId id) {
    return MaterialRegistry.getInstance().getMaterialStats(materialId, id)
        .map(stats -> (IMaterialStats)stats);
  }

  public List<ModifierEntry> getTraits(MaterialStatsId id) {
    return MaterialRegistry.getInstance().getTraits(materialId, id);
  }

  public List<IMaterialStats> getPresentStats() {
    return statIds.stream()
        .map(this::getStats)
        .flatMap(Optional::stream)
        .toList();
  }

  public int getMaterialColor() {
    return MaterialTooltipCache.getColor(materialId).getValue();
  }

  @Override
  public boolean supportsRecipeTree() {
    return false;
  }

  @Override
  public void addWidgets(WidgetHolder widgets) {
    addInputWidgets(widgets);
    int line = addHeader(widgets);
    addTraitWidgets(widgets, line);
    addStatWidgets(widgets, line);
  }

  protected void addInputWidgets(WidgetHolder widgets) {
    EmiRenderHelper.slot(widgets, getInputs().get(0), 6, 10);
    if (!fluid.isEmpty()) {
      EmiRenderHelper.tank(widgets, fluidIngredient(fluid), 36, 2, 16, 16, 1000);
    }
    if (!parts.isEmpty()) {
      EmiRenderHelper.slot(widgets, itemIngredient(parts), getDisplayWidth() - 14, 10);
    }
  }

  protected int addHeader(WidgetHolder widgets) {
    Component title = MaterialTooltipCache.getDisplayName(materialId)
        .copy().withStyle(ChatFormatting.UNDERLINE);
    int titleWidth = Minecraft.getInstance().font.width(title);
    int titleX = (getDisplayWidth() - titleWidth) / 2;
    widgets.addText(title, titleX, 30, getMaterialColor(), true);
    widgets.addTooltip(List.of(EmiRenderHelper.tooltip(
        Component.translatable(MaterialTooltipCache.getKey(materialId) + ".flavor"))), titleX, 28, titleWidth, 10);

    Component tier = TConstruct.makeTranslation("emi", "material.tier", material.getTier());
    TextWidget tierWidget = widgets.addText(tier, getDisplayWidth() / 2, 41, getMaterialColor(), true);
    tierWidget.horizontalAlign(TextWidget.Alignment.CENTER);
    return 55;
  }

  protected void addTraitWidgets(WidgetHolder widgets, int line) {
    List<ModifierEntry> traits = getPresentStats().stream()
        .findFirst()
        .map(stats -> getTraits(stats.getIdentifier()))
        .orElse(List.of());
    int y = line - 2;
    for (ModifierEntry trait : traits) {
      Component name = trait.getDisplayName();
      int width = Minecraft.getInstance().font.width(name);
      int x = getDisplayWidth() - width - 4;
      widgets.addText(name, x, y, getMaterialColor(), true);
      widgets.addTooltip(EmiRenderHelper.tooltip(trait.getModifier().getDescriptionList()), x, y, width, 10);
      y += 10;
    }
  }

  protected void addStatWidgets(WidgetHolder widgets, int line) {
    int y = line;
    for (IMaterialStats stats : getPresentStats()) {
      if (y >= getDisplayHeight() - 10) {
        break;
      }
      Component name = stats.getLocalizedName().withStyle(ChatFormatting.UNDERLINE);
      widgets.addText(name, 4, y, getMaterialColor(), true);
      y += 10;
      List<Component> info = stats.getLocalizedInfo();
      List<Component> descriptions = stats.getLocalizedDescriptions();
      for (int i = 0; i < info.size() && y < getDisplayHeight() - 10; i++) {
        Component value = info.get(i);
        widgets.addText(value, 4, y, 0xFFFFFF, false);
        if (i < descriptions.size() && !descriptions.get(i).equals(Component.empty())) {
          widgets.addTooltip(List.of(EmiRenderHelper.tooltip(descriptions.get(i))), 4, y, getDisplayWidth() - 8, 10);
        }
        y += 10;
      }
      y += 4;
    }
  }
}
