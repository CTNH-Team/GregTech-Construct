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
import slimeknights.tconstruct.library.addon.DynamicProviderRegistrar;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;
import slimeknights.tconstruct.data.pack.DynamicDataProviderRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class TiCDynamicTagGenerator {
    static final List<TagProviderEntry> ADDITIONAL_PROVIDER_ENTRIES = new ArrayList<>();

    private TiCDynamicTagGenerator() {}

    public static void register() {
        register((owner, providers) -> DynamicDataProviderRunner.run(owner, providers));
    }

    /** Adds an extra runtime tag provider for TiC's dynamic data pack. */
    public static synchronized void addProvider(String name, Function<PackOutput, ? extends DataProvider> factory) {
        ADDITIONAL_PROVIDER_ENTRIES.add(new TagProviderEntry(
            Objects.requireNonNull(name, "name"),
            Objects.requireNonNull(factory, "factory")
        ));
    }

    static void register(TagRunner runner) {
        runner.run("tconstruct-tags", createProviders());
    }

    public static void registerDefaultProviders(DynamicProviderRegistrar registrar) {
        TagProviderState state = new TagProviderState();
        registrar.addProvider("BlockTagProvider", state::createBlockTags);
        registrar.addProvider("ItemTagProvider", state::createItemTags);
        registrar.addProvider("FluidTagProvider", state::createFluidTags);
        registrar.addProvider("EntityTypeTagProvider", state::createEntityTypeTags);
        registrar.addProvider("BlockEntityTypeTagProvider", state::createBlockEntityTypeTags);
        registrar.addProvider("EnchantmentTagProvider", state::createEnchantmentTags);
        registrar.addProvider("MenuTypeTagProvider", state::createMenuTypeTags);
        registrar.addProvider("PotionTagProvider", state::createPotionTags);
        registrar.addProvider("DamageTypeTagProvider", state::createDamageTypeTags);
        registrar.addProvider("MaterialTagProvider", state::createMaterialTags);
        registrar.addProvider("ModifierTagProvider", state::createModifierTags);
    }

    static List<Function<PackOutput, ? extends DataProvider>> createProviders() {
        List<Function<PackOutput, ? extends DataProvider>> providers = new ArrayList<>();
        for (TagProviderEntry entry : createProviderEntries()) {
            providers.add(entry.factory());
        }
        return providers;
    }

    static List<TagProviderEntry> createProviderEntries() {
        List<TagProviderEntry> entries = new ArrayList<>();
        TiCAddonRegistry.collectTagProviders((name, factory) -> entries.add(new TagProviderEntry(name, factory)));
        synchronized (TiCDynamicTagGenerator.class) {
            entries.addAll(ADDITIONAL_PROVIDER_ENTRIES);
        }
        return List.copyOf(entries);
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
