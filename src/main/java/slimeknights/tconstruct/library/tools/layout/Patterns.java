package slimeknights.tconstruct.library.tools.layout;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;

public class Patterns {
  /** Pickaxe pattern used in center of the repair GUI */
  public static final Pattern PICKAXE = pattern("pickaxe");
  /* Icons used for the outer slots in repair UIs */
  public static final Pattern QUARTZ = pattern("quartz");
  public static final Pattern DUST = pattern("dust");
  public static final Pattern LAPIS = pattern("lapis");
  public static final Pattern INGOT = pattern("ingot");
  public static final Pattern GEM = pattern("gem");
  /** Shield for tool offhand slot */
  public static final Pattern SHIELD = pattern("shield");

  /** Repair icon, not an outline but a button icon */
  public static final Pattern REPAIR = pattern("button_repair");
  /** Icon with multiple plate armor pieces */
  public static final Pattern PLATE_ARMOR = pattern("plate_armor");
  public static final Pattern STANDARD_ARMOR = pattern("standard");
  public static final Pattern KNIGHTS_ARMOR = pattern("knights");
  public static final Pattern EXPLORERS_ARMOR = pattern("explorers");
  public static final Pattern COMPOSITE_ARMOR_SMALL = pattern("composite_small");
  public static final Pattern COMPOSITE_ARMOR_LARGE = pattern("composite_large");
  public static final Pattern FORGED_ARMOR_SMALL = pattern("forged_small");
  public static final Pattern FORGED_ARMOR_LARGE = pattern("forged_large");
  public static final Pattern PLATE_ARMOR_SMALL = pattern("plate_small");
  public static final Pattern PLATE_ARMOR_LARGE = pattern("plate_large");
  public static final Pattern ARMOR_CAST = pattern("armor_cast");
  public static final Pattern MASSIVE_ARMOR_CAST = pattern("massive_armor_cast");
  public static final Pattern ARMOR_FRAME = pattern("armor_frame");
  public static final Pattern FRAME_SMALL = pattern("frame_of_small");
  public static final Pattern FRAME_LARGE = pattern("frame_of_large");
  public static final Pattern MAIL_PLATE = pattern("mail_plate");
  public static final Pattern PLATING_SMALL = pattern("plating_small");
  public static final Pattern PLATING_LARGE = pattern("plating_large");
  /** Icon with multiple ammo items */
  public static final Pattern THROWN_AMMO = pattern("thrown_ammo");
  /** Pattern for generic plating */
  public static final Pattern PLATING = pattern("plating");
  /** Pattern for generic arrow parts */
  public static final Pattern ARROW_PART = pattern("arrow_part");
  /** Feather for arrows */
  public static final Pattern FEATHER = pattern("feather");
  /** Icon for a result slot */
  public static final Pattern RESULT = pattern("result");

  private static Pattern pattern(String name) {
    return new Pattern(TConstruct.MOD_ID, name);
  }

  private Patterns() {}
}
