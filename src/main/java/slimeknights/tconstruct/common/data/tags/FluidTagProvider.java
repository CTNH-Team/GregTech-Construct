package slimeknights.tconstruct.common.data.tags;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.TagsProvider.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.addon.DatagenTagProviderRegistrar;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class FluidTagProvider extends FluidTagsProvider {
    private final Consumer<DatagenTagProviderRegistrar.FluidTagRegistrar> addonTags;

    public FluidTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider, ExistingFileHelper helper) {
        super(packOutput, lookupProvider, TConstruct.MOD_ID, helper);
        // 自动收集 addon tag hooks
        DatagenTagProviderRegistrar registrar = new DatagenTagProviderRegistrar();
        TiCAddonRegistry.collectDatagenTagProviders(registrar);
        this.addonTags = registrar::applyFluidTags;
    }

    @Override
    protected void addTags(Provider pProvider) {
        // first, register common tags
        // slime
        fluidTag(TinkerFluids.earthSlime);
        fluidTag(TinkerFluids.skySlime);
        fluidTag(TinkerFluids.ichor);
        fluidTag(TinkerFluids.enderSlime);
        fluidTag(TinkerFluids.magma);
        fluidTag(TinkerFluids.venom);
        // basic molten
        fluidTag(TinkerFluids.searedStone);
        fluidTag(TinkerFluids.scorchedStone);
        fluidTag(TinkerFluids.moltenClay);
        fluidTag(TinkerFluids.moltenGlass);
        fluidTag(TinkerFluids.liquidSoul);
        fluidTag(TinkerFluids.moltenPorcelain);
        // fancy molten
        fluidTag(TinkerFluids.moltenObsidian);
        fluidTag(TinkerFluids.moltenEmerald);
        fluidTag(TinkerFluids.moltenQuartz);
        fluidTag(TinkerFluids.moltenDiamond);
        fluidTag(TinkerFluids.moltenAmethyst);
        fluidTag(TinkerFluids.moltenEnder);
        fluidTag(TinkerFluids.blazingBlood);
        // ores
        fluidTag(TinkerFluids.moltenIron);
        fluidTag(TinkerFluids.moltenGold);
        fluidTag(TinkerFluids.moltenCopper);
        fluidTag(TinkerFluids.moltenCobalt);
        fluidTag(TinkerFluids.moltenSteel);
        fluidTag(TinkerFluids.moltenDebris);
        // alloys
        fluidTag(TinkerFluids.moltenSlimesteel);
        fluidTag(TinkerFluids.moltenAmethystBronze);
        fluidTag(TinkerFluids.moltenRoseGold);
        fluidTag(TinkerFluids.moltenPigIron);
        // nether alloys
        fluidTag(TinkerFluids.moltenManyullyn);
        fluidTag(TinkerFluids.moltenHepatizon);
        fluidTag(TinkerFluids.moltenQueensSlime);
        fluidTag(TinkerFluids.moltenCinderslime);
        fluidTag(TinkerFluids.moltenSoulsteel);
        fluidTag(TinkerFluids.moltenNetherite);
        // end alloys
        fluidTag(TinkerFluids.moltenKnightmetal);
        fluidTag(TinkerFluids.moltenKnightslime);
        // compat ores
        fluidTag(TinkerFluids.moltenTin);
        fluidTag(TinkerFluids.moltenAluminum);
        fluidTag(TinkerFluids.moltenLead);
        fluidTag(TinkerFluids.moltenSilver);
        fluidTag(TinkerFluids.moltenNickel);
        fluidTag(TinkerFluids.moltenZinc);
        fluidTag(TinkerFluids.moltenPlatinum);
        fluidTag(TinkerFluids.moltenTungsten);
        fluidTag(TinkerFluids.moltenOsmium);
        fluidTag(TinkerFluids.moltenUranium);
        fluidTag(TinkerFluids.moltenChromium);
        fluidTag(TinkerFluids.moltenCadmium);
        fluidTag(TinkerFluids.moltenPolyethylene);
        fluidTag(TinkerFluids.moltenPolyvinylChloride);
        // compat alloys
        fluidTag(TinkerFluids.moltenBronze);
        fluidTag(TinkerFluids.moltenBrass);
        fluidTag(TinkerFluids.moltenElectrum);
        fluidTag(TinkerFluids.moltenInvar);
        fluidTag(TinkerFluids.moltenConstantan);
        fluidTag(TinkerFluids.moltenPewter);
        // thermal compat alloys
        fluidTag(TinkerFluids.moltenEnderium);
        fluidTag(TinkerFluids.moltenLumium);
        fluidTag(TinkerFluids.moltenSignalum);
        // mekanism compat alloys
        fluidTag(TinkerFluids.moltenRefinedGlowstone);
        fluidTag(TinkerFluids.moltenRefinedObsidian);
        // cosmere compat alloys
        fluidTag(TinkerFluids.moltenNicrosil);
        fluidTag(TinkerFluids.moltenDuralumin);
        fluidTag(TinkerFluids.moltenBendalloy);
        // twilight compat fluids
        fluidTag(TinkerFluids.moltenSteeleaf);
        fluidTag(TinkerFluids.fieryLiquid);
        // unplacable fluids
        fluidTag(TinkerFluids.honey);
        fluidTag(TinkerFluids.beetrootSoup);
        fluidTag(TinkerFluids.mushroomStew);
        fluidTag(TinkerFluids.rabbitStew);
        fluidTag(TinkerFluids.meatSoup);

        /* Normal tags */
        this.tag(TinkerTags.Fluids.SLIME)
                .addTag(TinkerFluids.earthSlime.getTag())
                .addTag(TinkerFluids.skySlime.getTag())
                .addTags(TinkerFluids.ichor.getTag())
                .addTag(TinkerFluids.enderSlime.getTag());

        fluidTag(TinkerFluids.potion);
        fluidTag(TinkerFluids.powderedSnow);

        // drowned want fluids that work nice in water, while wither skeletons want to complement the withering
        // both need to act as a swasher tutorial though
        tag(TinkerTags.Fluids.DROWNED_SWASHER).add(Fluids.LAVA, TinkerFluids.powderedSnow.get(), TinkerFluids.moltenGlass.get(), TinkerFluids.moltenObsidian.get());
        tag(TinkerTags.Fluids.WITHER_SKELETON_SWASHER).add(Fluids.LAVA, TinkerFluids.blazingBlood.get(), TinkerFluids.liquidSoul.get(), TinkerFluids.magma.get());

        // tag local tags with the chemthrower, do not include forge tags as its on other mods to choose how they want to support IE
        // block effects - mostly mining
        addLocalTags(this.tag(TinkerTags.Fluids.CHEMTHROWER_BLOCK_EFFECTS),
                // small gem
                TinkerFluids.moltenAmethyst, TinkerFluids.moltenQuartz,
                // large gem
                TinkerFluids.moltenEmerald, TinkerFluids.moltenDiamond, TinkerFluids.moltenDebris
        );
        // entity effects - most of these have block effects, but we don't want the clouds triggering mostly
        this.tag(TinkerTags.Fluids.CHEMTHROWER_ENTITY_EFFECTS).add(TinkerFluids.powderedSnow.get());
        addLocalTags(this.tag(TinkerTags.Fluids.CHEMTHROWER_ENTITY_EFFECTS),
                // common
                TinkerFluids.blazingBlood,
                // slime
                TinkerFluids.venom,
                // glass
                TinkerFluids.moltenGlass, TinkerFluids.liquidSoul, TinkerFluids.moltenObsidian,
                // clay
                TinkerFluids.moltenClay, TinkerFluids.searedStone, TinkerFluids.scorchedStone,
                // food
                TinkerFluids.honey,
                TinkerFluids.mushroomStew, TinkerFluids.rabbitStew, TinkerFluids.meatSoup,
                // tier 2 compat still registered by this mod
                TinkerFluids.moltenChromium, TinkerFluids.moltenCadmium,
                // tier 3
                TinkerFluids.moltenAmethystBronze, TinkerFluids.moltenPigIron,
                // tier 3 compat still registered by this mod
                TinkerFluids.moltenPewter, TinkerFluids.moltenConstantan,
                // tier 4
                TinkerFluids.moltenManyullyn, TinkerFluids.moltenHepatizon, TinkerFluids.moltenNetherite,
                TinkerFluids.moltenKnightmetal,
                // thermal alloys
                TinkerFluids.moltenLumium, TinkerFluids.moltenEnderium,
                // mekanism alloys
                TinkerFluids.moltenRefinedGlowstone, TinkerFluids.moltenRefinedObsidian,
                // cosmere alloys
                TinkerFluids.moltenNicrosil, TinkerFluids.moltenDuralumin, TinkerFluids.moltenBendalloy
        );
        this.tag(TinkerTags.Fluids.CHEMTHROWER_ENTITY_EFFECTS)
                .addTag(Tags.Fluids.MILK);
        addTagOrOptional(this.tag(TinkerTags.Fluids.CHEMTHROWER_ENTITY_EFFECTS), TinkerFluids.moltenPolyethylene, TinkerFluids.moltenPolyvinylChloride);
        // both effects - all the neat slimes
        addLocalTags(this.tag(TinkerTags.Fluids.CHEMTHROWER_BOTH_EFFECTS),
                // slime
                TinkerFluids.earthSlime, TinkerFluids.skySlime, TinkerFluids.magma, TinkerFluids.moltenEnder,
                // slime metal
                TinkerFluids.moltenSlimesteel, TinkerFluids.moltenQueensSlime, TinkerFluids.moltenCinderslime,
                // thermal alloys
                TinkerFluids.moltenSignalum
        );
        this.tag(TinkerTags.Fluids.CHEMTHROWER_BOTH_EFFECTS).addTags(TinkerFluids.ichor.getTag(), TinkerFluids.enderSlime.getTag());

        // tooltips //
        this.tag(TinkerTags.Fluids.GLASS_TOOLTIPS).addTags(TinkerFluids.moltenGlass.getTag(), TinkerFluids.liquidSoul.getTag(), TinkerFluids.moltenObsidian.getTag());
        this.tag(TinkerTags.Fluids.SLIME_TOOLTIPS).addTags(TinkerFluids.magma.getTag(), TinkerFluids.moltenEnder.getTag(), TinkerTags.Fluids.SLIME);
        this.tag(TinkerTags.Fluids.BOTTLE_TOOLTIPS).addTags(TinkerFluids.venom.getTag(), TinkerFluids.fieryLiquid.getTag());
        this.tag(TinkerTags.Fluids.CLAY_TOOLTIPS).addTags(TinkerFluids.moltenClay.getTag(), TinkerFluids.moltenPorcelain.getTag(), TinkerFluids.searedStone.getTag(), TinkerFluids.scorchedStone.getTag());
        TagAppender<Fluid> metalTooltips = this.tag(TinkerTags.Fluids.METAL_TOOLTIPS);
        addTagOrOptional(metalTooltips,
                // vanilla ores
                TinkerFluids.moltenIron, TinkerFluids.moltenGold, TinkerFluids.moltenCopper,
                TinkerFluids.moltenCobalt, TinkerFluids.moltenSteel, TinkerFluids.moltenDebris,
                // base alloys
                TinkerFluids.moltenSlimesteel, TinkerFluids.moltenAmethystBronze, TinkerFluids.moltenRoseGold, TinkerFluids.moltenPigIron,
                TinkerFluids.moltenManyullyn, TinkerFluids.moltenHepatizon, TinkerFluids.moltenQueensSlime, TinkerFluids.moltenCinderslime,
                TinkerFluids.moltenNetherite, TinkerFluids.moltenSoulsteel, TinkerFluids.moltenKnightmetal, TinkerFluids.moltenKnightslime,
                // compat ores
                TinkerFluids.moltenTin, TinkerFluids.moltenAluminum, TinkerFluids.moltenLead,
                TinkerFluids.moltenSilver, TinkerFluids.moltenNickel, TinkerFluids.moltenZinc,
                TinkerFluids.moltenPlatinum, TinkerFluids.moltenTungsten, TinkerFluids.moltenOsmium,
                TinkerFluids.moltenUranium, TinkerFluids.moltenChromium, TinkerFluids.moltenCadmium,
                TinkerFluids.moltenPolyethylene, TinkerFluids.moltenPolyvinylChloride,
                // compat alloys
                TinkerFluids.moltenBronze, TinkerFluids.moltenBrass, TinkerFluids.moltenElectrum,
                TinkerFluids.moltenInvar, TinkerFluids.moltenConstantan, TinkerFluids.moltenPewter,
                // thermal alloys
                TinkerFluids.moltenEnderium, TinkerFluids.moltenLumium, TinkerFluids.moltenSignalum,
                // mekanism alloys
                TinkerFluids.moltenRefinedGlowstone, TinkerFluids.moltenRefinedObsidian,
                // cosmere alloys
                TinkerFluids.moltenNicrosil, TinkerFluids.moltenDuralumin, TinkerFluids.moltenBendalloy,
                // Twilight alloys
                TinkerFluids.moltenSteeleaf
        );
        metalTooltips.add(BuiltInRegistries.FLUID.getResourceKey(TinkerFluids.moltenCinderslime.get()).orElseThrow());

        this.tag(TinkerTags.Fluids.LARGE_GEM_TOOLTIPS).addTags(TinkerFluids.moltenEmerald.getTag(), TinkerFluids.moltenDiamond.getTag());
        this.tag(TinkerTags.Fluids.SMALL_GEM_TOOLTIPS).addTags(TinkerFluids.moltenQuartz.getTag(), TinkerFluids.moltenAmethyst.getTag());
        this.tag(MantleTags.Fluids.SOUP).addTag(TinkerFluids.meatSoup.getTag()).addOptionalTag(TinkerTags.Fluids.SOUP_TOOLTIPS.location());

        // hide upcoming fluids
        tag(TinkerTags.Fluids.HIDDEN_IN_RECIPE_VIEWERS).add(TinkerFluids.moltenKnightslime.get(), TinkerFluids.moltenSoulsteel.get());
        // hide upcoming fluids that require NBT. Can expand this list if other mods report problems
        tag(TinkerTags.Fluids.HIDE_IN_CREATIVE_TANKS).add(TinkerFluids.potion.get()).addTag(TinkerTags.Fluids.HIDDEN_IN_RECIPE_VIEWERS);

        addonTags.accept(new FluidTagRegistrar());
    }

    @Override
    public String getName() {
        return "Tinkers Construct Fluid TinkerTags";
    }

    /** Adds tags for an unplacable fluid */
    private void fluidTag(FluidObject<?> fluid) {
        if (TinkerFluids.isCompatMaterialFluid(fluid)) {
            return;
        }
        tag(Objects.requireNonNull(fluid.getCommonTag())).add(fluid.get());
    }

    /** Adds tags for a placable fluid */
    private void fluidTag(FlowingFluidObject<?> fluid) {
        if (TinkerFluids.isCompatMaterialFluid(fluid)) {
            return;
        }
        tag(fluid.getLocalTag()).add(fluid.getStill(), fluid.getFlowing());
        TagKey<Fluid> tag = fluid.getCommonTag();
        if (tag != null) {
            tag(tag).addTag(fluid.getLocalTag());
        }
    }

    private static void addLocalTags(TagAppender<Fluid> appender, FlowingFluidObject<?>... fluids) {
        for (FlowingFluidObject<?> fluid : fluids) {
            if (!TinkerFluids.isCompatMaterialFluid(fluid)) {
                appender.addTag(fluid.getLocalTag());
            }
        }
    }

    private static void addTagOrOptional(TagAppender<Fluid> appender, FlowingFluidObject<?>... fluids) {
        for (FlowingFluidObject<?> fluid : fluids) {
            TagKey<Fluid> tag = fluid.getTag();
            if (TinkerFluids.isCompatMaterialFluid(fluid)) {
                appender.addOptionalTag(tag.location());
            } else {
                appender.addTag(tag);
            }
        }
    }

    private final class FluidTagRegistrar implements DatagenTagProviderRegistrar.FluidTagRegistrar {
        @Override
        public void add(FlowingFluidObject<?> fluid) {
            FluidTagProvider.this.fluidTag(fluid);
        }
    }
}
