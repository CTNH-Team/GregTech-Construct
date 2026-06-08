/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.tag;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.tconstruct.TConstruct;
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
        tag(CreateBlockTags.FAN_TRANSPARENT)
                .add(TinkerSmeltery.searedBasin.get(), TinkerSmeltery.searedTable.get(),
                        TinkerSmeltery.scorchedBasin.get(), TinkerSmeltery.scorchedTable.get(),
                        TinkerCommons.soulGlass.get(), TinkerCommons.soulGlassPane.get());
        tag(CreateBlockTags.BLAZE_BURNER_TARGETS)
                .add(TinkerSmeltery.searedMelter.get(), TinkerSmeltery.scorchedAlloyer.get());
    }
}
