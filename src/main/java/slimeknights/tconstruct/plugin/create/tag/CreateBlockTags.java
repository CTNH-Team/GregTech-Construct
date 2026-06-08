/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import slimeknights.tconstruct.TConstruct;

public final class CreateBlockTags {
  public static final TagKey<Block> FAN_TRANSPARENT = BlockTags.create(ResourceLocation.tryBuild("create", "fan_transparent"));
  public static final TagKey<Block> BLAZE_BURNER_TARGETS = BlockTags.create(TConstruct.getResource("create/blaze_burner_targets"));

  private CreateBlockTags() {}
}
