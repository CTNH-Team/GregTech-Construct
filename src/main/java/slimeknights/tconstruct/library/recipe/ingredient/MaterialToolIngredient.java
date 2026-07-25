package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolMaterialHook;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/** Ingredient matching modifiable tools with a selected material in a selected material slot. */
public class MaterialToolIngredient extends AbstractIngredient {
  private final MaterialVariantId material;
  private final int materialIndex;
  private final List<Item> tools;
  private final EnumSet<ArmorItem.Type> armorTypes;
  @Nullable
  private ItemStack[] displayItems;

  private MaterialToolIngredient(MaterialVariantId material, int materialIndex, Collection<Item> tools, Set<ArmorItem.Type> armorTypes) {
    super(Stream.of(PlaceholderValue.INSTANCE));
    if (materialIndex < 0) {
      throw new IllegalArgumentException("Material index must be nonnegative");
    }
    if (tools.isEmpty() && armorTypes.isEmpty()) {
      throw new IllegalArgumentException("Material tool ingredient requires at least one tool or armor type");
    }
    if (tools.stream().anyMatch(item -> !(item instanceof IModifiable))) {
      throw new IllegalArgumentException("Material tool ingredient tools must be modifiable");
    }
    this.material = material;
    this.materialIndex = materialIndex;
    this.tools = List.copyOf(new LinkedHashSet<>(tools));
    this.armorTypes = armorTypes.isEmpty() ? EnumSet.noneOf(ArmorItem.Type.class) : EnumSet.copyOf(armorTypes);
  }

  /** Creates a builder for a material tool ingredient. */
  public static Builder builder(MaterialVariantId material) {
    return new Builder(material);
  }

  /** Checks whether the given tool context matches this ingredient. */
  public boolean test(IToolContext tool) {
    return matchesTool(tool.getItem()) && material.matchesVariant(tool.getMaterial(materialIndex));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    return stack != null && !stack.isEmpty() && matchesTool(stack.getItem())
        && material.matchesVariant(ToolStack.from(stack).getMaterial(materialIndex));
  }

  private boolean matchesTool(Item item) {
    if (!(item instanceof IModifiable)) {
      return false;
    }
    if (tools.contains(item)) {
      return true;
    }
    return item instanceof ArmorItem armor && armorTypes.contains(armor.getType());
  }

  @Override
  public ItemStack[] getItems() {
    Collection<IModifiable> displayTools = getDisplayTools();
    if (!isDisplayDataLoaded(displayTools)) {
      return new ItemStack[] { emptyDisplayItem() };
    }
    if (displayItems == null) {
      MaterialVariant displayMaterial = MaterialVariant.of(material);
      ItemStack[] items = displayTools.stream()
          .map(tool -> buildDisplayItem(tool, displayMaterial))
          .filter(stack -> !stack.isEmpty() && ToolStack.isInitialized(stack))
          .filter(stack -> material.matchesVariant(ToolStack.from(stack).getMaterial(materialIndex)))
          .toArray(ItemStack[]::new);
      if (items.length == 0) {
        return new ItemStack[] { emptyDisplayItem() };
      }
      displayItems = items;
    }
    return displayItems;
  }

  private static boolean isDisplayDataLoaded(Collection<IModifiable> displayTools) {
    return MaterialRegistry.isFullyLoaded()
        && ModifierManager.INSTANCE.isDynamicModifiersLoaded()
        && TinkerTags.isTagsLoaded()
        && displayTools.stream().allMatch(tool -> tool.getToolDefinition().isDataLoaded());
  }

  private ItemStack buildDisplayItem(IModifiable tool, MaterialVariant displayMaterial) {
    List<MaterialStatsId> materialStats = ToolMaterialHook.stats(tool.getToolDefinition());
    if (materialIndex >= materialStats.size()) {
      return ItemStack.EMPTY;
    }
    MaterialNBT.Builder materials = MaterialNBT.builder();
    for (int index = 0; index < materialStats.size(); index++) {
      if (index == materialIndex) {
        materials.add(displayMaterial);
      } else {
        materials.add(MaterialVariant.of(MaterialRegistry.firstWithStatType(materialStats.get(index))));
      }
    }
    return ToolBuildHandler.buildItemFromMaterials(tool, materials.build());
  }

  private Collection<IModifiable> getDisplayTools() {
    LinkedHashSet<Item> displayTools = new LinkedHashSet<>(tools);
    if (!armorTypes.isEmpty()) {
      for (Item item : ForgeRegistries.ITEMS.getValues()) {
        if (item instanceof IModifiable && item instanceof ArmorItem armor && armorTypes.contains(armor.getType())) {
          displayTools.add(item);
        }
      }
    }
    List<IModifiable> result = new ArrayList<>(displayTools.size());
    for (Item item : displayTools) {
      if (item instanceof IModifiable tool) {
        result.add(tool);
      }
    }
    return result;
  }

  private static ItemStack emptyDisplayItem() {
    return new ItemStack(Blocks.BARRIER).setHoverName(Component.literal("Empty Material Tool Ingredient"));
  }

