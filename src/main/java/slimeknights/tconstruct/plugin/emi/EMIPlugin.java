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
import slimeknights.mantle.recipe.helper.RecipeHelper;import slimeknights.mantle.recipe.ingredient.EntityIngredient;
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
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
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
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

@EmiEntrypoint
public final class EMIPlugin implements EmiPlugin {
  @Override
  public void register(EmiRegistry registry) {
    RECIPE_ID_COUNTS.clear();
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
      registry.addRecipe(new CastingEmiRecipe(recipeId(recipe.getRecipeId(), outputMaterialPath(recipe)), category, recipe,
          category == EMIConstants.CASTING_BASIN));
    }
  }

  /**
   * 铸造展开配方按输出材料区分 id。输出栈带材料 NBT 时返回材料 path(如 manyullyn);
   * 非材料输出(如药水填充)返回 null,由 recipeId 退回数字序号。
   */
  @Nullable
  private static String outputMaterialPath(IDisplayableCastingRecipe recipe) {
    List<ItemStack> outputs = recipe.getOutputs();
    if (!outputs.isEmpty()) {
      MaterialVariantId material = IMaterialItem.getMaterialFromStack(outputs.get(0));
      if (!material.equals(IMaterial.UNKNOWN_ID)) {
        return material.getId().getPath();
      }
    }
    return null;
  }

  private static void registerMolding(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<MoldingRecipe> recipes = new java.util.ArrayList<>();
    recipes.addAll(RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.MOLDING_TABLE.get(), MoldingRecipe.class));
    recipes.addAll(RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.MOLDING_BASIN.get(), MoldingRecipe.class));
    for (int i = 0; i < recipes.size(); i++) {
      MoldingRecipe recipe = recipes.get(i);
      registry.addRecipe(new MoldingEmiRecipe(recipeId(recipe.getId()), recipe, access));
    }
  }

  private static void registerMelting(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<MeltingRecipe> recipes = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.MELTING.get(), MeltingRecipe.class);
    for (int i = 0; i < recipes.size(); i++) {
      MeltingRecipe recipe = recipes.get(i);
      registry.addRecipe(new MeltingEmiRecipe(recipeId(recipe.getId()), recipe));
      registry.addRecipe(new FoundryEmiRecipe(recipeId(recipe.getId()), recipe));
    }
  }

  private static void registerAlloy(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<AlloyRecipe> recipes = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.ALLOYING.get(), AlloyRecipe.class);
    for (int i = 0; i < recipes.size(); i++) {
      AlloyRecipe recipe = recipes.get(i);
      registry.addRecipe(new AlloyEmiRecipe(recipeId(recipe.getId()), recipe));
    }
  }

  private static void registerEntityMelting(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<EntityMeltingRecipe> recipes = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.ENTITY_MELTING.get(), EntityMeltingRecipe.class);
    for (int i = 0; i < recipes.size(); i++) {
      EntityMeltingRecipe recipe = recipes.get(i);
      registry.addRecipe(new EntityMeltingEmiRecipe(
          recipeId(recipe.getId()), recipe));
    }
    EntityIngredient defaultIngredient = getDefaultEntityIngredient(recipes);
    registry.addRecipe(new EntityMeltingEmiRecipe(
        recipeId(TConstruct.getResource("/__default")),
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
      registry.addRecipe(new SeveringEmiRecipe(recipeId(recipe.getId()), recipe));
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
      registry.addRecipe(new ModifierEmiRecipe(recipeId(recipe.getRecipeId()), recipe));
    }

    List<ToolBuildingRecipe> tools = RecipeHelper.getJEIRecipes(
            access, manager, TinkerRecipeTypes.TINKER_STATION.get(), ToolBuildingRecipe.class)
        .stream()
        .sorted(Comparator.comparingInt(
            recipe -> StationSlotLayoutLoader.getInstance().get(recipe.getLayoutSlotId()).getSortIndex()))
        .toList();
    for (int i = 0; i < tools.size(); i++) {
      ToolBuildingRecipe recipe = tools.get(i);
      registry.addRecipe(new ToolBuildingEmiRecipe(recipeId(recipe.getId()), recipe));
    }

    List<IDisplayPartBuilderRecipe> parts = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.PART_BUILDER.get(), IDisplayPartBuilderRecipe.class);
    for (int i = 0; i < parts.size(); i++) {
      IDisplayPartBuilderRecipe recipe = parts.get(i);
      registry.addRecipe(new PartBuilderEmiRecipe(recipeId(recipe.getId(), partBuilderMaterial(recipe)), recipe));
    }
  }

  /** 部件加工展开配方按输出材料区分 id;无材料(如无材料成本配方)返回 null,退回数字序号 */
  @Nullable
  private static String partBuilderMaterial(IDisplayPartBuilderRecipe recipe) {
    MaterialVariant material = recipe.getMaterial();
    return material.isEmpty() ? null : material.getVariant().getId().getPath();
  }

  private static void registerWorktable(EmiRegistry registry, RegistryAccess access, RecipeManager manager) {
    List<IModifierWorktableRecipe> recipes = RecipeHelper.getJEIRecipes(access, manager, TinkerRecipeTypes.MODIFIER_WORKTABLE.get(), IModifierWorktableRecipe.class);
    for (int i = 0; i < recipes.size(); i++) {
      IModifierWorktableRecipe recipe = recipes.get(i);
      registry.addRecipe(new ModifierWorktableEmiRecipe(
          recipeId(recipe.getId()), recipe));
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

  /**
   * 配方 id 保持玩家可读的数据包原始 id(与 JEI 时代显示一致)。同一原始 id 会由
   * IMultiRecipe 展开出多条显示配方(如铸造、部件加工按材料展开)或注册到多个分类
   * (如熔化/熔铸),而 EMI 的 byId 索引对重复 id 会互相覆盖并刷 "recipes loaded with
   * the same id" 日志,且对非 / 开头的合成 id 做 RecipeManager 校验报错
   * ("not present in recipe manager")。因此首个使用数据包原始 id,后续展开条目以
   * / 开头(EMI 合成配方约定,跳过校验)并追加材料名(如 /manyullyn)保持可读与唯一;
   * 无法提供材料时退回数字序号。每次 register 前清空计数,保证 reload 之间不串号。
   */
  /** 同包测试需要访问,故为包私有 */
  static final Map<ResourceLocation, Integer> RECIPE_ID_COUNTS = new HashMap<>();

  static ResourceLocation recipeId(ResourceLocation id) {
    return recipeId(id, null);
  }

  static ResourceLocation recipeId(ResourceLocation id, @Nullable String discriminator) {
    int count = RECIPE_ID_COUNTS.merge(id, 1, Integer::sum);
    if (count == 1) {
      return id;
    }
    String suffix = (discriminator == null || discriminator.isEmpty()) ? Integer.toString(count) : discriminator;
    return new ResourceLocation(id.getNamespace(), "/" + id.getPath() + "/" + suffix);
  }
}
