package slimeknights.tconstruct.fluids;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.InvertedFluid;
import slimeknights.mantle.fluid.UnplaceableFluid;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.registration.CompatMaterialFluidObject;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.fluids.fluids.PotionFluidType;
import slimeknights.tconstruct.fluids.fluids.SlimeFluid;
import slimeknights.tconstruct.fluids.item.BottleItem;
import slimeknights.tconstruct.fluids.item.ContainerFoodItem;
import slimeknights.tconstruct.fluids.item.ContainerFoodItem.FluidContainerFoodItem;
import slimeknights.tconstruct.fluids.item.MagmaBottleItem;
import slimeknights.tconstruct.fluids.item.PotionBucketItem;
import slimeknights.tconstruct.fluids.util.BottleBrewingRecipe;
import slimeknights.tconstruct.fluids.util.EmptyBottleIntoEmpty;
import slimeknights.tconstruct.fluids.util.EmptyBottleIntoWater;
import slimeknights.tconstruct.fluids.util.FillBottle;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.shared.TinkerEffects;
import slimeknights.tconstruct.shared.TinkerFood;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.item.CopperCanItem;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.network.FluidDataSerializer;
import slimeknights.tconstruct.world.TinkerWorld;

import static slimeknights.mantle.Mantle.commonResource;
import static slimeknights.tconstruct.fluids.block.BurningLiquidBlock.createBurning;
import static slimeknights.tconstruct.fluids.block.MobEffectLiquidBlock.createEffect;

/**
 * Contains all fluids used throughout the mod
 */
@SuppressWarnings("unused")
public final class TinkerFluids extends TinkerModule {
    public TinkerFluids() {
        ForgeMod.enableMilkFluid();
    }

    /** Creative tab for general items, or those that lack another tab */
    public static final RegistryObject<CreativeModeTab> tabFluids = CREATIVE_TABS.register(
            "fluids", () -> CreativeModeTab.builder().title(TConstruct.makeTranslation("itemGroup", "fluids"))
                    .icon(() -> new ItemStack(TinkerFluids.moltenGlass))
                    .displayItems(TinkerFluids::addTabItems)
                    .withTabsBefore(TinkerSmeltery.tabSmeltery.getId())
                    .withSearchBar()
                    .build());

    // basic
    public static final FlowingFluidObject<ForgeFlowingFluid> venom = FLUIDS.register("venom").type(slime("venom").temperature(310)).bucket().block(createEffect(MapColor.QUARTZ, 0, () -> new MobEffectInstance(MobEffects.POISON, 5*20))).flowing();
    public static final ItemObject<Item> venomBottle = ITEMS.register("venom_bottle", () -> new FluidContainerFoodItem(new Item.Properties().food(TinkerFood.VENOM_BOTTLE).stacksTo(16).craftRemainder(Items.GLASS_BOTTLE), () -> new FluidStack(venom.get(), FluidValues.BOTTLE)));
    public static final FluidObject<UnplaceableFluid> powderedSnow = FLUIDS.register("powdered_snow").bucket(() -> Items.POWDER_SNOW_BUCKET).type(powder("powdered_snow").temperature(270)).commonTag().unplacable();

