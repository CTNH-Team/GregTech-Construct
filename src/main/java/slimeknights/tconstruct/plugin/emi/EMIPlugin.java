package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierRecipeLookup;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipe;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.IDisplayPartBuilderRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import slimeknights.tconstruct.library.recipe.worktable.IModifierWorktableRecipe;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.tools.definition.module.build.ToolTraitHook;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.plugin.emi.casting.CastingEmiRecipe;
import slimeknights.tconstruct.plugin.emi.casting.MoldingEmiRecipe;
import slimeknights.tconstruct.plugin.emi.entity.EntityMeltingEmiRecipe;
import slimeknights.tconstruct.plugin.emi.entity.SeveringEmiRecipe;
import slimeknights.tconstruct.plugin.emi.melting.AlloyEmiRecipe;
import slimeknights.tconstruct.plugin.emi.melting.EmiMeltingFuelHandler;
import slimeknights.tconstruct.plugin.emi.melting.FoundryEmiRecipe;
import slimeknights.tconstruct.plugin.emi.melting.MeltingEmiRecipe;
import slimeknights.tconstruct.plugin.emi.material.MaterialStatsEmiConstants;
import slimeknights.tconstruct.plugin.emi.material.MaterialStatsEmiRecipe;
import slimeknights.tconstruct.plugin.emi.material.ArmorStatsEmiRecipe;
import slimeknights.tconstruct.plugin.emi.material.SkullStatsEmiRecipe;
import slimeknights.tconstruct.plugin.emi.modifiers.ModifierEmiRecipe;
import slimeknights.tconstruct.plugin.emi.modifiers.ModifierEmiStack;
import slimeknights.tconstruct.plugin.emi.modifiers.ModifierWorktableEmiRecipe;
import slimeknights.tconstruct.plugin.emi.partbuilder.PartBuilderEmiRecipe;
import slimeknights.tconstruct.plugin.emi.toolbuilding.ToolBuildingEmiRecipe;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.module.EntityMeltingModule;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Comparator;

@EmiEntrypoint
public final class EMIPlugin implements EmiPlugin {
  @Override
  public void register(EmiRegistry registry) {
    for (EMIConstants.TConstructEmiCategory category : EMIConstants.ALL) {
      registry.addCategory(category);
    }

    Level level = Minecraft.getInstance().level;
    if (level == null) {
      return;
    }
    RegistryAccess access = level.registryAccess();
    var manager = level.getRecipeManager();

    registerCasting(registry, access, manager, TinkerRecipeTypes.CASTING_BASIN.get(), EMIConstants.CASTING_BASIN);
    registerCasting(registry, access, manager, TinkerRecipeTypes.CASTING_TABLE.get(), EMIConstants.CASTING_TABLE);
    registerMolding(registry, access, manager);
    EmiMeltingFuelHandler.setFuels(
        RecipeHelper.getRecipes(manager, TinkerRecipeTypes.FUEL.get(), slimeknights.tconstruct.library.recipe.fuel.MeltingFuel.class));
    registerMelting(registry, access, manager);
    registerAlloy(registry, access, manager);
    registerEntityMelting(registry, access, manager);
    registerSevering(registry, access, manager);
    registerToolRecipes(registry, access, manager);
    registerWorktable(registry, access, manager);
    registerModifiers(registry);
    registerMaterialStats(registry, manager);

    addWorkstations(registry, manager);
  }

  private static void registerCasting(EmiRegistry registry, RegistryAccess access, RecipeManager manager,
                                      RecipeType<?> type, EMIConstants.TConstructEmiCategory category) {
    @SuppressWarnings({"rawtypes", "unchecked"})
    List<IDisplayableCastingRecipe> recipes = RecipeHelper.getJEIRecipes(access, manager, (net.minecraft.world.item.crafting.RecipeType) type, IDisplayableCastingRecipe.class);
    for (int i = 0; i < recipes.size(); i++) {
      IDisplayableCastingRecipe recipe = recipes.get(i);
      registry.addRecipe(new CastingEmiRecipe(recipeId(recipe.getRecipeId(), category, i), category, recipe,
          category == EMIConstants.CASTING_BASIN));
    }
  }

