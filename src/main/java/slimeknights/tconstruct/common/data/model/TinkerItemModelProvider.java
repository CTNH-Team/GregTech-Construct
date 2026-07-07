package slimeknights.tconstruct.common.data.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.data.resource.RuntimeResourceWriter;
import slimeknights.tconstruct.library.addon.DynamicResourceRegistrar;
import slimeknights.tconstruct.library.data.RuntimeResourceProvider;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.library.tools.part.MaterialItem;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.world.TinkerWorld;

import static slimeknights.tconstruct.TConstruct.getResource;

@SuppressWarnings("UnusedReturnValue")
public class TinkerItemModelProvider extends ItemModelProvider implements RuntimeResourceProvider {
    private final UncheckedModelFile GENERATED = new UncheckedModelFile("item/generated");

    public TinkerItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TConstruct.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // tool parts //
        // rock
        part(TinkerToolParts.pickHead, "pickaxe/head").offset(-2, 1);
        part(TinkerToolParts.hammerHead, "sledge_hammer/head").offset(-3, 3);
        // axe
        part(TinkerToolParts.smallAxeHead, "hand_axe/head").offset(-2, 3);
        part(TinkerToolParts.broadAxeHead, "broad_axe/blade").offset(0, 3);
        // blades
        part(TinkerToolParts.smallBlade);
        part(TinkerToolParts.broadBlade, "cleaver/head").offset(-1, 1);
        // plates
        part(TinkerToolParts.adzeHead, "pickadze/adze").offset(-5, 1);
        part(TinkerToolParts.largePlate);
        // bows
        part(TinkerToolParts.bowLimb, "longbow/limb_bottom").offset(5, -2);
        part(TinkerToolParts.bowGrip, "crossbow/body").offset(-2, -2);
        part(TinkerToolParts.bowstring);
        part(TinkerToolParts.arrowHead, "ammo/arrow_head").offset(-4, 3);
        part(TinkerToolParts.arrowShaft, "ammo/arrow_shaft").offset(1, -1);
        part(TinkerToolParts.fletching, "ammo/arrow_feather").offset(4, -5);
        // other
        part(TinkerToolParts.toolBinding);
        part(TinkerToolParts.toolHandle);
        part(TinkerToolParts.toughHandle);
        part(TinkerToolParts.toughBinding);
        part(TinkerToolParts.repairKit);
        part(TinkerToolParts.fakeIngot, "parts/ingot");
        // armor
        TinkerToolParts.plating.forEach((slot, item) -> {
            MaterialModelBuilder<ItemModelBuilder> b = this.part(item, "armor/plate/" + slot.getName() + "/plating");
            if (slot == ArmorItem.Type.HELMET) {
                b.offset(0, 2);
            } else if (slot == ArmorItem.Type.LEGGINGS) {
                b.offset(0, 1);
            }
        });
        part(TinkerToolParts.maille);
        part(TinkerToolParts.shieldCore, "armor/plate/shield/core");
        part(TinkerToolParts.armorPlate);
        part(TinkerToolParts.armorMail);
        TinkerToolParts.armorCast.forEach((slot, item) -> part(item, "parts/" + id(item).getPath()));
        TinkerToolParts.armorFrame.forEach((slot, item) -> part(item, "parts/" + id(item).getPath()));
        TinkerToolParts.massiveArmorCast.forEach((slot, item) -> part(item, "parts/" + id(item).getPath()));
        part(TinkerToolParts.linear);
        //gt
        part(TinkerToolParts.wrenchHead, "wrench/head_part");
        part(TinkerToolParts.wireCutterHead, "wire_cutter/head_part");
        part(TinkerToolParts.fileHead, "file/head_part");
        part(TinkerToolParts.screwdriverHead, "screwdriver/head_part");
        part(TinkerToolParts.sawBlade, "saw/head_part");
        part(TinkerToolParts.crowbarHead, "crowbar/head_part");
        part(TinkerToolParts.mortarHead, "mortar/head_part");
        part(TinkerToolParts.mortarBowl, "mortar/bowl_part");
        // gauges
        generated(TinkerSmeltery.copperGauge, "block/smeltery/io/gauge");
        generated(TinkerSmeltery.obsidianGauge, "block/foundry/io/gauge");

