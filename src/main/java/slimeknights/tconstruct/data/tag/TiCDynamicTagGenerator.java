package slimeknights.tconstruct.data.tag;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.data.DamageTypeProvider;
import slimeknights.tconstruct.common.data.tags.BlockEntityTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.BlockTagProvider;
import slimeknights.tconstruct.common.data.tags.DamageTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.EnchantmentTagProvider;
import slimeknights.tconstruct.common.data.tags.EntityTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.FluidTagProvider;
import slimeknights.tconstruct.common.data.tags.ItemTagProvider;
import slimeknights.tconstruct.common.data.tags.MaterialTagProvider;
import slimeknights.tconstruct.common.data.tags.MenuTypeTagProvider;
import slimeknights.tconstruct.common.data.tags.ModifierTagProvider;
import slimeknights.tconstruct.common.data.tags.PotionTagProvider;
import slimeknights.tconstruct.data.pack.DynamicDataProviderRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TiCDynamicTagGenerator {
    private TiCDynamicTagGenerator() {}

    public static void register() {
        register((owner, providers) -> DynamicDataProviderRunner.run(owner, providers));
    }

    static void register(TagRunner runner) {
        runner.run("tconstruct-tags", createProviders());
    }

    static List<Function<PackOutput, ? extends DataProvider>> createProviders() {
        List<Function<PackOutput, ? extends DataProvider>> providers = new ArrayList<>();
        for (TagProviderEntry entry : createProviderEntries()) {
            providers.add(entry.factory());
        }
        return providers;
    }

    static List<TagProviderEntry> createProviderEntries() {
        TagProviderState state = new TagProviderState();
        return List.of(
            new TagProviderEntry("BlockTagProvider", state::createBlockTags),
            new TagProviderEntry("ItemTagProvider", state::createItemTags),
            new TagProviderEntry("FluidTagProvider", state::createFluidTags),
            new TagProviderEntry("EntityTypeTagProvider", state::createEntityTypeTags),
            new TagProviderEntry("BlockEntityTypeTagProvider", state::createBlockEntityTypeTags),
            new TagProviderEntry("EnchantmentTagProvider", state::createEnchantmentTags),
            new TagProviderEntry("MenuTypeTagProvider", state::createMenuTypeTags),
            new TagProviderEntry("PotionTagProvider", state::createPotionTags),
            new TagProviderEntry("DamageTypeTagProvider", state::createDamageTypeTags),
            // Biome tags are not supported in the dynamic data pack path on Forge 1.20.1.
            // Keep them in normal datagen instead.
            // new TagProviderEntry("BiomeTagProvider", state::createBiomeTags),
            new TagProviderEntry("MaterialTagProvider", state::createMaterialTags),
            new TagProviderEntry("ModifierTagProvider", state::createModifierTags)
        );
    }

    @FunctionalInterface
    interface TagRunner {
        void run(String owner, List<Function<PackOutput, ? extends DataProvider>> providers);
    }

    record TagProviderEntry(String name, Function<PackOutput, ? extends DataProvider> factory) {}

    private static final class TagProviderState {
        private CompletableFuture<Provider> lookupProvider;
        private final ExistingFileHelper existingFileHelper = createExistingFileHelper();
        private BlockTagProvider blockTags;
        private DatapackBuiltinEntriesProvider datapackRegistryProvider;

        private CompletableFuture<Provider> lookupProvider() {
            if (lookupProvider == null) {
                lookupProvider = CompletableFuture.completedFuture(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
            }
            return lookupProvider;
        }

        private BlockTagProvider createBlockTags(PackOutput output) {
            this.blockTags = new BlockTagProvider(output, lookupProvider(), existingFileHelper);
            return this.blockTags;
        }

        private ItemTagProvider createItemTags(PackOutput output) {
            return new ItemTagProvider(output, lookupProvider(), blockTags.contentsGetter(), existingFileHelper);
        }

        private FluidTagProvider createFluidTags(PackOutput output) {
            return new FluidTagProvider(output, lookupProvider(), existingFileHelper);
        }

        private EntityTypeTagProvider createEntityTypeTags(PackOutput output) {
            return new EntityTypeTagProvider(output, lookupProvider(), existingFileHelper);
        }

        private BlockEntityTypeTagProvider createBlockEntityTypeTags(PackOutput output) {
            return new BlockEntityTypeTagProvider(output, lookupProvider(), existingFileHelper);
        }

        private EnchantmentTagProvider createEnchantmentTags(PackOutput output) {
            return new EnchantmentTagProvider(output, lookupProvider(), existingFileHelper);
        }

        private MenuTypeTagProvider createMenuTypeTags(PackOutput output) {
            return new MenuTypeTagProvider(output, lookupProvider(), existingFileHelper);
        }

        private PotionTagProvider createPotionTags(PackOutput output) {
            return new PotionTagProvider(output, lookupProvider(), existingFileHelper);
        }

        private DamageTypeTagProvider createDamageTypeTags(PackOutput output) {
            return new DamageTypeTagProvider(output, registryProvider(output).getRegistryProvider(), existingFileHelper);
        }

        private MaterialTagProvider createMaterialTags(PackOutput output) {
            return new MaterialTagProvider(output, existingFileHelper);
        }

        private ModifierTagProvider createModifierTags(PackOutput output) {
            return new ModifierTagProvider(output, existingFileHelper);
        }

        private DatapackBuiltinEntriesProvider registryProvider(PackOutput output) {
            if (datapackRegistryProvider == null) {
                RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder();
                DamageTypeProvider.register(registrySetBuilder);
                datapackRegistryProvider = new DatapackBuiltinEntriesProvider(output, lookupProvider(), registrySetBuilder, Set.of(TConstruct.MOD_ID));
            }
            return datapackRegistryProvider;
        }

        private static ExistingFileHelper createExistingFileHelper() {
            return new ExistingFileHelper(List.of(), Set.of(), false, null, null);
        }
    }
}
