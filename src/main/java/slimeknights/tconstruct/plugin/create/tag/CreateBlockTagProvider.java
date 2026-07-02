/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.addon.DatagenTagProviderRegistrar;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CreateBlockTagProvider extends BlockTagsProvider {

    public CreateBlockTagProvider(PackOutput output) {
        super(output, CompletableFuture.completedFuture(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)),
                TConstruct.MOD_ID, new ExistingFileHelper(List.of(), Set.of(), false, null, null));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        addTags(new DatagenTagProviderRegistrar.BlockTagRegistrar() {
            @Override
            public void add(TagKey<Block> tag, ResourceLocation... ids) {
                IntrinsicTagAppender<Block> appender = CreateBlockTagProvider.this.tag(tag);
                for (ResourceLocation id : ids) {
                    if (BuiltInRegistries.BLOCK.containsKey(id)) {
                        appender.add(BuiltInRegistries.BLOCK.get(id));
                    } else {
                        appender.addOptional(id);
                    }
                }
            }

            @Override
            public void addOptional(TagKey<Block> tag, ResourceLocation... ids) {
                for (ResourceLocation id : ids) {
                    CreateBlockTagProvider.this.tag(tag).addOptional(id);
                }
            }
        });
    }

    public static void addTags(DatagenTagProviderRegistrar.BlockTagRegistrar tags) {
        tags.add(CreateBlockTags.FAN_TRANSPARENT,
                TinkerSmeltery.searedBasin.getId(), TinkerSmeltery.searedTable.getId(),
                TinkerSmeltery.scorchedBasin.getId(), TinkerSmeltery.scorchedTable.getId(),
                TinkerCommons.soulGlass.getId(), TinkerCommons.soulGlassPane.getId());
        tags.add(CreateBlockTags.BLAZE_BURNER_TARGETS,
                TinkerSmeltery.searedMelter.getId(), TinkerSmeltery.scorchedAlloyer.getId());
    }

}