  private enum PlaceholderValue implements Ingredient.Value {
    INSTANCE;

    @Override
    public Collection<ItemStack> getItems() {
      return List.of(emptyDisplayItem());
    }

    @Override
    public JsonObject serialize() {
      return new JsonObject();
    }
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IIngredientSerializer<? extends Ingredient> getSerializer() {
    return Serializer.INSTANCE;
  }

  @Override
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("type", Serializer.ID.toString());
    json.addProperty("material", material.toString());
    json.addProperty("material_index", materialIndex);
    if (!tools.isEmpty()) {
      JsonArray tools = new JsonArray();
      for (Item tool : this.tools) {
        tools.add(BuiltInRegistries.ITEM.getKey(tool).toString());
      }
      json.add("tools", tools);
    }
    if (!armorTypes.isEmpty()) {
      JsonArray armorTypes = new JsonArray();
      for (ArmorItem.Type armorType : this.armorTypes) {
        armorTypes.add(armorType.name().toLowerCase(Locale.ROOT));
      }
      json.add("armor_types", armorTypes);
    }
    return json;
  }

  /** Builder for a material tool ingredient. */
  public static class Builder {
    private final MaterialVariantId material;
    private int materialIndex;
    private final List<Item> tools = new ArrayList<>();
    private final EnumSet<ArmorItem.Type> armorTypes = EnumSet.noneOf(ArmorItem.Type.class);

    private Builder(MaterialVariantId material) {
      this.material = material;
    }

    /** Sets the material slot to check. */
    public Builder materialIndex(int materialIndex) {
      this.materialIndex = materialIndex;
      return this;
    }

    /** Adds modifiable tools or equipment to this ingredient. */
    public Builder tools(IModifiable... tools) {
      for (IModifiable tool : tools) {
        this.tools.add(tool.asItem());
      }
      return this;
    }

    /** Adds armor types to this ingredient. */
    public Builder armorTypes(ArmorItem.Type... armorTypes) {
      for (ArmorItem.Type armorType : armorTypes) {
        this.armorTypes.add(armorType);
      }
      return this;
    }

    /** Builds the ingredient. */
    public MaterialToolIngredient build() {
      return new MaterialToolIngredient(material, materialIndex, tools, armorTypes);
    }
  }

  /** Serializer instance. */
  public enum Serializer implements IIngredientSerializer<MaterialToolIngredient> {
    INSTANCE;

    public static final ResourceLocation ID = TConstruct.getResource("material_tool");

    @Override
    public MaterialToolIngredient parse(JsonObject json) {
      Builder builder = builder(MaterialVariantId.fromJson(json, "material"))
          .materialIndex(GsonHelper.getAsInt(json, "material_index", 0));
      if (json.has("tools")) {
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "tools")) {
          builder.tools(parseTool(element.getAsString()));
        }
      }
      if (json.has("armor_types")) {
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "armor_types")) {
          builder.armorTypes(parseArmorType(element.getAsString()));
        }
      }
      return builder.build();
    }

    @Override
    public MaterialToolIngredient parse(FriendlyByteBuf buffer) {
      Builder builder = builder(MaterialVariantId.fromNetwork(buffer)).materialIndex(buffer.readVarInt());
      int toolCount = buffer.readVarInt();
      for (int index = 0; index < toolCount; index++) {
        builder.tools(parseTool(buffer.readResourceLocation()));
      }
      int armorTypeCount = buffer.readVarInt();
      for (int index = 0; index < armorTypeCount; index++) {
        builder.armorTypes(buffer.readEnum(ArmorItem.Type.class));
      }
      return builder.build();
    }

    @Override
    public void write(FriendlyByteBuf buffer, MaterialToolIngredient ingredient) {
      ingredient.material.toNetwork(buffer);
      buffer.writeVarInt(ingredient.materialIndex);
      buffer.writeVarInt(ingredient.tools.size());
      for (Item tool : ingredient.tools) {
        buffer.writeResourceLocation(BuiltInRegistries.ITEM.getKey(tool));
      }
      buffer.writeVarInt(ingredient.armorTypes.size());
      for (ArmorItem.Type armorType : ingredient.armorTypes) {
        buffer.writeEnum(armorType);
      }
    }

    private static IModifiable parseTool(String id) {
      ResourceLocation location = ResourceLocation.tryParse(id);
      if (location == null) {
        throw new JsonSyntaxException("Invalid modifiable tool ID: " + id);
      }
      return parseTool(location);
    }

    private static IModifiable parseTool(ResourceLocation id) {
      Item item = BuiltInRegistries.ITEM.getOptional(id)
          .orElseThrow(() -> new JsonSyntaxException("Unknown modifiable tool: " + id));
      if (item instanceof IModifiable tool) {
        return tool;
      }
      throw new JsonSyntaxException("Ingredient tool is not modifiable: " + id);
    }

    private static ArmorItem.Type parseArmorType(String name) {
      try {
        return ArmorItem.Type.valueOf(name.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException exception) {
        throw new JsonSyntaxException("Unknown armor type: " + name, exception);
      }
    }
  }
}