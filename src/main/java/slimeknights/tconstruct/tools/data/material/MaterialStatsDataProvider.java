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
    addMaterialStats(MaterialIds.redstone, StatlessMaterialStats.ARROW_HEAD);
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
        addMaterialStats(MaterialIds.copper,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(13f)
                        .platingArmor(1f, 3f, 2f, 1f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(13f)
                        .armor(1.0f, 3.0f, 2.0f, 1.0f)
                        .reduction(0.05f)
                        .protection(0.018f)
                        .durabilityMultiplier(-0.2f)
                        .armorMultiplier(0.07f)
                        .armorStrengthMultiplier(0.0f)
                        .armorToughnessMultiplier(0.0f)
                        .build());
        addMaterialStats(MaterialIds.leather, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f), StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.vine, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f));
        addMaterialStats(MaterialIds.wool, StatlessMaterialStats.LINEAR);
        // tier 2
        addMaterialStats(MaterialIds.slimewood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.venombone, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.necroticBone, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.slimeskin, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f), StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.skyslimeVine, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f), StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.weepingVine, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f));
        addMaterialStats(MaterialIds.twistingVine, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f));
        addMaterialStats(MaterialIds.iron,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(15f)
                        .platingArmor(2f, 5f, 4f, 2f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(15f)
                        .armor(2.0f, 6.0f, 5.0f, 2.0f)
                        .armorStrength(1.0f)
                        .armorToughness(0.5f)
                        .reduction(0.2f)
                        .protection(0.012f)
                        .durabilityMultiplier(0.1f)
                        .armorMultiplier(0.08f)
                        .armorStrengthMultiplier(0.0f)
                        .armorToughnessMultiplier(-0.1f)
                        .build());
        addMaterialStats(MaterialIds.gold,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(7f)
                        .platingArmor(1f, 4f, 3f, 1f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(7f)
                        .armor(1.0f, 4.0f, 3.0f, 1.0f)
                        .reduction(0.05f)
                        .protection(0.01f)
                        .durabilityMultiplier(0.0f)
                        .armorMultiplier(0.0f)
                        .armorStrengthMultiplier(0.0f)
                        .armorToughnessMultiplier(0.0f)
                        .build());
        addMaterialStats(MaterialIds.searedStone,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(14f)
                        .platingArmor(2f, 4f, 3f, 1f)
                        .platingKnockbackResistance(0.1f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(13.75f)
                        .armor(1.5f, 4.0f, 3.0f, 1.5f)
                        .armorStrength(1.0f)
                        .reduction(0.1f)
                        .protection(0.024f)
                        .knockbackResistance(0.1f)
                        .durabilityMultiplier(-0.15f)
                        .armorMultiplier(0.05f)
                        .armorStrengthMultiplier(0.1f)
                        .armorToughnessMultiplier(-0.1f)
                        .build());
        addMaterialStats(MaterialIds.scorchedStone,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(10f)
                        .platingArmor(2f, 5f, 4f, 1f)
                        .platingKnockbackResistance(0.05f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(10.5f)
                        .armor(2.0f, 4.5f, 4.0f, 1.5f)
                        .armorStrength(1.5f)
                        .reduction(0.16f)
                        .protection(0.04f)
                        .knockbackResistance(0.05f)
                        .durabilityMultiplier(-0.1f)
                        .armorMultiplier(0.0f)
                        .armorStrengthMultiplier(0.15f)
                        .armorToughnessMultiplier(-0.15f)
                        .build());
        // tier 2 - compat
        addMaterialStats(MaterialIds.treatedWood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.ironwood,    StatlessMaterialStats.SHIELD_CORE, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f));
        addMaterialStats(MaterialIds.osmium,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(25f)
                        .platingArmor(2f, 5f, 3f, 1f)
                        .platingKnockbackResistance(0.05f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(25.5f)
                        .armor(1.5f, 5.0f, 4.5f, 1.0f)
                        .armorToughness(0.5f)
                        .reduction(0.05f)
                        .protection(0.044f)
                        .knockbackResistance(0.05f)
                        .durabilityMultiplier(0.15f)
                        .armorMultiplier(0.03f)
                        .armorStrengthMultiplier(-0.2f)
                        .armorToughnessMultiplier(0.05f)
                        .build());
        addMaterialStats(MaterialIds.aluminum,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(13f)
                        .platingArmor(2f, 6f, 4f, 2f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(39f)
                        .armor(2.0f, 6.5f, 5.0f, 1.5f)
                        .reduction(0.18f)
                        .protection(0.01f)
                        .durabilityMultiplier(-0.12f)
                        .armorMultiplier(-0.1f)
                        .armorStrengthMultiplier(0.0f)
                        .armorToughnessMultiplier(0.12f)
                        .build());
        addMaterialStats(MaterialIds.silver,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(18f)
                        .platingArmor(2f, 5f, 4f, 1f)
                        .platingKnockbackResistance(0.05f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(16.5f)
                        .armor(1.0f, 3.5f, 2.5f, 1.0f)
                        .reduction(0.05f)
                        .protection(0.01f)
                        .durabilityMultiplier(-0.18f)
                        .armorMultiplier(0.0f)
                        .armorStrengthMultiplier(-0.25f)
                        .armorToughnessMultiplier(0.08f)
                        .build());
        addMaterialStats(MaterialIds.lead,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(12f)
                        .platingArmor(2f, 4f, 3f, 1f)
                        .platingKnockbackResistance(0.1f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(10.8f)
                        .armor(1.5f, 4.0f, 3.5f, 1.5f)
                        .armorToughness(0.75f)
                        .reduction(0.6f)
                        .protection(0.01f)
                        .knockbackResistance(0.1f)
                        .durabilityMultiplier(-0.1f)
                        .armorMultiplier(-0.04f)
                        .armorStrengthMultiplier(-0.05f)
                        .armorToughnessMultiplier(0.08f)
                        .build());
        // tier 3
        addMaterialStats(MaterialIds.nahuatl, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.ichorskin, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f), StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.slimesteel,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(40f)
                        .platingArmor(2f, 6f, 5f, 2f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(43.2f)
                        .armor(2.0f, 6.0f, 5.0f, 2.0f)
                        .armorStrength(0.25f)
                        .armorToughness(0.75f)
                        .reduction(0.15f)
                        .protection(0.016f)
                        .knockbackResistance(0.025f)
                        .durabilityMultiplier(0.25f)
                        .armorMultiplier(-0.07f)
                        .armorStrengthMultiplier(-0.08f)
                        .armorToughnessMultiplier(0.1f)
                        .build());
        addMaterialStats(MaterialIds.amethystBronze,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(28f)
                        .platingArmor(2f, 6f, 5f, 2f)
                        .platingToughness(2f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(39f)
                        .armor(2.5f, 7.0f, 5.5f, 2.0f)
                        .armorStrength(1.25f)
                        .armorToughness(2.25f)
                        .reduction(0.4f)
                        .protection(0.032f)
                        .durabilityMultiplier(0.0f)
                        .armorMultiplier(-0.07f)
                        .armorStrengthMultiplier(0.12f)
                        .armorToughnessMultiplier(0.15f)
                        .smallReductionFactor(0.6f)
                        .smallProtectionFactor(0.25f)
                        .build());
        addMaterialStats(MaterialIds.obsidian,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(11f)
                        .platingArmor(2f, 5f, 4f, 2f)
                        .platingKnockbackResistance(0.15f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(21f)
                        .armor(2.0f, 7.0f, 5.5f, 1.5f)
                        .armorStrength(1.75f)
                        .reduction(0.45f)
                        .protection(0.02f)
                        .knockbackResistance(0.15f)
                        .durabilityMultiplier(0.0f)
                        .armorMultiplier(0.1f)
                        .armorStrengthMultiplier(-0.1f)
                        .armorToughnessMultiplier(-0.2f)
                        .smallReductionFactor(0.2f)
                        .smallProtectionFactor(0.5f)
                        .build());
        addMaterialStats(MaterialIds.roseGold,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(9f)
                        .platingArmor(2f, 5f, 3f, 1f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(9f)
                        .armor(1.5f, 5.0f, 4.0f, 1.5f)
                        .armorStrength(0.25f)
                        .armorToughness(1.0f)
                        .reduction(0.05f)
                        .protection(0.01f)
                        .durabilityMultiplier(-0.3f)
                        .armorMultiplier(-0.1f)
                        .armorStrengthMultiplier(0.1f)
                        .armorToughnessMultiplier(0.08f)
                        .build());
        addMaterialStats(MaterialIds.pigIron,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(23f)
                        .platingArmor(1f, 4f, 3f, 1f)
                        .platingToughness(1f)
                        .platingKnockbackResistance(0.1f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(35f)
                        .armor(2.0f, 6.0f, 5.0f, 2.0f)
                        .armorStrength(1.5f)
                        .armorToughness(0.25f)
                        .reduction(0.3f)
                        .protection(0.06f)
                        .knockbackResistance(0.075f)
                        .durabilityMultiplier(0.07f)
                        .armorMultiplier(0.0f)
                        .armorStrengthMultiplier(0.12f)
                        .armorToughnessMultiplier(0.0f)
                        .build());
        addMaterialStats(MaterialIds.cobalt,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(30f)
                        .platingArmor(2f, 7f, 5f, 2f)
                        .platingToughness(1f)
                        .platingKnockbackResistance(0.05f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(30f)
                        .armor(2.5f, 6.5f, 5.0f, 2.0f)
                        .armorStrength(2.0f)
                        .armorToughness(1.0f)
                        .reduction(0.6f)
                        .protection(0.07f)
                        .knockbackResistance(0.05f)
                        .durabilityMultiplier(0.05f)
                        .armorMultiplier(0.05f)
                        .armorStrengthMultiplier(0.05f)
                        .armorToughnessMultiplier(0.05f)
                        .smallReductionFactor(0.6f)
                        .smallProtectionFactor(0.5f)
                        .build());
        addMaterialStats(MaterialIds.steel,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(29f)
                        .platingArmor(2f, 7f, 5f, 2f)
                        .platingToughness(2f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(32f)
                        .armor(2.5f, 7.5f, 6.0f, 2.0f)
                        .armorStrength(2.0f)
                        .armorToughness(2.0f)
                        .reduction(0.7f)
                        .protection(0.08f)
                        .durabilityMultiplier(0.2f)
                        .armorMultiplier(0.04f)
                        .armorStrengthMultiplier(0.05f)
                        .armorToughnessMultiplier(0.05f)
                        .smallReductionFactor(0.5f)
                        .smallProtectionFactor(0.6f)
                        .build());
        addMaterialStats(MaterialIds.wroughtIron,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(22f)
                        .platingArmor(2f, 6f, 4f, 2f)
                        .platingToughness(1f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(23.5f)
                        .armor(2.25f, 6.75f, 5.5f, 2.0f)
                        .armorStrength(1.5f)
                        .armorToughness(1.25f)
                        .reduction(0.45f)
                        .protection(0.046f)
                        .durabilityMultiplier(0.15f)
                        .armorMultiplier(0.06f)
                        .armorStrengthMultiplier(0.025f)
                        .armorToughnessMultiplier(-0.025f)
                        .smallReductionFactor(0.5f)
                        .smallProtectionFactor(0.5f)
                        .build());
        // tier 3 - compat
        addMaterialStats(MaterialIds.necronium, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.bronze,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(28f)
                        .platingArmor(2f, 6f, 5f, 2f)
                        .platingKnockbackResistance(0.1f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(34.3f)
                        .armor(2.0f, 6.0f, 5.0f, 2.0f)
                        .armorStrength(0.5f)
                        .armorToughness(0.25f)
                        .reduction(0.28f)
                        .protection(0.025f)
                        .knockbackResistance(0.05f)
                        .durabilityMultiplier(0.1f)
                        .armorMultiplier(0.04f)
                        .armorStrengthMultiplier(-0.05f)
                        .armorToughnessMultiplier(-0.05f)
                        .build());
        addMaterialStats(MaterialIds.constantan,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(25f)
                        .platingArmor(2f, 5f, 4f, 1f)
                        .platingToughness(2f)
                        .platingKnockbackResistance(0.05f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(26.25f)
                        .armor(2.0f, 5.0f, 4.0f, 2.0f)
                        .armorStrength(0.75f)
                        .armorToughness(2.0f)
                        .reduction(0.3f)
                        .protection(0.036f)
                        .knockbackResistance(0.05f)
                        .durabilityMultiplier(-0.05f)
                        .armorMultiplier(0.0f)
                        .armorStrengthMultiplier(0.18f)
                        .armorToughnessMultiplier(0.1f)
                        .build());
        addMaterialStats(MaterialIds.invar,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(24f)
                        .platingArmor(2f, 5f, 3f, 1f)
                        .platingKnockbackResistance(0.1f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(27f)
                        .armor(2.5f, 6.0f, 5.0f, 2.5f)
                        .armorStrength(0.75f)
                        .armorToughness(1.5f)
                        .reduction(0.12f)
                        .protection(0.048f)
                        .knockbackResistance(0.1f)
                        .durabilityMultiplier(0.0f)
                        .armorMultiplier(-0.06f)
                        .armorStrengthMultiplier(0.1f)
                        .armorToughnessMultiplier(0.18f)
                        .build());
        addMaterialStats(MaterialIds.pewter,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(16f)
                        .platingArmor(2f, 7f, 5f, 2f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(16f)
                        .armor(2.5f, 6.5f, 5.0f, 2.0f)
                        .armorStrength(0.5f)
                        .reduction(0.15f)
                        .protection(0.03f)
                        .durabilityMultiplier(-0.15f)
                        .armorMultiplier(0.02f)
                        .armorStrengthMultiplier(0.05f)
                        .armorToughnessMultiplier(-0.05f)
                        .build());
        addArmorShieldStats(MaterialIds.electrum,          PlatingMaterialStats.builder().durabilityFactor(14).armor(1, 3, 4, 2), ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f));
        addMaterialStats(MaterialIds.steeleaf,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(10f)
                        .platingArmor(2f, 7f, 5f, 2f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(10f)
                        .armor(2.0f, 6.5f, 5.5f, 2.0f)
                        .armorStrength(1.0f)
                        .armorToughness(1.0f)
                        .reduction(0.75f)
                        .protection(0.015f)
                        .knockbackResistance(0.025f)
                        .durabilityMultiplier(-0.15f)
                        .armorMultiplier(-0.05f)
                        .armorStrengthMultiplier(0.12f)
                        .armorToughnessMultiplier(0.08f)
                        .build());
        addArmorShieldStats(MaterialIds.polyethylene,      PlatingMaterialStats.builder().durabilityFactor(50).armor(3, 5, 8, 4).toughness(4).knockbackResistance(0.25f), StatlessMaterialStats.SHIELD_CORE, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f));
        addArmorShieldStats(MaterialIds.polyvinylChloride, PlatingMaterialStats.builder().durabilityFactor(60).armor(3, 5, 8, 4).toughness(4).knockbackResistance(0.35f), StatlessMaterialStats.SHIELD_CORE, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f));
        // tier 4
        addMaterialStats(MaterialIds.blazewood, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.blazingBone, StatlessMaterialStats.SHIELD_CORE);
        addMaterialStats(MaterialIds.cinderslime,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(42f)
                        .platingArmor(2f, 7f, 5f, 2f)
                        .platingKnockbackResistance(0.10f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(45.6f)
                        .armor(2.5f, 7.0f, 6.0f, 2.5f)
                        .armorStrength(2.25f)
                        .armorToughness(1.75f)
                        .reduction(0.36f)
                        .protection(0.056f)
                        .knockbackResistance(0.1f)
                        .durabilityMultiplier(0.16f)
                        .armorMultiplier(0.02f)
                        .armorStrengthMultiplier(0.08f)
                        .armorToughnessMultiplier(-0.08f)
                        .smallReductionFactor(0.4f)
                        .smallProtectionFactor(0.75f)
                        .build());
        addMaterialStats(MaterialIds.queensSlime,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(50f)
                        .platingArmor(2f, 7f, 5f, 2f)
                        .platingToughness(1f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(64f)
                        .armor(2.0f, 6.0f, 5.0f, 2.0f)
                        .armorStrength(0.75f)
                        .armorToughness(3.0f)
                        .reduction(0.6f)
                        .protection(0.075f)
                        .durabilityMultiplier(0.35f)
                        .armorMultiplier(0.0f)
                        .armorStrengthMultiplier(-0.1f)
                        .armorToughnessMultiplier(0.2f)
                        .smallReductionFactor(0.75f)
                        .smallProtectionFactor(0.6f)
                        .build());
        addMaterialStats(MaterialIds.hepatizon,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(32f)
                        .platingArmor(2f, 7f, 5f, 2f)
                        .platingToughness(2f)
                        .platingKnockbackResistance(0.10f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(32f)
                        .armor(3.0f, 7.5f, 6.5f, 3.0f)
                        .armorStrength(1.25f)
                        .armorToughness(2.5f)
                        .reduction(0.25f)
                        .protection(0.064f)
                        .knockbackResistance(0.1f)
                        .durabilityMultiplier(0.15f)
                        .armorMultiplier(0.0f)
                        .armorStrengthMultiplier(0.12f)
                        .armorToughnessMultiplier(0.2f)
                        .smallProtectionFactor(0.75f)
                        .build());
        addMaterialStats(MaterialIds.manyullyn,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(35f)
                        .platingArmor(2f, 7f, 5f, 2f)
                        .platingToughness(3f)
                        .platingKnockbackResistance(0.05f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(51.25f)
                        .armor(2.5f, 7.0f, 6.5f, 2.0f)
                        .armorStrength(3.0f)
                        .armorToughness(1.25f)
                        .reduction(0.75f)
                        .protection(0.05f)
                        .knockbackResistance(0.05f)
                        .durabilityMultiplier(0.2f)
                        .armorMultiplier(0.05f)
                        .armorStrengthMultiplier(0.2f)
                        .armorToughnessMultiplier(-0.1f)
                        .smallReductionFactor(0.8f)
                        .smallProtectionFactor(0.8f)
                        .build());
        addMaterialStats(MaterialIds.ancient,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(25f)
                        .platingArmor(2f, 6f, 4f, 2f)
                        .platingKnockbackResistance(0.15f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(28.8f)
                        .armor(2.0f, 6.0f, 4.5f, 1.5f)
                        .reduction(0.1f)
                        .protection(0.08f)
                        .knockbackResistance(0.15f)
                        .durabilityMultiplier(0.1f)
                        .armorMultiplier(0.0f)
                        .armorStrengthMultiplier(0.0f)
                        .armorToughnessMultiplier(0.0f)
                        .build());
        addMaterialStats(MaterialIds.ancientHide, StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.ancientHide, ArmorPartMaterialStats.maille(0.25f, 0.1f, 0.0f, 0.0f));
        // tier 4 (end)
        addMaterialStats(MaterialIds.knightmetal,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(20f)
                        .platingArmor(2f, 7f, 5f, 2f)
                        .platingToughness(2f)
                        .platingKnockbackResistance(0.05f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(22.8f)
                        .armor(3.0f, 8.0f, 6.5f, 2.5f)
                        .armorStrength(1.5f)
                        .armorToughness(2.25f)
                        .reduction(0.5f)
                        .protection(0.08f)
                        .knockbackResistance(0.05f)
                        .durabilityMultiplier(0.0f)
                        .armorMultiplier(0.12f)
                        .armorStrengthMultiplier(0.05f)
                        .armorToughnessMultiplier(0.08f)
                        .smallReductionFactor(0.375f)
                        .smallProtectionFactor(0.375f)
                        .build());
        addMaterialStats(MaterialIds.enderslimeVine, ArmorPartMaterialStats.maille(0f, 0f, 0f, 0f), StatlessMaterialStats.CUIRASS, StatlessMaterialStats.LINEAR);
        addMaterialStats(MaterialIds.dragonScale, ArmorPartMaterialStats.maille(-0.1f, 0.05f, 0.1f, 0.0f));
        addMaterialStats(MaterialIds.shulker, ArmorPartMaterialStats.maille(0.0f, 0.0f, 0.1f, 0.0f));
        // tier 4 (compat)
        addMaterialStats(MaterialIds.fiery,
                ArmorPartStatsBuilder.builder()
                        .platingDurability(25f)
                        .platingArmor(3f, 8f, 6f, 3f)
                        .platingToughness(1.5f)
                        .maille(0f, 0f, 0f, 0f)
                        .partDurability(25f)
                        .armor(3.0f, 8.0f, 6.0f, 3.0f)
                        .armorStrength(2.5f)
                        .armorToughness(1.5f)
                        .reduction(0.9f)
                        .protection(0.028f)
                        .durabilityMultiplier(-0.2f)
                        .armorMultiplier(0.15f)
                        .armorStrengthMultiplier(0.05f)
                        .armorToughnessMultiplier(0.03f)
                        .build());
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


  private void addArmorShieldStats(MaterialId location, PlatingMaterialStats.Builder statBuilder, IMaterialStats... otherStats) {
        PlatingMaterialStats[] plating = new PlatingMaterialStats[4];
        for (ArmorItem.Type slotType : ArmorItem.Type.values()) {
            plating[slotType.ordinal()] = statBuilder.build(slotType);
        }

        addMaterialStats(location, plating);
        if (otherStats.length > 0) {
            addMaterialStats(location, otherStats);
        }
        addMaterialStats(location, statBuilder.buildShield());
    }
}
