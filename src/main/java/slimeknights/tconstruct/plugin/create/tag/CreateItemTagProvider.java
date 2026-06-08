/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.tconstruct.TConstruct;

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
        tag(CreateItemTags.CRUSHING_BLACKLIST);
    }
}
