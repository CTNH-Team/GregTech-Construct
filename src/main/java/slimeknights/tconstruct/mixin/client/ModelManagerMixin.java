package slimeknights.tconstruct.mixin.client;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.events.RegisterDynamicResourcesEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 在模型管理器重载时触发动态资源注册事件。
 * 此时 ResourceManager 已初始化，可以安全访问纹理等资源。
 */
@Mixin(value = ModelManager.class)
public abstract class ModelManagerMixin {

  @Inject(method = "reload", at = @At(value = "HEAD"))
  private void tconstruct$loadDynamicResources(PreparableReloadListener.PreparationBarrier preparationBarrier,
                                                ResourceManager resourceManager, ProfilerFiller preparationsProfiler,
                                                ProfilerFiller reloadProfiler, Executor backgroundExecutor,
                                                Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
    if (!ModLoader.isLoadingStateValid()) {
      return;
    }

    long startTime = System.currentTimeMillis();
    ModLoader.get().postEventWrapContainerInModOrder(new RegisterDynamicResourcesEvent());
    TConstruct.LOG.info("TConstruct dynamic resource generation took {}ms", System.currentTimeMillis() - startTime);
  }
}
