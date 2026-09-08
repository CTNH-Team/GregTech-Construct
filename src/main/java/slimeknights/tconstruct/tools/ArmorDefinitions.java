package slimeknights.tconstruct.tools;

import net.minecraft.world.item.ArmorItem;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

import static slimeknights.tconstruct.TConstruct.getResource;

public class ArmorDefinitions {
  private static final ArmorItem.Type[] CHEST_AND_LEGS = {ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS};

  /** Balanced armor set */
  public static final ModifiableArmorMaterial TRAVELERS = ModifiableArmorMaterial.create(getResource("travelers"), Sounds.EQUIP_TRAVELERS.getSound());
  public static final ToolDefinition TRAVELERS_SHIELD = ToolDefinition.create(TinkerTools.travelersShield);

  /** High defense armor set */
  public static final ModifiableArmorMaterial PLATE = ModifiableArmorMaterial.create(getResource("plate"), Sounds.EQUIP_PLATE.getSound());
  public static final ToolDefinition PLATE_SHIELD = ToolDefinition.create(TinkerTools.plateShield);

  /** High modifiers armor set */
  public static final ModifiableArmorMaterial SLIMESUIT = ModifiableArmorMaterial.create(getResource("slime"), Sounds.EQUIP_SLIME.getSound());
  public static final ToolDefinition SLIME_WINGS = ToolDefinition.create(TinkerTools.slimeWings);

  public static final ModifiableArmorMaterial STANDARD = ModifiableArmorMaterial.create(getResource("standard"), Sounds.EQUIP_STANDARD.getSound());
  public static final ModifiableArmorMaterial KNIGHTS = ModifiableArmorMaterial.create(getResource("knights"), Sounds.EQUIP_KNIGHTS.getSound());
  public static final ModifiableArmorMaterial EXPLORERS = ModifiableArmorMaterial.create(getResource("explorers"), Sounds.EQUIP_EXPLORERS.getSound());

  public static final ModifiableArmorMaterial LIGHT_COMPOSITE = ModifiableArmorMaterial.create(getResource("light_composite"), Sounds.EQUIP_LIGHT.getSound());
  public static final ModifiableArmorMaterial HEAVY_COMPOSITE = ModifiableArmorMaterial.create(getResource("heavy_composite"), Sounds.EQUIP_HEAVY.getSound());
  public static final ModifiableArmorMaterial LIGHT_FORGED = ModifiableArmorMaterial.create(getResource("light_forged"), Sounds.EQUIP_LIGHT.getSound());
  public static final ModifiableArmorMaterial HEAVY_FORGED = ModifiableArmorMaterial.create(getResource("heavy_forged"), Sounds.EQUIP_HEAVY.getSound());

  public static final ModifiableArmorMaterial MIX_COMPOSITE = ModifiableArmorMaterial.create(getResource("mix_composite"), Sounds.EQUIP_MIX.getSound(), CHEST_AND_LEGS);
  public static final ModifiableArmorMaterial MIX_COMPOSITE_OTHER = ModifiableArmorMaterial.create(getResource("mix_composite_other"), Sounds.EQUIP_MIX.getSound(), CHEST_AND_LEGS);
  public static final ModifiableArmorMaterial MIX_FORGED = ModifiableArmorMaterial.create(getResource("mix_forged"), Sounds.EQUIP_MIX.getSound(), CHEST_AND_LEGS);
  public static final ModifiableArmorMaterial MIX_FORGED_OTHER = ModifiableArmorMaterial.create(getResource("mix_forged_other"), Sounds.EQUIP_MIX.getSound(), CHEST_AND_LEGS);
}
