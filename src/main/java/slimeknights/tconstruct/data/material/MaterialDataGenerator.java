package slimeknights.tconstruct.data.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.OrCondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.data.pack.TiCDynamicDataPack;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialManager;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsManager;
import slimeknights.tconstruct.library.materials.traits.MaterialTraitsManager;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.stats.GripMaterialStats;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.tools.stats.LimbMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.SkullStats;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider.*;

/**
 * 材料数据生成器
 * 运行时生成材料定义、属性、特性数据，替代 DataGen
 */
public class MaterialDataGenerator {

    /** TConstruct 命名空间 */
    private static final String TCONSTRUCT = TConstruct.MOD_ID;
    private static final Map<MaterialId, JsonObject> MATERIAL_STATS = new LinkedHashMap<>();

    /**
     * 注册所有材料数据
     * 在 TiCRecipes.registerRecipes() 中调用
     */
    public static void register() {
        addMaterialDefinitions();
        addMaterialStats();
        addMaterialTraits();
    }

    // ===== 材料定义 =====

    /**
     * 添加所有材料定义
     * 生成 JSON 到 tinkering/materials/definition/{name}.json
     */
    private static void addMaterialDefinitions() {
        // tier 1
        addMaterialDefinition(MaterialIds.wood,   0, ORDER_GENERAL, true);
        addMaterialDefinition(MaterialIds.rock,   1, ORDER_HARVEST, true);
        addMaterialDefinition(MaterialIds.flint,  1, ORDER_WEAPON,  true);
        addMaterialDefinition(MaterialIds.copper, 1, ORDER_SPECIAL, true);
        addMaterialDefinition(MaterialIds.bone,   1, ORDER_SPECIAL, true);
        addMaterialDefinition(MaterialIds.bamboo, 1, ORDER_RANGED,  true);
        // tier 1 - end
        addMaterialDefinition(MaterialIds.chorus, 1, ORDER_END,     true);
        // tier 1 - binding
        addMaterialDefinition(MaterialIds.string,  0, ORDER_GENERAL, true);
        addMaterialDefinition(MaterialIds.leather, 0, ORDER_BINDING, true);
        addMaterialDefinition(MaterialIds.vine,    1, ORDER_BINDING, true);
        // tier 1 - shield cores
        addMaterialDefinition(MaterialIds.cactus, 1, ORDER_BINDING, true);
        // tier 1 - ammo
        addMaterialDefinition(MaterialIds.feather, 0, ORDER_GENERAL, true);
        addMaterialDefinition(MaterialIds.wool,    1, ORDER_BINDING, true);
        addMaterialDefinition(MaterialIds.leaves,  1, ORDER_BINDING, true);
        addMaterialDefinition(MaterialIds.paper,   1, ORDER_BINDING, true);

        // tier 2
        addMaterialDefinition(MaterialIds.iron,        2, ORDER_GENERAL, false);
        addMaterialDefinition(MaterialIds.searedStone, 2, ORDER_HARVEST, false);
        addMaterialDefinition(MaterialIds.venombone,   2, ORDER_WEAPON,  true);
        addMaterialDefinition(MaterialIds.slimewood,   2, ORDER_SPECIAL, true);
        addMaterialDefinition(MaterialIds.slimeskin,   2, ORDER_BINDING, false);
        addMaterialDefinition(MaterialIds.gold,        2, ORDER_REPAIR, false);
        // tier 2 - nether
        addMaterialDefinition(MaterialIds.scorchedStone, 2, ORDER_NETHER, false);
        addMaterialDefinition(MaterialIds.necroticBone,  2, ORDER_NETHER, true);
        // tier 2 - end
        addMaterialDefinition(MaterialIds.whitestone, 2, ORDER_END, true);
        // tier 2 - binding
        addMaterialDefinition(MaterialIds.skyslimeVine, 2, ORDER_BINDING, true);
        addMaterialDefinition(MaterialIds.weepingVine,  2, ORDER_BINDING, true);
        addMaterialDefinition(MaterialIds.twistingVine, 2, ORDER_BINDING, true);
        // tier 2 - ammo
        addMaterialDefinition(MaterialIds.amethyst,   2, ORDER_REPAIR, false);
        addMaterialDefinition(MaterialIds.prismarine, 2, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.earthslime, 2, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.skyslime,   2, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.blaze,      2, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.enderPearl, 2, ORDER_REPAIR, false);
        addMaterialDefinition(MaterialIds.glass,      2, ORDER_REPAIR, false);
        addMaterialDefinition(MaterialIds.slimeball,  2, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.gunpowder,  2, ORDER_REPAIR, true);
        // bloodbone redirect
        addRedirectDefinition(new MaterialId(TCONSTRUCT, "bloodbone"), MaterialIds.venombone);

        // tier 3
        addMaterialDefinition(MaterialIds.slimesteel,     3, ORDER_GENERAL, false);
        addMaterialDefinition(MaterialIds.amethystBronze, 3, ORDER_HARVEST, false);
        addMaterialDefinition(MaterialIds.nahuatl,        3, ORDER_WEAPON,  true);
        addMaterialDefinition(MaterialIds.obsidian,       3, ORDER_WEAPON,  false);
        addMaterialDefinition(MaterialIds.roseGold,       3, ORDER_SPECIAL, false);
        addMaterialDefinition(MaterialIds.pigIron,        3, ORDER_SPECIAL, false);
        // tier 3 (nether)
        addMaterialDefinition(MaterialIds.steel,  3, ORDER_NETHER, false);
        addMaterialDefinition(MaterialIds.cobalt, 3, ORDER_NETHER, false);
        // tier 3 - binding
        addMaterialDefinition(MaterialIds.darkthread, 3, ORDER_BINDING, false);
        addMaterialDefinition(MaterialIds.ichorskin,  3, ORDER_BINDING, false);
        // tier 3 - shield cores
        addMaterialDefinition(MaterialIds.ice, 3, ORDER_BINDING, true);
        // tier 3 - ammo
        addMaterialDefinition(MaterialIds.quartz,    3, ORDER_REPAIR, false);
        addMaterialDefinition(MaterialIds.ichor,     3, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.glowstone, 3, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.magnetite, 3, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.magma,     3, ORDER_REPAIR, true);

        // tier 4
        addMaterialDefinition(MaterialIds.queensSlime, 4, ORDER_GENERAL, false);
        addMaterialDefinition(MaterialIds.cinderslime, 4, ORDER_GENERAL, false);
        addMaterialDefinition(MaterialIds.hepatizon,   4, ORDER_HARVEST, false);
        addMaterialDefinition(MaterialIds.manyullyn,   4, ORDER_WEAPON,  false);
        addMaterialDefinition(MaterialIds.blazingBone, 4, ORDER_SPECIAL, true);
        addMaterialDefinition(MaterialIds.knightmetal, 4, ORDER_END,     false);
        // tier 4 - binding
        addMaterialDefinition(MaterialIds.ancientHide, 4, ORDER_BINDING, false);
        addMaterialDefinition(MaterialIds.blazewood,   4, ORDER_BINDING, true);
        // tier 4 - ammo
        addMaterialDefinition(MaterialIds.shulker,     4, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.dragonScale, 4, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.enderslime,  4, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.knightly,    4, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.endRod,      4, ORDER_REPAIR, true);

        // tier 5 binding, temporarily in book 4
        addMaterialDefinition(MaterialIds.enderslimeVine, 4, ORDER_BINDING, true);

        // ancient - hidden material with no craftable
        addMaterialDefinition(MaterialIds.ancient, 4, ORDER_NETHER, false, true);

        // tier 2 (mod integration) - 运行时不添加条件，直接作为普通材料
        addMaterialDefinition(MaterialIds.osmium,   2, ORDER_COMPAT + ORDER_GENERAL, false);
        addMaterialDefinition(MaterialIds.lead,     2, ORDER_COMPAT + ORDER_HARVEST, false);
        addMaterialDefinition(MaterialIds.silver,   2, ORDER_COMPAT + ORDER_WEAPON, false);
        addMaterialDefinition(MaterialIds.aluminum, 2, ORDER_COMPAT + ORDER_RANGED, false);
        addMaterialDefinition(MaterialIds.manaSteel, 2, ORDER_COMPAT + ORDER_GENERAL, false);
        addMaterialDefinition(MaterialIds.ironwood, 2, ORDER_COMPAT + ORDER_GENERAL, true);
        addMaterialDefinition(MaterialIds.treatedWood, 2, ORDER_COMPAT + ORDER_GENERAL, true);
        // tier 3 (mod integration)
        addMaterialDefinition(MaterialIds.electrum,        3, ORDER_COMPAT + ORDER_GENERAL, false);
        addMaterialDefinition(MaterialIds.bronze,          3, ORDER_COMPAT + ORDER_HARVEST, false);
        addMaterialDefinition(MaterialIds.constantan,      3, ORDER_COMPAT + ORDER_HARVEST, false);
        addMaterialDefinition(MaterialIds.invar,           3, ORDER_COMPAT + ORDER_WEAPON,  false);
        addMaterialDefinition(MaterialIds.terraSteel, 3, ORDER_COMPAT + ORDER_GENERAL, false);
        addMaterialDefinition(MaterialIds.pewter,          3, ORDER_COMPAT + ORDER_WEAPON,  false);
        addMaterialDefinition(MaterialIds.platedSlimewood, 3, ORDER_COMPAT + ORDER_SPECIAL, false);
        addMaterialDefinition(MaterialIds.necronium,       3, ORDER_COMPAT + ORDER_WEAPON, true);
        addMaterialDefinition(MaterialIds.steeleaf, 3, ORDER_COMPAT + ORDER_SPECIAL, false);
        // tier 4 (mod integration)
        addMaterialDefinition(MaterialIds.fiery,           4, ORDER_COMPAT + ORDER_END, false);

        // slimesuit - textures
        addMaterialDefinition(MaterialIds.blood, 2, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.clay,  1, ORDER_REPAIR, true);
        addMaterialDefinition(MaterialIds.honey, 1, ORDER_REPAIR, true);
        // slimesuit - repair
        addMaterialDefinition(MaterialIds.phantom, 1, ORDER_REPAIR, true);

        // redirects
        addRedirectDefinition(new MaterialId(TCONSTRUCT, "chain"), MaterialIds.roseGold);
        addRedirectDefinition(new MaterialId(TCONSTRUCT, "rotten_flesh"), MaterialIds.leather);
        addRedirectDefinition(new MaterialId(TCONSTRUCT, "platinum"), MaterialIds.searedStone);
        // tungsten: redirect to lead (unconditional for simplicity)
        addRedirectDefinition(new MaterialId(TCONSTRUCT, "tungsten"), MaterialIds.lead);
    }

