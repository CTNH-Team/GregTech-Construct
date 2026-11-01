package slimeknights.tconstruct.shared;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Utility class to grant advancements to players, used by remaining static advancement grant call sites. */
public final class AchievementEvents {

  /** Grants the given advancement to the player */
  public static void grantAdvancement(ServerPlayer playerMP, ResourceLocation advancementResource) {
    if (advancementResource == null) {
      return;
    }
    MinecraftServer server = playerMP.getServer();
    if (server != null) {
      Advancement advancement = server.getAdvancements().getAdvancement(advancementResource);
      if (advancement != null) {
        AdvancementProgress advancementProgress = playerMP.getAdvancements().getOrStartProgress(advancement);
        if (!advancementProgress.isDone()) {
          // we use playerAdvancements.grantCriterion instead of progress.grantCriterion for the visibility stuff and toasts
          advancementProgress.getRemainingCriteria().forEach(criterion -> playerMP.getAdvancements().award(advancement, criterion));
        }
      }
    }
  }

  private AchievementEvents() {}
}