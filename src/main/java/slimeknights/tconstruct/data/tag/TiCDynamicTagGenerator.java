package slimeknights.tconstruct.data.tag;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
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
import slimeknights.tconstruct.library.addon.DynamicTagProviderRegistrar;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class TiCDynamicTagGenerator {
    private TiCDynamicTagGenerator() {}

    public static void addDatagenProviders(DataGenerator generator, PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper, boolean server) {
        DynamicTagProviderRegistrar registrar = new DynamicTagProviderRegistrar();
        TiCAddonRegistry.collectTagProviders(registrar);
        TagProviderState state = new TagProviderState(output, lookupProvider, existingFileHelper, registrar);
        BlockTagProvider blockTags = state.createBlockTags();
        generator.addProvider(server, blockTags);
        generator.addProvider(server, state.createItemTags(blockTags));
        generator.addProvider(server, state.createFluidTags());
        generator.addProvider(server, state.createEntityTypeTags());
        generator.addProvider(server, state.createBlockEntityTypeTags());
        generator.addProvider(server, state.createEnchantmentTags());
        generator.addProvider(server, state.createMenuTypeTags());
        generator.addProvider(server, state.createPotionTags());
        generator.addProvider(server, state.createDamageTypeTags());
        generator.addProvider(server, state.createMaterialTags());
        generator.addProvider(server, state.createModifierTags());
    }

    private static final class TagProviderState {
        private final PackOutput output;
        private final CompletableFuture<Provider> lookupProvider;
        private final ExistingFileHelper existingFileHelper;
        private final DynamicTagProviderRegistrar registrar;
        private DatapackBuiltinEntriesProvider datapackRegistryProvider;

        private TagProviderState(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper, DynamicTagProviderRegistrar registrar) {
            this.output = output;
            this.lookupProvider = lookupProvider;
            this.existingFileHelper = existingFileHelper;
            this.registrar = registrar;
        }

        private CompletableFuture<Provider> registryLookupProvider() {
            return CompletableFuture.completedFuture(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        }

        private BlockTagProvider createBlockTags() {
            return new BlockTagProvider(output, lookupProvider, existingFileHelper, registrar::applyBlockTags);
        }

        private ItemTagProvider createItemTags(BlockTagProvider blockTags) {
            return new ItemTagProvider(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper, registrar::applyItemTags);
        }

        private FluidTagProvider createFluidTags() {
            return new FluidTagProvider(output, lookupProvider, existingFileHelper, registrar::applyFluidTags);
        }

        private EntityTypeTagProvider createEntityTypeTags() {
            return new EntityTypeTagProvider(output, registryLookupProvider(), existingFileHelper);
        }

        private BlockEntityTypeTagProvider createBlockEntityTypeTags() {
            return new BlockEntityTypeTagProvider(output, registryLookupProvider(), existingFileHelper);
        }

        private EnchantmentTagProvider createEnchantmentTags() {
            return new EnchantmentTagProvider(output, registryLookupProvider(), existingFileHelper);
        }

        private MenuTypeTagProvider createMenuTypeTags() {
            return new MenuTypeTagProvider(output, registryLookupProvider(), existingFileHelper);
        }

        private PotionTagProvider createPotionTags() {
            return new PotionTagProvider(output, registryLookupProvider(), existingFileHelper);
        }

        private DamageTypeTagProvider createDamageTypeTags() {
            return new DamageTypeTagProvider(output, registryProvider().getRegistryProvider(), existingFileHelper);
        }

        private MaterialTagProvider createMaterialTags() {
            return new MaterialTagProvider(output, existingFileHelper, registrar::applyMaterialTags);
        }

        private ModifierTagProvider createModifierTags() {
            return new ModifierTagProvider(output, existingFileHelper, registrar::applyModifierTags);
        }

        private DatapackBuiltinEntriesProvider registryProvider() {
            if (datapackRegistryProvider == null) {
                RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder();
                DamageTypeProvider.register(registrySetBuilder);
                datapackRegistryProvider = new DatapackBuiltinEntriesProvider(output, lookupProvider, registrySetBuilder, Set.of(TConstruct.MOD_ID));
            }
            return datapackRegistryProvider;
        }
    }
}