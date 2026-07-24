package slimeknights.tconstruct.tools.data;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.DyedArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.FirstArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.FixedArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.MaterialArmorTextureSupplier;
import slimeknights.tconstruct.library.client.armor.texture.TrimArmorTextureSupplier;
import slimeknights.tconstruct.library.client.data.AbstractArmorModelProvider;
import slimeknights.tconstruct.tools.ArmorDefinitions;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

public class ArmorModelProvider extends AbstractArmorModelProvider {
  private static final String[] ARMOR_PART_SLOTS = { "helmet", "chestplate", "leggings", "boots" };
  private static final String[] ARMOR_PART_SMALL_SLOTS = { "helmet", "boots" };
  private static final String[] ARMOR_PART_LARGE_SLOTS = { "chestplate", "leggings" };

  public ArmorModelProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  protected void addModels() {
    addModel(ArmorDefinitions.TRAVELERS, name -> new ArmorTextureSupplier[] {
      FixedArmorTextureSupplier.builder(name, "/base_").build(),
      new FirstArmorTextureSupplier(
        new DyedArmorTextureSupplier(name, "/cuirass_", TinkerModifiers.dyed.getId(), null),
        new MaterialArmorTextureSupplier.Material(name, "/cuirass_", 1)
      ),
      new MaterialArmorTextureSupplier.Material(name, "/metal_", 0),
      TrimArmorTextureSupplier.INSTANCE
    });
    addModel(ArmorDefinitions.PLATE, name -> new ArmorTextureSupplier[] {
      new MaterialArmorTextureSupplier.Material(name, "/plating_", 0),
      new MaterialArmorTextureSupplier.Material(name, "/maille_", 1),
      TrimArmorTextureSupplier.INSTANCE
    });
    addModel(ArmorDefinitions.SLIMESUIT, name -> new ArmorTextureSupplier[] {
      new FirstArmorTextureSupplier(
        new MaterialArmorTextureSupplier.PersistentData(name, "/", TinkerModifiers.embellishment.getId()),
        FixedArmorTextureSupplier.builder(name, "/").materialSuffix(MaterialIds.enderslime).build()),
      TrimArmorTextureSupplier.INSTANCE
    });
    addArmorPartModels();
  }

  private void addArmorPartModels() {
    for (String slot : ARMOR_PART_SLOTS) {
      addArmorPartModel("standard/" + slot, from("linear_", 2), from("core_", 0));
      addArmorPartModel("knights/" + slot, from("linear_", 2), from("heavy_core_", 0));
      addArmorPartModel("explorers/" + slot, from("linear_", 2), from("layer_mail_1_", 1), from("frame_", 0));
    }

    for (String slot : ARMOR_PART_SMALL_SLOTS) {
      addArmorPartModel("light_composite/" + slot, from("layer_mail_1_", 1), from("frame_", 0), from("maille_", 2));
      addArmorPartModel("heavy_composite/" + slot, from("layer_plate_1_", 1), from("frame_", 0), from("maille_", 2));
      addArmorPartModel("light_forged/" + slot, from("layer_mail_1_", 2), from("frame_", 0), from("plating_", 1));
      addArmorPartModel("heavy_forged/" + slot, from("layer_plate_1_", 2), from("frame_", 0), from("plating_", 1));
    }

    for (String slot : ARMOR_PART_LARGE_SLOTS) {
      addArmorPartModel("light_composite/" + slot, from("layer_mail_1_", 1), from("layer_mail_2_", 2), from("frame_", 0), from("maille_", 3));
      addArmorPartModel("mix_composite/" + slot, from("layer_mail_1_", 1), from("layer_plate_2_", 2), from("frame_", 0), from("maille_", 3));
      addArmorPartModel("mix_composite_other/" + slot, from("layer_plate_1_", 1), from("layer_mail_2_", 2), from("frame_", 0), from("maille_", 3));
      addArmorPartModel("heavy_composite/" + slot, from("layer_plate_1_", 1), from("layer_plate_2_", 2), from("frame_", 0), from("maille_", 3));
      addArmorPartModel("light_forged/" + slot, from("layer_mail_1_", 2), from("layer_mail_2_", 3), from("frame_", 0), from("plating_", 1));
      addArmorPartModel("mix_forged/" + slot, from("layer_mail_1_", 2), from("layer_plate_2_", 3), from("frame_", 0), from("plating_", 1));
      addArmorPartModel("mix_forged_other/" + slot, from("layer_plate_1_", 2), from("layer_mail_2_", 3), from("frame_", 0), from("plating_", 1));
      addArmorPartModel("heavy_forged/" + slot, from("layer_plate_1_", 2), from("layer_plate_2_", 3), from("frame_", 0), from("plating_", 1));
    }
  }

  private void addArmorPartModel(String path, MaterialArmorTextureSupplier... layers) {
    ArmorTextureSupplier[] allLayers = new ArmorTextureSupplier[layers.length + 1];
    System.arraycopy(layers, 0, allLayers, 0, layers.length);
    allLayers[layers.length] = TrimArmorTextureSupplier.INSTANCE;
    addModel(TConstruct.getResource(path), allLayers);
  }

  private static MaterialArmorTextureSupplier from(String path, int index) {
    return new MaterialArmorTextureSupplier.Material(TConstruct.getResource(path), "", index);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Armor Models";
  }
}
