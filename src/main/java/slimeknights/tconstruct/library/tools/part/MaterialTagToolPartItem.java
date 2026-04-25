package slimeknights.tconstruct.library.tools.part;

import net.minecraft.tags.TagKey;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;

/**
 * Extension of {@link ToolPartItem} which restricts materials to only metals.
 * Used for GT tool parts like wrench heads, screwdriver heads, etc. that should
 * only be craftable from metal materials.
 */
public class MaterialTagToolPartItem extends ToolPartItem {

  private final TagKey<IMaterial> MaterialTag;

  public MaterialTagToolPartItem(Properties properties, MaterialStatsId id, TagKey<IMaterial> MaterialTag) {
    super(properties, id);
    this.MaterialTag = MaterialTag;
  }

  @Override
  public boolean canUseMaterial(MaterialId material) {
    if (!super.canUseMaterial(material)) {
      return false;
    }
    IMaterial mat = MaterialRegistry.getMaterial(material);
    if (mat == IMaterial.UNKNOWN) {
      return false;
    }
    return MaterialRegistry.getInstance().isInTag(material, MaterialTag);
  }
}
