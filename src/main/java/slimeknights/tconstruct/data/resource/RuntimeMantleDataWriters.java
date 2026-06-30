package slimeknights.tconstruct.data.resource;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.data.datamap.BlockStateDataMapProvider;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.fluid.tooltip.AbstractFluidTooltipProvider;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public final class RuntimeMantleDataWriters {
  private static final Class<?> BLOCK_STATE_PROVIDER = BlockStateDataMapProvider.class;
  private static final Field DATA_LOADER = field(BLOCK_STATE_PROVIDER, "dataLoader");
  private static final Field BLOCKS = field(BLOCK_STATE_PROVIDER, "blocks");
  private static final Field ENTRIES = field(BLOCK_STATE_PROVIDER, "entries");

  private static final Class<?> DATA_MAP;
  private static final Field DATA_MAP_OWNER;
  private static final Field DATA_MAP_VARIANTS;
  private static final Method ADD_ENTRIES;

  private static final Field TOOLTIP_BUILDERS = field(AbstractFluidTooltipProvider.class, "builders");
  private static final Field TOOLTIP_REDIRECTS = field(AbstractFluidTooltipProvider.class, "redirects");
  private static final Method ADD_FLUIDS;

  static {
    try {
      DATA_MAP = Class.forName("slimeknights.mantle.data.datamap.BlockStateDataMapProvider$DataMap");
      DATA_MAP_OWNER = field(DATA_MAP, "owner");
      DATA_MAP_VARIANTS = field(DATA_MAP, "variants");
      ADD_ENTRIES = BLOCK_STATE_PROVIDER.getDeclaredMethod("addEntries");
      ADD_ENTRIES.setAccessible(true);
      ADD_FLUIDS = AbstractFluidTooltipProvider.class.getDeclaredMethod("addFluids");
      ADD_FLUIDS.setAccessible(true);
    } catch (ReflectiveOperationException exception) {
      throw new ExceptionInInitializerError(exception);
    }
  }

  private RuntimeMantleDataWriters() {}

  public static <D> void writeBlockStateDataMap(DynamicResourceRegistrar registrar, BlockStateDataMapProvider<D> provider, String folder) {
    try {
      ADD_ENTRIES.invoke(provider);
      Loadable<D> dataLoader = cast(DATA_LOADER.get(provider));
      Map<?, ?> blocks = cast(BLOCKS.get(provider));
      Map<ResourceLocation, D> entries = cast(ENTRIES.get(provider));
      for (Object dataMap : blocks.values()) {
        Block owner = (Block) DATA_MAP_OWNER.get(dataMap);
        ResourceLocation ownerId = BuiltInRegistries.BLOCK.getKey(owner);
        registrar.addResource(ResourceLocation.tryBuild(ownerId.getNamespace(), folder + "/" + ownerId.getPath() + ".json"), serializeDataMap(dataMap, dataLoader));
      }
      for (Map.Entry<ResourceLocation, D> entry : entries.entrySet()) {
        registrar.addResource(ResourceLocation.tryBuild(entry.getKey().getNamespace(), folder + "/" + entry.getKey().getPath() + ".json"), dataLoader.serialize(entry.getValue()));
      }
      blocks.clear();
      entries.clear();
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Failed to serialize runtime block-state data map", exception);
    }
  }

  public static void writeFluidTooltips(DynamicResourceRegistrar registrar, AbstractFluidTooltipProvider provider) {
    try {
      ADD_FLUIDS.invoke(provider);
      Map<ResourceLocation, Object> builders = cast(TOOLTIP_BUILDERS.get(provider));
      Map<ResourceLocation, ResourceLocation> redirects = cast(TOOLTIP_REDIRECTS.get(provider));
      for (Map.Entry<ResourceLocation, Object> entry : builders.entrySet()) {
        Object fluidUnitList = entry.getValue().getClass().getDeclaredMethod("build").invoke(entry.getValue());
        registrar.addResource(ResourceLocation.tryBuild(entry.getKey().getNamespace(), FluidTooltipHandler.FOLDER + "/" + entry.getKey().getPath() + ".json"), FluidTooltipHandler.GSON.toJsonTree(fluidUnitList));
      }
      for (Map.Entry<ResourceLocation, ResourceLocation> entry : redirects.entrySet()) {
        JsonObject json = new JsonObject();
        json.addProperty("redirect", entry.getValue().toString());
        registrar.addResource(ResourceLocation.tryBuild(entry.getKey().getNamespace(), FluidTooltipHandler.FOLDER + "/" + entry.getKey().getPath() + ".json"), json);
      }
      builders.clear();
      redirects.clear();
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Failed to serialize runtime fluid tooltips", exception);
    }
  }

  private static <D> JsonObject serializeDataMap(Object dataMap, Loadable<D> dataLoader) throws ReflectiveOperationException {
    JsonObject variants = new JsonObject();
    List<?> variantList = cast(DATA_MAP_VARIANTS.get(dataMap));
    for (Object variant : variantList) {
      Object data = invokeRecordAccessor(variant, "data");
      ResourceLocation parent = cast(invokeRecordAccessor(variant, "parent"));
      Object variantString = invokeRecordAccessor(variant, "variant");
      String key = variantString.toString();
      if (data != null) {
        variants.add(key, dataLoader.serialize(cast(data)));
      } else if (parent != null) {
        variants.addProperty(key, parent.toString());
      }
    }
    JsonObject map = new JsonObject();
    map.add("variants", variants);
    return map;
  }

  private static Object invokeRecordAccessor(Object object, String name) throws ReflectiveOperationException {
    Method method = object.getClass().getDeclaredMethod(name);
    method.setAccessible(true);
    return method.invoke(object);
  }

  private static Field field(Class<?> owner, String name) {
    try {
      Field field = owner.getDeclaredField(name);
      field.setAccessible(true);
      return field;
    } catch (NoSuchFieldException exception) {
      throw new ExceptionInInitializerError(exception);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T cast(Object object) {
    return (T) object;
  }
}
