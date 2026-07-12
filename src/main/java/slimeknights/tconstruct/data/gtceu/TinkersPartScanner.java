package slimeknights.tconstruct.data.gtceu;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.tools.part.IRepairKitItem;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.MaterialTagToolPartItem;
import slimeknights.tconstruct.library.tools.part.PartCastItem;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

/**
 * Discovers material parts from the registered Tinkers' Construct casts.
 *
 * <p>The cast owns the actual part supplier, so this scanner does not duplicate the part list or
 * maintain aliases for names such as {@code helmet_plating}. Costs come from the dynamic material
 * casting lookup and stats come from the discovered item itself.</p>
 */
final class TinkersPartScanner {
  private static final Logger LOGGER = Util.getLogger("GTConstructParts");
  private static final Field PART_SUPPLIER = findPartSupplier();
  private static final Field MATERIAL_TAG = findMaterialTag();

  private TinkersPartScanner() {}

  static List<GTConstructRecipes.SolidifierPart> scan(GTConstructMaterialSupport support) {
    return build(discoverPartItems(), discoverCosts(), support);
  }

  static List<GTConstructRecipes.SolidifierPart> build(
    List<PartItem> parts,
    Map<Item, Integer> costs,
    GTConstructMaterialSupport support
  ) {
    return parts.stream()
      .map(part -> createSolidifierPart(part, costs, support))
      .flatMap(Optional::stream)
      .sorted(Comparator.comparing(part -> part.path()))
      .toList();
  }

  /**
   * Exposes the raw dynamic cast scan to package tests so they can seed the cost lookup without
   * duplicating the production discovery rules.
   */
  static List<PartItem> discoverPartItems() {
    Map<Item, PartItem> parts = new LinkedHashMap<>();
    for (Field field : TinkerSmeltery.class.getDeclaredFields()) {
      if (!isPublicStatic(field) || !CastItemObject.class.isAssignableFrom(field.getType())) {
        continue;
      }
      try {
        Object value = field.get(null);
        if (value instanceof CastItemObject cast) {
          findPart(cast).ifPresent(item -> {
            ResourceLocation itemId = ITEMS.getKey(item.asItem());
            String castPath = cast.getName().getPath();
            parts.putIfAbsent(item.asItem(), new PartItem(
              castPath,
              item.asItem(),
              cast,
              item instanceof ToolPartItem toolPart ? toolPart.getStatType() : null,
              getMaterialTag(item),
              itemId == null || castPath.equals(itemId.getPath())
            ));
          });
        }
      } catch (IllegalAccessException exception) {
        LOGGER.error(
          "Failed to read Tinkers' Construct cast field '{}'.",
          field.getName(),
          exception
        );
      }
    }
    return List.copyOf(parts.values());
  }

  private static boolean isPublicStatic(Field field) {
    int modifiers = field.getModifiers();
    return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);
  }

  private static Field findPartSupplier() {
    try {
      Field field = PartCastItem.class.getDeclaredField("part");
      if (!field.trySetAccessible()) {
        throw new IllegalStateException("Cannot access PartCastItem part supplier");
      }
      return field;
    } catch (NoSuchFieldException exception) {
      throw new IllegalStateException("TConstruct PartCastItem no longer exposes its part supplier", exception);
    }
  }

  private static Field findMaterialTag() {
    try {
      Field field = MaterialTagToolPartItem.class.getDeclaredField("MaterialTag");
      if (!field.trySetAccessible()) {
        throw new IllegalStateException("Cannot access MaterialTagToolPartItem material tag");
      }
      return field;
    } catch (NoSuchFieldException exception) {
      throw new IllegalStateException(
        "TConstruct MaterialTagToolPartItem no longer exposes its material tag",
        exception
      );
    }
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private static TagKey<IMaterial> getMaterialTag(IMaterialItem item) {
    if (!(item instanceof MaterialTagToolPartItem)) {
      return null;
    }
    try {
      return (TagKey<IMaterial>) MATERIAL_TAG.get(item);
    } catch (IllegalAccessException | RuntimeException exception) {
      LOGGER.debug("Failed to read the material tag from '{}'.", item.asItem(), exception);
      return null;
    }
  }

  private static Optional<IMaterialItem> findPart(CastItemObject cast) {
    Item castItem;
    try {
      castItem = cast.get();
    } catch (RuntimeException exception) {
      LOGGER.debug("Skipping unavailable Tinkers' Construct cast '{}'.", cast.getName(), exception);
      return Optional.empty();
    }
    if (!(castItem instanceof PartCastItem partCast)) {
      return Optional.empty();
    }

    try {
      Object value = PART_SUPPLIER.get(partCast);
      if (value instanceof Supplier<?> supplier && supplier.get() instanceof IMaterialItem item) {
        return Optional.of(item);
      }
    } catch (IllegalAccessException | RuntimeException exception) {
      LOGGER.debug("Skipping cast '{}' with an unavailable part supplier.", cast.getName(), exception);
    }
    return Optional.empty();
  }

  private static Map<Item, Integer> discoverCosts() {
    Map<Item, Integer> costs = new HashMap<>();
    for (Object2IntMap.Entry<IMaterialItem> entry : MaterialCastingLookup.getAllItemCosts()) {
      IMaterialItem item = entry.getKey();
      if (item != null) {
        costs.put(item.asItem(), entry.getIntValue());
      }
    }
    return costs;
  }

  private static Optional<GTConstructRecipes.SolidifierPart> createSolidifierPart(
    PartItem part,
    Map<Item, Integer> costs,
    GTConstructMaterialSupport support
  ) {
    int cost = costs.getOrDefault(part.item(), 0);
    if (cost <= 0) {
      LOGGER.debug("Skipping part '{}' without a dynamic material cost.", part.path());
      return Optional.empty();
    }

    Predicate<MaterialId> materialSupport;
    if (part.stat() != null) {
      materialSupport = material -> support.hasStat(material, part.stat());
    } else if (part.item() instanceof IRepairKitItem) {
      materialSupport = support::hasRepairKitSupport;
    } else {
      return Optional.empty();
    }
    if (part.materialTag() != null) {
      Predicate<MaterialId> statSupport = materialSupport;
      materialSupport = material -> statSupport.test(material) && support.hasTag(material, part.materialTag());
    }

    return Optional.of(new GTConstructRecipes.SolidifierPart(
      part.path(),
      () -> part.item(),
      cost,
      part.cast(),
      materialSupport,
      part.useToSeparator()
    ));
  }

  record PartItem(String path, Item item, CastItemObject cast,
                  @Nullable MaterialStatsId stat, @Nullable TagKey<IMaterial> materialTag,
                  boolean useToSeparator) {
    PartItem(String path, Item item, CastItemObject cast, @Nullable MaterialStatsId stat) {
      this(path, item, cast, stat, null, true);
    }

    PartItem(String path, Item item, CastItemObject cast,
             @Nullable MaterialStatsId stat, @Nullable TagKey<IMaterial> materialTag) {
      this(path, item, cast, stat, materialTag, true);
    }
  }
}