        // casts //
        // basic
        basicItem(TinkerSmeltery.blankSandCast, "sand_cast/blank");
        basicItem(TinkerSmeltery.blankRedSandCast, "red_sand_cast/blank");
        cast(TinkerSmeltery.ingotCast);
        cast(TinkerSmeltery.nuggetCast);
        cast(TinkerSmeltery.gemCast);
        cast(TinkerSmeltery.rodCast);
        cast(TinkerSmeltery.repairKitCast);
        // compat
        cast(TinkerSmeltery.plateCast);
        cast(TinkerSmeltery.gearCast);
        cast(TinkerSmeltery.coinCast);
        cast(TinkerSmeltery.wireCast);
        // small heads
        cast(TinkerSmeltery.pickHeadCast);
        cast(TinkerSmeltery.smallAxeHeadCast);
        cast(TinkerSmeltery.smallBladeCast);
        cast(TinkerSmeltery.adzeHeadCast);
        //gt
        cast(TinkerSmeltery.wrenchHeadCast);
        cast(TinkerSmeltery.wireCutterHeadCast);
        cast(TinkerSmeltery.fileHeadCast);
        cast(TinkerSmeltery.screwdriverHeadCast);
        cast(TinkerSmeltery.sawBladeCast);
        cast(TinkerSmeltery.crowbarHeadCast);
        cast(TinkerSmeltery.mortarHeadCast);
        cast(TinkerSmeltery.mortarBowlCast);
        // large heads
        cast(TinkerSmeltery.hammerHeadCast);
        cast(TinkerSmeltery.broadBladeCast);
        cast(TinkerSmeltery.broadAxeHeadCast);
        cast(TinkerSmeltery.largePlateCast);
        // bindings
        cast(TinkerSmeltery.toolBindingCast);
        cast(TinkerSmeltery.toughBindingCast);
        // tool rods
        cast(TinkerSmeltery.toolHandleCast);
        cast(TinkerSmeltery.toughHandleCast);
        // bow
        cast(TinkerSmeltery.bowLimbCast);
        cast(TinkerSmeltery.bowGripCast);
        basicItem(TinkerSmeltery.arrowCast.getId(), "cast/arrow");
        // armor
        cast(TinkerSmeltery.helmetPlatingCast);
        cast(TinkerSmeltery.chestplatePlatingCast);
        cast(TinkerSmeltery.leggingsPlatingCast);
        cast(TinkerSmeltery.bootsPlatingCast);
        cast(TinkerSmeltery.mailleCast);
        cast(TinkerSmeltery.armorPlateCast);
        cast(TinkerSmeltery.armorMailCast);
        cast(TinkerSmeltery.armorCastHelmetCast);
        cast(TinkerSmeltery.armorCastChestplateCast);
        cast(TinkerSmeltery.armorCastLeggingsCast);
        cast(TinkerSmeltery.armorCastBootsCast);
        cast(TinkerSmeltery.frameHelmetCast);
        cast(TinkerSmeltery.frameChestplateCast);
        cast(TinkerSmeltery.frameLeggingsCast);
        cast(TinkerSmeltery.frameBootsCast);
        cast(TinkerSmeltery.massiveCastHelmetCast);
        cast(TinkerSmeltery.massiveCastChestplateCast);
        cast(TinkerSmeltery.massiveCastLeggingsCast);
        cast(TinkerSmeltery.massiveCastBootsCast);
        cast(TinkerSmeltery.linearCast);
        // dummy parts
        basicEnumItems(TinkerSmeltery.dummyPlating, "tool/parts/plating_");

        // world //
        // shards
        basicItem(TinkerWorld.steelShard, "materials/steel_shard");
        basicItem(TinkerWorld.cobaltShard, "materials/cobalt_shard");
        basicItem(TinkerWorld.knightmetalShard, "materials/knightmetal_shard");
        generated(TinkerWorld.steelCluster, "block/geode/steel_cluster");
        generated(TinkerWorld.cobaltCluster, "block/geode/cobalt_cluster");
        generated(TinkerWorld.knightmetalCluster, "block/geode/knightmetal_cluster");
    }

    @Override
    public void addToDynamicPack(DynamicResourceRegistrar registrar) {
        generatedModels.clear();
        registerModels();
        RuntimeResourceWriter.writeModels(this, registrar);
        ArmorExtensionItemModelBuilder.write(registrar);
        generatedModels.clear();
    }

    @SuppressWarnings("deprecation") // no its not
    private ResourceLocation id(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem());
    }

    /** Generated item with a texture */
    private ItemModelBuilder generated(ResourceLocation item, ResourceLocation texture) {
        return getBuilder(item.toString()).parent(GENERATED).texture("layer0", texture);
    }

    /** Generated item with a texture */
    private ItemModelBuilder generated(ResourceLocation item, String texture) {
        return generated(item, ResourceLocation.tryBuild(item.getNamespace(), texture));
    }

    /** Generated item with a texture */
    private ItemModelBuilder generated(ItemLike item, String texture) {
        return generated(id(item), texture);
    }

    private ItemModelBuilder generated(ItemObject<?> item, String texture) {
        return generated(item.getId(), texture);
    }

    /** Generated item with a texture */
    private ItemModelBuilder basicItem(ResourceLocation item, String texture) {
        return generated(item, "item/" + texture);
    }

    /** Generated item with a texture */
    private ItemModelBuilder basicItem(ItemLike item, String texture) {
        return basicItem(id(item), texture);
    }

    private ItemModelBuilder basicItem(ItemObject<?> item, String texture) {
        return basicItem(item.getId(), texture);
    }

    private void basicEnumItems(EnumObject<ArmorItem.Type,?> items, String texturePrefix) {
        for (ArmorItem.Type type : items.keys()) {
            basicItem(getResource(type.getName() + "_plating_dummy"), texturePrefix + type.getName());
        }
    }


    /* Parts */

    /** Creates a part model with the given texture */
    private MaterialModelBuilder<ItemModelBuilder> part(ResourceLocation part, String texture) {
        return withExistingParent(part.getPath(), "forge:item/default")
                .texture("texture", getResource("item/tool/" + texture))
                .customLoader(MaterialModelBuilder::new);
    }

    /** Creates a part model in the parts folder */
    private MaterialModelBuilder<ItemModelBuilder> part(Item item, String texture) {
        return part(id(item), texture);
    }

    /** Creates a part model with the given texture */
    private MaterialModelBuilder<ItemModelBuilder> part(ItemObject<? extends MaterialItem> part, String texture) {
        return part(part.getId(), texture);
    }

    /** Creates a part model in the parts folder */
    private void part(ItemObject<? extends MaterialItem> part) {
        part(part, "parts/" + part.getId().getPath());
    }


    /** Creates models for the given cast object */
    private void cast(CastItemObject cast) {
        String name = cast.getName().getPath();
        ResourceLocation id = cast.getId();
        basicItem(id, "cast/" + name);
        basicItem(id.withPath(path -> name + "_sand_cast"), "sand_cast/" + name);
        basicItem(id.withPath(path -> name + "_red_sand_cast"), "red_sand_cast/" + name);
    }
}
