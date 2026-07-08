package slimeknights.tconstruct.tools.data.material;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialTraitDataProvider;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.stats.ArmorExtensionMaterialStats;
import slimeknights.tconstruct.tools.stats.PlatingMaterialStats;
import slimeknights.tconstruct.tools.stats.SkullStats;

import static slimeknights.tconstruct.library.materials.MaterialRegistry.*;

public class MaterialTraitsDataProvider extends AbstractMaterialTraitDataProvider {
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
        addTraits(MaterialIds.gold, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(TinkerModifiers.golden.getId(), 2), new ModifierEntry(ModifierIds.magicProtection, 2));
        addTraits(MaterialIds.gold, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(TinkerModifiers.golden.getId(), 2), new ModifierEntry(ModifierIds.magicProtection, 2));
        addTraits(MaterialIds.gold, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(TinkerModifiers.golden.getId(), 2), new ModifierEntry(ModifierIds.magicProtection, 2));
        addTraits(MaterialIds.gold, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(TinkerModifiers.golden.getId(), 2), new ModifierEntry(ModifierIds.magicProtection, 2));
        addTraits(MaterialIds.gold, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(TinkerModifiers.golden.getId(), 2), new ModifierEntry(ModifierIds.magicProtection, 2));
        addTraits(MaterialIds.gold, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(TinkerModifiers.golden.getId(), 2), new ModifierEntry(ModifierIds.magicProtection, 2));
        addTraits(MaterialIds.gold, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(TinkerModifiers.golden.getId(), 2), new ModifierEntry(ModifierIds.magicProtection, 2));
        addTraits(MaterialIds.gold, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(TinkerModifiers.golden.getId(), 2), new ModifierEntry(ModifierIds.magicProtection, 2));
        addTraits(MaterialIds.gold, PlatingMaterialStats.SHIELD.getId(), ModifierIds.magicProtection);

