/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.addon.DynamicTagProviderRegistrar;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CreateItemTagProvider extends ItemTagsProvider {

    public CreateItemTagProvider(PackOutput output) {
        super(output, CompletableFuture.completedFuture(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)),
                CompletableFuture.completedFuture(TagLookup.empty()), TConstruct.MOD_ID,
                new ExistingFileHelper(List.of(), Set.of(), false, null, null));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addTags(new DynamicTagProviderRegistrar.ItemTagRegistrar() {
            @Override
            public void add(TagKey<Item> tag, ResourceLocation... ids) {
                IntrinsicTagAppender<Item> appender = CreateItemTagProvider.this.tag(tag);
                for (ResourceLocation id : ids) {
                    if (BuiltInRegistries.ITEM.containsKey(id)) {
                        appender.add(BuiltInRegistries.ITEM.get(id));
                    } else {
                        appender.addOptional(id);
                    }
                }
            }

            @Override
            public void addOptional(TagKey<Item> tag, ResourceLocation... ids) {
                for (ResourceLocation id : ids) {
                    CreateItemTagProvider.this.tag(tag).addOptional(id);
                }
            }
        });
    }

    public static void addTags(DynamicTagProviderRegistrar.ItemTagRegistrar tags) {
        tags.add(CreateItemTags.CRUSHING_BLACKLIST);
    }
}
