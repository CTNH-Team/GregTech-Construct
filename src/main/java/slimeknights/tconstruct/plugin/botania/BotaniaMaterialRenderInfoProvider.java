package slimeknights.tconstruct.plugin.botania;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator;
import slimeknights.tconstruct.library.client.data.material.AbstractMaterialRenderInfoProvider;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

/**
 * Botania-owned material render info.
 */
public class BotaniaMaterialRenderInfoProvider extends AbstractMaterialRenderInfoProvider {
  public BotaniaMaterialRenderInfoProvider(PackOutput packOutput) {
    super(packOutput, new BotaniaMaterialSpriteProvider(), TiCDynamicResourceGenerator.createExistingFileHelperForAddons());
  }

  @Override
  protected void addMaterialRenderInfo() {
    buildRenderInfo(MaterialIds.manaSteel).color(0x67B9EE).fallbacks("metal");
    buildRenderInfo(MaterialIds.terraSteel).color(0x6ae862).fallbacks("metal");
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Botania Material Render Info";
  }
}