    // slime -  note second name parameter is forge tag name
    public static final FlowingFluidObject<SlimeFluid> earthSlime = FLUIDS.registerSlime("earth_slime").type(slime("earth_slime").temperature(350)).bucket().block(createEffect(MapColor.GRASS, 0, () -> new MobEffectInstance(TinkerEffects.bouncy.get(), 5*20))).commonTag("slime").flowing(SlimeFluid.Source::new, SlimeFluid.Flowing::new);
    public static final FlowingFluidObject<SlimeFluid> skySlime   = FLUIDS.registerSlime("sky_slime"  ).type(slime("sky_slime"  ).temperature(310)).bucket().block(createEffect(MapColor.DIAMOND, 0, () -> new MobEffectInstance(TinkerEffects.ricochet.get(), 5*20))).flowing(SlimeFluid.Source::new, SlimeFluid.Flowing::new);
    public static final FlowingFluidObject<SlimeFluid> enderSlime = FLUIDS.registerSlime("ender_slime").type(slime("ender_slime").temperature(370)).bucket().block(createEffect(MapColor.COLOR_PURPLE, 0, () -> new MobEffectInstance(TinkerEffects.enderference.get(), 5 * 20))).flowing(SlimeFluid.Source::new, SlimeFluid.Flowing::new);
    public static final FlowingFluidObject<SlimeFluid> magma      = FLUIDS.registerSlime("magma").type(slime("magma").temperature(600).lightLevel(3)).bucket().commonTag().block(createBurning(MapColor.NETHER, 3, 8, 3f)).flowing(SlimeFluid.Source::new, SlimeFluid.Flowing::new);
    public static final FlowingFluidObject<InvertedFluid> ichor   = FLUIDS.registerSlime("ichor").invertedType(slime("ichor").temperature(1000).density(-1600)).bucket().block(MapColor.COLOR_ORANGE, 0).invertedFlowing();
    public static final EnumObject<SlimeType, Fluid> slime = new EnumObject.Builder<SlimeType, Fluid>(SlimeType.class).put(SlimeType.EARTH, earthSlime).put(SlimeType.SKY, skySlime).put(SlimeType.ENDER, enderSlime).put(SlimeType.ICHOR, ichor).build();
    // bottles of slime
    public static final EnumObject<SlimeType, Item> slimeBottle = ITEMS.registerEnum(SlimeType.values(), "slime_bottle", type -> new FluidContainerFoodItem(
            new Item.Properties().food(TinkerFood.getBottle(type)).stacksTo(16).craftRemainder(Items.GLASS_BOTTLE), () -> new FluidStack(slime.get(type), FluidValues.BOTTLE)));
    public static final ItemObject<Item> magmaBottle = ITEMS.register("magma_bottle", () -> new MagmaBottleItem(new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE), 15));

    // foods
    public static FlowingFluidObject<ForgeFlowingFluid> honey        = FLUIDS.registerSlime("honey").type(slime("honey").temperature(301)).bucket().block(createEffect(MapColor.COLOR_ORANGE, 0, () -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5*20))).commonTag().flowing();
    public static FlowingFluidObject<ForgeFlowingFluid> beetrootSoup = FLUIDS.register("beetroot_soup").type(cool("beetroot_soup").temperature(400)).bucket().block(MapColor.COLOR_RED, 0).commonTag().flowing();
    public static FlowingFluidObject<ForgeFlowingFluid> mushroomStew = FLUIDS.register("mushroom_stew").type(cool("mushroom_stew").temperature(400)).bucket().block(MapColor.DIRT, 0).commonTag().flowing();
    public static FlowingFluidObject<ForgeFlowingFluid> rabbitStew   = FLUIDS.register("rabbit_stew").type(cool("rabbit_stew").temperature(400)).bucket().block(MapColor.PODZOL, 0).commonTag().flowing();
    public static FlowingFluidObject<ForgeFlowingFluid> meatSoup     = FLUIDS.register("meat_soup").type(cool("meat_soup").temperature(400)).bucket().block(MapColor.CRIMSON_NYLIUM, 0).flowing();
    public static final ItemObject<Item> meatSoupBowl = ITEMS.register("meat_soup", () -> new ContainerFoodItem(new Item.Properties().food(TinkerFood.MEAT_SOUP).stacksTo(1).craftRemainder(Items.BOWL)));

    // potion
    public static final FluidObject<UnplaceableFluid> potion = FLUIDS.register("potion").type(() -> new PotionFluidType(cool().descriptionId("item.minecraft.potion.effect.empty").density(1100).viscosity(1100).temperature(315).sound(SoundActions.BUCKET_FILL, SoundEvents.BOTTLE_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BOTTLE_EMPTY))).bucket(fluid -> new PotionBucketItem(fluid, RegistrationHelper.BUCKET_PROPS)).commonTag().unplacable();
    public static final ItemObject<Item> splashBottle = ITEMS.register("splash_bottle", () -> new BottleItem(Items.SPLASH_POTION, ITEM_PROPS));
    public static final ItemObject<Item> lingeringBottle = ITEMS.register("lingering_bottle", () -> new BottleItem(Items.LINGERING_POTION, ITEM_PROPS));

    // base molten fluids
    public static final FlowingFluidObject<ForgeFlowingFluid> searedStone   = FLUIDS.registerStone("seared_stone").type(hot("seared_stone").temperature(900).lightLevel(6)).block(createBurning(MapColor.DEEPSLATE, 6, 8, 2f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> scorchedStone = FLUIDS.registerStone("scorched_stone").type(hot("scorched_stone").temperature(800).lightLevel(4)).block(createBurning(MapColor.TERRACOTTA_BROWN, 4, 7, 2f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenClay    = FLUIDS.registerStone("clay").type(hot("clay").temperature(750).lightLevel(3)).block(createBurning(MapColor.COLOR_ORANGE, 3, 5, 2f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenGlass   = FLUIDS.registerGlass("glass").type(hot("glass").temperature(1050).lightLevel(1)).block(createBurning(MapColor.ICE, 1, 5, 2f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> liquidSoul    = FLUIDS.registerGlass("liquid_soul").type(hot("liquid_soul").temperature(700).lightLevel(2)).block(createEffect(MapColor.COLOR_BROWN, 2, () -> new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20))).bucket().flowing();
    // ceramics compat
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenPorcelain = FLUIDS.registerStone("porcelain").type(hot("porcelain").temperature(1000).lightLevel(2)).block(createBurning(MapColor.QUARTZ, 2, 5, 2f)).bucket().flowing();
    // fancy molten fluids
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenObsidian = FLUIDS.registerStone("obsidian").type(hot("obsidian").temperature(1300).lightLevel(3)).block(createBurning(MapColor.COLOR_BLACK, 3, 12, 4f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenEnder    = FLUIDS.registerStone("ender").type(hot("ender").temperature(777).lightLevel(5)).block(createEffect(MapColor.PLANT, 5, () -> new MobEffectInstance(TinkerEffects.enderference.get(), 5 * 20))).bucket().commonTag("ender").flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> blazingBlood   = FLUIDS.register("blazing_blood").type(hot("blazing_blood").temperature(1800).lightLevel(15).density(3500)).block(createBurning(MapColor.COLOR_ORANGE, 15, 15, 5f)).bucket().flowing();

    // ores
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenEmerald  = FLUIDS.registerGem("emerald").type(hot("emerald").temperature(1234).lightLevel(9)).block(createBurning(MapColor.EMERALD, 9, 10, 6f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenQuartz   = FLUIDS.registerGem("quartz").type(hot("quartz").temperature(937).lightLevel(6)).block(createBurning(MapColor.QUARTZ, 6, 10, 5f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenAmethyst = FLUIDS.registerGem("amethyst").type(hot("amethyst").temperature(1250).lightLevel(11)).block(createBurning(MapColor.COLOR_PURPLE, 11, 10, 5f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenDiamond  = FLUIDS.registerGem("diamond").type(hot("diamond").temperature(1750).lightLevel(13)).block(createBurning(MapColor.DIAMOND, 13, 10, 7f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenDebris   = FLUIDS.registerGem("debris").type(hot("debris").temperature(1475).lightLevel(14)).block(createBurning(MapColor.COLOR_BLACK, 14, 10, 8f)).bucket().flowing();
    // metal ores
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenIron   = FLUIDS.registerCompatMetal("iron", hot("iron").temperature(1100).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenGold   = FLUIDS.registerCompatMetal("gold", hot("gold").temperature(1000).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenCopper = FLUIDS.registerCompatMetal("copper", hot("copper").temperature(800).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenCobalt = FLUIDS.registerCompatMetal("cobalt", hot("cobalt").temperature(1250).lightLevel(8));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenSteel  = FLUIDS.registerCompatMetal("steel", hot("steel").temperature(1250).lightLevel(13));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenKnightmetal = FLUIDS.registerMetal("knightmetal").type(hot("knightmetal").temperature(1600).lightLevel(10)).block(createBurning(MapColor.GRASS, 10, 10, 8f)).bucket().flowing();
    // alloys
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenSlimesteel     = FLUIDS.registerMetal("slimesteel").type(hot("slimesteel").temperature(1200).lightLevel(10)).block(createBurning(MapColor.DIAMOND, 10, 10, 6f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenAmethystBronze = FLUIDS.registerMetal("amethyst_bronze").type(hot("amethyst_bronze").temperature(1120).lightLevel(12)).block(createBurning(MapColor.COLOR_MAGENTA, 12, 10, 6f)).bucket().commonTag().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenRoseGold       = FLUIDS.registerCompatMetal("rose_gold", hot("rose_gold").temperature(850).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenPigIron        = FLUIDS.registerMetal("pig_iron").type(hot("pig_iron").temperature(1111).lightLevel(10)).block(createBurning(MapColor.TERRACOTTA_WHITE, 10, 10, 6f)).bucket().flowing();

    public static final FlowingFluidObject<ForgeFlowingFluid> moltenManyullyn   = FLUIDS.registerMetal("manyullyn").type(hot("manyullyn").temperature(1500).lightLevel(11)).block(createBurning(MapColor.COLOR_PURPLE, 11, 10, 8f)).bucket().commonTag().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenHepatizon   = FLUIDS.registerMetal("hepatizon").type(hot("hepatizon").temperature(1700).lightLevel(8)).block(createBurning(MapColor.TERRACOTTA_BLUE, 8, 10, 7f)).bucket().commonTag().flowing();
    public static final FlowingFluidObject<InvertedFluid>     moltenCinderslime = FLUIDS.registerMetal("cinderslime").invertedType(hot("cinderslime").temperature(1350).lightLevel(10).density(-2000)).burningBlock(MapColor.COLOR_RED, 10, 10, 7f).bucket().invertedFlowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenQueensSlime = FLUIDS.registerMetal("queens_slime").type(hot("queens_slime").temperature(1450).lightLevel(9)).block(createBurning(MapColor.COLOR_GREEN, 9, 10, 6f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenSoulsteel   = FLUIDS.registerMetal("soulsteel").type(hot("soulsteel").temperature(1500).lightLevel(6)).block(createBurning(MapColor.COLOR_BROWN, 6, 10, 7f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenNetherite   = FLUIDS.registerMetal("netherite").type(hot("netherite").temperature(1550).lightLevel(14)).block(createBurning(MapColor.COLOR_BLACK, 14, 10, 10f)).bucket().commonTag().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenKnightslime = FLUIDS.registerMetal("knightslime").type(hot("knightslime").temperature(1425).lightLevel(12)).block(createBurning(MapColor.COLOR_MAGENTA, 12, 10, 8f)).bucket().flowing();

    // compat ores
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenTin      = FLUIDS.registerCompatMetal("tin", hot("tin").temperature(525).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenWroughtIron = FLUIDS.registerCompatMetal("wrought_iron", hot("wrought_iron").temperature(1175).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenAluminum = FLUIDS.registerCompatMetal("aluminium", hot("aluminium").temperature(725).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenLead     = FLUIDS.registerCompatMetal("lead", hot("lead").temperature(630).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenSilver   = FLUIDS.registerCompatMetal("silver", hot("silver").temperature(1090).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenNickel   = FLUIDS.registerCompatMetal("nickel", hot("nickel").temperature(1250).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenZinc     = FLUIDS.registerCompatMetal("zinc", hot("zinc").temperature(720).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenPlatinum = FLUIDS.registerCompatMetal("platinum", hot("platinum").temperature(1270).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenTungsten = FLUIDS.registerCompatMetal("tungsten", hot("tungsten").temperature(1250).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenOsmium   = FLUIDS.registerCompatMetal("osmium", hot("osmium").temperature(1275).lightLevel(4));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenUranium  = FLUIDS.registerCompatMetal("uranium", hot("uranium").temperature(1130).lightLevel(15));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenChromium = FLUIDS.registerCompatMetal("chromium",hot("chromium").temperature(1200).lightLevel(13));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenCadmium  = FLUIDS.registerMetal("cadmium").type(hot("cadmium").temperature(594).lightLevel(10)).block(createBurning(MapColor.COLOR_BROWN, 10, 10, 5f)).bucket().commonTag().flowing();
    // gt
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenPolyethylene = FLUIDS.registerCompatMetal("polyethylene", cool("polyethylene").temperature(445));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenPolyvinylChloride = FLUIDS.registerCompatMetal("polyvinyl_chloride", cool("polyvinyl_chloride").temperature(470));

    // compat alloys
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenAndesiteAlloy = FLUIDS.registerCompatMetal("andesite_alloy", hot("andesite_alloy").temperature(1100).lightLevel(12));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenBronze     = FLUIDS.registerCompatMetal("bronze", hot("bronze").temperature(1000).lightLevel(10));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenBrass      = FLUIDS.registerCompatMetal("brass", hot("brass").temperature(905).lightLevel(10));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenElectrum   = FLUIDS.registerCompatMetal("electrum", hot("electrum").temperature(1060).lightLevel(10));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenInvar      = FLUIDS.registerCompatMetal("invar", hot("invar").temperature(1200).lightLevel(10));
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenConstantan = FLUIDS.registerMetal("constantan").type(hot("constantan").temperature(1220).lightLevel(10)).block(createBurning(MapColor.TERRACOTTA_RED, 10, 10, 6f)).bucket().commonTag().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenPewter     = FLUIDS.registerMetal("pewter").type(hot("pewter").temperature(700).lightLevel(10)).block(createBurning(MapColor.COLOR_GRAY, 10, 10, 6f)).bucket().commonTag().flowing();

    // mod-specific compat
    // thermal
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenEnderium = FLUIDS.registerMetal("enderium").type(hot("enderium").temperature(1650).lightLevel(12)).block(createBurning(MapColor.COLOR_CYAN, 12, 10, 7f)).bucket().commonTag().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenLumium   = FLUIDS.registerMetal("lumium").type(hot("lumium").temperature(1350).lightLevel(15)).block(createBurning(MapColor.GOLD, 15, 10, 7f)).bucket().commonTag().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenSignalum = FLUIDS.registerMetal("signalum").type(hot("signalum").temperature(1299).lightLevel(13)).block(createBurning(MapColor.FIRE, 13, 10, 7f)).bucket().commonTag().flowing();
    // mekanism
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenRefinedGlowstone = FLUIDS.registerMetal("refined_glowstone").type(hot("refined_glowstone").temperature(1125).lightLevel(15)).block(createBurning(MapColor.COLOR_YELLOW, 15, 10, 7f)).bucket().commonTag().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenRefinedObsidian  = FLUIDS.registerMetal("refined_obsidian").type(hot("refined_obsidian").temperature(1775).lightLevel(7)).block(createBurning(MapColor.TERRACOTTA_BLUE, 7, 10, 7f)).bucket().commonTag().flowing();
    // cosmere metals
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenNicrosil = FLUIDS.registerMetal("nicrosil").type(hot("nicrosil").temperature(1400).lightLevel(14)).block(createBurning(MapColor.SNOW, 12, 10, 6f)).bucket().commonTag().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenDuralumin = FLUIDS.registerMetal("duralumin").type(hot("duralumin").temperature(925).lightLevel(10)).block(createBurning(MapColor.COLOR_LIGHT_GREEN, 10, 10, 6f)).bucket().commonTag().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenBendalloy = FLUIDS.registerMetal("bendalloy").type(hot("bendalloy").temperature(400).lightLevel(9)).block(createBurning(MapColor.SNOW, 9, 10, 6f)).bucket().commonTag().flowing();
    // twilight
    public static final FlowingFluidObject<ForgeFlowingFluid> moltenSteeleaf = FLUIDS.registerMetal("steeleaf").type(hot("steeleaf").temperature(1234).lightLevel(10)).block(createBurning(MapColor.COLOR_GREEN, 10, 10, 6f)).bucket().flowing();
    public static final FlowingFluidObject<ForgeFlowingFluid> fieryLiquid = FLUIDS.register("fiery_liquid").type(hot("fiery_liquid").temperature(1800).lightLevel(15)).block(createBurning(MapColor.CRIMSON_HYPHAE, 15, 20, 6f)).tickRate(30).bucket().flowing();
    // fluid data serializer
    public static final FluidDataSerializer FLUID_DATA_SERIALIZER = new FluidDataSerializer();
    public static final RegistryObject<EntityDataSerializer<?>> FLUID_DATA_SERIALIZER_REGISTRY = DATA_SERIALIZERS.register("fluid", () -> FLUID_DATA_SERIALIZER);

    /** Creates a builder for a cool fluid with sounds */
    private static FluidType.Properties cool() {
        return FluidType.Properties.create()
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .motionScale(0.0023333333333333335D)
                .canExtinguish(true);

    }

    /** Creates a builder for a cool fluid with sounds and description */
    private static FluidType.Properties cool(String name) {
        return cool().descriptionId(TConstruct.makeDescriptionId("fluid", name))
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
    }

    /** Creates a builder for a cool fluid with sounds and description */
    private static FluidType.Properties slime(String name) {
        return cool(name).density(1600).viscosity(1600);
    }

    /** Creates a builder for a cool fluid with sounds and description */
    @SuppressWarnings("SameParameterValue")
    private static FluidType.Properties powder(String name) {
        return FluidType.Properties.create().descriptionId(TConstruct.makeDescriptionId("fluid", name))
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_POWDER_SNOW)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
    }

    /** Creates a builder for a hot with sounds and description */
    private static FluidType.Properties hot(String name) {
        return FluidType.Properties.create().density(2000).viscosity(10000).temperature(1000)
                .descriptionId(TConstruct.makeDescriptionId("fluid", name))
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                // from forge lava type
                .motionScale(0.0023333333333333335D)
                .canSwim(false).canDrown(false)
                .pathType(BlockPathTypes.LAVA).adjacentPathType(null);
    }


    public static boolean isCompatMaterialFluid(FluidObject<?> fluid) {
        return fluid instanceof CompatMaterialFluidObject;
    }

    private static boolean isCompatMaterialFluid(ItemLike item) {
        return item instanceof FluidObject<?> fluid && isCompatMaterialFluid(fluid);
    }

    private static void registerDispenserBehavior(FluidObject<?> fluid, DispenseItemBehavior behavior) {
        if (!isCompatMaterialFluid(fluid)) {
            DispenserBlock.registerBehavior(fluid, behavior);
        }
    }

    @SubscribeEvent
    void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CauldronInteraction.WATER.put(splashBottle.get(), new FillBottle(Items.SPLASH_POTION));
            CauldronInteraction.WATER.put(lingeringBottle.get(), new FillBottle(Items.LINGERING_POTION));
            CauldronInteraction.WATER.put(Items.SPLASH_POTION,    new EmptyBottleIntoWater(splashBottle,    CauldronInteraction.WATER.get(Items.SPLASH_POTION)));
            CauldronInteraction.WATER.put(Items.LINGERING_POTION, new EmptyBottleIntoWater(lingeringBottle, CauldronInteraction.WATER.get(Items.LINGERING_POTION)));
            CauldronInteraction.EMPTY.put(Items.SPLASH_POTION,    new EmptyBottleIntoEmpty(splashBottle,    CauldronInteraction.EMPTY.get(Items.SPLASH_POTION)));
            CauldronInteraction.EMPTY.put(Items.LINGERING_POTION, new EmptyBottleIntoEmpty(lingeringBottle, CauldronInteraction.EMPTY.get(Items.LINGERING_POTION)));
            // brew bottles into each other, bit weird but feels better than shapeless
            BrewingRecipeRegistry.addRecipe(new BottleBrewingRecipe(Ingredient.of(Items.GLASS_BOTTLE), Items.POTION, Items.SPLASH_POTION, new ItemStack(splashBottle)));
            BrewingRecipeRegistry.addRecipe(new BottleBrewingRecipe(Ingredient.of(MantleTags.Items.SPLASH_BOTTLE), Items.SPLASH_POTION, Items.LINGERING_POTION, new ItemStack(lingeringBottle)));
        });

        // dispense buckets
        DispenseItemBehavior dispenseBucket = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            @Override
            public ItemStack execute(BlockSource source, ItemStack stack) {
                DispensibleContainerItem container = (DispensibleContainerItem)stack.getItem();
                BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
                Level level = source.getLevel();
                if (container.emptyContents(null, level, blockpos, null, stack)) {
                    container.checkExtraContent(null, level, stack, blockpos);
                    return new ItemStack(Items.BUCKET);
                } else {
                    return this.defaultDispenseItemBehavior.dispense(source, stack);
                }
            }
        };
        event.enqueueWork(() -> {
            // slime
            registerDispenserBehavior(venom, dispenseBucket);
            registerDispenserBehavior(earthSlime, dispenseBucket);
            registerDispenserBehavior(skySlime, dispenseBucket);
            registerDispenserBehavior(enderSlime, dispenseBucket);
            registerDispenserBehavior(magma, dispenseBucket);
            // foods
            registerDispenserBehavior(honey, dispenseBucket);
            registerDispenserBehavior(beetrootSoup, dispenseBucket);
            registerDispenserBehavior(mushroomStew, dispenseBucket);
            registerDispenserBehavior(rabbitStew, dispenseBucket);
            registerDispenserBehavior(meatSoup, dispenseBucket);
            // base molten fluids
            registerDispenserBehavior(searedStone, dispenseBucket);
            registerDispenserBehavior(scorchedStone, dispenseBucket);
            registerDispenserBehavior(moltenClay, dispenseBucket);
            registerDispenserBehavior(moltenGlass, dispenseBucket);
            registerDispenserBehavior(liquidSoul, dispenseBucket);
            registerDispenserBehavior(moltenPorcelain, dispenseBucket);
            registerDispenserBehavior(moltenObsidian, dispenseBucket);
            registerDispenserBehavior(moltenEnder, dispenseBucket);
            registerDispenserBehavior(blazingBlood, dispenseBucket);
            // ores
            registerDispenserBehavior(moltenEmerald, dispenseBucket);
            registerDispenserBehavior(moltenQuartz, dispenseBucket);
            registerDispenserBehavior(moltenAmethyst, dispenseBucket);
            registerDispenserBehavior(moltenDiamond, dispenseBucket);
            registerDispenserBehavior(moltenDebris, dispenseBucket);
            // metal ores
            registerDispenserBehavior(moltenIron, dispenseBucket);
            registerDispenserBehavior(moltenGold, dispenseBucket);
            registerDispenserBehavior(moltenCopper, dispenseBucket);
            registerDispenserBehavior(moltenCobalt, dispenseBucket);
            // alloys
            registerDispenserBehavior(moltenSlimesteel, dispenseBucket);
            registerDispenserBehavior(moltenAmethystBronze, dispenseBucket);
            registerDispenserBehavior(moltenRoseGold, dispenseBucket);
            registerDispenserBehavior(moltenPigIron, dispenseBucket);
            registerDispenserBehavior(moltenManyullyn, dispenseBucket);
            registerDispenserBehavior(moltenHepatizon, dispenseBucket);
            registerDispenserBehavior(moltenQueensSlime, dispenseBucket);
            registerDispenserBehavior(moltenSoulsteel, dispenseBucket);
            registerDispenserBehavior(moltenNetherite, dispenseBucket);
            registerDispenserBehavior(moltenKnightslime, dispenseBucket);
            // compat ores
            registerDispenserBehavior(moltenTin, dispenseBucket);
            registerDispenserBehavior(moltenWroughtIron, dispenseBucket);
            registerDispenserBehavior(moltenAluminum, dispenseBucket);
            registerDispenserBehavior(moltenLead, dispenseBucket);
            registerDispenserBehavior(moltenSilver, dispenseBucket);
            registerDispenserBehavior(moltenNickel, dispenseBucket);
            registerDispenserBehavior(moltenZinc, dispenseBucket);
            registerDispenserBehavior(moltenPlatinum, dispenseBucket);
            registerDispenserBehavior(moltenTungsten, dispenseBucket);
            registerDispenserBehavior(moltenOsmium, dispenseBucket);
            registerDispenserBehavior(moltenUranium, dispenseBucket);
            registerDispenserBehavior(moltenChromium, dispenseBucket);
            registerDispenserBehavior(moltenCadmium, dispenseBucket);
            // compat alloys
            registerDispenserBehavior(moltenAndesiteAlloy, dispenseBucket);
            registerDispenserBehavior(moltenBronze, dispenseBucket);
            registerDispenserBehavior(moltenBrass, dispenseBucket);
            registerDispenserBehavior(moltenElectrum, dispenseBucket);
            registerDispenserBehavior(moltenInvar, dispenseBucket);
            registerDispenserBehavior(moltenConstantan, dispenseBucket);
            registerDispenserBehavior(moltenPewter, dispenseBucket);
            registerDispenserBehavior(moltenSteel, dispenseBucket);
            // mod-specific compat alloys
            registerDispenserBehavior(moltenEnderium, dispenseBucket);
            registerDispenserBehavior(moltenLumium, dispenseBucket);
            registerDispenserBehavior(moltenSignalum, dispenseBucket);
            registerDispenserBehavior(moltenRefinedGlowstone, dispenseBucket);
            registerDispenserBehavior(moltenRefinedObsidian, dispenseBucket);
            registerDispenserBehavior(moltenNicrosil, dispenseBucket);
            registerDispenserBehavior(moltenDuralumin, dispenseBucket);
            registerDispenserBehavior(moltenBendalloy, dispenseBucket);
            registerDispenserBehavior(moltenSteeleaf, dispenseBucket);
            registerDispenserBehavior(fieryLiquid, dispenseBucket);

            // brew congealed slime into bottles to get slime bottles, easy melting
            for (SlimeType slime : SlimeType.values()) {
                BrewingRecipeRegistry.addRecipe(new BrewingRecipe(Ingredient.of(Items.GLASS_BOTTLE), Ingredient.of(TinkerWorld.congealedSlime.get(slime)), new ItemStack(TinkerFluids.slimeBottle.get(slime))));
            }
            BrewingRecipeRegistry.addRecipe(new BrewingRecipe(Ingredient.of(Items.GLASS_BOTTLE), Ingredient.of(Blocks.MAGMA_BLOCK), new ItemStack(TinkerFluids.magmaBottle)));
        });
    }

    /** Adds all relevant items to the creative tab, called by smeltery */
    @SuppressWarnings("deprecation")
    private static void addTabItems(ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
        // containers
        output.accept(splashBottle);
        output.accept(lingeringBottle);
        // slime
        output.accept(earthSlime);
        output.accept(skySlime);
        output.accept(ichor);
        output.accept(enderSlime);
        accept(output, slimeBottle);
        output.accept(magma);
        output.accept(magmaBottle);
        output.accept(venom);
        output.accept(venomBottle);

        // food
        output.accept(honey);
        output.accept(beetrootSoup);
        output.accept(mushroomStew);
        output.accept(rabbitStew);
        output.accept(meatSoup);
        output.accept(meatSoupBowl);

        // stone
        output.accept(searedStone);
        output.accept(scorchedStone);
        acceptRegistered(output, moltenClay);
        if (ModList.get().isLoaded("ceramics")) {
            acceptRegistered(output, moltenPorcelain);
        }
        acceptRegistered(output, moltenGlass);
        acceptRegistered(output, moltenObsidian);
        output.accept(liquidSoul);
        acceptRegistered(output, moltenEnder);
        output.accept(blazingBlood);

        // ores
        acceptRegistered(output, moltenEmerald);
        acceptRegistered(output, moltenQuartz);
        acceptRegistered(output, moltenAmethyst);
        acceptRegistered(output, moltenDiamond);
        acceptRegistered(output, moltenDebris);
        // metal ores
        acceptRegistered(output, moltenCopper);
        acceptRegistered(output, moltenIron);
        acceptRegistered(output, moltenGold);
        acceptRegistered(output, moltenCobalt);
        acceptRegistered(output, moltenSteel);

        // overworld alloys
        acceptRegistered(output, moltenSlimesteel);
        acceptRegistered(output, moltenAmethystBronze);
        acceptRegistered(output, moltenRoseGold);
        acceptRegistered(output, moltenPigIron);
        // nether alloys
        acceptRegistered(output, moltenCinderslime);
        acceptRegistered(output, moltenQueensSlime);
        acceptRegistered(output, moltenManyullyn);
        acceptRegistered(output, moltenHepatizon);
        acceptRegistered(output, moltenNetherite);
        acceptRegistered(output, moltenKnightmetal);
        acceptRegistered(output, moltenKnightslime);
        // future: soulsteel

        // compat ores
        acceptMolten(output, moltenTin);
        acceptCompat(output, moltenWroughtIron, MaterialIds.wroughtIron);
        acceptCompat(output, moltenAluminum, MaterialIds.aluminum);
        acceptCompat(output, moltenLead, MaterialIds.lead);
        acceptCompat(output, moltenSilver, MaterialIds.silver);
        acceptMolten(output, moltenNickel);
        acceptMolten(output, moltenZinc);
        acceptMolten(output, moltenPlatinum);
        acceptMolten(output, moltenTungsten);
        acceptCompat(output, moltenOsmium, MaterialIds.osmium);
        acceptMolten(output, moltenUranium, MaterialIds.necronium);
        acceptMolten(output, moltenChromium);
        acceptMolten(output, moltenCadmium);
        TiCAddonRegistry.addFluidTabItems(output);
        acceptCompat(output, moltenPolyethylene, MaterialIds.polyethylene);
        acceptCompat(output, moltenPolyvinylChloride, MaterialIds.polyvinylChloride);
        // compat alloys
        acceptCompat(output, moltenAndesiteAlloy, MaterialIds.andesiteAlloy);
        acceptCompat(output, moltenBronze, MaterialIds.bronze);
        acceptMolten(output, moltenBrass, MaterialIds.platedSlimewood);
        acceptCompat(output, moltenElectrum, MaterialIds.electrum);
        acceptCompat(output, moltenInvar, MaterialIds.invar);
        acceptCompat(output, moltenConstantan, MaterialIds.constantan);
        acceptCompat(output, moltenPewter, MaterialIds.pewter);
        acceptMolten(output, moltenEnderium);
        acceptMolten(output, moltenLumium);
        acceptMolten(output, moltenSignalum);
        acceptMolten(output, moltenRefinedGlowstone);
        acceptMolten(output, moltenRefinedObsidian);
        acceptMolten(output, moltenNicrosil);
        acceptMolten(output, moltenDuralumin);
        acceptMolten(output, moltenBendalloy);
        acceptCompat(output, moltenSteeleaf, MaterialIds.steeleaf);
        acceptCompat(output, fieryLiquid, "fiery", MaterialIds.fiery);
        BuiltInRegistries.POTION.holders().filter(holder -> {
            Potion potion = holder.get();
            return potion != Potions.EMPTY && potion != Potions.WATER;
        }).forEachOrdered(holder ->
                output.accept(PotionFluidType.potionBucket(holder.key())));

        // add copper cans, tanks, and lanterns for all the fluids
        CopperCanItem.addFilledVariants(output::accept);
        TankItem.addFilledVariants(output::accept);
    }

    /**
     * Accepts the given item if the passed ingot is present
     */
    private static void acceptCompat(Output output, ItemLike item, String ingot) {
        if (isCompatMaterialFluid(item)) {
            return;
        }
        acceptIfTag(output, item, ItemTags.create(commonResource("ingots/" + ingot)));
    }

    /** Accepts the given item if the passed ingot or material is present */
    private static void acceptCompat(CreativeModeTab.Output output, ItemLike item, String ingot, MaterialId material) {
        if (isCompatMaterialFluid(item)) {
            return;
        }
        if (!acceptIfMaterial(output, item, material)) {
            acceptCompat(output, item, ingot);
        }
    }

    /** Accepts the given item if the passed material or same named ingot is present */
    private static void acceptCompat(CreativeModeTab.Output output, ItemLike item, MaterialId material) {
        acceptCompat(output, item, material.getPath(), material);
    }

    /** Accepts the given item if the ingot named after the fluid is present */
    private static void acceptMolten(CreativeModeTab.Output output, FluidObject<?> fluid) {
        acceptCompat(output, fluid, withoutMolten(fluid));
    }

    /** Accepts the given item if the ingot named after the fluid or the material is present */
    private static void acceptMolten(CreativeModeTab.Output output, FluidObject<?> fluid, MaterialId material) {
        acceptCompat(output, fluid, withoutMolten(fluid), material);
    }

    private static void acceptRegistered(CreativeModeTab.Output output, FluidObject<?> fluid) {
        if (!isCompatMaterialFluid(fluid)) {
            output.accept(fluid);
        }
    }

    /** Length of the molten prefix */
    private static final int MOLTEN_LENGTH = "".length();

    /** Removes the "molten_" prefix from the fluids ID */
    public static String withoutMolten(FluidObject<?> fluid) {
        return fluid.getId().getPath().substring(MOLTEN_LENGTH);
    }

}
