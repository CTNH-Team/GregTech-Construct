/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.tag;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.tconstruct.TConstruct;

public final class CreateItemTags {
  public static final TagKey<Item> CRUSHING_BLACKLIST = ItemTags.create(TConstruct.getResource("create/crushing_blacklist"));

  private CreateItemTags() {}
}
