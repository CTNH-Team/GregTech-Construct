package slimeknights.tconstruct.tools.data;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.data.tinkering.AbstractStationSlotLayoutProvider;
import slimeknights.tconstruct.library.tools.layout.Patterns;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.function.Consumer;

public class StationSlotLayoutProvider extends AbstractStationSlotLayoutProvider {
    public StationSlotLayoutProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void addLayouts() {
        // stations
        Ingredient modifiable = Ingredient.of(TinkerTags.Items.MODIFIABLE);
        define(TinkerTables.tinkerStation)
                .translationKey(TConstruct.makeTranslationKey("gui", "tinker_station.repair_limited"))
                .icon(Patterns.REPAIR)
                .toolSlot(53, 41, modifiable)
                .addInputSlot(Patterns.QUARTZ, 11, 41)
                .addInputSlot(Patterns.DUST,   31, 30)
                .addInputSlot(Patterns.LAPIS,  31, 50)
                .build();
        Consumer<ItemLike> addAnvil = item ->
                define(item)
                        .translationKey(TConstruct.makeTranslationKey("gui", "tinker_station.repair"))
                        .icon(Patterns.REPAIR)
                        .toolSlot(33, 41, modifiable)
                        .addInputSlot(Patterns.QUARTZ, 15, 62)
                        .addInputSlot(Patterns.DUST, 11, 37)
                        .addInputSlot(Patterns.LAPIS, 33, 19)
                        .addInputSlot(Patterns.INGOT, 55, 37)
                        .addInputSlot(Patterns.GEM, 51, 62)
                        .build();
        addAnvil.accept(TinkerTables.tinkersAnvil);
        addAnvil.accept(TinkerTables.scorchedAnvil);

        // tools
        // pickaxes
        defineModifiable(TinkerTools.pickaxe)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.pickHead,    53, 22)
                .addInputItem(TinkerToolParts.toolHandle,  15, 60)
                .addInputItem(TinkerToolParts.toolBinding, 33, 42)
                .build();
        defineModifiable(TinkerTools.sledgeHammer)
                .sortIndex(SORT_HARVEST + SORT_LARGE)
                .addInputItem(TinkerToolParts.hammerHead,  44, 29)
                .addInputItem(TinkerToolParts.toughHandle, 21, 52)
                .addInputItem(TinkerToolParts.largePlate,  50, 48)
                .addInputItem(TinkerToolParts.largePlate,  25, 20)
                .build();
        defineModifiable(TinkerTools.veinHammer)
                .sortIndex(SORT_HARVEST + SORT_LARGE)
                .addInputItem(TinkerToolParts.hammerHead,   44, 29)
                .addInputItem(TinkerToolParts.toughHandle,  21, 52)
                .addInputItem(TinkerToolParts.toughBinding, 41, 49)
                .addInputItem(TinkerToolParts.largePlate,   25, 20)
                .build();

