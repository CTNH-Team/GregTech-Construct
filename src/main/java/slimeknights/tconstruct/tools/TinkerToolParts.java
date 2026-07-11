package slimeknights.tconstruct.tools;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.json.loot.ToolPartLootEntry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.IRepairKitItem;
import slimeknights.tconstruct.library.tools.part.MaterialTagToolPartItem;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.library.tools.part.block.MaterialBlock;
import slimeknights.tconstruct.library.tools.part.block.MaterialBlockEntity;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.item.FakeIngotItem;
import slimeknights.tconstruct.tools.item.FakeStorageBlockItem;
import slimeknights.tconstruct.tools.item.ModifierCrystalItem;
import slimeknights.tconstruct.tools.item.RepairKitItem;
import slimeknights.tconstruct.tools.stats.*;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TinkerToolParts extends TinkerModule {
    /** Tab for all tool parts or tool components with many variants */
    public static final RegistryObject<CreativeModeTab> tabToolParts = CREATIVE_TABS.register(
            "tool_parts", () -> CreativeModeTab.builder().title(TConstruct.makeTranslation("itemGroup", "tool_parts"))
                    .icon(() -> {
                        MaterialVariantId material;
                        if (MaterialRegistry.isFullyLoaded()) {
                            material = ToolBuildHandler.RANDOM.getMaterial(HeadMaterialStats.ID, RandomSource.create());
                        } else {
                            material = ToolBuildHandler.getRenderMaterial(0);
                        }
                        return TinkerToolParts.pickHead.get().withMaterialForDisplay(material);
                    })
                    .displayItems(TinkerToolParts::addTabItems)
                    .withTabsBefore(TinkerTools.tabTools.getId())
                    .withSearchBar()
                    .build());

    // repair kits
    public static final ItemObject<RepairKitItem> repairKit = ITEMS.register("repair_kit", () -> new RepairKitItem(ITEM_PROPS));
    /** Fake ingot tool part for the sake of compat materials that lack an ingot form. Mainly used for compat alloys such as bronze which can be activated by their components. */
    public static final ItemObject<FakeIngotItem> fakeIngot = ITEMS.register("fake_ingot", () -> new FakeIngotItem(ITEM_PROPS, 1, TinkerTags.Materials.COMPATABILITY_METALS));
    /** Fake block tool part for the sake of compat materials that lack an ingot form. Mainly used for compat alloys such as bronze which can be activated by their components. */
    public static final ItemObject<MaterialBlock> fakeStorageBlock = BLOCKS.register("fake_storage_block", () -> new MaterialBlock(metalBuilder(MapColor.COLOR_GRAY), MaterialBlockEntity::new), block -> new FakeStorageBlockItem(block, ITEM_PROPS, 9, TinkerTags.Materials.COMPATABILITY_BLOCKS));
    /** Same as {@link #fakeStorageBlock} but casted to an appropriate item interface form */
    public static final Supplier<IRepairKitItem> fakeStorageBlockItem = () -> (IRepairKitItem) fakeStorageBlock.asItem();

    // rock
    public static final ItemObject<ToolPartItem> pickHead = ITEMS.register("pick_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
    public static final ItemObject<ToolPartItem> hammerHead = ITEMS.register("hammer_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
    // axe
    public static final ItemObject<ToolPartItem> smallAxeHead = ITEMS.register("small_axe_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
    public static final ItemObject<ToolPartItem> broadAxeHead = ITEMS.register("broad_axe_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
    // blades
    public static final ItemObject<ToolPartItem> smallBlade = ITEMS.register("small_blade", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
    public static final ItemObject<ToolPartItem> broadBlade = ITEMS.register("broad_blade", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
    // plates
    public static final ItemObject<ToolPartItem> adzeHead = ITEMS.register("adze_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
    public static final ItemObject<ToolPartItem> largePlate = ITEMS.register("large_plate", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));
    // bows
    public static final ItemObject<ToolPartItem> bowLimb = ITEMS.register("bow_limb", () -> new ToolPartItem(ITEM_PROPS, LimbMaterialStats.ID));
    public static final ItemObject<ToolPartItem> bowGrip = ITEMS.register("bow_grip", () -> new ToolPartItem(ITEM_PROPS, GripMaterialStats.ID));
    public static final ItemObject<ToolPartItem> bowstring = ITEMS.register("bowstring", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.BOWSTRING.getIdentifier()));
    // ammo
    public static final ItemObject<ToolPartItem> arrowHead = ITEMS.register("arrow_head", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.ARROW_HEAD.getIdentifier()));
    public static final ItemObject<ToolPartItem> arrowShaft = ITEMS.register("arrow_shaft", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.ARROW_SHAFT.getIdentifier()));
    public static final ItemObject<ToolPartItem> fletching = ITEMS.register("fletching", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.FLETCHING.getIdentifier()));
    // other parts
    public static final ItemObject<ToolPartItem> toolBinding = ITEMS.register("tool_binding", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.BINDING.getIdentifier()));
    public static final ItemObject<ToolPartItem> toughBinding = ITEMS.register("tough_binding", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.BINDING.getIdentifier()));
    public static final ItemObject<ToolPartItem> toolHandle = ITEMS.register("tool_handle", () -> new ToolPartItem(ITEM_PROPS, HandleMaterialStats.ID));
    public static final ItemObject<ToolPartItem> toughHandle = ITEMS.register("tough_handle", () -> new ToolPartItem(ITEM_PROPS, HandleMaterialStats.ID));
    // armor
    public static final EnumObject<ArmorItem.Type,ToolPartItem> plating = ITEMS.registerEnum(ArmorItem.Type.values(), "plating", type -> new ToolPartItem(ITEM_PROPS, PlatingMaterialStats.TYPES.get(type.ordinal()).getId()));
    public static final ItemObject<ToolPartItem> maille = ITEMS.register("maille", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.MAILLE.getIdentifier()));
    public static final ItemObject<ToolPartItem> shieldCore = ITEMS.register("shield_core", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.SHIELD_CORE.getIdentifier()));
    public static final ItemObject<ToolPartItem> armorPlate = ITEMS.register("armor_plate", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.ARMOR_PLATE.getId()));
    public static final ItemObject<ToolPartItem> armorMail = ITEMS.register("armor_mail", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.ARMOR_MAIL.getId()));
    public static final EnumObject<ArmorItem.Type,ToolPartItem> armorCast = new EnumObject.Builder<ArmorItem.Type,ToolPartItem>(ArmorItem.Type.class)
            .put(ArmorItem.Type.HELMET, ITEMS.register("cast_helmet", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.CAST_TYPES.get(ArmorItem.Type.HELMET.ordinal()).getId())))
            .put(ArmorItem.Type.CHESTPLATE, ITEMS.register("cast_chestplate", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.CAST_TYPES.get(ArmorItem.Type.CHESTPLATE.ordinal()).getId())))
            .put(ArmorItem.Type.LEGGINGS, ITEMS.register("cast_leggings", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.CAST_TYPES.get(ArmorItem.Type.LEGGINGS.ordinal()).getId())))
            .put(ArmorItem.Type.BOOTS, ITEMS.register("cast_boots", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.CAST_TYPES.get(ArmorItem.Type.BOOTS.ordinal()).getId())))
            .build();
    public static final EnumObject<ArmorItem.Type,ToolPartItem> armorFrame = new EnumObject.Builder<ArmorItem.Type,ToolPartItem>(ArmorItem.Type.class)
            .put(ArmorItem.Type.HELMET, ITEMS.register("frame_of_helmet", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.FRAME_TYPES.get(ArmorItem.Type.HELMET.ordinal()).getId())))
            .put(ArmorItem.Type.CHESTPLATE, ITEMS.register("frame_of_chestplate", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.FRAME_TYPES.get(ArmorItem.Type.CHESTPLATE.ordinal()).getId())))
            .put(ArmorItem.Type.LEGGINGS, ITEMS.register("frame_of_leggings", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.FRAME_TYPES.get(ArmorItem.Type.LEGGINGS.ordinal()).getId())))
            .put(ArmorItem.Type.BOOTS, ITEMS.register("frame_of_boots", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.FRAME_TYPES.get(ArmorItem.Type.BOOTS.ordinal()).getId())))
            .build();
    public static final EnumObject<ArmorItem.Type,ToolPartItem> massiveArmorCast = new EnumObject.Builder<ArmorItem.Type,ToolPartItem>(ArmorItem.Type.class)
            .put(ArmorItem.Type.HELMET, ITEMS.register("massive_cast_helmet", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.MASSIVE_CAST_TYPES.get(ArmorItem.Type.HELMET.ordinal()).getId())))
            .put(ArmorItem.Type.CHESTPLATE, ITEMS.register("massive_cast_chestplate", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.MASSIVE_CAST_TYPES.get(ArmorItem.Type.CHESTPLATE.ordinal()).getId())))
            .put(ArmorItem.Type.LEGGINGS, ITEMS.register("massive_cast_leggings", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.MASSIVE_CAST_TYPES.get(ArmorItem.Type.LEGGINGS.ordinal()).getId())))
            .put(ArmorItem.Type.BOOTS, ITEMS.register("massive_cast_boots", () -> new ToolPartItem(ITEM_PROPS, ArmorExtensionMaterialStats.MASSIVE_CAST_TYPES.get(ArmorItem.Type.BOOTS.ordinal()).getId())))
            .build();
    public static final ItemObject<ToolPartItem> linear = ITEMS.register("linear", () -> new ToolPartItem(ITEM_PROPS, StatlessMaterialStats.LINEAR.getIdentifier()));
    //gtceu
    public static final ItemObject<MaterialTagToolPartItem> wrenchHead = ITEMS.register("wrench_head", () -> new MaterialTagToolPartItem(ITEM_PROPS, HeadMaterialStats.ID,TinkerTags.Materials.METALS));
    public static final ItemObject<MaterialTagToolPartItem> wireCutterHead = ITEMS.register("wire_cutter_head", () -> new MaterialTagToolPartItem(ITEM_PROPS, HeadMaterialStats.ID,TinkerTags.Materials.METALS));
    public static final ItemObject<MaterialTagToolPartItem> fileHead = ITEMS.register("file_head", () -> new MaterialTagToolPartItem(ITEM_PROPS, HeadMaterialStats.ID,TinkerTags.Materials.METALS));
    public static final ItemObject<MaterialTagToolPartItem> screwdriverHead = ITEMS.register("screwdriver_head", () -> new MaterialTagToolPartItem(ITEM_PROPS, HeadMaterialStats.ID ,TinkerTags.Materials.METALS));
    public static final ItemObject<MaterialTagToolPartItem> sawBlade = ITEMS.register("saw_blade", () -> new MaterialTagToolPartItem(ITEM_PROPS, HeadMaterialStats.ID,TinkerTags.Materials.METALS));
    public static final ItemObject<MaterialTagToolPartItem> crowbarHead = ITEMS.register("crowbar_head", () -> new MaterialTagToolPartItem(ITEM_PROPS, HeadMaterialStats.ID,TinkerTags.Materials.METALS));
    public static final ItemObject<MaterialTagToolPartItem> mortarHead = ITEMS.register("mortar_head", () -> new MaterialTagToolPartItem(ITEM_PROPS, HeadMaterialStats.ID, TinkerTags.Materials.HARD));
    public static final ItemObject<MaterialTagToolPartItem> mortarBowl = ITEMS.register("mortar_bowl", () -> new MaterialTagToolPartItem(ITEM_PROPS, HeadMaterialStats.ID, TinkerTags.Materials.HARD));
    // block entities
    public static final RegistryObject<BlockEntityType<MaterialBlockEntity>> materialBlock = BLOCK_ENTITIES.register("material_block", MaterialBlockEntity::new, fakeStorageBlock);

    // loot
    public static final RegistryObject<LootPoolEntryType> toolPartLootEntry = LOOT_ENTRIES.register("tool_part", () -> new LootPoolEntryType(new ToolPartLootEntry.Serializer()));

    /** Adds all relevant items to the creative tab */
    private static void addTabItems(ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output tab) {
        Consumer<ItemStack> output = tab::accept;
        accept(output, repairKit);
        // fake ingot is in materials tab, helps the illusion
        // small heads
        accept(output, pickHead);
        accept(output, smallAxeHead);
        accept(output, smallBlade);
        accept(output, adzeHead);
        // large heads
        accept(output, hammerHead);
        accept(output, broadAxeHead);
        accept(output, broadBlade);
        accept(output, largePlate);
        // binding and rods
        accept(output, toolHandle);
        accept(output, toolBinding);
        accept(output, toughHandle);
        accept(output, toughBinding);
        //gt
        accept(output, wrenchHead);
        accept(output, wireCutterHead);
        accept(output, fileHead);
        accept(output, screwdriverHead);
        accept(output, sawBlade);
        accept(output, crowbarHead);
        accept(output, mortarHead);
        accept(output, mortarBowl);
        // ranged
        accept(output, bowLimb);
        accept(output, bowGrip);
        accept(output, bowstring);
        accept(output, arrowHead);
        accept(output, arrowShaft);
        accept(output, fletching);
        // plating, pair each one with the dummy plating item
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            tab.accept(TinkerSmeltery.dummyPlating.get(type));
            plating.get(type).addVariants(output, "");
        }
        accept(output, maille);
        accept(output, shieldCore);
        accept(output, armorPlate);
        accept(output, armorMail);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            armorCast.get(type).addVariants(output, "");
            armorFrame.get(type).addVariants(output, "");
            massiveArmorCast.get(type).addVariants(output, "");
        }
        accept(output, linear);

        // end with modifier crystal dynamic listing
        ModifierCrystalItem.addVariants(output);
    }

    /** Adds a tool part to the tab */
    private static void accept(Consumer<ItemStack> output, Supplier<? extends IMaterialItem> item) {
        item.get().addVariants(output, "");
    }
}
