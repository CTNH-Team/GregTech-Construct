package slimeknights.tconstruct.library.addon;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.util.JsonHelper;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public interface DynamicDataRegistrar {
  void addData(ResourceLocation location, byte[] bytes);

  void addFilter(ResourceLocation location);

  void addRecipeFilter(ResourceLocation recipeId);

  default void addJson(ResourceLocation location, JsonElement json) {
    addData(location, json.toString().getBytes(StandardCharsets.UTF_8));
  }

  default void addJson(String folder, ResourceLocation id, JsonElement json) {
    addJson(fileLocation(folder, id), json);
  }

  default void addJson(String folder, ResourceLocation id, Object object) {
    addJson(folder, id, JsonHelper.DEFAULT_GSON.toJsonTree(object));
  }

  default void addJson(String folder, ResourceLocation id, Object object, Gson gson) {
    addJson(folder, id, gson.toJsonTree(object));
  }

  default void addRecipe(FinishedRecipe recipe) {
    ResourceLocation recipeId = recipe.getId();
    addJson(fileLocation("recipes", recipeId), recipe.serializeRecipe());
    addRecipeFilter(recipeId);

    JsonObject advancement = recipe.serializeAdvancement();
    if (advancement != null) {
      ResourceLocation advancementId = Objects.requireNonNull(recipe.getAdvancementId());
      ResourceLocation advancementLocation = fileLocation("advancements", advancementId);
      addJson(advancementLocation, advancement);
      addFilter(advancementLocation);
    }
  }

  static ResourceLocation fileLocation(String folder, ResourceLocation id) {
    return ResourceLocation.tryBuild(id.getNamespace(), folder + "/" + id.getPath() + ".json");
  }
}