        // tier 2
        addDefaultTraits(MaterialIds.iron, TinkerModifiers.magnetic);
        addTraits(MaterialIds.iron, ARMOR, ModifierIds.projectileProtection);
        addTraits(MaterialIds.iron, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.projectileProtection, 2));
        addTraits(MaterialIds.iron, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.projectileProtection, 2));
        addTraits(MaterialIds.iron, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.projectileProtection, 2));
        addTraits(MaterialIds.iron, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.projectileProtection, 2));
        addTraits(MaterialIds.iron, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.projectileProtection, 2));
        addTraits(MaterialIds.iron, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.projectileProtection, 2));
        addTraits(MaterialIds.iron, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.projectileProtection, 2));
        addTraits(MaterialIds.iron, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.projectileProtection, 2));
        addDefaultTraits(MaterialIds.copper, TinkerModifiers.dwarven);
        addTraits(MaterialIds.copper, ARMOR, ModifierIds.depthProtection);
        addTraits(MaterialIds.copper, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.depthProtection, 2));
        addTraits(MaterialIds.copper, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.depthProtection, 2));
        addTraits(MaterialIds.copper, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.depthProtection, 2));
        addTraits(MaterialIds.copper, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.depthProtection, 2));
        addTraits(MaterialIds.copper, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.depthProtection, 2));
        addTraits(MaterialIds.copper, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.depthProtection, 2));
        addTraits(MaterialIds.copper, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.depthProtection, 2));
        addTraits(MaterialIds.copper, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.depthProtection, 2));
        addDefaultTraits(MaterialIds.searedStone, ModifierIds.searing);
        addTraits(MaterialIds.searedStone, ARMOR, ModifierIds.fireProtection);
        addTraits(MaterialIds.searedStone, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.fireProtection, 2));
        addTraits(MaterialIds.searedStone, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.fireProtection, 2));
        addTraits(MaterialIds.searedStone, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.fireProtection, 2));
        addTraits(MaterialIds.searedStone, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.fireProtection, 2));
        addTraits(MaterialIds.searedStone, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.fireProtection, 2));
        addTraits(MaterialIds.searedStone, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.fireProtection, 2));
        addTraits(MaterialIds.searedStone, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.fireProtection, 2));
        addTraits(MaterialIds.searedStone, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.fireProtection, 2));
        addDefaultTraits(MaterialIds.slimewood, ModifierIds.overgrowth, TinkerModifiers.overslime.getId());
        addTraits(MaterialIds.slimewood, AMMO, new ModifierEntry(ModifierIds.bounce, 2));
        addDefaultTraits(MaterialIds.slimeskin, ModifierIds.overgrowth, TinkerModifiers.overslime.getId());
        addDefaultTraits(MaterialIds.venombone, ModifierIds.antitoxin);
        addTraits(MaterialIds.venombone, AMMO, ModifierIds.venom);
        addTraits(MaterialIds.venombone, ARMOR, ModifierIds.venom);
        addDefaultTraits(MaterialIds.aluminum, ModifierIds.featherweight);
        addTraits(MaterialIds.aluminum, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.featherweight, 2));
        addTraits(MaterialIds.aluminum, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.featherweight, 2));
        addTraits(MaterialIds.aluminum, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.featherweight, 2));
        addTraits(MaterialIds.aluminum, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.featherweight, 2));
        addTraits(MaterialIds.aluminum, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.featherweight, 2));
        addTraits(MaterialIds.aluminum, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.featherweight, 2));
        addTraits(MaterialIds.aluminum, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.featherweight, 2));
        addTraits(MaterialIds.aluminum, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.featherweight, 2));
        // tier 2 - nether
        addDefaultTraits(MaterialIds.necroticBone, TinkerModifiers.necrotic);
        addDefaultTraits(MaterialIds.scorchedStone, ModifierIds.scorching);
        addTraits(MaterialIds.scorchedStone, ARMOR, ModifierIds.scorchProtection);
        addTraits(MaterialIds.scorchedStone, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.scorchProtection, 2));
        addTraits(MaterialIds.scorchedStone, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.scorchProtection, 2));
        addTraits(MaterialIds.scorchedStone, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.scorchProtection, 2));
        addTraits(MaterialIds.scorchedStone, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.scorchProtection, 2));
        addTraits(MaterialIds.scorchedStone, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.scorchProtection, 2));
        addTraits(MaterialIds.scorchedStone, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.scorchProtection, 2));
        addTraits(MaterialIds.scorchedStone, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.scorchProtection, 2));
        addTraits(MaterialIds.scorchedStone, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.scorchProtection, 2));
        // tier 2 - end
        addDefaultTraits(MaterialIds.whitestone, ModifierIds.stoneshield);
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
        addTraits(MaterialIds.slimesteel, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.overcast, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.slimesteel, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.overcast, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.slimesteel, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.overcast, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.slimesteel, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.overcast, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.slimesteel, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.overcast, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.slimesteel, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.overcast, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.slimesteel, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.overcast, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.slimesteel, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.overcast, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.amethystBronze, MELEE_HARVEST, ModifierIds.crumbling);
        addTraits(MaterialIds.amethystBronze, RANGED, ModifierIds.crystalbound);
        addTraits(MaterialIds.amethystBronze, ARMOR, ModifierIds.crystalstrike);
        addTraits(MaterialIds.amethystBronze, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.crystalstrike, 2));
        addTraits(MaterialIds.amethystBronze, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.crystalstrike, 2));
        addTraits(MaterialIds.amethystBronze, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.crystalstrike, 2));
        addTraits(MaterialIds.amethystBronze, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.crystalstrike, 2));
        addTraits(MaterialIds.amethystBronze, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.crystalstrike, 2));
        addTraits(MaterialIds.amethystBronze, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.crystalstrike, 2));
        addTraits(MaterialIds.amethystBronze, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.crystalstrike, 2));
        addTraits(MaterialIds.amethystBronze, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.crystalstrike, 2));
        addDefaultTraits(MaterialIds.nahuatl, TinkerModifiers.lacerating);
        addDefaultTraits(MaterialIds.roseGold, ModifierIds.enhanced);
        addTraits(MaterialIds.roseGold, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.enhanced, 2));
        addTraits(MaterialIds.roseGold, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.enhanced, 2));
        addTraits(MaterialIds.roseGold, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.enhanced, 2));
        addTraits(MaterialIds.roseGold, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.enhanced, 2));
        addTraits(MaterialIds.roseGold, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.enhanced, 2));
        addTraits(MaterialIds.roseGold, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.enhanced, 2));
        addTraits(MaterialIds.roseGold, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.enhanced, 2));
        addTraits(MaterialIds.roseGold, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.enhanced, 2));
        addDefaultTraits(MaterialIds.pigIron, TinkerModifiers.tasty);
        addTraits(MaterialIds.pigIron, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(TinkerModifiers.tasty, 2));
        addTraits(MaterialIds.pigIron, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(TinkerModifiers.tasty, 2));
        addTraits(MaterialIds.pigIron, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(TinkerModifiers.tasty, 2));
        addTraits(MaterialIds.pigIron, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(TinkerModifiers.tasty, 2));
        addTraits(MaterialIds.pigIron, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(TinkerModifiers.tasty, 2));
        addTraits(MaterialIds.pigIron, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(TinkerModifiers.tasty, 2));
        addTraits(MaterialIds.pigIron, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(TinkerModifiers.tasty, 2));
        addTraits(MaterialIds.pigIron, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(TinkerModifiers.tasty, 2));
        addTraits(MaterialIds.obsidian, ARMOR, ModifierIds.blastProtection);
        addTraits(MaterialIds.obsidian, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.blastProtection, 2));
        addTraits(MaterialIds.obsidian, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.blastProtection, 2));
        addTraits(MaterialIds.obsidian, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.blastProtection, 2));
        addTraits(MaterialIds.obsidian, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.blastProtection, 2));
        addTraits(MaterialIds.obsidian, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.blastProtection, 2));
        addTraits(MaterialIds.obsidian, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.blastProtection, 2));
        addTraits(MaterialIds.obsidian, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.blastProtection, 2));
        addTraits(MaterialIds.obsidian, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.blastProtection, 2));
        // tier 3 - nether
        addDefaultTraits(MaterialIds.cobalt, ModifierIds.lightweight);
        addTraits(MaterialIds.cobalt, ARMOR, ModifierIds.meleeProtection);
        addTraits(MaterialIds.cobalt, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.meleeProtection, 2));
        addTraits(MaterialIds.cobalt, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.meleeProtection, 2));
        addTraits(MaterialIds.cobalt, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.meleeProtection, 2));
        addTraits(MaterialIds.cobalt, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.meleeProtection, 2));
        addTraits(MaterialIds.cobalt, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.meleeProtection, 2));
        addTraits(MaterialIds.cobalt, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.meleeProtection, 2));
        addTraits(MaterialIds.cobalt, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.meleeProtection, 2));
        addTraits(MaterialIds.cobalt, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.meleeProtection, 2));
        addDefaultTraits(MaterialIds.steel, ModifierIds.ductile);
        addTraits(MaterialIds.steel, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.ductile, 2));
        addTraits(MaterialIds.steel, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.ductile, 2));
        addTraits(MaterialIds.steel, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.ductile, 2));
        addTraits(MaterialIds.steel, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.ductile, 2));
        addTraits(MaterialIds.steel, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.ductile, 2));
        addTraits(MaterialIds.steel, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.ductile, 2));
        addTraits(MaterialIds.steel, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.ductile, 2));
        addTraits(MaterialIds.steel, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.ductile, 2));
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
        addTraits(MaterialIds.cinderslime, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.overburn, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.cinderslime, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.overburn, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.cinderslime, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.overburn, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.cinderslime, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.overburn, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.cinderslime, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.overburn, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.cinderslime, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.overburn, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.cinderslime, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.overburn, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.cinderslime, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.overburn, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addDefaultTraits(MaterialIds.queensSlime, ModifierIds.overlord, TinkerModifiers.overslime.getId());
        addTraits(MaterialIds.queensSlime, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.overlord, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.queensSlime, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.overlord, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.queensSlime, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.overlord, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.queensSlime, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.overlord, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.queensSlime, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.overlord, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.queensSlime, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.overlord, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.queensSlime, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.overlord, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addTraits(MaterialIds.queensSlime, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.overlord, 2), new ModifierEntry(TinkerModifiers.overslime.getId(), 2));
        addDefaultTraits(MaterialIds.hepatizon, TinkerModifiers.momentum);
        addTraits(MaterialIds.hepatizon, ARMOR, ModifierIds.recurrentProtection);
        addTraits(MaterialIds.hepatizon, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.recurrentProtection, 2));
        addTraits(MaterialIds.hepatizon, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.recurrentProtection, 2));
        addTraits(MaterialIds.hepatizon, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.recurrentProtection, 2));
        addTraits(MaterialIds.hepatizon, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.recurrentProtection, 2));
        addTraits(MaterialIds.hepatizon, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.recurrentProtection, 2));
        addTraits(MaterialIds.hepatizon, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.recurrentProtection, 2));
        addTraits(MaterialIds.hepatizon, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.recurrentProtection, 2));
        addTraits(MaterialIds.hepatizon, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.recurrentProtection, 2));
        addDefaultTraits(MaterialIds.manyullyn, TinkerModifiers.insatiable);
        addTraits(MaterialIds.manyullyn, ARMOR, ModifierIds.kinetic);
        addTraits(MaterialIds.manyullyn, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.kinetic, 2));
        addTraits(MaterialIds.manyullyn, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.kinetic, 2));
        addTraits(MaterialIds.manyullyn, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.kinetic, 2));
        addTraits(MaterialIds.manyullyn, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.kinetic, 2));
        addTraits(MaterialIds.manyullyn, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.kinetic, 2));
        addTraits(MaterialIds.manyullyn, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.kinetic, 2));
        addTraits(MaterialIds.manyullyn, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.kinetic, 2));
        addTraits(MaterialIds.manyullyn, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.kinetic, 2));
        addDefaultTraits(MaterialIds.blazingBone, TinkerModifiers.conducting);
        addTraits(MaterialIds.blazingBone, AMMO, ModifierIds.conductive);
        addTraits(MaterialIds.blazingBone, ARMOR, ModifierIds.conductive);
        addDefaultTraits(MaterialIds.blazewood, ModifierIds.spectral);
        addDefaultTraits(MaterialIds.ancient, ModifierIds.vintage, ModifierIds.worldbound);
        addTraits(MaterialIds.ancient, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.vintage, 2), new ModifierEntry(ModifierIds.worldbound, 2));
        addTraits(MaterialIds.ancient, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.vintage, 2), new ModifierEntry(ModifierIds.worldbound, 2));
        addTraits(MaterialIds.ancient, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.vintage, 2), new ModifierEntry(ModifierIds.worldbound, 2));
        addTraits(MaterialIds.ancient, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.vintage, 2), new ModifierEntry(ModifierIds.worldbound, 2));
        addTraits(MaterialIds.ancient, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.vintage, 2), new ModifierEntry(ModifierIds.worldbound, 2));
        addTraits(MaterialIds.ancient, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.vintage, 2), new ModifierEntry(ModifierIds.worldbound, 2));
        addTraits(MaterialIds.ancient, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.vintage, 2), new ModifierEntry(ModifierIds.worldbound, 2));
        addTraits(MaterialIds.ancient, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.vintage, 2), new ModifierEntry(ModifierIds.worldbound, 2));
        // tier 4 - binding
        addTraits(MaterialIds.ancientHide, MELEE_HARVEST, ModifierIds.fortune);
        addDefaultTraits(MaterialIds.ancientHide, ModifierIds.fortified);
        addTraits(MaterialIds.dragonScale, ARMOR, ModifierIds.dragonborn);
        addTraits(MaterialIds.dragonScale, AMMO, ModifierIds.dragonshot);
        addTraits(MaterialIds.shulker, ARMOR, ModifierIds.shulking);
        addTraits(MaterialIds.shulker, AMMO, ModifierIds.reclaim);
        // tier 4 - ammo
        addTraits(MaterialIds.enderslime, AMMO, ModifierIds.enderclearance);

        // tier 4 (end)
        addDefaultTraits(MaterialIds.knightmetal, ModifierIds.valiant);
        addTraits(MaterialIds.knightmetal, ARMOR, ModifierIds.stalwart);
        addTraits(MaterialIds.knightmetal, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.stalwart, 2));
        addTraits(MaterialIds.knightmetal, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.stalwart, 2));
        addTraits(MaterialIds.knightmetal, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.stalwart, 2));
        addTraits(MaterialIds.knightmetal, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.stalwart, 2));
        addTraits(MaterialIds.knightmetal, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.stalwart, 2));
        addTraits(MaterialIds.knightmetal, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.stalwart, 2));
        addTraits(MaterialIds.knightmetal, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.stalwart, 2));
        addTraits(MaterialIds.knightmetal, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.stalwart, 2));
        addDefaultTraits(MaterialIds.knightly, ModifierIds.valiant);
        addDefaultTraits(MaterialIds.enderslimeVine, TinkerModifiers.enderporting);
        addTraits(MaterialIds.enderslimeVine, ARMOR, ModifierIds.enderclearance);
        addDefaultTraits(MaterialIds.endRod, ModifierIds.hover);

        // tier 2 - mod compat
        addDefaultTraits(MaterialIds.osmium, ModifierIds.dense);
        addTraits(MaterialIds.osmium, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.dense, 2));
        addTraits(MaterialIds.osmium, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.dense, 2));
        addTraits(MaterialIds.osmium, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.dense, 2));
        addTraits(MaterialIds.osmium, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.dense, 2));
        addTraits(MaterialIds.osmium, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.dense, 2));
        addTraits(MaterialIds.osmium, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.dense, 2));
        addTraits(MaterialIds.osmium, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.dense, 2));
        addTraits(MaterialIds.osmium, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.dense, 2));
        addDefaultTraits(MaterialIds.lead, ModifierIds.heavy);
        addTraits(MaterialIds.lead, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.heavy, 2));
        addTraits(MaterialIds.lead, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.heavy, 2));
        addTraits(MaterialIds.lead, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.heavy, 2));
        addTraits(MaterialIds.lead, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.heavy, 2));
        addTraits(MaterialIds.lead, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.heavy, 2));
        addTraits(MaterialIds.lead, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.heavy, 2));
        addTraits(MaterialIds.lead, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.heavy, 2));
        addTraits(MaterialIds.lead, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.heavy, 2));
        addTraits(MaterialIds.silver, MELEE_HARVEST, ModifierIds.smite);
        addTraits(MaterialIds.silver, RANGED, ModifierIds.holy);
        addTraits(MaterialIds.silver, ARMOR, ModifierIds.consecrated);
        addTraits(MaterialIds.silver, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.consecrated, 2));
        addTraits(MaterialIds.silver, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.consecrated, 2));
        addTraits(MaterialIds.silver, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.consecrated, 2));
        addTraits(MaterialIds.silver, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.consecrated, 2));
        addTraits(MaterialIds.silver, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.consecrated, 2));
        addTraits(MaterialIds.silver, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.consecrated, 2));
        addTraits(MaterialIds.silver, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.consecrated, 2));
        addTraits(MaterialIds.silver, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.consecrated, 2));
        addDefaultTraits(MaterialIds.treatedWood, ModifierIds.preserved);
        addDefaultTraits(MaterialIds.ironwood, ModifierIds.deciduous);
        addDefaultTraits(MaterialIds.polyethylene, ModifierIds.plastic);
        addTraits(MaterialIds.polyethylene, ARMOR, ModifierIds.plastic, ModifierIds.insulation);
        // tier 3 - mod compat
        addDefaultTraits(MaterialIds.bronze, ModifierIds.maintained);
        addTraits(MaterialIds.bronze, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.maintained, 2));
        addTraits(MaterialIds.bronze, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.maintained, 2));
        addTraits(MaterialIds.bronze, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.maintained, 2));
        addTraits(MaterialIds.bronze, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.maintained, 2));
        addTraits(MaterialIds.bronze, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.maintained, 2));
        addTraits(MaterialIds.bronze, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.maintained, 2));
        addTraits(MaterialIds.bronze, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.maintained, 2));
        addTraits(MaterialIds.bronze, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.maintained, 2));
        addDefaultTraits(MaterialIds.constantan, ModifierIds.temperate);
        addTraits(MaterialIds.constantan, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.temperate, 2));
        addTraits(MaterialIds.constantan, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.temperate, 2));
        addTraits(MaterialIds.constantan, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.temperate, 2));
        addTraits(MaterialIds.constantan, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.temperate, 2));
        addTraits(MaterialIds.constantan, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.temperate, 2));
        addTraits(MaterialIds.constantan, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.temperate, 2));
        addTraits(MaterialIds.constantan, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.temperate, 2));
        addTraits(MaterialIds.constantan, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.temperate, 2));
        addDefaultTraits(MaterialIds.invar, ModifierIds.solid);
        addTraits(MaterialIds.invar, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.solid, 2));
        addTraits(MaterialIds.invar, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.solid, 2));
        addTraits(MaterialIds.invar, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.solid, 2));
        addTraits(MaterialIds.invar, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.solid, 2));
        addTraits(MaterialIds.invar, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.solid, 2));
        addTraits(MaterialIds.invar, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.solid, 2));
        addTraits(MaterialIds.invar, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.solid, 2));
        addTraits(MaterialIds.invar, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.solid, 2));
        addDefaultTraits(MaterialIds.pewter, ModifierIds.raging);
        addTraits(MaterialIds.pewter, ARMOR, ModifierIds.vitalProtection);
        addTraits(MaterialIds.pewter, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.vitalProtection, 2));
        addTraits(MaterialIds.pewter, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.vitalProtection, 2));
        addTraits(MaterialIds.pewter, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.vitalProtection, 2));
        addTraits(MaterialIds.pewter, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.vitalProtection, 2));
        addTraits(MaterialIds.pewter, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.vitalProtection, 2));
        addTraits(MaterialIds.pewter, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.vitalProtection, 2));
        addTraits(MaterialIds.pewter, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.vitalProtection, 2));
        addTraits(MaterialIds.pewter, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.vitalProtection, 2));
        addDefaultTraits(MaterialIds.necronium, TinkerModifiers.decay);
        addTraits(MaterialIds.necronium, AMMO, new ModifierEntry(TinkerModifiers.decay, 2));
        addDefaultTraits(MaterialIds.electrum, ModifierIds.shock);
        addDefaultTraits(MaterialIds.platedSlimewood, TinkerModifiers.overworked, TinkerModifiers.overslime);
        addDefaultTraits(MaterialIds.steeleaf, ModifierIds.experienced);
        addTraits(MaterialIds.steeleaf, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.experienced, 2));
        addTraits(MaterialIds.steeleaf, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.experienced, 2));
        addTraits(MaterialIds.steeleaf, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.experienced, 2));
        addTraits(MaterialIds.steeleaf, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.experienced, 2));
        addTraits(MaterialIds.steeleaf, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.experienced, 2));
        addTraits(MaterialIds.steeleaf, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.experienced, 2));
        addTraits(MaterialIds.steeleaf, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.experienced, 2));
        addTraits(MaterialIds.steeleaf, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.experienced, 2));
        addTraits(MaterialIds.steeleaf, AMMO, ModifierIds.looting);
        addDefaultTraits(MaterialIds.polyvinylChloride, ModifierIds.thermaldecomposite);
        addTraits(MaterialIds.polyvinylChloride, ARMOR, ModifierIds.thermaldecomposite, ModifierIds.insulation);
        // tier 4 - mod compat
        addDefaultTraits(MaterialIds.fiery, TinkerModifiers.autosmelt);
        addTraits(MaterialIds.fiery, ARMOR, ModifierIds.temperedProtection);
        addTraits(MaterialIds.fiery, ArmorExtensionMaterialStats.CAST_HELMET.getId(), new ModifierEntry(ModifierIds.temperedProtection, 2));
        addTraits(MaterialIds.fiery, ArmorExtensionMaterialStats.CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.temperedProtection, 2));
        addTraits(MaterialIds.fiery, ArmorExtensionMaterialStats.CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.temperedProtection, 2));
        addTraits(MaterialIds.fiery, ArmorExtensionMaterialStats.CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.temperedProtection, 2));
        addTraits(MaterialIds.fiery, ArmorExtensionMaterialStats.MASSIVE_CAST_HELMET.getId(), new ModifierEntry(ModifierIds.temperedProtection, 2));
        addTraits(MaterialIds.fiery, ArmorExtensionMaterialStats.MASSIVE_CAST_CHESTPLATE.getId(), new ModifierEntry(ModifierIds.temperedProtection, 2));
        addTraits(MaterialIds.fiery, ArmorExtensionMaterialStats.MASSIVE_CAST_LEGGINGS.getId(), new ModifierEntry(ModifierIds.temperedProtection, 2));
        addTraits(MaterialIds.fiery, ArmorExtensionMaterialStats.MASSIVE_CAST_BOOTS.getId(), new ModifierEntry(ModifierIds.temperedProtection, 2));

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
    }

}
