package slimeknights.tconstruct.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class TConstructMixinPlugin implements IMixinConfigPlugin {
  private static final String CREATE_MIXIN_PACKAGE = "slimeknights.tconstruct.mixin.create.";
  private static final String TCONSTRUCT_CREATE_MIXIN_PACKAGE = "slimeknights.tconstruct.mixin.tconstruct.create.";
  private static final String BOTANIA_MIXIN_PACKAGE = "slimeknights.tconstruct.mixin.botania.";

  private final boolean createLoaded = LoadingModList.get().getModFileById("create") != null;
  private final boolean botaniaLoaded = LoadingModList.get().getModFileById("botania") != null;

  @Override
  public void onLoad(String mixinPackage) {}

  @Nullable
  @Override
  public String getRefMapperConfig() {
    return null;
  }

  @Override
  public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
    if (mixinClassName.startsWith(CREATE_MIXIN_PACKAGE) || mixinClassName.startsWith(TCONSTRUCT_CREATE_MIXIN_PACKAGE)) {
      return createLoaded;
    }
    if (mixinClassName.startsWith(BOTANIA_MIXIN_PACKAGE)) {
      return botaniaLoaded;
    }
    return true;
  }

  @Override
  public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

  @Nullable
  @Override
  public List<String> getMixins() {
    return null;
  }

  @Override
  public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

  @Override
  public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
