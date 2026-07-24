package slimeknights.tconstruct.common.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ArmorItem;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static slimeknights.tconstruct.TConstruct.getResource;

final class ArmorItemModelBuilder {
  private static final ArmorItem.Type[] MIXED_SLOTS = {ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS};
  private static final Map<String,List<PartEntry>> ITEM_PARTS = Map.ofEntries(
    entry("standard", parts(
      part("linear", "family", "linear", 2, 2),
      part("cast", "shared", "cast", 0, 0)
    )),
    entry("knights", parts(
      part("linear", "family", "linear", 2, 2),
      part("massive_cast", "shared", "massive_cast", 0, 0)
    )),
    entry("explorers", parts(
      part("linear", "family", "linear", 2, 2),
      part("armor_mail", "shared", "armor_mail", 1, 1),
      part("frame_of", "shared", "frame_of", 0, 0)
    )),
    entry("light_composite", parts(
      part("armor_mail", "shared", "armor_mail", 1, 1),
      part("frame_of", "shared", "frame_of", 0, 0),
      part("maille", "composite", "maille", 2, 3)
    )),
    entry("heavy_composite", parts(
      part("armor_plate", "shared", "armor_plate", 1, 1),
      part("frame_of", "shared", "frame_of", 0, 0),
      part("maille", "composite", "maille", 2, 3)
    )),
    entry("light_forged", parts(
      part("armor_mail", "shared", "armor_mail", 2, 2),
      part("frame_of", "shared", "frame_of", 0, 0),
      part("plating", "forged", "plating", 1, 1)
    )),
    entry("heavy_forged", parts(
      part("armor_plate", "shared", "armor_plate", 2, 2),
      part("frame_of", "shared", "frame_of", 0, 0),
      part("plating", "forged", "plating", 1, 1)
    )),
    entry("mix_composite", parts(
      part("armor_mail", "shared", "armor_mail", 1, 1),
      part("armor_plate", "shared", "armor_plate", 2, 2),
      part("frame_of", "shared", "frame_of", 0, 0),
      part("maille", "composite", "maille", 3, 4)
    )),
    entry("mix_composite_other", parts(
      part("armor_plate", "shared", "armor_plate", 1, 1),
      part("armor_mail", "shared", "armor_mail", 2, 2),
      part("frame_of", "shared", "frame_of", 0, 0),
      part("maille", "composite", "maille", 3, 4)
    )),
    entry("mix_forged", parts(
      part("armor_mail", "shared", "armor_mail", 2, 2),
      part("armor_plate", "shared", "armor_plate", 3, 3),
      part("frame_of", "shared", "frame_of", 0, 0),
      part("plating", "forged", "plating", 1, 1)
    )),
    entry("mix_forged_other", parts(
      part("armor_plate", "shared", "armor_plate", 2, 2),
      part("armor_mail", "shared", "armor_mail", 3, 3),
      part("frame_of", "shared", "frame_of", 0, 0),
      part("plating", "forged", "plating", 1, 1)
    ))
  );
  private static final Map<String,String> MIXED_PARTS = Map.of();

  private ArmorItemModelBuilder() {}

  static void write(DynamicResourceRegistrar registrar) {
    ITEM_PARTS.forEach((family, parts) -> {
      // Mixed armor families only have chest and legs
      ArmorItem.Type[] slots = family.startsWith("mix_") ? MIXED_SLOTS : ArmorItem.Type.values();
      for (ArmorItem.Type slot : slots) {
        write(registrar, family, slot, parts, isSmall(slot));
      }
    });
  }

  private static void write(DynamicResourceRegistrar registrar, String family, ArmorItem.Type slot, List<PartEntry> parts, boolean small) {
    String slotName = slot.getName();
    JsonObject model = buildModel(family, slotName, parts, small);
    registrar.addItemModel(getResource(family + "_" + slotName), model);
    registrar.addItemModel(getResource("armor/" + family + "/" + slotName + "_broken"), buildBrokenModel(model));
  }

  private static JsonObject buildModel(String family, String slot, List<PartEntry> parts, boolean small) {
    JsonObject model = new JsonObject();
    model.addProperty("loader", "tconstruct:tool");
    model.addProperty("parent", "forge:item/default");

    JsonObject textures = new JsonObject();
    JsonArray partArray = new JsonArray();
    for (PartEntry part : parts) {
      textures.addProperty(part.key(), texture(part, family, slot));
      JsonObject partObject = new JsonObject();
      partObject.addProperty("name", part.key());
      partObject.addProperty("index", small ? part.smallIndex() : part.largeIndex());
      partArray.add(partObject);
    }
    model.add("textures", textures);
    model.add("parts", partArray);

    JsonArray modifierRoots = new JsonArray();
    modifierRoots.add(TConstruct.MOD_ID + ":" + family + "/" + slot);
    model.add("modifier_roots", modifierRoots);

    JsonArray firstModifiers = new JsonArray();
    firstModifiers.add("tconstruct:trim");
    model.add("first_modifiers", firstModifiers);

    JsonObject predicate = new JsonObject();
    predicate.addProperty("tconstruct:broken", 1);
    JsonObject override = new JsonObject();
    override.add("predicate", predicate);
    override.addProperty("model", TConstruct.MOD_ID + ":item/armor/" + family + "/" + slot + "_broken");
    JsonArray overrides = new JsonArray();
    overrides.add(override);
    model.add("overrides", overrides);
    return model;
  }

  private static String texture(PartEntry part, String family, String slot) {
    String base = TConstruct.MOD_ID + ":item/armor/";
    return switch (part.textureType()) {
      case "shared" -> base + "shared/" + slot + "/" + part.textureName();
      case "family" -> base + family + "/" + slot + "/" + part.textureName();
      case "composite" -> base + "composite/" + slot + "/" + part.textureName();
      case "forged" -> base + "forged/" + slot + "/" + part.textureName();
      default -> throw new IllegalStateException("Unknown armor texture type: " + part.textureType());
    };
  }

  private static JsonObject buildBrokenModel(JsonObject base) {
    JsonObject broken = base.deepCopy();
    broken.remove("overrides");

    JsonObject brokenTextures = new JsonObject();
    for (Map.Entry<String,JsonElement> entry : base.getAsJsonObject("textures").entrySet()) {
      brokenTextures.addProperty(entry.getKey(), entry.getValue().getAsString() + "_broken");
    }
    broken.add("textures", brokenTextures);

    JsonArray brokenRoots = new JsonArray();
    for (JsonElement element : base.getAsJsonArray("modifier_roots")) {
      brokenRoots.add(element.getAsString() + "_broken");
    }
    broken.add("modifier_roots", brokenRoots);
    return broken;
  }

  private static boolean isSmall(ArmorItem.Type type) {
    return type == ArmorItem.Type.HELMET || type == ArmorItem.Type.BOOTS;
  }

  private static PartEntry part(String key, String textureType, String textureName, int smallIndex, int largeIndex) {
    return new PartEntry(key, textureType, textureName, smallIndex, largeIndex);
  }

  private static List<PartEntry> parts(PartEntry... entries) {
    return List.of(entries);
  }

  private record PartEntry(String key, String textureType, String textureName, int smallIndex, int largeIndex) {}
}
