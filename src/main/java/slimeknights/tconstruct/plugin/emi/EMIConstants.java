package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Items;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.plugin.emi.material.MaterialStatsEmiConstants;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.List;

public final class EMIConstants {
  public static final ResourceLocation CASTING_TEXTURE = TConstruct.getResource("textures/gui/emi/casting.png");
  public static final ResourceLocation MELTING_TEXTURE = TConstruct.getResource("textures/gui/emi/melting.png");
  public static final ResourceLocation ALLOY_TEXTURE = TConstruct.getResource("textures/gui/emi/alloy.png");
  public static final ResourceLocation TINKER_STATION_TEXTURE = TConstruct.getResource("textures/gui/emi/tinker_station.png");

  public static final TConstructEmiCategory CASTING_BASIN = category("casting_basin", TinkerSmeltery.searedBasin,
      CASTING_TEXTURE, 0, 0, 117, 54);
  public static final TConstructEmiCategory CASTING_TABLE = category("casting_table", TinkerSmeltery.searedTable,
      CASTING_TEXTURE, 0, 0, 117, 54);
  public static final TConstructEmiCategory MOLDING = category("molding", TinkerSmeltery.searedBasin,
      CASTING_TEXTURE, 0, 55, 70, 57);
  public static final TConstructEmiCategory MELTING = category("melting", TinkerSmeltery.searedMelter,
      MELTING_TEXTURE, 0, 0, 132, 40);
  public static final TConstructEmiCategory FOUNDRY = category("foundry", TinkerSmeltery.foundryController,
      MELTING_TEXTURE, 0, 0, 132, 40);
  public static final TConstructEmiCategory ALLOY = category("alloy", TinkerSmeltery.smelteryController,
      ALLOY_TEXTURE, 0, 0, 172, 62);
  public static final TConstructEmiCategory ENTITY_MELTING = category("entity_melting", TinkerSmeltery.smelteryController,
      MELTING_TEXTURE, 0, 41, 150, 62);
  public static final TConstructEmiCategory MODIFIERS = category("modifiers", TinkerTables.tinkerStation,
      TINKER_STATION_TEXTURE, 0, 0, 128, 77);
  public static final TConstructEmiCategory SEVERING = category("severing", TinkerTables.tinkerStation,
      TINKER_STATION_TEXTURE, 0, 78, 100, 38);
  public static final TConstructEmiCategory TOOL_BUILDING = category("tool_recipes", TinkerTables.tinkerStation,
      TINKER_STATION_TEXTURE, 122, 77, 134, 66);
  public static final TConstructEmiCategory PART_BUILDER = category("part_builder", TinkerTables.partBuilder,
      TINKER_STATION_TEXTURE, 0, 117, 121, 46);
  public static final TConstructEmiCategory MODIFIER_WORKTABLE = category("worktable", TinkerTables.modifierWorktable,
      TINKER_STATION_TEXTURE, 0, 166, 121, 35);
  public static final TConstructEmiCategory HARVEST_STATS = category("harvest_stats", TinkerTables.tinkerStation,
      TINKER_STATION_TEXTURE, 0, 0, 178, 200);
  public static final TConstructEmiCategory RANGED_STATS = category("ranged_stats", TinkerTables.tinkerStation,
      TINKER_STATION_TEXTURE, 0, 0, 178, 200);
  public static final TConstructEmiCategory ARMOR_STATS = category("armor_stats", TinkerTables.tinkerStation,
      TINKER_STATION_TEXTURE, 0, 0, 178, 200);
  public static final TConstructEmiCategory AMMO_STATS = category("ammo_stats", TinkerTools.arrow,
      TINKER_STATION_TEXTURE, 0, 0, 178, 200);
  public static final TConstructEmiCategory SKULL_STATS = category("skull_stats", Items.SKELETON_SKULL,
      TINKER_STATION_TEXTURE, 0, 0, 178, 200);

  public static final List<TConstructEmiCategory> ALL = List.of(
      CASTING_BASIN, CASTING_TABLE, MOLDING, MELTING, FOUNDRY, ALLOY,
      ENTITY_MELTING, MODIFIERS, SEVERING, TOOL_BUILDING, PART_BUILDER, MODIFIER_WORKTABLE,
      HARVEST_STATS, RANGED_STATS, ARMOR_STATS, AMMO_STATS, SKULL_STATS);

  private EMIConstants() {}

  private static TConstructEmiCategory category(String path, ItemLike icon, ResourceLocation texture,
                                                int u, int v, int width, int height) {
    return new TConstructEmiCategory(TConstruct.getResource(path), EmiStack.of(icon), path, texture, u, v, width, height);
  }

  public static final class TConstructEmiCategory extends EmiRecipeCategory {
    private final String path;
    @Getter
    private final ResourceLocation texture;
    private final int u;
    private final int v;
    private final int width;
    private final int height;

    private TConstructEmiCategory(ResourceLocation id, EmiStack icon, String path, ResourceLocation texture,
                                   int u, int v, int width, int height) {
      super(id, icon);
      this.path = path;
      this.texture = texture;
      this.u = u;
      this.v = v;
      this.width = width;
      this.height = height;
    }

    public int getTextureU() {
      return u;
    }

    public int getTextureV() {
      return v;
    }

    public int getDisplayWidth() {
      return width;
    }

    public int getDisplayHeight() {
      return height;
    }

    @Override
    public Component getName() {
      return Component.translatable("tconstruct.emi." + path);
    }
  }
}
