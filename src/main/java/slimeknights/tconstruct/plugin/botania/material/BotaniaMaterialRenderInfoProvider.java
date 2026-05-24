package slimeknights.tconstruct.plugin.botania.material;

import net.minecraft.data.PackOutput;
import slimeknights.tconstruct.data.resource.TiCDynamicResourceGenerator;
import slimeknights.tconstruct.library.client.data.material.AbstractMaterialRenderInfoProvider;

/**
 * Botania-owned material render info.
 */
public class BotaniaMaterialRenderInfoProvider extends AbstractMaterialRenderInfoProvider {
  public BotaniaMaterialRenderInfoProvider(PackOutput packOutput) {
    super(packOutput, new BotaniaMaterialSpriteProvider(), TiCDynamicResourceGenerator.createExistingFileHelperForAddons());
  }

  @Override
  protected void addMaterialRenderInfo() {
    buildRenderInfo(BotaniaMaterialIds.manaSteel).color(0x67B9EE).fallbacks("metal");
    buildRenderInfo(BotaniaMaterialIds.terraSteel).color(0x6ae862).fallbacks("metal");
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Botania Material Render Info";
  }
}