  private static void registerMolding(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<MoldingRecipe> recipes = new java.util.ArrayList<>();
    recipes.addAll(RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.MOLDING_TABLE.get(), MoldingRecipe.class));
    recipes.addAll(RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.MOLDING_BASIN.get(), MoldingRecipe.class));
    for (int i = 0; i < recipes.size(); i++) {
      MoldingRecipe recipe = recipes.get(i);
      registry.addRecipe(new MoldingEmiRecipe(recipeId(recipe.getId(), EMIConstants.MOLDING, i), recipe, access));
    }
  }

  private static void registerMelting(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<MeltingRecipe> recipes = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.MELTING.get(), MeltingRecipe.class);
    for (int i = 0; i < recipes.size(); i++) {
      MeltingRecipe recipe = recipes.get(i);
      registry.addRecipe(new MeltingEmiRecipe(recipeId(recipe.getId(), EMIConstants.MELTING, i), recipe));
      registry.addRecipe(new FoundryEmiRecipe(recipeId(recipe.getId(), EMIConstants.FOUNDRY, i), recipe));
    }
  }

  private static void registerAlloy(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<AlloyRecipe> recipes = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.ALLOYING.get(), AlloyRecipe.class);
    for (int i = 0; i < recipes.size(); i++) {
      AlloyRecipe recipe = recipes.get(i);
      registry.addRecipe(new AlloyEmiRecipe(recipeId(recipe.getId(), EMIConstants.ALLOY, i), recipe));
    }
  }

  private static void registerEntityMelting(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<EntityMeltingRecipe> recipes = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.ENTITY_MELTING.get(), EntityMeltingRecipe.class);
    for (int i = 0; i < recipes.size(); i++) {
      EntityMeltingRecipe recipe = recipes.get(i);
      registry.addRecipe(new EntityMeltingEmiRecipe(
          recipeId(recipe.getId(), EMIConstants.ENTITY_MELTING, i), recipe));
    }
    EntityIngredient defaultIngredient = getDefaultEntityIngredient(recipes);
    registry.addRecipe(new EntityMeltingEmiRecipe(
        recipeId(TConstruct.getResource("__default"), EMIConstants.ENTITY_MELTING, recipes.size()),
        defaultIngredient, EntityMeltingModule.getDefaultFluid(), 2));
  }

  private static EntityIngredient getDefaultEntityIngredient(List<EntityMeltingRecipe> recipes) {
    Set<EntityType<?>> unusedTypes = new LinkedHashSet<>();
    typeLoop:
    for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES) {
      if (type.is(TinkerTags.EntityTypes.MELTING_HIDE)) {
        continue;
      }
      if (type.getCategory() == MobCategory.MISC && !type.is(TinkerTags.EntityTypes.MELTING_SHOW)) {
        continue;
      }
      for (EntityMeltingRecipe recipe : recipes) {
        if (recipe.matches(type)) {
          continue typeLoop;
        }
      }
      unusedTypes.add(type);
    }
    return EntityIngredient.of(unusedTypes);
  }

  private static void registerSevering(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<SeveringRecipe> recipes = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.SEVERING.get(), SeveringRecipe.class);
    for (int i = 0; i < recipes.size(); i++) {
      SeveringRecipe recipe = recipes.get(i);
      registry.addRecipe(new SeveringEmiRecipe(recipeId(recipe.getId(), EMIConstants.SEVERING, i), recipe));
    }
  }

  private static void registerToolRecipes(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<IDisplayModifierRecipe> modifiers = RecipeHelper.getJEIRecipes(
            access, manager, TinkerRecipeTypes.TINKER_STATION.get(), IDisplayModifierRecipe.class)
        .stream()
        .sorted(Comparator.comparing(recipe -> {
          var type = recipe.getSlotType();
          return type == null ? "zzzzzzzzzz" : type.getName();
        }))
        .toList();
    for (int i = 0; i < modifiers.size(); i++) {
      IDisplayModifierRecipe recipe = modifiers.get(i);
      registry.addRecipe(new ModifierEmiRecipe(recipeId(recipe.getRecipeId(), EMIConstants.MODIFIERS, i), recipe));
    }

    List<ToolBuildingRecipe> tools = RecipeHelper.getJEIRecipes(
            access, manager, TinkerRecipeTypes.TINKER_STATION.get(), ToolBuildingRecipe.class)
        .stream()
        .sorted(Comparator.comparingInt(
            recipe -> StationSlotLayoutLoader.getInstance().get(recipe.getLayoutSlotId()).getSortIndex()))
        .toList();
    for (int i = 0; i < tools.size(); i++) {
      ToolBuildingRecipe recipe = tools.get(i);
      registry.addRecipe(new ToolBuildingEmiRecipe(recipeId(recipe.getId(), EMIConstants.TOOL_BUILDING, i), recipe));
    }

    List<IDisplayPartBuilderRecipe> parts = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.PART_BUILDER.get(), IDisplayPartBuilderRecipe.class);
    for (int i = 0; i < parts.size(); i++) {
      IDisplayPartBuilderRecipe recipe = parts.get(i);
      registry.addRecipe(new PartBuilderEmiRecipe(recipeId(recipe.getId(), EMIConstants.PART_BUILDER, i), recipe));
    }
  }

  private static void registerWorktable(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<IModifierWorktableRecipe> recipes = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.MODIFIER_WORKTABLE.get(), IModifierWorktableRecipe.class);
    for (int i = 0; i < recipes.size(); i++) {
      IModifierWorktableRecipe recipe = recipes.get(i);
      registry.addRecipe(new ModifierWorktableEmiRecipe(
          recipeId(recipe.getId(), EMIConstants.MODIFIER_WORKTABLE, i), recipe));
    }
  }

  private static void registerModifiers(EmiRegistry registry) {
    if (!Config.CLIENT.showModifiersInEMI.get()) {
      return;
    }
    ModifierRecipeLookup.getRecipeModifierList().stream()
        .filter(ModifierEntry::isBound)
        .filter(entry -> !ModifierManager.isInTag(entry.getId(), TinkerTags.Modifiers.HIDDEN_FROM_EMI))
        .forEach(entry -> {
          ModifierEmiStack stack = new ModifierEmiStack(entry);
          registry.addEmiStack(stack);
          registry.addAlias(stack, Component.translatable("emi.tconstruct.modifiers.title"));
        });
  }

  private static void registerMaterialStats(EmiRegistry registry, RecipeManager manager) {
    if (!MaterialRegistry.isFullyLoaded()) {
      return;
    }
    IMaterialRegistry materials = MaterialRegistry.getInstance();
    for (IMaterial material : materials.getVisibleMaterials()) {
      addMaterialStats(registry, manager, material, MaterialStatsEmiConstants.HARVEST_STAT_IDS,
          EMIConstants.HARVEST_STATS, TinkerTags.Items.HARVEST);
      addMaterialStats(registry, manager, material, MaterialStatsEmiConstants.RANGED_STAT_IDS,
          EMIConstants.RANGED_STATS, TinkerTags.Items.RANGED);
      addMaterialStats(registry, manager, material, MaterialStatsEmiConstants.ARMOR_STAT_IDS,
          EMIConstants.ARMOR_STATS, TinkerTags.Items.ARMOR);
      addMaterialStats(registry, manager, material, MaterialStatsEmiConstants.AMMO_STAT_IDS,
          EMIConstants.AMMO_STATS, TinkerTags.Items.AMMO);
      addMaterialStats(registry, manager, material, MaterialStatsEmiConstants.SKULL_STAT_IDS,
          EMIConstants.SKULL_STATS, null);
    }
  }

  private static void addMaterialStats(EmiRegistry registry, RecipeManager manager, IMaterial material,
                                       List<MaterialStatsId> statIds, EMIConstants.TConstructEmiCategory category,
                                       net.minecraft.tags.TagKey<Item> partTag) {
    IMaterialRegistry materials = MaterialRegistry.getInstance();
    if (statIds.stream().noneMatch(id -> materials.getMaterialStats(material.getIdentifier(), id).isPresent())) {
      return;
    }
    if (category == EMIConstants.ARMOR_STATS) {
      registry.addRecipe(new ArmorStatsEmiRecipe(category, material, statIds, manager));
    } else if (category == EMIConstants.SKULL_STATS) {
      registry.addRecipe(new SkullStatsEmiRecipe(category, material, statIds, manager));
    } else {
      registry.addRecipe(new MaterialStatsEmiRecipe(category, material, statIds, partTag, manager));
    }
  }

  private static void addWorkstations(EmiRegistry registry, RecipeManager manager) {
    addWorkstation(registry, EMIConstants.CASTING_BASIN, TinkerSmeltery.searedBasin);
    addWorkstation(registry, EMIConstants.CASTING_BASIN, TinkerSmeltery.scorchedBasin);
    addWorkstation(registry, EMIConstants.CASTING_TABLE, TinkerSmeltery.searedTable);
    addWorkstation(registry, EMIConstants.CASTING_TABLE, TinkerSmeltery.scorchedTable);
    if (!manager.byType(TinkerRecipeTypes.MOLDING_BASIN.get()).isEmpty()) {
      addWorkstation(registry, EMIConstants.MOLDING, TinkerSmeltery.searedBasin);
      addWorkstation(registry, EMIConstants.MOLDING, TinkerSmeltery.scorchedBasin);
    }
    if (!manager.byType(TinkerRecipeTypes.MOLDING_TABLE.get()).isEmpty()) {
      addWorkstation(registry, EMIConstants.MOLDING, TinkerSmeltery.searedTable);
      addWorkstation(registry, EMIConstants.MOLDING, TinkerSmeltery.scorchedTable);
    }
    addWorkstation(registry, EMIConstants.MELTING, TinkerSmeltery.searedMelter);
    addWorkstation(registry, EMIConstants.MELTING, TinkerSmeltery.smelteryController);
    addWorkstation(registry, EMIConstants.FOUNDRY, TinkerSmeltery.foundryController);
    addWorkstation(registry, EMIConstants.ALLOY, TinkerSmeltery.smelteryController);
    addWorkstation(registry, EMIConstants.ALLOY, TinkerSmeltery.scorchedAlloyer);
    addWorkstation(registry, EMIConstants.ENTITY_MELTING, TinkerSmeltery.smelteryController);
    addWorkstation(registry, EMIConstants.MODIFIERS, TinkerTables.tinkerStation);
    addWorkstation(registry, EMIConstants.MODIFIERS, TinkerTables.tinkersAnvil);
    addWorkstation(registry, EMIConstants.MODIFIERS, TinkerTables.scorchedAnvil);
    addWorkstation(registry, EMIConstants.TOOL_BUILDING, TinkerTables.tinkerStation);
    addWorkstation(registry, EMIConstants.TOOL_BUILDING, TinkerTables.tinkersAnvil);
    addWorkstation(registry, EMIConstants.TOOL_BUILDING, TinkerTables.scorchedAnvil);
    addWorkstation(registry, EMIConstants.PART_BUILDER, TinkerTables.partBuilder);
    addWorkstation(registry, EMIConstants.MODIFIER_WORKTABLE, TinkerTables.modifierWorktable);
    addWorkstation(registry, EMIConstants.HARVEST_STATS, TinkerTables.tinkerStation);
    addWorkstation(registry, EMIConstants.HARVEST_STATS, TinkerTables.tinkersAnvil);
    addWorkstation(registry, EMIConstants.HARVEST_STATS, TinkerTables.scorchedAnvil);
    addWorkstation(registry, EMIConstants.RANGED_STATS, TinkerTables.tinkerStation);
    addWorkstation(registry, EMIConstants.RANGED_STATS, TinkerTables.tinkersAnvil);
    addWorkstation(registry, EMIConstants.RANGED_STATS, TinkerTables.scorchedAnvil);
    addWorkstation(registry, EMIConstants.ARMOR_STATS, TinkerTables.tinkerStation);
    addWorkstation(registry, EMIConstants.ARMOR_STATS, TinkerTables.tinkersAnvil);
    addWorkstation(registry, EMIConstants.ARMOR_STATS, TinkerTables.scorchedAnvil);
    addWorkstation(registry, EMIConstants.AMMO_STATS, TinkerTables.tinkerStation);
    addWorkstation(registry, EMIConstants.AMMO_STATS, TinkerTables.tinkersAnvil);
    addWorkstation(registry, EMIConstants.AMMO_STATS, TinkerTables.scorchedAnvil);
    addWorkstation(registry, EMIConstants.SKULL_STATS, TinkerSmeltery.searedBasin);
    addWorkstation(registry, EMIConstants.SKULL_STATS, TinkerSmeltery.scorchedBasin);
    registry.addWorkstation(EMIConstants.SEVERING,
        new ModifierEmiStack(new ModifierEntry(TinkerModifiers.severing, 1)));
    registry.addWorkstation(EMIConstants.MELTING,
        new ModifierEmiStack(new ModifierEntry(TinkerModifiers.melting, 1)));
    registry.addWorkstation(EMIConstants.ENTITY_MELTING,
        new ModifierEmiStack(new ModifierEntry(TinkerModifiers.melting, 1)));

    for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.MODIFIABLE)) {
      if (item.get() instanceof IModifiableDisplay modifiable) {
        ModifierNBT traits = ToolTraitHook.getTraits(modifiable.getToolDefinition(), MaterialNBT.EMPTY);
        if (traits.getLevel(TinkerModifiers.severing.getId()) > 0) {
          addWorkstation(registry, EMIConstants.SEVERING, modifiable.getRenderTool());
        }
        if (traits.getLevel(TinkerModifiers.melting.getId()) > 0) {
          addWorkstation(registry, EMIConstants.MELTING, modifiable.getRenderTool());
          if (item.containsTag(TinkerTags.Items.MELEE)) {
            addWorkstation(registry, EMIConstants.ENTITY_MELTING, modifiable.getRenderTool());
          }
        }
      }
    }
  }

  private static void addWorkstation(EmiRegistry registry, EMIConstants.TConstructEmiCategory category, ItemLike item) {
    registry.addWorkstation(category, EmiStack.of(item));
  }

  private static void addWorkstation(EmiRegistry registry, EMIConstants.TConstructEmiCategory category, ItemStack stack) {
    registry.addWorkstation(category, EmiStack.of(stack));
  }

  private static ResourceLocation recipeId(ResourceLocation id, EMIConstants.TConstructEmiCategory category,
                                           int index) {
    String source = id == null ? "generated" : id.getNamespace() + "/" + id.getPath();
    return TConstruct.getResource("/emi/" + category.getId().getPath() + "/" + source + "/" + index);
  }
}
