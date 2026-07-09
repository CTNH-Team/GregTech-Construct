package slimeknights.tconstruct.tools.data.material;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialTraitDataProvider;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.stats.ArmorExtensionMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.SkullStats;

import static slimeknights.tconstruct.library.materials.MaterialRegistry.*;

public class MaterialTraitsDataProvider extends AbstractMaterialTraitDataProvider {
    private static final MaterialStatsId[] ARMOR_CASTS = {
      ArmorExtensionMaterialStats.CAST_HELMET.getId(),
      ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(),
      ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(),
      ArmorExtensionMaterialStats.CAST_BOOTS.getId(),
      ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(),
      ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(),
      ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(),
      ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId()
    };

    public MaterialTraitsDataProvider(PackOutput packOutput, AbstractMaterialDataProvider materials) {
        super(packOutput, materials);
    }

    @Override
    public String getName() {
        return "Tinker's Construct Material Traits";
    }

    @Override
    protected void addMaterialTraits() {
        // tier 1
        addDefaultTraits(MaterialIds.wood, ModifierIds.cultivated);
        addTraits(MaterialIds.wood, AMMO, ModifierIds.economical);
        addDefaultTraits(MaterialIds.rock, ModifierIds.stonebound);
        addDefaultTraits(MaterialIds.flint, ModifierIds.jagged);
        addTraits(MaterialIds.flint, AMMO, ModifierIds.tipped);
        addDefaultTraits(MaterialIds.bone, ModifierIds.pierce);
        addTraits(MaterialIds.bone, AMMO, ModifierIds.spike);
        addDefaultTraits(MaterialIds.bamboo, ModifierIds.unburdened);
        addTraits(MaterialIds.bamboo, AMMO, ModifierIds.woodwind);
        addDefaultTraits(MaterialIds.cactus, ModifierIds.spiny);
        addTraits(MaterialIds.cactus, ARMOR, ModifierIds.thorns);
        addTraits(MaterialIds.wool, ARMOR, ModifierIds.cushion);
        addTraits(MaterialIds.wool, AMMO, ModifierIds.soft);
        noTraits(MaterialIds.feather);
        addTraits(MaterialIds.paper, AMMO, ModifierIds.weak);
        addTraits(MaterialIds.leaves, AMMO, ModifierIds.cheap);
        // tier 1 - end
        addDefaultTraits(MaterialIds.chorus, TinkerModifiers.enderference);
        addTraits(MaterialIds.chorus, ARMOR, ModifierIds.enderclearance);
        // tier 1 - binding
        addDefaultTraits(MaterialIds.string, ModifierIds.stringy);
        addDefaultTraits(MaterialIds.leather, TinkerModifiers.tanned);
        addDefaultTraits(MaterialIds.vine, TinkerModifiers.solarPowered);
        addTraits(MaterialIds.gold, ARMOR, TinkerModifiers.golden.getId(), ModifierIds.magicProtection);
        addTraits(MaterialIds.gold, PlatingMaterialStats.SHIELD.getId(), ModifierIds.magicProtection);

        // tier 2
        addDefaultTraits(MaterialIds.iron, TinkerModifiers.magnetic);
        addTraits(MaterialIds.iron, ARMOR, ModifierIds.projectileProtection);
        addDefaultTraits(MaterialIds.copper, TinkerModifiers.dwarven);
        addTraits(MaterialIds.copper, ARMOR, ModifierIds.depthProtection);
        addDefaultTraits(MaterialIds.searedStone, ModifierIds.searing);
        addTraits(MaterialIds.searedStone, ARMOR, ModifierIds.fireProtection);
        addDefaultTraits(MaterialIds.slimewood, ModifierIds.overgrowth, TinkerModifiers.overslime.getId());
        addTraits(MaterialIds.slimewood, AMMO, new ModifierEntry(ModifierIds.bounce, 2));
        addDefaultTraits(MaterialIds.slimeskin, ModifierIds.overgrowth, TinkerModifiers.overslime.getId());
        addDefaultTraits(MaterialIds.venombone, ModifierIds.antitoxin);
        addTraits(MaterialIds.venombone, AMMO, ModifierIds.venom);
        addTraits(MaterialIds.venombone, ARMOR, ModifierIds.venom);
        addDefaultTraits(MaterialIds.aluminum, ModifierIds.featherweight);
        // tier 2 - nether
        addDefaultTraits(MaterialIds.necroticBone, TinkerModifiers.necrotic);
        addDefaultTraits(MaterialIds.scorchedStone, ModifierIds.scorching);
        addTraits(MaterialIds.scorchedStone, ARMOR, ModifierIds.scorchProtection);
        // tier 2 - end
        addDefaultTraits(MaterialIds.whitestone, ModifierIds.stoneshield);
        addTraits(MaterialIds.whitestone, ARMOR, ModifierIds.malleability);
        // tier 2 - binding
        addDefaultTraits(MaterialIds.skyslimeVine, ModifierIds.airborne);
        addTraits(MaterialIds.skyslimeVine, ARMOR, ModifierIds.skyfall);
        addDefaultTraits(MaterialIds.weepingVine, ModifierIds.flamestance);
        addDefaultTraits(MaterialIds.twistingVine, ModifierIds.entangled);
        // tier 2 - ammo
        addTraits(MaterialIds.amethyst, AMMO, ModifierIds.crystalbound);
        addTraits(MaterialIds.prismarine, AMMO, ModifierIds.finsAmmo, ModifierIds.lureRod);
        addTraits(MaterialIds.earthslime, AMMO, ModifierIds.drawback);
        addTraits(MaterialIds.skyslime, AMMO, ModifierIds.punch);
        addDefaultTraits(MaterialIds.blaze, ModifierIds.fiery);
        addTraits(MaterialIds.enderPearl, AMMO, TinkerModifiers.enderporting);
        addTraits(MaterialIds.glass, AMMO, ModifierIds.amorphous, ModifierIds.smashingAmmo, ModifierIds.spillingRod);
        addTraits(MaterialIds.slimeball, AMMO, ModifierIds.erratic);
        addTraits(MaterialIds.gunpowder, AMMO, ModifierIds.explosive);

        // tier 3
        addDefaultTraits(MaterialIds.slimesteel, ModifierIds.overcast, TinkerModifiers.overslime.getId());
        addTraits(MaterialIds.amethystBronze, MELEE_HARVEST, ModifierIds.crumbling);
        addTraits(MaterialIds.amethystBronze, RANGED, ModifierIds.crystalbound);
        addTraits(MaterialIds.amethystBronze, ARMOR, ModifierIds.crystalstrike, ModifierIds.crystalLattice, ModifierIds.crystalizing, ModifierIds.crystalSolidity);
        addDefaultTraits(MaterialIds.nahuatl, TinkerModifiers.lacerating);
        addDefaultTraits(MaterialIds.roseGold, ModifierIds.enhanced);
        addDefaultTraits(MaterialIds.pigIron, TinkerModifiers.tasty);
        addTraits(MaterialIds.obsidian, ARMOR, ModifierIds.blastProtection);
        // tier 3 - nether
        addDefaultTraits(MaterialIds.cobalt, ModifierIds.lightweight);
        addTraits(MaterialIds.cobalt, ARMOR, ModifierIds.meleeProtection);
        addDefaultTraits(MaterialIds.steel, ModifierIds.ductile);
        // tier 3 - binding
        addDefaultTraits(MaterialIds.darkthread, ModifierIds.looting);
        addDefaultTraits(MaterialIds.ichorskin, ModifierIds.overshield, TinkerModifiers.overslime.getId());
        addDefaultTraits(MaterialIds.ice, ModifierIds.frostshield);
        addTraits(MaterialIds.ice, AMMO, ModifierIds.freezing);
        // tier 3 - ammo
        addTraits(MaterialIds.quartz, AMMO, ModifierIds.keen);
        addTraits(MaterialIds.ichor, AMMO, ModifierIds.rebound, ModifierIds.bounce);
        addTraits(MaterialIds.glowstone, AMMO, ModifierIds.spectral);
        addDefaultTraits(MaterialIds.magnetite, ModifierIds.attractive);
        addTraits(MaterialIds.magma, AMMO, ModifierIds.fuse);

        // tier 4
        addDefaultTraits(MaterialIds.cinderslime, ModifierIds.overburn, TinkerModifiers.overslime.getId());
        addDefaultTraits(MaterialIds.queensSlime, ModifierIds.overlord, TinkerModifiers.overslime.getId());
        addDefaultTraits(MaterialIds.hepatizon, TinkerModifiers.momentum);
        addTraits(MaterialIds.hepatizon, ARMOR, ModifierIds.recurrentProtection, ModifierIds.recurrence);
        addDefaultTraits(MaterialIds.manyullyn, TinkerModifiers.insatiable);
        addTraits(MaterialIds.manyullyn, ARMOR, ModifierIds.kinetic);
        addDefaultTraits(MaterialIds.blazingBone, TinkerModifiers.conducting);
        addTraits(MaterialIds.blazingBone, AMMO, ModifierIds.conductive);
        addTraits(MaterialIds.blazingBone, ARMOR, ModifierIds.conductive);
        addDefaultTraits(MaterialIds.blazewood, ModifierIds.spectral);
        addDefaultTraits(MaterialIds.ancient, ModifierIds.vintage, ModifierIds.worldbound);
        // tier 4 - binding
        addTraits(MaterialIds.ancientHide, MELEE_HARVEST, ModifierIds.fortune);
        addDefaultTraits(MaterialIds.ancientHide, ModifierIds.fortified);
        addTraits(MaterialIds.ancientHide, ARMOR, ModifierIds.totem);
        addTraits(MaterialIds.dragonScale, ARMOR, ModifierIds.dragonborn);
        addTraits(MaterialIds.dragonScale, AMMO, ModifierIds.dragonshot);
        addTraits(MaterialIds.shulker, ARMOR, ModifierIds.shulking);
        addTraits(MaterialIds.shulker, AMMO, ModifierIds.reclaim);
        // tier 4 - ammo
        addTraits(MaterialIds.enderslime, AMMO, ModifierIds.enderclearance);

        // tier 4 (end)
        addDefaultTraits(MaterialIds.knightmetal, ModifierIds.valiant);
        addTraits(MaterialIds.knightmetal, ARMOR, ModifierIds.stalwart, ModifierIds.guarding, ModifierIds.plating, ModifierIds.hardening);
        addDefaultTraits(MaterialIds.knightly, ModifierIds.valiant);
        addDefaultTraits(MaterialIds.enderslimeVine, TinkerModifiers.enderporting);
        addTraits(MaterialIds.enderslimeVine, ARMOR, ModifierIds.enderclearance);
        addDefaultTraits(MaterialIds.endRod, ModifierIds.hover);

        // tier 2 - mod compat
        addDefaultTraits(MaterialIds.osmium, ModifierIds.dense);
        addDefaultTraits(MaterialIds.lead, ModifierIds.heavy);
        addTraits(MaterialIds.silver, MELEE_HARVEST, ModifierIds.smite);
        addTraits(MaterialIds.silver, RANGED, ModifierIds.holy);
        addTraits(MaterialIds.silver, ARMOR, ModifierIds.consecrated);
        addDefaultTraits(MaterialIds.treatedWood, ModifierIds.preserved);
        addDefaultTraits(MaterialIds.ironwood, ModifierIds.deciduous);
        addDefaultTraits(MaterialIds.polyethylene, ModifierIds.plastic);
        addTraits(MaterialIds.polyethylene, ARMOR, ModifierIds.plastic, ModifierIds.insulation);
        // tier 3 - mod compat
        addDefaultTraits(MaterialIds.bronze, ModifierIds.maintained);
        addDefaultTraits(MaterialIds.constantan, ModifierIds.temperate);
        addDefaultTraits(MaterialIds.invar, ModifierIds.solid);
        addDefaultTraits(MaterialIds.pewter, ModifierIds.raging);
        addTraits(MaterialIds.pewter, ARMOR, ModifierIds.vitalProtection);
        addDefaultTraits(MaterialIds.necronium, TinkerModifiers.decay);
        addTraits(MaterialIds.necronium, AMMO, new ModifierEntry(TinkerModifiers.decay, 2));
        addDefaultTraits(MaterialIds.electrum, ModifierIds.shock);
        addDefaultTraits(MaterialIds.platedSlimewood, TinkerModifiers.overworked, TinkerModifiers.overslime);
        addDefaultTraits(MaterialIds.steeleaf, ModifierIds.experienced);
        addTraits(MaterialIds.steeleaf, AMMO, ModifierIds.looting);
        addDefaultTraits(MaterialIds.polyvinylChloride, ModifierIds.thermaldecomposite);
        addTraits(MaterialIds.polyvinylChloride, ARMOR, ModifierIds.thermaldecomposite, ModifierIds.insulation);
        // tier 4 - mod compat
        addDefaultTraits(MaterialIds.fiery, TinkerModifiers.autosmelt);
        addTraits(MaterialIds.fiery, ARMOR, ModifierIds.temperedProtection);
        addTraits(new MaterialId("tinkersinnovation", "zinc"), ARMOR, ModifierIds.malleability);

        // slimeskull
        addTraits(MaterialIds.glass,        SkullStats.ID, TinkerModifiers.selfDestructive.getId(), ModifierIds.creeperDisguise);
        addTraits(MaterialIds.enderPearl,   SkullStats.ID, TinkerModifiers.enderdodging.getId(), ModifierIds.endermanDisguise);
        addTraits(MaterialIds.bone,         SkullStats.ID, TinkerModifiers.strongBones.getId(), ModifierIds.skeletonDisguise);
        addTraits(MaterialIds.venombone,    SkullStats.ID, TinkerModifiers.frosttouch.getId(), ModifierIds.strayDisguise);
        addTraits(MaterialIds.necroticBone, SkullStats.ID, TinkerModifiers.withered.getId(), ModifierIds.witherSkeletonDisguise);
        addTraits(MaterialIds.string,       SkullStats.ID, TinkerModifiers.boonOfSssss.getId(), ModifierIds.spiderDisguise);
        addTraits(MaterialIds.darkthread,   SkullStats.ID, ModifierIds.mithridatism, ModifierIds.caveSpiderDisguise);
        addTraits(MaterialIds.leather,      SkullStats.ID, TinkerModifiers.wildfire.getId(), ModifierIds.zombieDisguise);
        addTraits(MaterialIds.iron,         SkullStats.ID, TinkerModifiers.plague.getId(), ModifierIds.huskDisguise);
        addTraits(MaterialIds.copper,       SkullStats.ID, TinkerModifiers.breathtaking.getId(), ModifierIds.drownedDisguise);
        // TODO 1.21: use MaterialIds.blazeRod instead
        addTraits(MaterialIds.blazingBone,  SkullStats.ID, TinkerModifiers.firebreath.getId(), ModifierIds.blazeDisguise);
        addTraits(MaterialIds.gold,         SkullStats.ID, TinkerModifiers.chrysophilite.getId(), ModifierIds.piglinDisguise, TinkerModifiers.golden.getId());
        addTraits(MaterialIds.roseGold,     SkullStats.ID, TinkerModifiers.goldGuard.getId(), ModifierIds.piglinBruteDisguise, TinkerModifiers.golden.getId());
        addTraits(MaterialIds.pigIron,      SkullStats.ID, TinkerModifiers.revenge.getId(), ModifierIds.zombifiedPiglinDisguise);
        // slimesuit
        noTraits(MaterialIds.blood);
        noTraits(MaterialIds.clay);
        noTraits(MaterialIds.honey);
        noTraits(MaterialIds.phantom);

        copyArmorTraitsToCasts(MaterialIds.aluminum);
        copyArmorTraitsToCasts(MaterialIds.amethystBronze);
        copyArmorTraitsToCasts(MaterialIds.ancient);
        copyArmorTraitsToCasts(MaterialIds.bronze);
        copyArmorTraitsToCasts(MaterialIds.cinderslime);
        copyArmorTraitsToCasts(MaterialIds.cobalt);
        copyArmorTraitsToCasts(MaterialIds.constantan);
        copyArmorTraitsToCasts(MaterialIds.copper);
        copyArmorTraitsToCasts(MaterialIds.fiery);
        copyArmorTraitsToCasts(MaterialIds.gold);
        copyArmorTraitsToCasts(MaterialIds.hepatizon);
        copyArmorTraitsToCasts(MaterialIds.invar);
        copyArmorTraitsToCasts(MaterialIds.iron);
        copyArmorTraitsToCasts(MaterialIds.knightmetal);
        copyArmorTraitsToCasts(MaterialIds.lead);
        copyArmorTraitsToCasts(MaterialIds.manyullyn);
        copyArmorTraitsToCasts(MaterialIds.obsidian);
        copyArmorTraitsToCasts(MaterialIds.osmium);
        copyArmorTraitsToCasts(MaterialIds.pewter);
        copyArmorTraitsToCasts(MaterialIds.pigIron);
        copyArmorTraitsToCasts(MaterialIds.queensSlime);
        copyArmorTraitsToCasts(MaterialIds.roseGold);
        copyArmorTraitsToCasts(MaterialIds.scorchedStone);
        copyArmorTraitsToCasts(MaterialIds.searedStone);
        copyArmorTraitsToCasts(MaterialIds.silver);
        copyArmorTraitsToCasts(MaterialIds.slimesteel);
        copyArmorTraitsToCasts(MaterialIds.steel);
        copyArmorTraitsToCasts(MaterialIds.steeleaf);

        overrideTraits(MaterialIds.wool, ARMOR, ModifierIds.cushion);
        overrideTraits(MaterialIds.leather, ARMOR, TinkerModifiers.tanned.getId());
        overrideTraits(MaterialIds.knightmetal, ARMOR, ModifierIds.guarding, ModifierIds.plating, ModifierIds.hardening);
        overrideTraits(MaterialIds.amethystBronze, ARMOR, ModifierIds.crystalLattice, ModifierIds.crystalizing, ModifierIds.crystalSolidity);
        overrideTraits(MaterialIds.ancientHide, ARMOR, ModifierIds.totem);
        overrideTraits(MaterialIds.hepatizon, ARMOR, ModifierIds.recurrence);
    }

    private void copyArmorTraitsToCasts(MaterialId material) {
        var traits = material(material);
        for (MaterialStatsId cast : ARMOR_CASTS) {
            traits.copyTraits(ARMOR, cast, 2, true);
        }
    }

}