        // shovels
        defineModifiable(TinkerTools.mattock)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.smallAxeHead, 31, 22)
                .addInputItem(TinkerToolParts.toolHandle,   22, 53)
                .addInputItem(TinkerToolParts.adzeHead,     51, 34)
                .build();
        defineModifiable(TinkerTools.pickadze)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.pickHead,   31, 22)
                .addInputItem(TinkerToolParts.toolHandle, 22, 53)
                .addInputItem(TinkerToolParts.adzeHead,   51, 34)
                .build();
        defineModifiable(TinkerTools.excavator)
                .sortIndex(SORT_HARVEST + SORT_LARGE)
                .addInputItem(TinkerToolParts.largePlate,   45, 26)
                .addInputItem(TinkerToolParts.toughHandle,  25, 46)
                .addInputItem(TinkerToolParts.toughBinding, 25, 26)
                .addInputItem(TinkerToolParts.toughHandle,   7, 62)
                .build();

        // axes
        defineModifiable(TinkerTools.handAxe)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.smallAxeHead, 31, 22)
                .addInputItem(TinkerToolParts.toolHandle,   22, 53)
                .addInputItem(TinkerToolParts.toolBinding,  51, 34)
                .build();
        defineModifiable(TinkerTools.broadAxe)
                .sortIndex(SORT_HARVEST + SORT_LARGE)
                .addInputItem(TinkerToolParts.broadAxeHead, 25, 20)
                .addInputItem(TinkerToolParts.toughHandle,  21, 52)
                .addInputItem(TinkerToolParts.pickHead,     50, 48)
                .addInputItem(TinkerToolParts.toughBinding, 44, 29)
                .build();

        // scythes
        defineModifiable(TinkerTools.kama)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.smallBlade,  31, 22)
                .addInputItem(TinkerToolParts.toolHandle,  22, 53)
                .addInputItem(TinkerToolParts.toolBinding, 51, 34)
                .build();
        defineModifiable(TinkerTools.scythe)
                .sortIndex(SORT_HARVEST + SORT_LARGE)
                .addInputItem(TinkerToolParts.broadBlade,   35, 20)
                .addInputItem(TinkerToolParts.toughHandle,  12, 55)
                .addInputItem(TinkerToolParts.toughBinding, 50, 40)
                .addInputItem(TinkerToolParts.toughHandle,  30, 40)
                .build();

        // swords
        defineModifiable(TinkerTools.dagger)
                .sortIndex(SORT_WEAPON)
                .addInputItem(TinkerToolParts.smallBlade, 39, 35)
                .addInputItem(TinkerToolParts.toolHandle, 21, 53)
                .build();
        defineModifiable(TinkerTools.sword)
                .sortIndex(SORT_WEAPON)
                .addInputItem(TinkerToolParts.smallBlade, 48, 26)
                .addInputItem(TinkerToolParts.toolHandle, 12, 62)
                .addInputItem(TinkerToolParts.toolHandle, 30, 44)
                .build();
        defineModifiable(TinkerTools.cleaver)
                .sortIndex(SORT_WEAPON + SORT_LARGE)
                .addInputItem(TinkerToolParts.broadBlade,  45, 26)
                .addInputItem(TinkerToolParts.toughHandle,  7, 62)
                .addInputItem(TinkerToolParts.toughHandle, 25, 46)
                .addInputItem(TinkerToolParts.largePlate,  45, 46)
                .build();

        //gt
        defineModifiable(TinkerTools.wireCutter)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.wireCutterHead, 54, 20)
                .addInputItem(TinkerToolParts.toughHandle,    18, 56)
                .addInputItem(TinkerToolParts.toolBinding,    38, 36)
                .build();
        defineModifiable(TinkerTools.wrench)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.wrenchHead,  50, 24)
                .addInputItem(TinkerToolParts.toughHandle, 14, 60)
                .addInputItem(TinkerToolParts.toolBinding, 34, 40)
                .build();
        defineModifiable(TinkerTools.file)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.fileHead,    42, 32)
                .addInputItem(TinkerToolParts.toughHandle, 22, 52)
                .build();
        defineModifiable(TinkerTools.screwdriver)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.screwdriverHead, 42, 32)
                .addInputItem(TinkerToolParts.toughHandle,     22, 52)
                .build();
        defineModifiable(TinkerTools.mallet)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.hammerHead,  50, 24)
                .addInputItem(TinkerToolParts.toolHandle,  14, 60)
                .addInputItem(TinkerToolParts.toolBinding, 34, 40)
                .build();
        defineModifiable(TinkerTools.saw)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.sawBlade,    14, 24)
                .addInputItem(TinkerToolParts.toolHandle,  50, 66)
                .addInputItem(TinkerToolParts.toolBinding, 34, 48)
                .build();
        defineModifiable(TinkerTools.crowbar)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.crowbarHead,  50, 24)
                .addInputItem(TinkerToolParts.toughBinding, 32, 42)
                .addInputItem(TinkerToolParts.crowbarHead,  14, 60)
                .build();
        defineModifiable(TinkerTools.mortar)
                .sortIndex(SORT_HARVEST)
                .addInputItem(TinkerToolParts.mortarHead, 32, 30)
                .addInputItem(TinkerToolParts.mortarBowl, 32, 54)
                .build();

        defineModifiable(TinkerTools.crossbow)
                .sortIndex(SORT_RANGED)
                .addInputItem(TinkerToolParts.bowLimb,   10, 20)
                .addInputItem(TinkerToolParts.bowGrip,   46, 56)
                .addInputItem(TinkerToolParts.bowstring, 28, 38)
                .build();
        defineModifiable(TinkerTools.longbow)
                .sortIndex(SORT_RANGED + SORT_LARGE)
                .addInputItem(TinkerToolParts.bowLimb,   20, 55)
                .addInputItem(TinkerToolParts.bowLimb,   45, 30)
                .addInputItem(TinkerToolParts.bowGrip,   25, 35)
                .addInputItem(TinkerToolParts.bowstring, 45, 55)
                .build();
        defineModifiable(TinkerTools.fishingRod)
                .sortIndex(SORT_RANGED)
                .addInputItem(TinkerToolParts.bowLimb,   28, 38)
                .addInputItem(TinkerToolParts.bowstring, 50, 38)
                .addInputItem(TinkerToolParts.arrowHead, 50, 58)
                .build();
        defineModifiable(TinkerTools.javelin)
                .sortIndex(SORT_RANGED + SORT_LARGE)
                .addInputItem(TinkerToolParts.smallBlade, 45, 26)
                .addInputItem(TinkerToolParts.toolHandle,  7, 62)
                .addInputItem(TinkerToolParts.bowLimb,    45, 46)
                .addInputItem(TinkerToolParts.bowGrip,    25, 46)
                .build();
        defineModifiable(TinkerTools.arrow)
                .sortIndex(SORT_AMMO)
                .addInputItem(TinkerToolParts.arrowHead,  53, 22)
                .addInputItem(TinkerToolParts.arrowShaft, 33, 42)
                .addInputItem(TinkerToolParts.fletching,  15, 60)
                .build();
        // use a single button for both throwing weapons
        definePattern(Patterns.THROWN_AMMO)
                .sortIndex(SORT_AMMO)
                .translationKey(TConstruct.makeTranslationKey("gui", "thrown_ammo"))
                .addInputItem(TinkerToolParts.arrowHead, 33, 29)
                .addInputPattern(Patterns.ARROW_PART, 33, 53, Ingredient.of(TinkerToolParts.arrowHead, TinkerToolParts.arrowShaft))
                .build();
        defineArmorPartLayouts();
    }

    private void defineArmorPartLayouts() {
        definePattern(Patterns.PLATE_ARMOR_SMALL)
                .sortIndex(32)
                .translationKey(TConstruct.makeTranslationKey("gui", "plate_small"))
                .addInputPattern(Patterns.PLATING_SMALL, 33, 32, smallPlating())
                .addInputItem(TinkerToolParts.maille, 33, 58)
                .build();
        definePattern(Patterns.PLATE_ARMOR_LARGE)
                .sortIndex(33)
                .translationKey(TConstruct.makeTranslationKey("gui", "plate_large"))
                .addInputPattern(Patterns.PLATING_LARGE, 20, 33, largePlating())
                .addInputPattern(Patterns.PLATING_LARGE, 46, 33, largePlating())
                .addInputItem(TinkerToolParts.maille, 33, 56)
                .build();
        definePattern(Patterns.EXPLORERS_ARMOR)
                .sortIndex(34)
                .translationKey(TConstruct.makeTranslationKey("gui", "explorers"))
                .addInputPattern(Patterns.ARMOR_FRAME, 33, 20, allFrames())
                .addInputItem(TinkerToolParts.armorMail, 33, 46)
                .addInputItem(TinkerToolParts.linear, 33, 72)
                .build();
        definePattern(Patterns.STANDARD_ARMOR)
                .sortIndex(35)
                .translationKey(TConstruct.makeTranslationKey("gui", "standard"))
                .addInputPattern(Patterns.ARMOR_CAST, 33, 33, allArmorCasts())
                .addInputItem(TinkerToolParts.maille, 20, 56)
                .addInputItem(TinkerToolParts.linear, 46, 56)
                .build();
        definePattern(Patterns.COMPOSITE_ARMOR_SMALL)
                .sortIndex(36)
                .translationKey(TConstruct.makeTranslationKey("gui", "composite_small"))
                .addInputPattern(Patterns.ARMOR_FRAME_SMALL, 33, 20, smallFrames())
                .addInputPattern(Patterns.MAIL_PLATE, 33, 46, mailOrPlate())
                .addInputItem(TinkerToolParts.maille, 33, 72)
                .build();
        definePattern(Patterns.COMPOSITE_ARMOR_LARGE)
                .sortIndex(37)
                .translationKey(TConstruct.makeTranslationKey("gui", "composite_large"))
                .addInputPattern(Patterns.ARMOR_FRAME_LARGE, 33, 23, largeFrames())
                .addInputPattern(Patterns.MAIL_PLATE, 46, 46, mailOrPlate())
                .addInputPattern(Patterns.MAIL_PLATE, 20, 46, mailOrPlate())
                .addInputItem(TinkerToolParts.maille, 33, 69)
                .build();
        definePattern(Patterns.FORGED_ARMOR_SMALL)
                .sortIndex(38)
                .translationKey(TConstruct.makeTranslationKey("gui", "forged_small"))
                .addInputPattern(Patterns.ARMOR_FRAME_SMALL, 33, 46, smallFrames())
                .addInputPattern(Patterns.PLATING_SMALL, 33, 20, smallPlating())
                .addInputPattern(Patterns.MAIL_PLATE, 33, 72, mailOrPlate())
                .build();
        definePattern(Patterns.FORGED_ARMOR_LARGE)
                .sortIndex(39)
                .translationKey(TConstruct.makeTranslationKey("gui", "forged_large"))
                .addInputPattern(Patterns.ARMOR_FRAME_LARGE, 33, 46, largeFrames())
                .addInputPattern(Patterns.PLATING_LARGE, 33, 20, largePlating())
                .addInputPattern(Patterns.MAIL_PLATE, 10, 59, mailOrPlate())
                .addInputPattern(Patterns.MAIL_PLATE, 56, 59, mailOrPlate())
                .build();
        definePattern(Patterns.KNIGHTS_ARMOR)
                .sortIndex(40)
                .translationKey(TConstruct.makeTranslationKey("gui", "knights"))
                .addInputPattern(Patterns.MASSIVE_ARMOR_CAST, 33, 33, allMassiveArmorCasts())
                .addInputItem(TinkerToolParts.maille, 20, 56)
                .addInputItem(TinkerToolParts.linear, 46, 56)
                .build();
    }

    private static Ingredient allArmorCasts() {
        return Ingredient.of(TinkerToolParts.armorCast.values().toArray(new Item[0]));
    }

    private static Ingredient allMassiveArmorCasts() {
        return Ingredient.of(TinkerToolParts.massiveArmorCast.values().toArray(new Item[0]));
    }

    private static Ingredient allFrames() {
        return Ingredient.of(TinkerToolParts.armorFrame.values().toArray(new Item[0]));
    }

    private static Ingredient smallFrames() {
        return Ingredient.of(TinkerToolParts.armorFrame.get(ArmorItem.Type.HELMET), TinkerToolParts.armorFrame.get(ArmorItem.Type.BOOTS));
    }

    private static Ingredient largeFrames() {
        return Ingredient.of(TinkerToolParts.armorFrame.get(ArmorItem.Type.CHESTPLATE), TinkerToolParts.armorFrame.get(ArmorItem.Type.LEGGINGS));
    }

    private static Ingredient smallPlating() {
        return Ingredient.of(TinkerToolParts.plating.get(ArmorItem.Type.HELMET), TinkerToolParts.plating.get(ArmorItem.Type.BOOTS));
    }

    private static Ingredient largePlating() {
        return Ingredient.of(TinkerToolParts.plating.get(ArmorItem.Type.CHESTPLATE), TinkerToolParts.plating.get(ArmorItem.Type.LEGGINGS));
    }

    private static Ingredient mailOrPlate() {
        return Ingredient.of(TinkerToolParts.armorMail, TinkerToolParts.armorPlate);
    }

    @Override
    public String getName() {
        return "Tinkers' Construct Tinker Station Slot Layouts";
    }
}
