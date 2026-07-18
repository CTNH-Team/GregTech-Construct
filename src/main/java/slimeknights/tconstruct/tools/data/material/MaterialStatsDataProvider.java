package slimeknights.tconstruct.tools.data.material;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ArmorItem;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialStatsDataProvider;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.tools.modules.ArmorModuleBuilder;
import slimeknights.tconstruct.tools.stats.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.minecraft.world.item.Tiers.*;

public class MaterialStatsDataProvider extends AbstractMaterialStatsDataProvider {
    public MaterialStatsDataProvider(PackOutput packOutput, AbstractMaterialDataProvider materials) {
        super(packOutput, materials);
    }

    @Override
    public String getName() {
        return "Tinker's Construct Material Stats";
    }

    @Override
    protected void addMaterialStats() {
        addMeleeHarvest();
        addRanged();
        addAmmo();
        addArmor();
        addMisc();
        addArmorExtensionMaterialStats();
    }

    private void addMeleeHarvest() {
        // head order is durability, mining speed, mining level, damage

        // tier 1
        // vanilla wood: 59, 2f, WOOD, 0f
        addMaterialStats(MaterialIds.wood,
                new HeadMaterialStats(60, 2f, WOOD, 0f),
                HandleMaterialStats.percents().build(), // flat all around
                StatlessMaterialStats.BINDING);
        // vanilla stone: 131, 4f, STONE, 1f
        addMaterialStats(MaterialIds.rock,
                new HeadMaterialStats(130, 4f, STONE, 1f),
                HandleMaterialStats.multipliers().durability(0.9f).miningSpeed(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.flint,
                new HeadMaterialStats(85, 3.5f, STONE, 1.25f),
                HandleMaterialStats.multipliers().durability(0.85f).attackDamage(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.copper,
                new HeadMaterialStats(210, 5.0f, IRON, 0.5f),
                HandleMaterialStats.multipliers().durability(0.80f).miningSpeed(1.1f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.bone,
                new HeadMaterialStats(100, 2.5f, STONE, 1.25f),
                HandleMaterialStats.multipliers().durability(0.75f).attackSpeed(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.chorus,
                new HeadMaterialStats(180, 3.0f, STONE, 1.0f),
                HandleMaterialStats.multipliers().durability(1.1f).miningSpeed(0.95f).attackSpeed(0.9f).build(),
                StatlessMaterialStats.BINDING);
        // tier 1 - binding
        addMaterialStats(MaterialIds.string, StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.leather, StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.vine, StatlessMaterialStats.BINDING);

        // tier 2
        // vanilla iron: 250, 6f, IRON, 2f
        addMaterialStats(MaterialIds.iron,
                new HeadMaterialStats(250, 6f, IRON, 2f),
                HandleMaterialStats.multipliers().durability(1.10f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.searedStone,
                new HeadMaterialStats(225, 6.5f, IRON, 1.5f),
                HandleMaterialStats.multipliers().durability(0.85f).miningSpeed(1.10f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.venombone,
                new HeadMaterialStats(175, 4.5f, IRON, 2.25f),
                HandleMaterialStats.multipliers().durability(0.9f).attackSpeed(1.1f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.slimewood,
                new HeadMaterialStats(375, 4f, IRON, 1f),
                HandleMaterialStats.multipliers().durability(1.3f).miningSpeed(0.85f).attackDamage(0.85f).build(),
                StatlessMaterialStats.BINDING);
        // tier 2 - nether
        addMaterialStats(MaterialIds.scorchedStone,
                new HeadMaterialStats(120, 4.5f, IRON, 2.5f),
                HandleMaterialStats.multipliers().durability(0.8f).attackSpeed(1.05f).attackDamage(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.necroticBone,
                new HeadMaterialStats(125, 4f, IRON, 2.25f),
                HandleMaterialStats.multipliers().durability(0.7f).attackSpeed(1.15f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        // tier 2 - end
        addMaterialStats(MaterialIds.whitestone,
                new HeadMaterialStats(275, 6.0f, IRON, 1.25f),
                HandleMaterialStats.multipliers().durability(0.95f).miningSpeed(1.1f).attackSpeed(0.95f).build(),
                StatlessMaterialStats.BINDING);
        // tier 2 - bindings
        addMaterialStats(MaterialIds.skyslimeVine, StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.weepingVine, StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.twistingVine, StatlessMaterialStats.BINDING);

        // tier 2 (mod integration)
        addMaterialStats(MaterialIds.osmium,
                new HeadMaterialStats(500, 4.5f, IRON, 2.0f),
                HandleMaterialStats.multipliers().durability(1.2f).attackSpeed(0.9f).miningSpeed(0.9f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.silver,
                new HeadMaterialStats(300, 5.5f, IRON, 2.25f),
                HandleMaterialStats.multipliers().durability(0.9f).miningSpeed(1.05f).attackSpeed(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.lead,
                new HeadMaterialStats(200, 6.5f, IRON, 1.75f),
                HandleMaterialStats.multipliers().durability(0.9f).miningSpeed(1.1f).attackSpeed(0.9f).attackDamage(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.treatedWood,
                new HeadMaterialStats(300, 3.5f, STONE, 1.5f),
                HandleMaterialStats.multipliers().durability(1.25f).attackDamage(0.9f).miningSpeed(0.9f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.ironwood,
                new HeadMaterialStats(510, 6.5f, IRON, 2f),
                HandleMaterialStats.multipliers().durability(1.15f).attackSpeed(0.95f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.andesiteAlloy,
                new HeadMaterialStats(220, 5.5f, IRON, 1.5f),
                new HandleMaterialStats(0.15f, 0f, 0f, -0.05f),
                StatlessMaterialStats.BINDING,
                StatlessMaterialStats.ARROW_HEAD);

        // tier 3
        // vanilla diamond: 1561, 8f, DIAMOND, 3f
        addMaterialStats(MaterialIds.slimesteel,
                new HeadMaterialStats(1040, 6f, DIAMOND, 2.5f),
                HandleMaterialStats.multipliers().durability(1.2f).attackSpeed(0.95f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.amethystBronze,
                new HeadMaterialStats(720, 7f, DIAMOND, 1.5f),
                HandleMaterialStats.multipliers().miningSpeed(1.10f).attackSpeed(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.nahuatl,
                new HeadMaterialStats(350, 4.5f, DIAMOND, 3f),
                HandleMaterialStats.multipliers().durability(0.9f).attackSpeed(0.9f).attackDamage(1.25f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.pigIron,
                new HeadMaterialStats(580, 6f, DIAMOND, 2.5f),
                HandleMaterialStats.multipliers().durability(1.10f).miningSpeed(0.85f).attackDamage(1.15f).build(),
                StatlessMaterialStats.BINDING);
        // vanilla gold: 32, 12f, WOOD, 0f
        addMaterialStats(MaterialIds.roseGold,
                new HeadMaterialStats(175, 9f, GOLD, 1f), // gold mining level technically puts it in tier 0, but lets see if some mod does something weird
                HandleMaterialStats.multipliers().durability(0.7f).miningSpeed(1.10f).attackSpeed(1.10f).build(),
                StatlessMaterialStats.BINDING);
        // tier 3 (nether)
        addMaterialStats(MaterialIds.cobalt,
                new HeadMaterialStats(800, 6.5f, DIAMOND, 2.25f),
                HandleMaterialStats.multipliers().durability(1.05f).miningSpeed(1.05f).attackSpeed(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.steel,
                new HeadMaterialStats(775, 6f, DIAMOND, 2.75f),
                HandleMaterialStats.multipliers().durability(1.05f).miningSpeed(1.05f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.wroughtIron,
                new HeadMaterialStats(512, 6f, DIAMOND, 2.375f),
                HandleMaterialStats.multipliers().durability(1.075f).miningSpeed(1.025f).attackDamage(1.025f).build(),
                StatlessMaterialStats.BINDING);
        // tier 3 - binding
        addMaterialStats(MaterialIds.darkthread, StatlessMaterialStats.BINDING);

        // tier 3 (mod integration)
        addMaterialStats(MaterialIds.bronze,
                new HeadMaterialStats(760, 6.5f, DIAMOND, 2.25f),
                HandleMaterialStats.multipliers().durability(1.10f).miningSpeed(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.constantan,
                new HeadMaterialStats(675, 7.5f, DIAMOND, 1.75f),
                HandleMaterialStats.multipliers().durability(0.95f).miningSpeed(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.invar,
                new HeadMaterialStats(630, 5.5f, DIAMOND, 2.5f),
                HandleMaterialStats.multipliers().miningSpeed(0.9f).attackSpeed(1.05f).attackDamage(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.pewter,
                new HeadMaterialStats(316, 3.5f, DIAMOND, 3.0f),
                HandleMaterialStats.multipliers().durability(0.75f).miningSpeed(0.8f).attackDamage(1.2f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.necronium,
                new HeadMaterialStats(357, 4.0f, DIAMOND, 2.75f),
                HandleMaterialStats.multipliers().durability(0.8f).attackSpeed(1.15f).attackDamage(1.1f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.electrum,
                new HeadMaterialStats(225, 8f, IRON, 1.5f),
                HandleMaterialStats.multipliers().durability(0.65f).attackSpeed(1.15f).miningSpeed(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.platedSlimewood,
                new HeadMaterialStats(595, 5.0f, DIAMOND, 2.0f),
                HandleMaterialStats.multipliers().durability(1.25f).miningSpeed(0.9f).attackSpeed(0.9f).attackDamage(1.05f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.steeleaf,
                new HeadMaterialStats(200, 8, DIAMOND, 3),
                HandleMaterialStats.multipliers().durability(0.65f).attackSpeed(1.15f).miningSpeed(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.polyethylene,
                new HeadMaterialStats(600, 9f, WOOD, 0.5f),
                HandleMaterialStats.multipliers().durability(1.5f).miningSpeed(1.25f).attackSpeed(1.25f).attackDamage(1.0f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.polyvinylChloride,
                new HeadMaterialStats(900, 10f, WOOD, 0.5f),
                HandleMaterialStats.multipliers().durability(1.75f).miningSpeed(1.35f).attackSpeed(1.35f).attackDamage(1.0f).build(),
                StatlessMaterialStats.BINDING);

        // tier 4
        // vanilla netherite: 2031, 9f, NETHERITE, 4f
        addMaterialStats(MaterialIds.cinderslime,
                new HeadMaterialStats(1221, 6.5f, NETHERITE, 2.25f),
                HandleMaterialStats.multipliers().durability(1.2f).miningSpeed(1.1f).attackSpeed(0.90f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.queensSlime,
                new HeadMaterialStats(1650, 6f, NETHERITE, 2f),
                HandleMaterialStats.multipliers().durability(1.35f).miningSpeed(0.9f).attackSpeed(0.95f).attackDamage(0.95f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.hepatizon,
                new HeadMaterialStats(975, 8f, NETHERITE, 2.5f),
                HandleMaterialStats.multipliers().durability(1.1f).miningSpeed(1.2f).attackDamage(0.9f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.manyullyn,
                new HeadMaterialStats(1250, 6.5f, NETHERITE, 3.5f),
                HandleMaterialStats.multipliers().durability(1.1f).miningSpeed(0.9f).attackSpeed(0.95f).attackDamage(1.20f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.blazingBone,
                new HeadMaterialStats(530, 6f, IRON, 3f),
                HandleMaterialStats.multipliers().durability(0.85f).attackDamage(1.05f).attackSpeed(1.2f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.ancientHide, StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.ancient, new HeadMaterialStats(745, 7f, NETHERITE, 2.5f));

        // tier 4 (end)
        addMaterialStats(MaterialIds.knightmetal,
                new HeadMaterialStats(512, 8f, NETHERITE, 3.0f),
                HandleMaterialStats.multipliers().miningSpeed(0.85f).attackSpeed(1.05f).attackDamage(1.15f).build(),
                StatlessMaterialStats.BINDING);
        addMaterialStats(MaterialIds.enderslimeVine, StatlessMaterialStats.BINDING);

        // tier 4 (mod integration)
        addMaterialStats(MaterialIds.fiery,
                new HeadMaterialStats(1024, 8, NETHERITE, 3.5f),
                HandleMaterialStats.multipliers().miningSpeed(1.15f).attackSpeed(0.9f).attackDamage(1.15f).build(),
                StatlessMaterialStats.BINDING);
    }

    private void addRanged() {
        // limb order is durability, drawspeed, velocity, accuracy
        // grip order is durability, accuracy, melee

        // tier 1 - wood is basically the only one from vanilla so it has (mostly) vanilla stats
        addMaterialStats(MaterialIds.wood,
                new LimbMaterialStats(60, 0, 0, 0),
                new GripMaterialStats(0f, 0, 0));
        addMaterialStats(MaterialIds.bamboo,
                new LimbMaterialStats(70, 0.1f, -0.05f, -0.05f),
                new GripMaterialStats(-0.05f, 0.05f, 0.75f));
        addMaterialStats(MaterialIds.cactus,
                new LimbMaterialStats(110, -0.05f, 0.05f, -0.10f),
                new GripMaterialStats(-0.1f, -0.05f, 1.5f));
        addMaterialStats(MaterialIds.bone,
                new LimbMaterialStats(100, 0.05f, -0.05f, 0.05f),
                new GripMaterialStats(-0.25f, 0.05f, 1.25f));
        addMaterialStats(MaterialIds.copper,
                new LimbMaterialStats(210, -0.10f, 0.05f, 0f),
                new GripMaterialStats(-0.2f, 0f, 0.5f));
        addMaterialStats(MaterialIds.chorus,
                new LimbMaterialStats(180, -0.05f, -0.05f, 0.1f),
                new GripMaterialStats(0.1f, -0.1f, 1.0f));
        // tier 1 - bowstring
        addMaterialStats(MaterialIds.string, StatlessMaterialStats.BOWSTRING);
        addMaterialStats(MaterialIds.vine, StatlessMaterialStats.BOWSTRING);
        addMaterialStats(MaterialIds.leather, StatlessMaterialStats.BOWSTRING);

        // tier 2
        addMaterialStats(MaterialIds.slimewood,
                new LimbMaterialStats(375, 0, -0.05f, 0.1f),
                new GripMaterialStats(0.4f, -0.2f, 1f));
        addMaterialStats(MaterialIds.venombone,
                new LimbMaterialStats(175, 0.1f, -0.1f, 0.05f),
                new GripMaterialStats(-0.1f, -0.1f, 2.25f));
        addMaterialStats(MaterialIds.iron,
                new LimbMaterialStats(250, -0.2f, 0.1f, 0),
                new GripMaterialStats(0.1f, 0f, 2f));
        addMaterialStats(MaterialIds.necroticBone,
                new LimbMaterialStats(125, 0.05f, 0.05f, -0.15f),
                new GripMaterialStats(-0.3f, 0.1f, 2.25f));
        // tier 2 - bowstring
        addMaterialStats(MaterialIds.skyslimeVine, StatlessMaterialStats.BOWSTRING);
        addMaterialStats(MaterialIds.weepingVine, StatlessMaterialStats.BOWSTRING);
        addMaterialStats(MaterialIds.twistingVine, StatlessMaterialStats.BOWSTRING);
        addMaterialStats(MaterialIds.slimeskin, StatlessMaterialStats.BOWSTRING);

        // tier 2 - compat
        addMaterialStats(MaterialIds.aluminum,
                new LimbMaterialStats(225, 0.15f, -0.15f, -0.05f),
                new GripMaterialStats(-0.15f, 0.15f, 2f));
        addMaterialStats(MaterialIds.silver,
                new LimbMaterialStats(300, -0.05f, 0, 0.1f),
                new GripMaterialStats(-0.1f, -0.05f, 2.25f));
        addMaterialStats(MaterialIds.lead,
                new LimbMaterialStats(200, -0.3f, 0.15f, -0.05f),
                new GripMaterialStats(-0.1f, 0.1f, 1.75f));
        addMaterialStats(MaterialIds.treatedWood,
                new LimbMaterialStats(300, 0.05f, -0.1f, 0.05f),
                new GripMaterialStats(0.25f, -0.15f, 1.5f));
        addMaterialStats(MaterialIds.ironwood,
                new LimbMaterialStats(512, 0.05f, 0.05f, -0.15f),
                new GripMaterialStats(0.15f, -0.15f, 2f));
        addMaterialStats(MaterialIds.andesiteAlloy,
                new LimbMaterialStats(220, -0.2f, 0.1f, 0f),
                new GripMaterialStats(0.1f, 0f, 1.5f));

        // tier 3
        addMaterialStats(MaterialIds.slimesteel,
                new LimbMaterialStats(1040, -0.05f, -0.05f, 0.15f),
                new GripMaterialStats(0.2f, -0.1f, 2.5f));
        addMaterialStats(MaterialIds.nahuatl,
                new LimbMaterialStats(350, 0.2f, -0.15f, 0.1f),
                new GripMaterialStats(-0.1f, -0.15f, 3f));
        addMaterialStats(MaterialIds.amethystBronze,
                new LimbMaterialStats(720, -0.25f, 0.15f, -0.1f),
                new GripMaterialStats(0f, 0.1f, 1.5f));
        addMaterialStats(MaterialIds.roseGold,
                new LimbMaterialStats(175, 0.15f, -0.25f, 0.15f),
                new GripMaterialStats(-0.3f, 0.25f, 1.0f),
                StatlessMaterialStats.BOWSTRING);
        addMaterialStats(MaterialIds.cobalt,
                new LimbMaterialStats(800, 0.05f, 0.05f, 0.05f),
                new GripMaterialStats(0.05f, 0.05f, 2.25f));
        addMaterialStats(MaterialIds.blazingBone,
                new LimbMaterialStats(530, 0.1f, 0.1f, -0.3f),
                new GripMaterialStats(-0.15f, -0.10f, 3f));
        // tier 3 - bowstring
        addMaterialStats(MaterialIds.darkthread, StatlessMaterialStats.BOWSTRING);

        // tier 3 - compat
        addMaterialStats(MaterialIds.invar,
                new LimbMaterialStats(630, -0.15f, -0.1f, 0.2f),
                new GripMaterialStats(0, 0.05f, 2.5f));
        addMaterialStats(MaterialIds.pewter,
                new LimbMaterialStats(316, 0.1f, -0.05f, -0.2f),
                new GripMaterialStats(-0.2f, 0.15f, 3.0f));
        addMaterialStats(MaterialIds.necronium,
                new LimbMaterialStats(357, 0.15f, -0.1f, -0.05f),
                new GripMaterialStats(-0.2f, 0.15f, 2.75f));
        addMaterialStats(MaterialIds.constantan,
                new LimbMaterialStats(675, 0.2f, -0.05f, -0.25f),
                new GripMaterialStats(-0.05f, 0.1f, 1.75f));
        addMaterialStats(MaterialIds.steel,
                new LimbMaterialStats(775, -0.3f, 0.2f, -0.1f),
                new GripMaterialStats(0.05f, -0.05f, 2.75f));
        addMaterialStats(MaterialIds.wroughtIron,
                new LimbMaterialStats(512, -0.25f, 0.15f, -0.05f),
                new GripMaterialStats(0.075f, -0.025f, 2.375f));
        addMaterialStats(MaterialIds.bronze,
                new LimbMaterialStats(760, -0.2f, 0.15f, -0.2f),
                new GripMaterialStats(0.1f, 0f, 2.25f));
        addMaterialStats(MaterialIds.electrum,
                new LimbMaterialStats(225, -0.25f, 0.1f, 0.15f),
                new GripMaterialStats(-0.35f, 0.2f, 1.5f));
        addMaterialStats(MaterialIds.platedSlimewood,
                new LimbMaterialStats(595, 0.15f, -0.15f, 0),
                new GripMaterialStats(0.25f, -0.1f, 2f));
        addMaterialStats(MaterialIds.steeleaf,
                new LimbMaterialStats(200, 0, 0, 0.15f),
                new GripMaterialStats(-0.35f, 0, 2.75f));
        addMaterialStats(MaterialIds.polyethylene,
                new LimbMaterialStats(600, 0.2f, 0.0f, 0.1f),
                new GripMaterialStats(0.5f, 0.1f, 0.5f));
        addMaterialStats(MaterialIds.polyvinylChloride,
                new LimbMaterialStats(900, 0.25f, 0.0f, 0.2f),
                new GripMaterialStats(0.5f, 0.2f, 0.5f));


        // tier 4
        addMaterialStats(MaterialIds.cinderslime,
                new LimbMaterialStats(1221, -0.2f, 0, 0.25f),
                new GripMaterialStats(0.20f, 0.05f, 2.25f));
        addMaterialStats(MaterialIds.queensSlime,
                new LimbMaterialStats(1650, 0f, -0.15f, 0.25f),
                new GripMaterialStats(0.35f, -0.15f, 2f));
        addMaterialStats(MaterialIds.hepatizon,
                new LimbMaterialStats(975, 0.25f, -0.05f, -0.10f),
                new GripMaterialStats(0.1f, 0.15f, 2.5f));
        addMaterialStats(MaterialIds.manyullyn,
                new LimbMaterialStats(1250, -0.35f, 0.25f, 0f),
                new GripMaterialStats(0.1f, -0.20f, 3.5f));
        addMaterialStats(MaterialIds.ancient, new LimbMaterialStats(745, -0.15f, 0.1f, 0.1f));
        addMaterialStats(MaterialIds.ancientHide, StatlessMaterialStats.BOWSTRING);

        // tier 4 (end)
        addMaterialStats(MaterialIds.knightmetal,
                new LimbMaterialStats(512, 0.2f, 0.05f, -0.1f),
                new GripMaterialStats(0, 0.1f, 3.0f));
        addMaterialStats(MaterialIds.enderslimeVine, StatlessMaterialStats.BOWSTRING);

        // tier 4 (compat)
        addMaterialStats(MaterialIds.fiery,
                new LimbMaterialStats(1024, -0.25f, 0.2f, -0.05f),
                new GripMaterialStats(0, 0.05f, 3.5f));
    }

    private void addAmmo() {
        // tier 1
        addMaterialStats(MaterialIds.flint, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.wool, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.wood, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.bone, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.bamboo, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.chorus, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.cactus, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.feather, StatlessMaterialStats.FLETCHING);
        addMaterialStats(MaterialIds.leaves, StatlessMaterialStats.FLETCHING);
        addMaterialStats(MaterialIds.paper, StatlessMaterialStats.FLETCHING);
        // tier 2
        addMaterialStats(MaterialIds.amethyst, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.prismarine, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.earthslime, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.skyslime, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.enderPearl, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.glass, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.slimewood, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.necroticBone, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.blaze, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.venombone, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.steeleaf, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.slimeball, StatlessMaterialStats.FLETCHING);
        addMaterialStats(MaterialIds.gunpowder, StatlessMaterialStats.ARROW_HEAD);
        // tier 3
        addMaterialStats(MaterialIds.ice, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.quartz, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.ichor, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.glowstone, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.nahuatl, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.necronium, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.magnetite, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.magma, StatlessMaterialStats.FLETCHING);
        // tier 4
        addMaterialStats(MaterialIds.enderslime, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.dragonScale, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.shulker, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.blazewood, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.blazingBone, StatlessMaterialStats.ARROW_SHAFT);
        addMaterialStats(MaterialIds.knightly, StatlessMaterialStats.ARROW_HEAD);
        addMaterialStats(MaterialIds.endRod, StatlessMaterialStats.ARROW_SHAFT);
    }

    private void addArmor() {
        // tier 1
        addMaterialStats(MaterialIds.wood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.bamboo, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.chorus, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.ice, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.cactus, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.bone, StatlessMaterialStats.SHIELD_CORE);
        addArmorShieldStats(MaterialIds.copper, PlatingMaterialStats.builder().durabilityFactor(13).armor(1, 2, 3, 1), StatlessMaterialStats.MAILLE);
        addMaterialStats(MaterialIds.leather, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.vine, StatlessMaterialStats.MAILLE);
        addMaterialStats(MaterialIds.wool, StatlessMaterialStats.LINEAR);
        // tier 2
        addMaterialStats(MaterialIds.slimewood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.venombone, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.necroticBone, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.slimeskin, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.skyslimeVine, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.weepingVine, StatlessMaterialStats.MAILLE);
        addMaterialStats(MaterialIds.twistingVine, StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.iron,          PlatingMaterialStats.builder().durabilityFactor(15).armor(2, 4, 5, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.gold,          PlatingMaterialStats.builder().durabilityFactor( 7).armor(1, 3, 4, 1), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.searedStone,   PlatingMaterialStats.builder().durabilityFactor(14).armor(1, 3, 4, 2).knockbackResistance(0.1f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.scorchedStone, PlatingMaterialStats.builder().durabilityFactor(10).armor(1, 4, 5, 2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        // tier 2 - compat
        addMaterialStats(MaterialIds.treatedWood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.ironwood,    StatlessMaterialStats.SHIELD_CORE, StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.osmium,    PlatingMaterialStats.builder().durabilityFactor(25).armor(1, 3, 5, 2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.aluminum,  PlatingMaterialStats.builder().durabilityFactor(13).armor(2, 4, 6, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.silver,    PlatingMaterialStats.builder().durabilityFactor(18).armor(1, 4, 5, 2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.lead,      PlatingMaterialStats.builder().durabilityFactor(12).armor(1, 3, 4, 2).knockbackResistance(0.1f), StatlessMaterialStats.MAILLE);
        // tier 3
        addMaterialStats(MaterialIds.nahuatl, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.ichorskin, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addArmorShieldStats(MaterialIds.slimesteel,     PlatingMaterialStats.builder().durabilityFactor(40).armor(2, 5, 6, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.amethystBronze, PlatingMaterialStats.builder().durabilityFactor(28).armor(2, 5, 6, 2).toughness(2), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.obsidian,       PlatingMaterialStats.builder().durabilityFactor(11).armor(2, 4, 5, 2).knockbackResistance(0.15f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.roseGold,       PlatingMaterialStats.builder().durabilityFactor( 9).armor(1, 3, 5, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.pigIron,        PlatingMaterialStats.builder().durabilityFactor(23).armor(1, 3, 4, 1).toughness(1).knockbackResistance(0.1f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.cobalt,         PlatingMaterialStats.builder().durabilityFactor(30).armor(2, 5, 7, 2).toughness(1).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.steel,          PlatingMaterialStats.builder().durabilityFactor(29).armor(2, 5, 7, 2).toughness(2), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.wroughtIron,    PlatingMaterialStats.builder().durabilityFactor(22).armor(2, 4, 6, 2).toughness(1), StatlessMaterialStats.MAILLE);
        // tier 3 - compat
        addMaterialStats(MaterialIds.necronium, StatlessMaterialStats.SHIELD_CORE);
        addArmorShieldStats(MaterialIds.bronze,            PlatingMaterialStats.builder().durabilityFactor(28).armor(2, 5, 6, 2).knockbackResistance(0.1f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.constantan,        PlatingMaterialStats.builder().durabilityFactor(25).armor(1, 4, 5, 2).toughness(2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.invar,             PlatingMaterialStats.builder().durabilityFactor(24).armor(1, 3, 5, 2).knockbackResistance(0.1f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.pewter,            PlatingMaterialStats.builder().durabilityFactor(16).armor(2, 5, 7, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.electrum,          PlatingMaterialStats.builder().durabilityFactor(14).armor(1, 3, 4, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.steeleaf,          PlatingMaterialStats.builder().durabilityFactor(10).armor(2, 5, 7, 2), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.polyethylene,      PlatingMaterialStats.builder().durabilityFactor(50).armor(3, 5, 8, 4).toughness(4).knockbackResistance(0.25f), StatlessMaterialStats.SHIELD_CORE, StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.polyvinylChloride, PlatingMaterialStats.builder().durabilityFactor(60).armor(3, 5, 8, 4).toughness(4).knockbackResistance(0.35f), StatlessMaterialStats.SHIELD_CORE, StatlessMaterialStats.MAILLE);
        // tier 4
        addMaterialStats(MaterialIds.blazewood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.blazingBone, StatlessMaterialStats.SHIELD_CORE);
        addArmorShieldStats(MaterialIds.cinderslime, PlatingMaterialStats.builder().durabilityFactor(42).armor(2, 5, 7, 2).knockbackResistance(0.10f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.queensSlime, PlatingMaterialStats.builder().durabilityFactor(50).armor(2, 5, 7, 2).toughness(1), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.hepatizon,   PlatingMaterialStats.builder().durabilityFactor(32).armor(2, 5, 7, 2).toughness(2).knockbackResistance(0.10f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.manyullyn,   PlatingMaterialStats.builder().durabilityFactor(35).armor(2, 5, 7, 2).toughness(3).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addArmorShieldStats(MaterialIds.ancient,     PlatingMaterialStats.builder().durabilityFactor(25).armor(2, 4, 6, 2).knockbackResistance(0.15f));
        addMaterialStats(MaterialIds.ancientHide, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        // tier 4 (end)
        addArmorShieldStats(MaterialIds.knightmetal, PlatingMaterialStats.builder().durabilityFactor(20).armor(2, 5, 7, 2).toughness(2).knockbackResistance(0.05f), StatlessMaterialStats.MAILLE);
        addMaterialStats(MaterialIds.enderslimeVine, StatlessMaterialStats.MAILLE, StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.dragonScale, StatlessMaterialStats.MAILLE);
        addMaterialStats(MaterialIds.shulker, StatlessMaterialStats.MAILLE);
        // tier 4 (compat)
        addArmorShieldStats(MaterialIds.fiery, PlatingMaterialStats.builder().durabilityFactor(25).armor(3, 6, 8, 3).toughness(1.5f), StatlessMaterialStats.MAILLE);
    }

    private void addMisc() {
        // travelers gear
        addMaterialStats(MaterialIds.leather, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStats(MaterialIds.slimeskin, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStats(MaterialIds.skyslimeVine, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStats(MaterialIds.ichorskin, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStats(MaterialIds.enderslimeVine, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStats(MaterialIds.ancientHide, StatlessMaterialStats.REPAIR_KIT);
        // travelers's shield
        addMaterialStats(MaterialIds.ice, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStats(MaterialIds.blazewood, StatlessMaterialStats.REPAIR_KIT);
        // slimeskull
        addMaterialStats(MaterialIds.glass,        new SkullStats( 90, 0));
        addMaterialStats(MaterialIds.enderPearl,   new SkullStats(180, 0));
        addMaterialStats(MaterialIds.bone,         new SkullStats(100, 0));
        addMaterialStats(MaterialIds.venombone,    new SkullStats(175, 1));
        addMaterialStats(MaterialIds.necroticBone, new SkullStats(125, 0));
        addMaterialStats(MaterialIds.string,       new SkullStats(140, 0));
        addMaterialStats(MaterialIds.darkthread,   new SkullStats(200, 1));
        addMaterialStats(MaterialIds.leather,      new SkullStats(150, 2));
        addMaterialStats(MaterialIds.iron,         new SkullStats(165, 2));
        addMaterialStats(MaterialIds.wroughtIron,  new SkullStats(165, 2));
        addMaterialStats(MaterialIds.copper,       new SkullStats(145, 2));
        addMaterialStats(MaterialIds.blazingBone,  new SkullStats(205, 1));
        addMaterialStats(MaterialIds.gold,         new SkullStats(125, 0));
        addMaterialStats(MaterialIds.roseGold,     new SkullStats(175, 1));
        addMaterialStats(MaterialIds.pigIron,      new SkullStats(150, 2));
        // slimesuit
        addMaterialStats(MaterialIds.enderslime, StatlessMaterialStats.REPAIR_KIT);
        addMaterialStats(MaterialIds.phantom, StatlessMaterialStats.REPAIR_KIT);
        // slimesuit embellishments
        addMaterialStats(MaterialIds.blood);
        addMaterialStats(MaterialIds.clay);
        addMaterialStats(MaterialIds.honey);
    }



    private static final float DEFAULT_SMALL_REDUCTION_FACTOR = 0.5f;
    private static final float DEFAULT_SMALL_PROTECTION_FACTOR = 0.4f;

    private static final Map<MaterialId,ArmorDefaults> ARMOR_EXTENSION_DEFAULTS = Map.ofEntries(
            Map.entry(MaterialIds.aluminum, armorDefaults(39f, 2.0f, 6.5f, 5.0f, 1.5f, null, null, 0.18f, 0.01f, null, -0.12f, -0.1f, 0.0f, 0.12f)),
            Map.entry(MaterialIds.amethystBronze, armorDefaults(39f, 2.5f, 7.0f, 5.5f, 2.0f, 1.25f, 2.25f, 0.4f, 0.032f, null, 0.0f, -0.07f, 0.12f, 0.15f, 0.6f, 0.25f)),
            Map.entry(MaterialIds.ancient, armorDefaults(28.8f, 2.0f, 6.0f, 4.5f, 1.5f, null, null, 0.1f, 0.08f, 0.15f, 0.1f, 0.0f, 0.0f, 0.0f)),
            Map.entry(MaterialIds.bronze, armorDefaults(34.3f, 2.0f, 6.0f, 5.0f, 2.0f, 0.5f, 0.25f, 0.28f, 0.025f, 0.05f, 0.1f, 0.04f, -0.05f, -0.05f)),
            Map.entry(MaterialIds.cinderslime, armorDefaults(45.6f, 2.5f, 7.0f, 6.0f, 2.5f, 2.25f, 1.75f, 0.36f, 0.056f, 0.1f, 0.16f, 0.02f, 0.08f, -0.08f, 0.4f, 0.75f)),
            Map.entry(MaterialIds.cobalt, armorDefaults(30f, 2.5f, 6.5f, 5.0f, 2.0f, 2.0f, 1.0f, 0.6f, 0.07f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.6f, 0.5f)),
            Map.entry(MaterialIds.constantan, armorDefaults(26.25f, 2.0f, 5.0f, 4.0f, 2.0f, 0.75f, 2.0f, 0.3f, 0.036f, 0.05f, -0.05f, 0.0f, 0.18f, 0.1f)),
            Map.entry(MaterialIds.copper, armorDefaults(13f, 1.0f, 3.0f, 2.0f, 1.0f, null, null, 0.05f, 0.018f, null, -0.2f, 0.07f, 0.0f, 0.0f)),
            Map.entry(MaterialIds.fiery, armorDefaults(25f, 3.0f, 8.0f, 6.0f, 3.0f, 2.5f, 1.5f, 0.9f, 0.028f, null, -0.2f, 0.15f, 0.05f, 0.03f)),
            Map.entry(MaterialIds.gold, armorDefaults(7f, 1.0f, 4.0f, 3.0f, 1.0f, null, null, 0.05f, 0.01f, null, 0.0f, 0.0f, 0.0f, 0.0f)),
            Map.entry(MaterialIds.hepatizon, armorDefaults(32f, 3.0f, 7.5f, 6.5f, 3.0f, 1.25f, 2.5f, 0.25f, 0.064f, 0.1f, 0.15f, 0.0f, 0.12f, 0.2f, null, 0.75f)),
            Map.entry(MaterialIds.invar, armorDefaults(27f, 2.5f, 6.0f, 5.0f, 2.5f, 0.75f, 1.5f, 0.12f, 0.048f, 0.1f, 0.0f, -0.06f, 0.1f, 0.18f)),
            Map.entry(MaterialIds.iron, armorDefaults(15f, 2.0f, 6.0f, 5.0f, 2.0f, 1.0f, 0.5f, 0.2f, 0.012f, null, 0.1f, 0.08f, 0.0f, -0.1f)),
            Map.entry(MaterialIds.wroughtIron, armorDefaults(23.5f, 2.25f, 6.75f, 5.5f, 2.0f, 1.5f, 1.25f, 0.45f, 0.046f, null, 0.15f, 0.06f, 0.025f, -0.025f, 0.5f, 0.5f)),
            Map.entry(MaterialIds.knightmetal, armorDefaults(22.8f, 3.0f, 8.0f, 6.5f, 2.5f, 1.5f, 2.25f, 0.5f, 0.08f, 0.05f, 0.0f, 0.12f, 0.05f, 0.08f, 0.375f, 0.375f)),
            Map.entry(MaterialIds.lead, armorDefaults(10.8f, 1.5f, 4.0f, 3.5f, 1.5f, null, 0.75f, 0.6f, 0.01f, 0.1f, -0.1f, -0.04f, -0.05f, 0.08f)),
            Map.entry(MaterialIds.manyullyn, armorDefaults(51.25f, 2.5f, 7.0f, 6.5f, 2.0f, 3.0f, 1.25f, 0.75f, 0.05f, 0.05f, 0.2f, 0.05f, 0.2f, -0.1f, 0.8f, 0.8f)),
            Map.entry(MaterialIds.obsidian, armorDefaults(21f, 2.0f, 7.0f, 5.5f, 1.5f, 1.75f, null, 0.45f, 0.02f, 0.15f, 0.0f, 0.1f, -0.1f, -0.2f, 0.2f, 0.5f)),
            Map.entry(MaterialIds.osmium, armorDefaults(25.5f, 1.5f, 5.0f, 4.5f, 1.0f, null, 0.5f, 0.05f, 0.044f, 0.05f, 0.15f, 0.03f, -0.2f, 0.05f)),
            Map.entry(MaterialIds.pewter, armorDefaults(16f, 2.5f, 6.5f, 5.0f, 2.0f, 0.5f, null, 0.15f, 0.03f, null, -0.15f, 0.02f, 0.05f, -0.05f)),
            Map.entry(MaterialIds.pigIron, armorDefaults(35f, 2.0f, 6.0f, 5.0f, 2.0f, 1.5f, 0.25f, 0.3f, 0.06f, 0.075f, 0.07f, 0.0f, 0.12f, 0.0f)),
            Map.entry(MaterialIds.queensSlime, armorDefaults(64f, 2.0f, 6.0f, 5.0f, 2.0f, 0.75f, 3.0f, 0.6f, 0.075f, null, 0.35f, 0.0f, -0.1f, 0.2f, 0.75f, 0.6f)),
            Map.entry(MaterialIds.roseGold, armorDefaults(9f, 1.5f, 5.0f, 4.0f, 1.5f, 0.25f, 1.0f, 0.05f, 0.01f, null, -0.3f, -0.1f, 0.1f, 0.08f)),
            Map.entry(MaterialIds.scorchedStone, armorDefaults(10.5f, 2.0f, 4.5f, 4.0f, 1.5f, 1.5f, null, 0.16f, 0.04f, 0.05f, -0.1f, 0.0f, 0.15f, -0.15f)),
            Map.entry(MaterialIds.searedStone, armorDefaults(13.75f, 1.5f, 4.0f, 3.0f, 1.5f, 1.0f, null, 0.1f, 0.024f, 0.1f, -0.15f, 0.05f, 0.1f, -0.1f)),
            Map.entry(MaterialIds.silver, armorDefaults(16.5f, 1.0f, 3.5f, 2.5f, 1.0f, null, null, 0.05f, 0.01f, null, -0.18f, 0.0f, -0.25f, 0.08f)),
            Map.entry(MaterialIds.slimesteel, armorDefaults(43.2f, 2.0f, 6.0f, 5.0f, 2.0f, 0.25f, 0.75f, 0.15f, 0.016f, 0.025f, 0.25f, -0.07f, -0.08f, 0.1f)),
            Map.entry(MaterialIds.steel, armorDefaults(32f, 2.5f, 7.5f, 6.0f, 2.0f, 2.0f, 2.0f, 0.7f, 0.08f, null, 0.2f, 0.04f, 0.05f, 0.05f, 0.5f, 0.6f)),
            Map.entry(MaterialIds.steeleaf, armorDefaults(10f, 2.0f, 6.5f, 5.5f, 2.0f, 1.0f, 1.0f, 0.75f, 0.015f, 0.025f, -0.15f, -0.05f, 0.12f, 0.08f))
    );


    private static final Map<MaterialId,ArmorDefaults> ARMOR_EXTENSION_COMPAT_DEFAULTS = Map.ofEntries(
            Map.entry(new MaterialId("tgears", "cardboard"), armorDefaults(2.5f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.5f, -0.1f, 0.0f, 0.0f)),
            Map.entry(new MaterialId("tinkersinnovation", "andesite_alloy"), armorDefaults(21.6f, 2.0f, 4.0f, 3.0f, 2.0f, 0.75f, 0.0f, 0.24f, 0.018f, 0.0f, 0.15f, 0.02f, 0.05f, -0.1f)),
            Map.entry(new MaterialId("tinkersinnovation", "brass"), armorDefaults(46.75f, 3.0f, 6.5f, 5.0f, 2.5f, 1.4f, 0.0f, 0.35f, 0.02f, 0.1f, 0.2f, 0.04f, 0.08f, -0.16f)),
            Map.entry(new MaterialId("tinkersinnovation", "chaos"), armorDefaults(38.4f, 2.0f, 6.0f, 5.0f, 2.0f, 0.25f, 2.75f, 0.2f, 0.038f, 0.0f, -0.07f, 0.0f, -0.1f, 0.13f)),
            Map.entry(new MaterialId("tinkersinnovation", "clonate"), armorDefaults(75.0f, 3.0f, 7.0f, 5.0f, 3.0f, 0.6f, 2.4f, 0.65f, 0.049f, 0.1f, -0.05f, 0.05f, -0.05f, 0.0f)),
            Map.entry(new MaterialId("tinkersinnovation", "decline"), armorDefaults(66.5f, 3.0f, 8.0f, 6.0f, 3.0f, 1.5f, 3.0f, 0.02f, 0.052f, 0.01f, 0.0f, -0.04f, 0.1f, 0.1f)),
            Map.entry(new MaterialId("tinkersinnovation", "enchantment_essence"), armorDefaults(36.6f, 1.5f, 3.0f, 2.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.1f, 0.01f, -0.15f, -0.1f, 0.0f, 0.0f)),
            Map.entry(new MaterialId("tinkersinnovation", "eternium"), armorDefaults(9999.0f, 3.0f, 8.0f, 6.0f, 3.0f, 5.0f, 7.5f, 1.25f, 0.1f, 1.0f, 1.0f, -0.05f, 0.1f, 0.25f)),
            Map.entry(new MaterialId("tinkersinnovation", "experience"), armorDefaults(18.75f, 1.0f, 2.0f, 2.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.07f, 0.0f, -0.3f, -0.15f, -0.2f, -0.2f)),
            Map.entry(new MaterialId("tinkersinnovation", "farseeing_alloy"), armorDefaults(69.5f, 3.0f, 6.0f, 5.0f, 2.0f, 2.5f, 4.0f, 0.9f, 0.08f, 0.2f, -0.1f, 0.08f, 0.05f, 0.15f)),
            Map.entry(new MaterialId("tinkersinnovation", "fools_gold"), armorDefaults(51.7f, 3.0f, 6.0f, 5.0f, 2.0f, 1.5f, 1.0f, 0.45f, 0.033f, 0.0f, 0.17f, -0.06f, 0.0f, 0.05f)),
            Map.entry(new MaterialId("tinkersinnovation", "hostilium"), armorDefaults(67.6f, 6.0f, 10.0f, 9.0f, 5.0f, 6.0f, 4.0f, 0.8f, 0.2f, 0.1f, 0.1f, -0.1f, 0.1f, 0.1f, null, 0.75f)),
            Map.entry(new MaterialId("tinkersinnovation", "machine"), armorDefaults(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)),
            Map.entry(new MaterialId("tinkersinnovation", "miracle"), armorDefaults(69.0f, 4.0f, 7.0f, 6.0f, 3.0f, 4.5f, 3.0f, 1.05f, 0.16f, 0.05f, -0.15f, 0.09f, 0.3f, -0.1f)),
            Map.entry(new MaterialId("tinkersinnovation", "mudslime"), armorDefaults(32.5f, 1.5f, 4.0f, 3.0f, 1.5f, 0.25f, 1.0f, 0.01f, 0.01f, 0.0f, 0.05f, -0.07f, -0.05f, 0.18f)),
            Map.entry(new MaterialId("tinkersinnovation", "polychrome_alloy"), armorDefaults(65.0f, 3.0f, 6.0f, 5.0f, 2.0f, 3.2f, 8.0f, 1.0f, 0.125f, 0.01f, 0.2f, 0.1f, -0.05f, 0.3f)),
            Map.entry(new MaterialId("tinkersinnovation", "poseidite"), armorDefaults(42.2f, 3.0f, 8.0f, 6.0f, 3.0f, 3.6f, 2.0f, 0.48f, 0.09f, 0.0f, -0.2f, -0.1f, 0.25f, 0.15f)),
            Map.entry(new MaterialId("tinkersinnovation", "ruby"), armorDefaults(20.5f, 2.0f, 5.5f, 4.5f, 2.0f, 0.9f, 1.5f, 0.55f, 0.015f, 0.05f, -0.1f, -0.05f, -0.3f, -0.1f)),
            Map.entry(new MaterialId("tinkersinnovation", "sapphire"), armorDefaults(23.0f, 2.0f, 5.0f, 4.0f, 2.0f, 1.25f, 2.5f, 0.32f, 0.05f, 0.0f, 0.05f, 0.03f, -0.1f, 0.1f)),
            Map.entry(new MaterialId("tinkersinnovation", "sculkium"), armorDefaults(40.0f, 6.0f, 10.0f, 9.0f, 5.0f, 7.5f, 4.0f, 1.0f, 0.15f, 1.0f, 0.0f, -0.1f, 0.3f, 0.2f)),
            Map.entry(new MaterialId("tinkersinnovation", "shulkerate"), armorDefaults(400.0f, 3.0f, 8.0f, 6.0f, 3.0f, 1.6f, 2.0f, 0.4f, 0.1f, 0.0f, 0.4f, 0.1f, -0.08f, -0.1f)),
            Map.entry(new MaterialId("tinkersinnovation", "slimton"), armorDefaults(68.0f, 5.0f, 9.0f, 7.0f, 4.0f, 5.0f, 5.0f, 1.2f, 0.06f, 0.05f, 0.0f, -0.05f, 0.2f, 0.2f)),
            Map.entry(new MaterialId("tinkersinnovation", "straddlite_alloy"), armorDefaults(1890.0f, 4.0f, 7.0f, 6.0f, 3.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f)),
            Map.entry(new MaterialId("tinkersinnovation", "sunsoul_alloy"), armorDefaults(70.5f, 3.0f, 6.0f, 5.0f, 2.0f, 2.1f, 3.0f, 0.65f, 0.08f, 0.1f, 0.2f, -0.07f, 0.05f, 0.1f)),
            Map.entry(new MaterialId("tinkersinnovation", "totemic_gold"), armorDefaults(15.0f, 2.0f, 6.0f, 5.0f, 2.0f, 1.0f, 0.0f, 0.15f, 0.03f, 0.0f, 0.1f, 0.0f, 0.0f, -0.1f)),
            Map.entry(new MaterialId("tinkersinnovation", "zinc"), armorDefaults(39.0f, 2.0f, 4.0f, 3.0f, 2.0f, 0.0f, 0.0f, 0.1f, 0.01f, 0.0f, -0.25f, -0.15f, 0.0f, 0.0f))
    );

    private static final List<MaterialId> ARMOR_EXTENSION_DEFAULT_MAILLE_MATERIALS = List.of(
            MaterialIds.leather, MaterialIds.slimeskin, MaterialIds.skyslimeVine,
            MaterialIds.ichorskin, MaterialIds.enderslimeVine
    );
    private static final List<MaterialId> ARMOR_EXTENSION_LINEAR_MATERIALS = List.of(
            MaterialIds.leather, MaterialIds.slimeskin, MaterialIds.skyslimeVine,
            MaterialIds.ichorskin, MaterialIds.enderslimeVine, MaterialIds.wool,
            MaterialIds.ancientHide
    );
    private static final Map<MaterialId, IMaterialStats> ARMOR_EXTENSION_MAILLE_OVERRIDES = Map.of(
            MaterialIds.dragonScale, ArmorExtensionMaterialStats.maille(-0.1f, 0.05f, 0.1f, 0f),
            MaterialIds.shulker, ArmorExtensionMaterialStats.maille(0f, 0f, 0.1f, 0f),
            MaterialIds.ancientHide, ArmorExtensionMaterialStats.maille(0.25f, 0.1f, 0f, 0f)
    );
    private static final List<MaterialStatsId> DEFAULT_ARMOR_EXTENSION_SPRITE_STATS = defaultArmorExtensionSpriteStats();
    private static final List<MaterialStatsId> LINEAR_ARMOR_EXTENSION_SPRITE_STATS = List.of(StatlessMaterialStats.LINEAR.getIdentifier());
    private static final List<MaterialStatsId> MAILLE_ARMOR_EXTENSION_SPRITE_STATS = List.of(ArmorExtensionMaterialStats.MAILLE.getId());
    private static final List<MaterialStatsId> LINEAR_AND_MAILLE_ARMOR_EXTENSION_SPRITE_STATS = List.of(
            StatlessMaterialStats.LINEAR.getIdentifier(), ArmorExtensionMaterialStats.MAILLE.getId()
    );

    private void addArmorExtensionMaterialStats() {
        ARMOR_EXTENSION_DEFAULTS.forEach(this::addArmorDefaultStats);
        ARMOR_EXTENSION_COMPAT_DEFAULTS.forEach(this::addArmorDefaultStats);
        for (MaterialId material : ARMOR_EXTENSION_DEFAULT_MAILLE_MATERIALS) {
            addMaterialStats(material, StatlessMaterialStats.CUIRASS, ArmorExtensionMaterialStats.MAILLE_DEFAULT, StatlessMaterialStats.LINEAR);
        }
        addMaterialStats(MaterialIds.wool, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.ancientHide, StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        ARMOR_EXTENSION_MAILLE_OVERRIDES.forEach((material, maille) -> addMaterialStats(material, maille));
    }

    public static List<MaterialStatsId> getArmorExtensionSpriteStats(MaterialId material) {
        if (ARMOR_EXTENSION_DEFAULTS.containsKey(material) || ARMOR_EXTENSION_COMPAT_DEFAULTS.containsKey(material)) {
            return DEFAULT_ARMOR_EXTENSION_SPRITE_STATS;
        }
        boolean linear = ARMOR_EXTENSION_LINEAR_MATERIALS.contains(material);
        boolean maille = ARMOR_EXTENSION_DEFAULT_MAILLE_MATERIALS.contains(material) || ARMOR_EXTENSION_MAILLE_OVERRIDES.containsKey(material);
        if (linear && maille) {
            return LINEAR_AND_MAILLE_ARMOR_EXTENSION_SPRITE_STATS;
        }
        if (linear) {
            return LINEAR_ARMOR_EXTENSION_SPRITE_STATS;
        }
        if (maille) {
            return MAILLE_ARMOR_EXTENSION_SPRITE_STATS;
        }
        return List.of();
    }

    private static List<MaterialStatsId> defaultArmorExtensionSpriteStats() {
        List<MaterialStatsId> stats = new ArrayList<>();
        stats.add(ArmorExtensionMaterialStats.ARMOR_PLATE.getId());
        stats.add(ArmorExtensionMaterialStats.ARMOR_MAIL.getId());
        stats.add(ArmorExtensionMaterialStats.MAILLE.getId());
        addSpriteStats(stats, ArmorExtensionMaterialStats.CAST_TYPES);
        addSpriteStats(stats, ArmorExtensionMaterialStats.FRAME_TYPES);
        addSpriteStats(stats, ArmorExtensionMaterialStats.MASSIVE_CAST_TYPES);
        return List.copyOf(stats);
    }

    private static void addSpriteStats(List<MaterialStatsId> stats, List<? extends MaterialStatType<?>> types) {
        for (MaterialStatType<?> type : types) {
            stats.add(type.getId());
        }
    }

    private void addArmorDefaultStats(MaterialId material, ArmorDefaults defaults) {
        addMaterialStats(material, defaults.platingStats());
        addMaterialStats(material, defaults.extensionStats());
        addMaterialStats(material, defaults.shieldStats());
        addMaterialStats(material, ArmorExtensionMaterialStats.MAILLE_DEFAULT);
    }

    private static ArmorDefaults armorDefaults(float durabilityBase, float helmet, float chestplate, float leggings, float boots,
                                               Float armorStrength, Float armorToughness, float reduction, float protection,
                                               Float knockbackResistance, float durabilityMultiplier, float armorMultiplier,
                                               float armorStrengthMultiplier, float armorToughnessMultiplier) {
        return armorDefaults(durabilityBase, helmet, chestplate, leggings, boots, armorStrength, armorToughness, reduction, protection,
                knockbackResistance, durabilityMultiplier, armorMultiplier, armorStrengthMultiplier, armorToughnessMultiplier, null, null);
    }

    private static ArmorDefaults armorDefaults(float durabilityBase, float helmet, float chestplate, float leggings, float boots,
                                               Float armorStrength, Float armorToughness, float reduction, float protection,
                                               Float knockbackResistance, float durabilityMultiplier, float armorMultiplier,
                                               float armorStrengthMultiplier, float armorToughnessMultiplier,
                                               Float smallReductionFactor, Float smallProtectionFactor) {
        return new ArmorDefaults(durabilityBase, helmet, chestplate, leggings, boots,
                armorStrength == null ? 0f : armorStrength,
                armorToughness == null ? 0f : armorToughness,
                reduction, protection,
                knockbackResistance == null ? 0f : knockbackResistance,
                durabilityMultiplier, armorMultiplier, armorStrengthMultiplier, armorToughnessMultiplier,
                smallReductionFactor == null ? DEFAULT_SMALL_REDUCTION_FACTOR : smallReductionFactor,
                smallProtectionFactor == null ? DEFAULT_SMALL_PROTECTION_FACTOR : smallProtectionFactor);
    }

    private record ArmorDefaults(float durabilityBase, float helmet, float chestplate, float leggings, float boots,
                                 float armorStrength, float armorToughness, float reduction, float protection,
                                 float knockbackResistance, float durabilityMultiplier, float armorMultiplier,
                                 float armorStrengthMultiplier, float armorToughnessMultiplier,
                                 float smallReductionFactor, float smallProtectionFactor) {
        private IMaterialStats[] platingStats() {
            IMaterialStats[] stats = new IMaterialStats[4];
            for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
                stats[slotType.ordinal()] = new PlatingMaterialStats(PlatingMaterialStats.TYPES.get(slotType.ordinal()), durability(slotType), armor(slotType), armorStrength, armorToughness, knockbackResistance);
            }
            return stats;
        }

        private PlatingMaterialStats shieldStats() {
            return new PlatingMaterialStats(PlatingMaterialStats.SHIELD, shieldDurability(), 0f, armorStrength, armorToughness, knockbackResistance);
        }

        private IMaterialStats[] extensionStats() {
            IMaterialStats[] stats = new IMaterialStats[14];
            stats[0] = new ArmorExtensionMaterialStats.ArmorLayerStats(ArmorExtensionMaterialStats.ARMOR_PLATE, durabilityMultiplier, armorMultiplier, armorStrength, armorToughness, reduction * smallReductionFactor, protection);
            stats[1] = new ArmorExtensionMaterialStats.ArmorLayerStats(ArmorExtensionMaterialStats.ARMOR_MAIL, durabilityMultiplier, armorMultiplier, armorStrength, armorToughness, 0f, protection);
            int index = 2;
            for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
                stats[index++] = new ArmorExtensionMaterialStats.ArmorPieceStats(ArmorExtensionMaterialStats.CAST_TYPES.get(slotType.ordinal()), durability(slotType), armor(slotType), armorStrength, armorToughness, reduction * smallReductionFactor, protection * smallProtectionFactor, knockbackResistance);
            }
            for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
                stats[index++] = new ArmorExtensionMaterialStats.ArmorFrameStats(ArmorExtensionMaterialStats.FRAME_TYPES.get(slotType.ordinal()), durability(slotType), armor(slotType), armorStrengthMultiplier, armorToughnessMultiplier, knockbackResistance);
            }
            for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
                stats[index++] = new ArmorExtensionMaterialStats.ArmorPieceStats(ArmorExtensionMaterialStats.MASSIVE_CAST_TYPES.get(slotType.ordinal()), durability(slotType), armor(slotType), armorStrength, armorToughness, reduction, protection, knockbackResistance);
            }
            return stats;
        }

        private int durability(ArmorItem.Type slot) {
            return (int)(ArmorModuleBuilder.MAX_DAMAGE_ARRAY[slot.ordinal()] * durabilityBase);
        }

        private int shieldDurability() {
            return (int)(ArmorModuleBuilder.SHIELD_DAMAGE * durabilityBase);
        }

        private float armor(ArmorItem.Type slot) {
            return switch (slot) {
                case HELMET -> helmet;
                case CHESTPLATE -> chestplate;
                case LEGGINGS -> leggings;
                case BOOTS -> boots;
            };
        }
    }

    private void addArmorShieldStats(MaterialId location, PlatingMaterialStats.Builder statBuilder, IMaterialStats... otherStats) {
        PlatingMaterialStats[] plating = new PlatingMaterialStats[4];
        for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
            plating[slotType.ordinal()] = statBuilder.build(slotType);
        }

        addMaterialStats(location, plating);
        addMaterialStats(location, armorExtensionStats(plating));
        if (otherStats.length > 0) {
            addMaterialStats(location, otherStats);
        }
        addMaterialStats(location, statBuilder.buildShield());
    }

    private static IMaterialStats[] armorExtensionStats(PlatingMaterialStats[] plating) {
        IMaterialStats[] stats = new IMaterialStats[14];
        stats[0] = new ArmorExtensionMaterialStats.ArmorLayerStats(
                ArmorExtensionMaterialStats.ARMOR_PLATE, 0.10f, 0.08f, 0f, averageToughness(plating) * 0.10f, 0f, 0f);
        stats[1] = new ArmorExtensionMaterialStats.ArmorLayerStats(
                ArmorExtensionMaterialStats.ARMOR_MAIL, 0.05f, 0.04f, 0f, averageToughness(plating) * 0.05f, 0f, 0f);

        int index = 2;
        for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
            PlatingMaterialStats slot = plating[slotType.ordinal()];
            stats[index++] = piece(ArmorExtensionMaterialStats.CAST_TYPES.get(slotType.ordinal()), slot, 1f);
        }
        for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
            PlatingMaterialStats slot = plating[slotType.ordinal()];
            stats[index++] = frame(ArmorExtensionMaterialStats.FRAME_TYPES.get(slotType.ordinal()), slot);
        }
        for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
            PlatingMaterialStats slot = plating[slotType.ordinal()];
            stats[index++] = piece(ArmorExtensionMaterialStats.MASSIVE_CAST_TYPES.get(slotType.ordinal()), slot, 1f);
        }
        return stats;
    }

    private static ArmorExtensionMaterialStats.ArmorPieceStats piece(MaterialStatType<?> type, PlatingMaterialStats source, float scale) {
        return new ArmorExtensionMaterialStats.ArmorPieceStats(
                type,
                Math.max(1, Math.round(source.durability() * scale)),
                source.armor() * scale,
                0f,
                source.toughness() * scale,
                0f,
                0f,
                source.knockbackResistance() * scale);
    }

    private static ArmorExtensionMaterialStats.ArmorFrameStats frame(MaterialStatType<?> type, PlatingMaterialStats source) {
        return new ArmorExtensionMaterialStats.ArmorFrameStats(
                type,
                Math.max(1, Math.round(source.durability() * 0.5f)),
                source.armor() * 0.25f,
                0f,
                0f,
                source.knockbackResistance() * 0.5f);
    }

    private static float averageToughness(PlatingMaterialStats[] plating) {
        float total = 0f;
        for (PlatingMaterialStats stats : plating) {
            total += stats.toughness();
        }
        return total / plating.length;
    }
}