    // ===== 材料属性 =====

    /**
     * 添加所有材料属性
     * 生成 JSON 到 tinkering/materials/stats/{name}.json
     */
    private static void addMaterialStats() {
        MATERIAL_STATS.clear();
        addMeleeHarvestStats();
        addRangedStats();
        addAmmoStats();
        addArmorStats();
        addMiscStats();
        MATERIAL_STATS.forEach(MaterialDataGenerator::writeMaterialStatsJson);
        MATERIAL_STATS.clear();
    }

    private static void addMeleeHarvestStats() {
        // tier 1
        addMaterialStatsJson(MaterialIds.wood,
                new HeadMaterialStats(60, 2f, Tiers.WOOD, 0f),
                HandleMaterialStats.percents().build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.rock,
                new HeadMaterialStats(130, 4f, Tiers.STONE, 1f),
                HandleMaterialStats.multipliers().durability(0.9f).miningSpeed(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.flint,
                new HeadMaterialStats(85, 3.5f, Tiers.STONE, 1.25f),
                HandleMaterialStats.multipliers().durability(0.85f).attackDamage(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.copper,
                new HeadMaterialStats(210, 5.0f, Tiers.IRON, 0.5f),
                HandleMaterialStats.multipliers().durability(0.80f).miningSpeed(1.1f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.bone,
                new HeadMaterialStats(100, 2.5f, Tiers.STONE, 1.25f),
                HandleMaterialStats.multipliers().durability(0.75f).attackSpeed(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.chorus,
                new HeadMaterialStats(180, 3.0f, Tiers.STONE, 1.0f),
                HandleMaterialStats.multipliers().durability(1.1f).miningSpeed(0.95f).attackSpeed(0.9f).build(),
                StatlessMaterialStats.BINDING);
        // tier 1 - binding
        addMaterialStatsJson(MaterialIds.string, StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.leather, StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.vine, StatlessMaterialStats.BINDING);

        // tier 2
        addMaterialStatsJson(MaterialIds.iron,
                new HeadMaterialStats(250, 6f, Tiers.IRON, 2f),
                HandleMaterialStats.multipliers().durability(1.10f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.searedStone,
                new HeadMaterialStats(225, 6.5f, Tiers.IRON, 1.5f),
                HandleMaterialStats.multipliers().durability(0.85f).miningSpeed(1.10f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.venombone,
                new HeadMaterialStats(175, 4.5f, Tiers.IRON, 2.25f),
                HandleMaterialStats.multipliers().durability(0.9f).attackSpeed(1.1f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.slimewood,
                new HeadMaterialStats(375, 4f, Tiers.IRON, 1f),
                HandleMaterialStats.multipliers().durability(1.3f).miningSpeed(0.85f).attackDamage(0.85f).build(),
                StatlessMaterialStats.BINDING);
        // tier 2 - nether
        addMaterialStatsJson(MaterialIds.scorchedStone,
                new HeadMaterialStats(120, 4.5f, Tiers.IRON, 2.5f),
                HandleMaterialStats.multipliers().durability(0.8f).attackSpeed(1.05f).attackDamage(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.necroticBone,
                new HeadMaterialStats(125, 4f, Tiers.IRON, 2.25f),
                HandleMaterialStats.multipliers().durability(0.7f).attackSpeed(1.15f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        // tier 2 - end
        addMaterialStatsJson(MaterialIds.whitestone,
                new HeadMaterialStats(275, 6.0f, Tiers.IRON, 1.25f),
                HandleMaterialStats.multipliers().durability(0.95f).miningSpeed(1.1f).attackSpeed(0.95f).build(),
                StatlessMaterialStats.BINDING);
        // tier 2 - bindings
        addMaterialStatsJson(MaterialIds.skyslimeVine, StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.weepingVine, StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.twistingVine, StatlessMaterialStats.BINDING);

        // tier 2 (mod integration)
        addMaterialStatsJson(MaterialIds.osmium,
                new HeadMaterialStats(500, 4.5f, Tiers.IRON, 2.0f),
                HandleMaterialStats.multipliers().durability(1.2f).attackSpeed(0.9f).miningSpeed(0.9f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.silver,
                new HeadMaterialStats(300, 5.5f, Tiers.IRON, 2.25f),
                HandleMaterialStats.multipliers().durability(0.9f).miningSpeed(1.05f).attackSpeed(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.lead,
                new HeadMaterialStats(200, 6.5f, Tiers.IRON, 1.75f),
                HandleMaterialStats.multipliers().durability(0.9f).miningSpeed(1.1f).attackSpeed(0.9f).attackDamage(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.treatedWood,
                new HeadMaterialStats(300, 3.5f, Tiers.STONE, 1.5f),
                HandleMaterialStats.multipliers().durability(1.25f).attackDamage(0.9f).miningSpeed(0.9f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.ironwood,
                new HeadMaterialStats(510, 6.5f, Tiers.IRON, 2f),
                HandleMaterialStats.multipliers().durability(1.15f).attackSpeed(0.95f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.manaSteel,
                new HeadMaterialStats(400, 6.5f, Tiers.IRON, 2.5f),
                HandleMaterialStats.multipliers().durability(1.15f).miningSpeed(1.05f).attackSpeed(1.05f).attackDamage(1.1f).build(),
                StatlessMaterialStats.BINDING);

        // tier 3
        addMaterialStatsJson(MaterialIds.slimesteel,
                new HeadMaterialStats(1040, 6f, Tiers.DIAMOND, 2.5f),
                HandleMaterialStats.multipliers().durability(1.2f).attackSpeed(0.95f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.amethystBronze,
                new HeadMaterialStats(720, 7f, Tiers.DIAMOND, 1.5f),
                HandleMaterialStats.multipliers().miningSpeed(1.10f).attackSpeed(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.nahuatl,
                new HeadMaterialStats(350, 4.5f, Tiers.DIAMOND, 3f),
                HandleMaterialStats.multipliers().durability(0.9f).attackSpeed(0.9f).attackDamage(1.25f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.pigIron,
                new HeadMaterialStats(580, 6f, Tiers.DIAMOND, 2.5f),
                HandleMaterialStats.multipliers().durability(1.10f).miningSpeed(0.85f).attackDamage(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.roseGold,
                new HeadMaterialStats(175, 9f, Tiers.GOLD, 1f),
                HandleMaterialStats.multipliers().durability(0.7f).miningSpeed(1.10f).attackSpeed(1.10f).build(),
                StatlessMaterialStats.BINDING);
        // tier 3 (nether)
        addMaterialStatsJson(MaterialIds.cobalt,
                new HeadMaterialStats(800, 6.5f, Tiers.DIAMOND, 2.25f),
                HandleMaterialStats.multipliers().durability(1.05f).miningSpeed(1.05f).attackSpeed(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.steel,
                new HeadMaterialStats(775, 6f, Tiers.DIAMOND, 2.75f),
                HandleMaterialStats.multipliers().durability(1.05f).miningSpeed(1.05f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        // tier 3 - binding
        addMaterialStatsJson(MaterialIds.darkthread, StatlessMaterialStats.BINDING);

        // tier 3 (mod integration)
        addMaterialStatsJson(MaterialIds.bronze,
                new HeadMaterialStats(760, 6.5f, Tiers.DIAMOND, 2.25f),
                HandleMaterialStats.multipliers().durability(1.10f).miningSpeed(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.constantan,
                new HeadMaterialStats(675, 7.5f, Tiers.DIAMOND, 1.75f),
                HandleMaterialStats.multipliers().durability(0.95f).miningSpeed(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.invar,
                new HeadMaterialStats(630, 5.5f, Tiers.DIAMOND, 2.5f),
                HandleMaterialStats.multipliers().miningSpeed(0.9f).attackSpeed(1.05f).attackDamage(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.pewter,
                new HeadMaterialStats(316, 3.5f, Tiers.DIAMOND, 3.0f),
                HandleMaterialStats.multipliers().durability(0.75f).miningSpeed(0.8f).attackDamage(1.2f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.necronium,
                new HeadMaterialStats(357, 4.0f, Tiers.DIAMOND, 2.75f),
                HandleMaterialStats.multipliers().durability(0.8f).attackSpeed(1.15f).attackDamage(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.electrum,
                new HeadMaterialStats(225, 8f, Tiers.IRON, 1.5f),
                HandleMaterialStats.multipliers().durability(0.65f).attackSpeed(1.15f).miningSpeed(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.platedSlimewood,
                new HeadMaterialStats(595, 5.0f, Tiers.DIAMOND, 2.0f),
                HandleMaterialStats.multipliers().durability(1.25f).miningSpeed(0.9f).attackSpeed(0.9f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.steeleaf,
                new HeadMaterialStats(200, 8, Tiers.DIAMOND, 3),
                HandleMaterialStats.multipliers().durability(0.65f).attackSpeed(1.15f).miningSpeed(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.terraSteel,
                new HeadMaterialStats(750, 7.5f, Tiers.DIAMOND, 3.5f),
                HandleMaterialStats.multipliers().durability(1.35f).miningSpeed(1.2f).attackSpeed(1.2f).attackDamage(1.3f).build(),
                StatlessMaterialStats.BINDING);

        // tier 4
        addMaterialStatsJson(MaterialIds.cinderslime,
                new HeadMaterialStats(1221, 6.5f, Tiers.NETHERITE, 2.25f),
                HandleMaterialStats.multipliers().durability(1.2f).miningSpeed(1.1f).attackSpeed(0.90f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.queensSlime,
                new HeadMaterialStats(1650, 6f, Tiers.NETHERITE, 2f),
                HandleMaterialStats.multipliers().durability(1.35f).miningSpeed(0.9f).attackSpeed(0.95f).attackDamage(0.95f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.hepatizon,
                new HeadMaterialStats(975, 8f, Tiers.NETHERITE, 2.5f),
                HandleMaterialStats.multipliers().durability(1.1f).miningSpeed(1.2f).attackDamage(0.9f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.manyullyn,
                new HeadMaterialStats(1250, 6.5f, Tiers.NETHERITE, 3.5f),
                HandleMaterialStats.multipliers().durability(1.1f).miningSpeed(0.9f).attackSpeed(0.95f).attackDamage(1.20f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.blazingBone,
                new HeadMaterialStats(530, 6f, Tiers.IRON, 3f),
                HandleMaterialStats.multipliers().durability(0.85f).attackDamage(1.05f).attackSpeed(1.2f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.ancientHide, StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.ancient, new HeadMaterialStats(745, 7f, Tiers.NETHERITE, 2.5f));

        // tier 4 (end)
        addMaterialStatsJson(MaterialIds.knightmetal,
                new HeadMaterialStats(512, 8f, Tiers.NETHERITE, 3.0f),
                HandleMaterialStats.multipliers().miningSpeed(0.85f).attackSpeed(1.05f).attackDamage(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStatsJson(MaterialIds.enderslimeVine, StatlessMaterialStats.BINDING);

        // tier 4 (mod integration)
        addMaterialStatsJson(MaterialIds.fiery,
                new HeadMaterialStats(1024, 8, Tiers.NETHERITE, 3.5f),
                HandleMaterialStats.multipliers().miningSpeed(1.15f).attackSpeed(0.9f).attackDamage(1.15f).build(),
                StatlessMaterialStats.BINDING);
    }

    private static void addRangedStats() {
        // tier 1
        addMaterialStatsJson(MaterialIds.wood,
                new LimbMaterialStats(60, 0, 0, 0),
                new GripMaterialStats(0f, 0, 0));
        addMaterialStatsJson(MaterialIds.bamboo,
                new LimbMaterialStats(70, 0.1f, -0.05f, -0.05f),
                new GripMaterialStats(-0.05f, 0.05f, 0.75f));
        addMaterialStatsJson(MaterialIds.cactus,
                new LimbMaterialStats(110, -0.05f, 0.05f, -0.10f),
                new GripMaterialStats(-0.1f, -0.05f, 1.5f));
        addMaterialStatsJson(MaterialIds.bone,
                new LimbMaterialStats(100, 0.05f, -0.05f, 0.05f),
                new GripMaterialStats(-0.25f, 0.05f, 1.25f));
        addMaterialStatsJson(MaterialIds.copper,
                new LimbMaterialStats(210, -0.10f, 0.05f, 0f),
                new GripMaterialStats(-0.2f, 0f, 0.5f));
        addMaterialStatsJson(MaterialIds.chorus,
                new LimbMaterialStats(180, -0.05f, -0.05f, 0.1f),
                new GripMaterialStats(0.1f, -0.1f, 1.0f));
        // tier 1 - bowstring
        addMaterialStatsJson(MaterialIds.string, StatlessMaterialStats.BOWSTRING);
        addMaterialStatsJson(MaterialIds.vine, StatlessMaterialStats.BOWSTRING);
        addMaterialStatsJson(MaterialIds.leather, StatlessMaterialStats.BOWSTRING);

        // tier 2
        addMaterialStatsJson(MaterialIds.slimewood,
                new LimbMaterialStats(375, 0, -0.05f, 0.1f),
                new GripMaterialStats(0.4f, -0.2f, 1f));
        addMaterialStatsJson(MaterialIds.venombone,
                new LimbMaterialStats(175, 0.1f, -0.1f, 0.05f),
                new GripMaterialStats(-0.1f, -0.1f, 2.25f));
        addMaterialStatsJson(MaterialIds.iron,
                new LimbMaterialStats(250, -0.2f, 0.1f, 0),
                new GripMaterialStats(0.1f, 0f, 2f));
        addMaterialStatsJson(MaterialIds.necroticBone,
                new LimbMaterialStats(125, 0.05f, 0.05f, -0.15f),
                new GripMaterialStats(-0.3f, 0.1f, 2.25f));
        // tier 2 - bowstring
        addMaterialStatsJson(MaterialIds.skyslimeVine, StatlessMaterialStats.BOWSTRING);
        addMaterialStatsJson(MaterialIds.weepingVine, StatlessMaterialStats.BOWSTRING);
        addMaterialStatsJson(MaterialIds.twistingVine, StatlessMaterialStats.BOWSTRING);
        addMaterialStatsJson(MaterialIds.slimeskin, StatlessMaterialStats.BOWSTRING);

        // tier 2 - compat
        addMaterialStatsJson(MaterialIds.aluminum,
                new LimbMaterialStats(225, 0.15f, -0.15f, -0.05f),
                new GripMaterialStats(-0.15f, 0.15f, 2f));
        addMaterialStatsJson(MaterialIds.silver,
                new LimbMaterialStats(300, -0.05f, 0, 0.1f),
                new GripMaterialStats(-0.1f, -0.05f, 2.25f));
        addMaterialStatsJson(MaterialIds.lead,
                new LimbMaterialStats(200, -0.3f, 0.15f, -0.05f),
                new GripMaterialStats(-0.1f, 0.1f, 1.75f));
        addMaterialStatsJson(MaterialIds.treatedWood,
                new LimbMaterialStats(300, 0.05f, -0.1f, 0.05f),
                new GripMaterialStats(0.25f, -0.15f, 1.5f));
        addMaterialStatsJson(MaterialIds.ironwood,
                new LimbMaterialStats(512, 0.05f, 0.05f, -0.15f),
                new GripMaterialStats(0.15f, -0.15f, 2f));
        addMaterialStatsJson(MaterialIds.manaSteel,
                new LimbMaterialStats(350, 0.15f, 0.1f, 0.1f),
                new GripMaterialStats(0.15f, 0.1f, 2.5f));

        // tier 3
        addMaterialStatsJson(MaterialIds.slimesteel,
                new LimbMaterialStats(1040, -0.05f, -0.05f, 0.15f),
                new GripMaterialStats(0.2f, -0.1f, 2.5f));
        addMaterialStatsJson(MaterialIds.nahuatl,
                new LimbMaterialStats(350, 0.2f, -0.15f, 0.1f),
                new GripMaterialStats(-0.1f, -0.15f, 3f));
        addMaterialStatsJson(MaterialIds.amethystBronze,
                new LimbMaterialStats(720, -0.25f, 0.15f, -0.1f),
                new GripMaterialStats(0f, 0.1f, 1.5f));
        addMaterialStatsJson(MaterialIds.roseGold,
                new LimbMaterialStats(175, 0.15f, -0.25f, 0.15f),
                new GripMaterialStats(-0.3f, 0.25f, 1.0f),
                StatlessMaterialStats.BOWSTRING);
        addMaterialStatsJson(MaterialIds.cobalt,
                new LimbMaterialStats(800, 0.05f, 0.05f, 0.05f),
                new GripMaterialStats(0.05f, 0.05f, 2.25f));
        addMaterialStatsJson(MaterialIds.blazingBone,
                new LimbMaterialStats(530, 0.1f, 0.1f, -0.3f),
                new GripMaterialStats(-0.15f, -0.10f, 3f));
        // tier 3 - bowstring
        addMaterialStatsJson(MaterialIds.darkthread, StatlessMaterialStats.BOWSTRING);

        // tier 3 - compat
        addMaterialStatsJson(MaterialIds.invar,
                new LimbMaterialStats(630, -0.15f, -0.1f, 0.2f),
                new GripMaterialStats(0, 0.05f, 2.5f));
        addMaterialStatsJson(MaterialIds.pewter,
                new LimbMaterialStats(316, 0.1f, -0.05f, -0.2f),
                new GripMaterialStats(-0.2f, 0.15f, 3.0f));
        addMaterialStatsJson(MaterialIds.necronium,
                new LimbMaterialStats(357, 0.15f, -0.1f, -0.05f),
                new GripMaterialStats(-0.2f, 0.15f, 2.75f));
        addMaterialStatsJson(MaterialIds.constantan,
                new LimbMaterialStats(675, 0.2f, -0.05f, -0.25f),
                new GripMaterialStats(-0.05f, 0.1f, 1.75f));
        addMaterialStatsJson(MaterialIds.steel,
                new LimbMaterialStats(775, -0.3f, 0.2f, -0.1f),
                new GripMaterialStats(0.05f, -0.05f, 2.75f));
        addMaterialStatsJson(MaterialIds.bronze,
                new LimbMaterialStats(760, -0.2f, 0.15f, -0.2f),
                new GripMaterialStats(0.1f, 0f, 2.25f));
        addMaterialStatsJson(MaterialIds.electrum,
                new LimbMaterialStats(225, -0.25f, 0.1f, 0.15f),
                new GripMaterialStats(-0.35f, 0.2f, 1.5f));
        addMaterialStatsJson(MaterialIds.platedSlimewood,
                new LimbMaterialStats(595, 0.15f, -0.15f, 0),
                new GripMaterialStats(0.25f, -0.1f, 2f));
        addMaterialStatsJson(MaterialIds.steeleaf,
                new LimbMaterialStats(200, 0, 0, 0.15f),
                new GripMaterialStats(-0.35f, 0, 2.75f));
        addMaterialStatsJson(MaterialIds.terraSteel,
                new LimbMaterialStats(750, 0.2f, 0.3f, 0.3f),
                new GripMaterialStats(0.35f, 0.3f, 3.5f));

        // tier 4
        addMaterialStatsJson(MaterialIds.cinderslime,
                new LimbMaterialStats(1221, -0.2f, 0, 0.25f),
                new GripMaterialStats(0.20f, 0.05f, 2.25f));
        addMaterialStatsJson(MaterialIds.queensSlime,
                new LimbMaterialStats(1650, 0f, -0.15f, 0.25f),
                new GripMaterialStats(0.35f, -0.15f, 2f));
        addMaterialStatsJson(MaterialIds.hepatizon,
                new LimbMaterialStats(975, 0.25f, -0.05f, -0.10f),
                new GripMaterialStats(0.1f, 0.15f, 2.5f));
        addMaterialStatsJson(MaterialIds.manyullyn,
                new LimbMaterialStats(1250, -0.35f, 0.25f, 0f),
                new GripMaterialStats(0.1f, -0.20f, 3.5f));
        addMaterialStatsJson(MaterialIds.ancient, new LimbMaterialStats(745, -0.15f, 0.1f, 0.1f));
        addMaterialStatsJson(MaterialIds.ancientHide, StatlessMaterialStats.BOWSTRING);

        // tier 4 (end)
        addMaterialStatsJson(MaterialIds.knightmetal,
                new LimbMaterialStats(512, 0.2f, 0.05f, -0.1f),
                new GripMaterialStats(0, 0.1f, 3.0f));
        addMaterialStatsJson(MaterialIds.enderslimeVine, StatlessMaterialStats.BOWSTRING);

        // tier 4 (compat)
        addMaterialStatsJson(MaterialIds.fiery,
                new LimbMaterialStats(1024, -0.25f, 0.2f, -0.05f),
                new GripMaterialStats(0, 0.05f, 3.5f));
    }

    private static void addAmmoStats() {
        // tier 1
        addMaterialStatsJson(MaterialIds.flint, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.wool, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.wood, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.bone, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.bamboo, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.chorus, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.cactus, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.feather, StatlessMaterialStats.FLETCHING);
        addMaterialStatsJson(MaterialIds.leaves, StatlessMaterialStats.FLETCHING);
        addMaterialStatsJson(MaterialIds.paper, StatlessMaterialStats.FLETCHING);
        // tier 2
        addMaterialStatsJson(MaterialIds.amethyst, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.prismarine, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.earthslime, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.skyslime, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.enderPearl, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.glass, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.slimewood, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.necroticBone, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.blaze, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.venombone, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.steeleaf, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.slimeball, StatlessMaterialStats.FLETCHING);
        addMaterialStatsJson(MaterialIds.gunpowder, StatlessMaterialStats.ARROW_HEAD);
        // tier 3
        addMaterialStatsJson(MaterialIds.ice, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.quartz, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.ichor, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.glowstone, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.nahuatl, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.necronium, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.magnetite, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.magma, StatlessMaterialStats.FLETCHING);
        // tier 4
        addMaterialStatsJson(MaterialIds.enderslime, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.dragonScale, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.shulker, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.blazewood, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.blazingBone, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStatsJson(MaterialIds.knightly, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStatsJson(MaterialIds.endRod, StatlessMaterialStats.ARROW_SHAFT);
    }

    private static void addArmorStats() {
        // tier 1
        addMaterialStatsJson(MaterialIds.wood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.bamboo, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.chorus, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.ice, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.cactus, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.bone, StatlessMaterialStats.SHIELD_CORE);
        addArmorShieldStatsJson(MaterialIds.copper, PlatingMaterialStats.builder().durabilityFactor(13).armor(1, 2, 3, 1), StatlessMaterialStats.MAILLE);
        addMaterialStatsJson(MaterialIds.leather, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS);
        addMaterialStatsJson(MaterialIds.vine, StatlessMaterialStats.MAILLE);
        // tier 2
        addMaterialStatsJson(MaterialIds.slimewood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.venombone, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.necroticBone, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.slimeskin, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS);
        addMaterialStatsJson(MaterialIds.skyslimeVine, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS);
        addMaterialStatsJson(MaterialIds.weepingVine, StatlessMaterialStats.MAILLE);
        addMaterialStatsJson(MaterialIds.twistingVine, StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.iron,          PlatingMaterialStats.builder().durabilityFactor(15).armor(2, 4, 5, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.gold,          PlatingMaterialStats.builder().durabilityFactor( 7).armor(1, 3, 4, 1), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.searedStone,   PlatingMaterialStats.builder().durabilityFactor(14).armor(1, 3, 4, 2).knockbackResistance(0.1f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.scorchedStone, PlatingMaterialStats.builder().durabilityFactor(10).armor(1, 4, 5, 2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        // tier 2 - compat
        addArmorShieldStatsJson(MaterialIds.osmium,   PlatingMaterialStats.builder().durabilityFactor(25).armor(1, 3, 5, 2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.aluminum, PlatingMaterialStats.builder().durabilityFactor(13).armor(2, 4, 6, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.silver,   PlatingMaterialStats.builder().durabilityFactor(18).armor(1, 4, 5, 2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.lead,     PlatingMaterialStats.builder().durabilityFactor(12).armor(1, 3, 4, 2).knockbackResistance(0.1f), StatlessMaterialStats.MAILLE);
        addMaterialStatsJson(MaterialIds.treatedWood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.ironwood, StatlessMaterialStats.SHIELD_CORE, StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.manaSteel, PlatingMaterialStats.builder().durabilityFactor(20).armor(3, 5, 6, 3).toughness(2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        // tier 3
        addMaterialStatsJson(MaterialIds.nahuatl, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.ichorskin, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS);
        addArmorShieldStatsJson(MaterialIds.slimesteel,     PlatingMaterialStats.builder().durabilityFactor(40).armor(2, 5, 6, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.amethystBronze, PlatingMaterialStats.builder().durabilityFactor(28).armor(2, 5, 6, 2).toughness(2), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.obsidian,       PlatingMaterialStats.builder().durabilityFactor(11).armor(2, 4, 5, 2).knockbackResistance(0.15f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.roseGold,       PlatingMaterialStats.builder().durabilityFactor( 9).armor(1, 3, 5, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.pigIron,        PlatingMaterialStats.builder().durabilityFactor(23).armor(1, 3, 4, 1).toughness(1).knockbackResistance(0.1f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.cobalt,         PlatingMaterialStats.builder().durabilityFactor(30).armor(2, 5, 7, 2).toughness(1).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.steel,          PlatingMaterialStats.builder().durabilityFactor(29).armor(2, 5, 7, 2).toughness(2), StatlessMaterialStats.MAILLE);
        // tier 3 - compat
        addMaterialStatsJson(MaterialIds.necronium, StatlessMaterialStats.SHIELD_CORE);
        addArmorShieldStatsJson(MaterialIds.bronze,     PlatingMaterialStats.builder().durabilityFactor(28).armor(2, 5, 6, 2).knockbackResistance(0.1f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.constantan, PlatingMaterialStats.builder().durabilityFactor(25).armor(1, 4, 5, 2).toughness(2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.invar,      PlatingMaterialStats.builder().durabilityFactor(24).armor(1, 3, 5, 2).knockbackResistance(0.1f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.pewter,     PlatingMaterialStats.builder().durabilityFactor(16).armor(2, 5, 7, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.electrum,   PlatingMaterialStats.builder().durabilityFactor(14).armor(1, 3, 4, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.steeleaf,   PlatingMaterialStats.builder().durabilityFactor(10).armor(2, 5, 7, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.terraSteel, PlatingMaterialStats.builder().durabilityFactor(50).armor(3, 6, 8, 4).toughness(3).knockbackResistance(0.15f), StatlessMaterialStats.MAILLE);
        // tier 4
        addMaterialStatsJson(MaterialIds.blazewood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStatsJson(MaterialIds.blazingBone, StatlessMaterialStats.SHIELD_CORE);
        addArmorShieldStatsJson(MaterialIds.cinderslime, PlatingMaterialStats.builder().durabilityFactor(42).armor(2, 5, 7, 2).knockbackResistance(0.10f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.queensSlime, PlatingMaterialStats.builder().durabilityFactor(50).armor(2, 5, 7, 2).toughness(1), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.hepatizon,   PlatingMaterialStats.builder().durabilityFactor(32).armor(2, 5, 7, 2).toughness(2).knockbackResistance(0.10f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.manyullyn,   PlatingMaterialStats.builder().durabilityFactor(35).armor(2, 5, 7, 2).toughness(3).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addArmorShieldStatsJson(MaterialIds.ancient,     PlatingMaterialStats.builder().durabilityFactor(25).armor(2, 4, 6, 2).knockbackResistance(0.15f));
        addMaterialStatsJson(MaterialIds.ancientHide, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS);
        // tier 4 (end)
        addArmorShieldStatsJson(MaterialIds.knightmetal, PlatingMaterialStats.builder().durabilityFactor(20).armor(2, 5, 7, 2).toughness(2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addMaterialStatsJson(MaterialIds.enderslimeVine, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS);
        addMaterialStatsJson(MaterialIds.dragonScale, StatlessMaterialStats.MAILLE);
        addMaterialStatsJson(MaterialIds.shulker, StatlessMaterialStats.MAILLE);
        // tier 4 (compat)
        addArmorShieldStatsJson(MaterialIds.fiery, PlatingMaterialStats.builder().durabilityFactor(25).armor(3, 6, 8, 3).toughness(1.5f), StatlessMaterialStats.MAILLE);
    }

    private static void addMiscStats() {
        // travelers gear
        addMaterialStatsJson(MaterialIds.leather, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStatsJson(MaterialIds.slimeskin, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStatsJson(MaterialIds.skyslimeVine, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStatsJson(MaterialIds.ichorskin, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStatsJson(MaterialIds.enderslimeVine, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStatsJson(MaterialIds.ancientHide, StatlessMaterialStats.REPAIR_KIT);
        // travelers's shield
        addMaterialStatsJson(MaterialIds.ice, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStatsJson(MaterialIds.blazewood, StatlessMaterialStats.REPAIR_KIT);
        // slimeskull
        addMaterialStatsJson(MaterialIds.glass,        new SkullStats( 90, 0));
        addMaterialStatsJson(MaterialIds.enderPearl,   new SkullStats(180, 0));
        addMaterialStatsJson(MaterialIds.bone,         new SkullStats(100, 0));
        addMaterialStatsJson(MaterialIds.venombone,    new SkullStats(175, 1));
        addMaterialStatsJson(MaterialIds.necroticBone, new SkullStats(125, 0));
        addMaterialStatsJson(MaterialIds.string,       new SkullStats(140, 0));
        addMaterialStatsJson(MaterialIds.darkthread,   new SkullStats(200, 1));
        addMaterialStatsJson(MaterialIds.leather,      new SkullStats(150, 2));
        addMaterialStatsJson(MaterialIds.iron,         new SkullStats(165, 2));
        addMaterialStatsJson(MaterialIds.copper,       new SkullStats(145, 2));
        addMaterialStatsJson(MaterialIds.blazingBone,  new SkullStats(205, 1));
        addMaterialStatsJson(MaterialIds.gold,         new SkullStats(125, 0));
        addMaterialStatsJson(MaterialIds.roseGold,     new SkullStats(175, 1));
        addMaterialStatsJson(MaterialIds.pigIron,      new SkullStats(150, 2));
        // slimesuit
        addMaterialStatsJson(MaterialIds.enderslime, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStatsJson(MaterialIds.phantom, StatlessMaterialStats.REPAIR_KIT);
        // slimesuit embellishments
        addMaterialStatsJson(MaterialIds.blood);
        addMaterialStatsJson(MaterialIds.clay);
        addMaterialStatsJson(MaterialIds.honey);
    }

    // ===== 材料特性 =====

    /**
     * 添加所有材料特性
     * 生成 JSON 到 tinkering/materials/traits/{name}.json
     */
    private static void addMaterialTraits() {
        // tier 1
        addMaterialTraitsJson(MaterialIds.wood, defaultTraits(ModifierIds.cultivated), perStatTraits(MaterialRegistry.AMMO, ModifierIds.economical));
        addMaterialTraitsJson(MaterialIds.rock, defaultTraits(ModifierIds.stonebound));
        addMaterialTraitsJson(MaterialIds.flint, defaultTraits(ModifierIds.jagged), perStatTraits(MaterialRegistry.AMMO, ModifierIds.tipped));
        addMaterialTraitsJson(MaterialIds.bone, defaultTraits(ModifierIds.pierce), perStatTraits(MaterialRegistry.AMMO, ModifierIds.spike));
        addMaterialTraitsJson(MaterialIds.bamboo, defaultTraits(ModifierIds.unburdened), perStatTraits(MaterialRegistry.AMMO, ModifierIds.woodwind));
        addMaterialTraitsJson(MaterialIds.cactus, defaultTraits(ModifierIds.spiny), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.thorns));
        addMaterialTraitsJson(MaterialIds.wool, perStatTraits(MaterialRegistry.AMMO, ModifierIds.soft));
        addMaterialTraitsJson(MaterialIds.feather, emptyTraits());
        addMaterialTraitsJson(MaterialIds.paper, perStatTraits(MaterialRegistry.AMMO, ModifierIds.weak));
        addMaterialTraitsJson(MaterialIds.leaves, perStatTraits(MaterialRegistry.AMMO, ModifierIds.cheap));
        // tier 1 - end
        addMaterialTraitsJson(MaterialIds.chorus, defaultTraits(TinkerModifiers.enderference.getId()), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.enderclearance));
        // tier 1 - binding
        addMaterialTraitsJson(MaterialIds.string, defaultTraits(ModifierIds.stringy));
        addMaterialTraitsJson(MaterialIds.leather, defaultTraits(TinkerModifiers.tanned.getId()));
        addMaterialTraitsJson(MaterialIds.vine, defaultTraits(TinkerModifiers.solarPowered.getId()));
        addMaterialTraitsJson(MaterialIds.gold,
                perStatTraits(MaterialRegistry.ARMOR, TinkerModifiers.golden.getId(), ModifierIds.magicProtection),
                perStatTraits(PlatingMaterialStats.SHIELD.getId(), ModifierIds.magicProtection));

        // tier 2
        addMaterialTraitsJson(MaterialIds.iron, defaultTraits(TinkerModifiers.magnetic.getId()), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.projectileProtection));
        addMaterialTraitsJson(MaterialIds.copper, defaultTraits(TinkerModifiers.dwarven.getId()), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.depthProtection));
        addMaterialTraitsJson(MaterialIds.searedStone, defaultTraits(ModifierIds.searing), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.fireProtection));
        addMaterialTraitsJson(MaterialIds.slimewood, defaultTraits(ModifierIds.overgrowth, TinkerModifiers.overslime.getId()), perStatTraitsWithLevel(MaterialRegistry.AMMO, 2, ModifierIds.bounce));
        addMaterialTraitsJson(MaterialIds.slimeskin, defaultTraits(ModifierIds.overgrowth, TinkerModifiers.overslime.getId()));
        addMaterialTraitsJson(MaterialIds.venombone, defaultTraits(ModifierIds.antitoxin), perStatTraits(MaterialRegistry.AMMO, ModifierIds.venom), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.venom));
        addMaterialTraitsJson(MaterialIds.aluminum, defaultTraits(ModifierIds.featherweight));
        // tier 2 - nether
        addMaterialTraitsJson(MaterialIds.necroticBone, defaultTraits(TinkerModifiers.necrotic.getId()));
        addMaterialTraitsJson(MaterialIds.scorchedStone, defaultTraits(ModifierIds.scorching), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.scorchProtection));
        // tier 2 - end
        addMaterialTraitsJson(MaterialIds.whitestone, defaultTraits(ModifierIds.stoneshield));
        // tier 2 - binding
        addMaterialTraitsJson(MaterialIds.skyslimeVine, defaultTraits(ModifierIds.airborne), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.skyfall));
        addMaterialTraitsJson(MaterialIds.weepingVine, defaultTraits(ModifierIds.flamestance));
        addMaterialTraitsJson(MaterialIds.twistingVine, defaultTraits(ModifierIds.entangled));
        // tier 2 - ammo
        addMaterialTraitsJson(MaterialIds.amethyst, perStatTraits(MaterialRegistry.AMMO, ModifierIds.crystalbound));
        addMaterialTraitsJson(MaterialIds.prismarine, perStatTraits(MaterialRegistry.AMMO, ModifierIds.finsAmmo, ModifierIds.lureRod));
        addMaterialTraitsJson(MaterialIds.earthslime, perStatTraits(MaterialRegistry.AMMO, ModifierIds.drawback));
        addMaterialTraitsJson(MaterialIds.skyslime, perStatTraits(MaterialRegistry.AMMO, ModifierIds.punch));
        addMaterialTraitsJson(MaterialIds.blaze, defaultTraits(ModifierIds.fiery));
        addMaterialTraitsJson(MaterialIds.enderPearl, perStatTraits(MaterialRegistry.AMMO, TinkerModifiers.enderporting.getId()));
        addMaterialTraitsJson(MaterialIds.glass, perStatTraits(MaterialRegistry.AMMO, ModifierIds.amorphous, ModifierIds.smashingAmmo, ModifierIds.spillingRod));
        addMaterialTraitsJson(MaterialIds.slimeball, perStatTraits(MaterialRegistry.AMMO, ModifierIds.erratic));
        addMaterialTraitsJson(MaterialIds.gunpowder, perStatTraits(MaterialRegistry.AMMO, ModifierIds.explosive));

        // tier 3
        addMaterialTraitsJson(MaterialIds.slimesteel, defaultTraits(ModifierIds.overcast, TinkerModifiers.overslime.getId()));
        addMaterialTraitsJson(MaterialIds.amethystBronze, perStatTraits(MaterialRegistry.MELEE_HARVEST, ModifierIds.crumbling), perStatTraits(MaterialRegistry.RANGED, ModifierIds.crystalbound), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.crystalstrike));
        addMaterialTraitsJson(MaterialIds.nahuatl, defaultTraits(TinkerModifiers.lacerating.getId()));
        addMaterialTraitsJson(MaterialIds.roseGold, defaultTraits(ModifierIds.enhanced));
        addMaterialTraitsJson(MaterialIds.pigIron, defaultTraits(TinkerModifiers.tasty.getId()));
        addMaterialTraitsJson(MaterialIds.obsidian, perStatTraits(MaterialRegistry.ARMOR, ModifierIds.blastProtection));
        // tier 3 - nether
        addMaterialTraitsJson(MaterialIds.cobalt, defaultTraits(ModifierIds.lightweight), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.meleeProtection));
        addMaterialTraitsJson(MaterialIds.steel, defaultTraits(ModifierIds.ductile));
        // tier 3 - binding
        addMaterialTraitsJson(MaterialIds.darkthread, defaultTraits(ModifierIds.looting));
        addMaterialTraitsJson(MaterialIds.ichorskin, defaultTraits(ModifierIds.overshield, TinkerModifiers.overslime.getId()));
        addMaterialTraitsJson(MaterialIds.ice, defaultTraits(ModifierIds.frostshield), perStatTraits(MaterialRegistry.AMMO, ModifierIds.freezing));
        // tier 3 - ammo
        addMaterialTraitsJson(MaterialIds.quartz, perStatTraits(MaterialRegistry.AMMO, ModifierIds.keen));
        addMaterialTraitsJson(MaterialIds.ichor, perStatTraits(MaterialRegistry.AMMO, ModifierIds.rebound, ModifierIds.bounce));
        addMaterialTraitsJson(MaterialIds.glowstone, perStatTraits(MaterialRegistry.AMMO, ModifierIds.spectral));
        addMaterialTraitsJson(MaterialIds.magnetite, defaultTraits(ModifierIds.attractive));
        addMaterialTraitsJson(MaterialIds.magma, perStatTraits(MaterialRegistry.AMMO, ModifierIds.fuse));

        // tier 4
        addMaterialTraitsJson(MaterialIds.cinderslime, defaultTraits(ModifierIds.overburn, TinkerModifiers.overslime.getId()));
        addMaterialTraitsJson(MaterialIds.queensSlime, defaultTraits(ModifierIds.overlord, TinkerModifiers.overslime.getId()));
        addMaterialTraitsJson(MaterialIds.hepatizon, defaultTraits(TinkerModifiers.momentum.getId()), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.recurrentProtection));
        addMaterialTraitsJson(MaterialIds.manyullyn, defaultTraits(TinkerModifiers.insatiable.getId()), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.kinetic));
        addMaterialTraitsJson(MaterialIds.blazingBone, defaultTraits(TinkerModifiers.conducting.getId()), perStatTraits(MaterialRegistry.AMMO, ModifierIds.conductive), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.conductive));
        addMaterialTraitsJson(MaterialIds.blazewood, defaultTraits(ModifierIds.spectral));
        addMaterialTraitsJson(MaterialIds.ancient, defaultTraits(ModifierIds.vintage, ModifierIds.worldbound));
        // tier 4 - binding
        addMaterialTraitsJson(MaterialIds.ancientHide, defaultTraits(ModifierIds.fortified), perStatTraits(MaterialRegistry.MELEE_HARVEST, ModifierIds.fortune));
        addMaterialTraitsJson(MaterialIds.dragonScale, perStatTraits(MaterialRegistry.ARMOR, ModifierIds.dragonborn), perStatTraits(MaterialRegistry.AMMO, ModifierIds.dragonshot));
        addMaterialTraitsJson(MaterialIds.shulker, perStatTraits(MaterialRegistry.ARMOR, ModifierIds.shulking), perStatTraits(MaterialRegistry.AMMO, ModifierIds.reclaim));
        // tier 4 - ammo
        addMaterialTraitsJson(MaterialIds.enderslime, perStatTraits(MaterialRegistry.AMMO, ModifierIds.enderclearance));

        // tier 4 (end)
        addMaterialTraitsJson(MaterialIds.knightmetal, defaultTraits(ModifierIds.valiant), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.stalwart));
        addMaterialTraitsJson(MaterialIds.knightly, defaultTraits(ModifierIds.valiant));
        addMaterialTraitsJson(MaterialIds.enderslimeVine, defaultTraits(TinkerModifiers.enderporting.getId()), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.enderclearance));
        addMaterialTraitsJson(MaterialIds.endRod, defaultTraits(ModifierIds.hover));

        // tier 2 - mod compat
        addMaterialTraitsJson(MaterialIds.osmium, defaultTraits(ModifierIds.dense));
        addMaterialTraitsJson(MaterialIds.lead, defaultTraits(ModifierIds.heavy));
        addMaterialTraitsJson(MaterialIds.silver, perStatTraits(MaterialRegistry.MELEE_HARVEST, ModifierIds.smite), perStatTraits(MaterialRegistry.RANGED, ModifierIds.holy), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.consecrated));
        addMaterialTraitsJson(MaterialIds.treatedWood, defaultTraits(ModifierIds.preserved));
        addMaterialTraitsJson(MaterialIds.ironwood, defaultTraits(ModifierIds.deciduous));
        addMaterialTraitsJson(MaterialIds.manaSteel, defaultTraits(TinkerModifiers.manafix.getId()));
        // tier 3 - mod compat
        addMaterialTraitsJson(MaterialIds.bronze, defaultTraits(ModifierIds.maintained));
        addMaterialTraitsJson(MaterialIds.constantan, defaultTraits(ModifierIds.temperate));
        addMaterialTraitsJson(MaterialIds.invar, defaultTraits(ModifierIds.solid));
        addMaterialTraitsJson(MaterialIds.pewter, defaultTraits(ModifierIds.raging), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.vitalProtection));
        addMaterialTraitsJson(MaterialIds.necronium, defaultTraits(TinkerModifiers.decay.getId()), perStatTraitsWithLevel(MaterialRegistry.AMMO, 2, TinkerModifiers.decay.getId()));
        addMaterialTraitsJson(MaterialIds.electrum, defaultTraits(ModifierIds.shock));
        addMaterialTraitsJson(MaterialIds.platedSlimewood, defaultTraits(TinkerModifiers.overworked.getId(), TinkerModifiers.overslime.getId()));
        addMaterialTraitsJson(MaterialIds.steeleaf, defaultTraits(ModifierIds.experienced), perStatTraits(MaterialRegistry.AMMO, ModifierIds.looting));
        addMaterialTraitsJson(MaterialIds.terraSteel, defaultTraits(TinkerModifiers.manafix.getId(), TinkerModifiers.terrarecover.getId()));
        // tier 4 - mod compat
        addMaterialTraitsJson(MaterialIds.fiery, defaultTraits(TinkerModifiers.autosmelt.getId()), perStatTraits(MaterialRegistry.ARMOR, ModifierIds.temperedProtection));

        // slimeskull
        addMaterialTraitsJson(MaterialIds.glass,        perStatTraits(SkullStats.ID, TinkerModifiers.selfDestructive.getId(), ModifierIds.creeperDisguise));
        addMaterialTraitsJson(MaterialIds.enderPearl,   perStatTraits(SkullStats.ID, TinkerModifiers.enderdodging.getId(), ModifierIds.endermanDisguise));
        addMaterialTraitsJson(MaterialIds.bone,         perStatTraits(SkullStats.ID, TinkerModifiers.strongBones.getId(), ModifierIds.skeletonDisguise));
        addMaterialTraitsJson(MaterialIds.venombone,    perStatTraits(SkullStats.ID, TinkerModifiers.frosttouch.getId(), ModifierIds.strayDisguise));
        addMaterialTraitsJson(MaterialIds.necroticBone, perStatTraits(SkullStats.ID, TinkerModifiers.withered.getId(), ModifierIds.witherSkeletonDisguise));
        addMaterialTraitsJson(MaterialIds.string,       perStatTraits(SkullStats.ID, TinkerModifiers.boonOfSssss.getId(), ModifierIds.spiderDisguise));
        addMaterialTraitsJson(MaterialIds.darkthread,   perStatTraits(SkullStats.ID, ModifierIds.mithridatism, ModifierIds.caveSpiderDisguise));
        addMaterialTraitsJson(MaterialIds.leather,      perStatTraits(SkullStats.ID, TinkerModifiers.wildfire.getId(), ModifierIds.zombieDisguise));
        addMaterialTraitsJson(MaterialIds.iron,         perStatTraits(SkullStats.ID, TinkerModifiers.plague.getId(), ModifierIds.huskDisguise));
        addMaterialTraitsJson(MaterialIds.copper,       perStatTraits(SkullStats.ID, TinkerModifiers.breathtaking.getId(), ModifierIds.drownedDisguise));
        addMaterialTraitsJson(MaterialIds.blazingBone,  perStatTraits(SkullStats.ID, TinkerModifiers.firebreath.getId(), ModifierIds.blazeDisguise));
        addMaterialTraitsJson(MaterialIds.gold,         perStatTraits(SkullStats.ID, TinkerModifiers.chrysophilite.getId(), ModifierIds.piglinDisguise, TinkerModifiers.golden.getId()));
        addMaterialTraitsJson(MaterialIds.roseGold,     perStatTraits(SkullStats.ID, TinkerModifiers.goldGuard.getId(), ModifierIds.piglinBruteDisguise, TinkerModifiers.golden.getId()));
        addMaterialTraitsJson(MaterialIds.pigIron,      perStatTraits(SkullStats.ID, TinkerModifiers.revenge.getId(), ModifierIds.zombifiedPiglinDisguise));
        // slimesuit
        addMaterialTraitsJson(MaterialIds.blood, emptyTraits());
        addMaterialTraitsJson(MaterialIds.clay, emptyTraits());
        addMaterialTraitsJson(MaterialIds.honey, emptyTraits());
        addMaterialTraitsJson(MaterialIds.phantom, emptyTraits());
    }


    // ===== 辅助方法 - 材料定义 =====

    /**
     * 添加普通材料定义
     */
    private static void addMaterialDefinition(MaterialId id, int tier, int order, boolean craftable) {
        addMaterialDefinition(id, tier, order, craftable, false);
    }

    /**
     * 添加材料定义（带隐藏标志）
     */
    private static void addMaterialDefinition(MaterialId id, int tier, int order, boolean craftable, boolean hidden) {
        JsonObject json = new JsonObject();
        json.addProperty("craftable", craftable);
        json.addProperty("hidden", hidden);
        json.addProperty("sortOrder", order);
        json.addProperty("tier", tier);
        addDefinition(id, json);
    }

    /**
     * 添加重定向材料定义
     */
    private static void addRedirectDefinition(MaterialId id, MaterialId target) {
        JsonObject json = new JsonObject();
        JsonArray redirectArray = new JsonArray();
        JsonObject redirect = new JsonObject();
        redirect.addProperty("id", target.toString());
        redirectArray.add(redirect);
        json.add("redirect", redirectArray);
        addDefinition(id, json);
    }

    /**
     * 将材料定义写入动态数据包
     */
    private static void addDefinition(MaterialId id, JsonObject json) {
        ResourceLocation location = new ResourceLocation(
            id.getNamespace(),
            MaterialManager.FOLDER + "/" + id.getPath() + ".json"
        );
        TiCDynamicDataPack.addData(location, json.toString().getBytes(StandardCharsets.UTF_8));
    }


    // ===== 辅助方法 - 材料属性 =====

    /**
     * 添加材料属性 JSON（多个 stat 类型）
     */
    private static void addMaterialStatsJson(MaterialId id, IMaterialStats... stats) {
        JsonObject statsObj = new JsonObject();
        for (IMaterialStats stat : stats) {
            JsonObject statJson = encodeStat(stat);
            statsObj.add(stat.getIdentifier().toString(), statJson);
        }
        mergeMaterialStatsJson(id, statsObj);
    }

    private static void mergeMaterialStatsJson(MaterialId id, JsonObject statsObj) {
        JsonObject mergedStats = MATERIAL_STATS.computeIfAbsent(id, material -> new JsonObject());
        for (Map.Entry<String, com.google.gson.JsonElement> entry : statsObj.entrySet()) {
            String statType = entry.getKey();
            JsonObject statJson = entry.getValue().getAsJsonObject();
            if (mergedStats.has(statType)) {
                JsonObject existing = mergedStats.getAsJsonObject(statType);
                for (Map.Entry<String, com.google.gson.JsonElement> statEntry : statJson.entrySet()) {
                    existing.add(statEntry.getKey(), statEntry.getValue());
                }
            } else {
                mergedStats.add(statType, statJson);
            }
        }
    }

    private static void writeMaterialStatsJson(MaterialId id, JsonObject statsObj) {
        JsonObject json = new JsonObject();
        json.add("stats", statsObj);

        ResourceLocation location = new ResourceLocation(
            id.getNamespace(),
            MaterialStatsManager.FOLDER + "/" + id.getPath() + ".json"
        );
        TiCDynamicDataPack.addData(location, json.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 添加护甲和盾牌属性
     */
    private static void addArmorShieldStatsJson(MaterialId id, PlatingMaterialStats.Builder builder, IMaterialStats... otherStats) {
        // 四件护甲
        JsonObject statsObj = new JsonObject();
        for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
            PlatingMaterialStats plating = builder.build(slotType);
            statsObj.add(plating.getType().getId().toString(), encodeStat(plating));
        }
        // 盾牌
        PlatingMaterialStats shield = builder.buildShield();
        statsObj.add(shield.getType().getId().toString(), encodeStat(shield));
        // 其他属性
        for (IMaterialStats stat : otherStats) {
            statsObj.add(stat.getIdentifier().toString(), encodeStat(stat));
        }
        mergeMaterialStatsJson(id, statsObj);
    }

    /**
     * 编码单个属性为 JsonObject
     * 使用 MaterialStatType 的 loadable 序列化器
     */
    @SuppressWarnings("unchecked")
    private static <T extends IMaterialStats> JsonObject encodeStat(IMaterialStats stats) {
        MaterialStatType<T> type = (MaterialStatType<T>) stats.getType();
        JsonObject json = new JsonObject();
        type.getLoadable().serialize((T) stats, json);
        return json;
    }


    // ===== 辅助方法 - 材料特性 =====

    /**
     * 空特性（无默认特性和无按属性特性）
     */
    private static JsonObject emptyTraits() {
        return new JsonObject();
    }

    /**
     * 默认特性列表
     */
    private static JsonObject defaultTraits(ModifierId... traitIds) {
        JsonObject json = new JsonObject();
        JsonArray defaultArray = new JsonArray();
        for (ModifierId traitId : traitIds) {
            defaultArray.add(modifierEntryJson(traitId, 1));
        }
        json.add("default", defaultArray);
        return json;
    }

    /**
     * 按属性特性列表
     */
    private static JsonObject perStatTraits(MaterialStatsId statId, ModifierId... traitIds) {
        return perStatTraitsWithLevel(statId, 1, traitIds);
    }

    /**
     * 按属性特性列表（自定义等级）
     */
    private static JsonObject perStatTraitsWithLevel(MaterialStatsId statId, int level, ModifierId... traitIds) {
        JsonObject json = new JsonObject();
        JsonObject perStat = new JsonObject();
        JsonArray traitArray = new JsonArray();
        for (ModifierId traitId : traitIds) {
            traitArray.add(modifierEntryJson(traitId, level));
        }
        perStat.add(statId.toString(), traitArray);
        json.add("perStat", perStat);
        return json;
    }

    /**
     * 修饰器条目 JSON
     */
    private static JsonObject modifierEntryJson(ModifierId id, int level) {
        JsonObject entry = new JsonObject();
        entry.addProperty("name", id.toString());
        entry.addProperty("level", level);
        return entry;
    }

    /**
     * 合并多个特性 JSON 对象
     * 将多个 default/perStat 合并到一个 JsonObject 中
     */
    private static void addMaterialTraitsJson(MaterialId id, JsonObject... traitObjects) {
        JsonObject json = new JsonObject();
        JsonArray defaultTraits = null;
        JsonObject perStat = new JsonObject();
        boolean hasPerStat = false;

        for (JsonObject traitObj : traitObjects) {
            if (traitObj.has("default")) {
                if (defaultTraits == null) {
                    defaultTraits = new JsonArray();
                }
                traitObj.getAsJsonArray("default").forEach(defaultTraits::add);
            }
            if (traitObj.has("perStat")) {
                hasPerStat = true;
                mergePerStat(perStat, traitObj.getAsJsonObject("perStat"));
            }
        }

        if (defaultTraits != null) {
            json.add("default", defaultTraits);
        }
        if (hasPerStat) {
            json.add("perStat", perStat);
        }

        ResourceLocation location = new ResourceLocation(
            id.getNamespace(),
            MaterialTraitsManager.FOLDER + "/" + id.getPath() + ".json"
        );
        TiCDynamicDataPack.addData(location, json.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 合并 perStat 对象到目标
     */
    private static void mergePerStat(JsonObject target, JsonObject source) {
        for (Map.Entry<String, com.google.gson.JsonElement> entry : source.entrySet()) {
            if (!target.has(entry.getKey())) {
                target.add(entry.getKey(), new JsonArray());
            }
            entry.getValue().getAsJsonArray().forEach(e -> target.getAsJsonArray(entry.getKey()).add(e));
        }
    }
}
