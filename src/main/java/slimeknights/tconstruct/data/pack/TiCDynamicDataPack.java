package slimeknights.tconstruct.data.pack;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.SharedConstants;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.fml.loading.FMLPaths;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Dynamic data pack for server-side data (recipes, advancements, etc.).
 * Uses {@link TiCDynamicPackContents} for in-memory storage of generated data.
 *
 * @author based on GTDynamicDataPack by GTCEu
 */
@ParametersAreNonnullByDefault
public class TiCDynamicDataPack implements PackResources {

    /** Set of resource domains that are considered server-side data */
    protected static final ObjectSet<String> SERVER_DOMAINS = new ObjectOpenHashSet<>();

    /** Shared contents between data pack instances */
    protected static final TiCDynamicPackContents CONTENTS = new TiCDynamicPackContents();

    /** Set of recipe IDs to filter out via pack metadata */
    public static final Set<ResourceLocation> RECIPE_FILTERS = new HashSet<>();

    /** The name of this pack */
    private final String name;

    static {
        SERVER_DOMAINS.addAll(Sets.newHashSet(TConstruct.MOD_ID, "minecraft", "forge", "c"));
    }

    public TiCDynamicDataPack(String name) {
        this(name, Set.of());
    }

    public TiCDynamicDataPack(String name, Collection<String> additionalDomains) {
        this.name = name;
        SERVER_DOMAINS.addAll(additionalDomains);
    }

    /**
     * Clears all server-side data from the pack contents.
     * Should be called on server stop or data reload.
     */
    public static void clearServer() {
        CONTENTS.clearData();
        RECIPE_FILTERS.clear();
    }

    /**
     * Adds data to the pack contents.
     *
     * @param location the resource location for the data
     * @param bytes the data bytes to store
     */
    private static void addToData(ResourceLocation location, byte[] bytes) {
        CONTENTS.addToData(location, bytes);
    }

    /**
     * Adds a finished recipe to the dynamic data pack.
     * Also adds the associated advancement if present.
     *
     * @param recipe the finished recipe to add
     */
    public static void addRecipe(FinishedRecipe recipe) {
        JsonObject recipeJson = recipe.serializeRecipe();
        byte[] recipeBytes = recipeJson.toString().getBytes(StandardCharsets.UTF_8);
        ResourceLocation recipeId = recipe.getId();
        addToData(getRecipeLocation(recipeId), recipeBytes);

        if (recipe.serializeAdvancement() != null) {
            JsonObject advancement = recipe.serializeAdvancement();
            byte[] advancementBytes = advancement.toString().getBytes(StandardCharsets.UTF_8);
            addToData(getAdvancementLocation(Objects.requireNonNull(recipe.getAdvancementId())), advancementBytes);
        }
    }

    /**
     * Writes JSON data to a file in the dump directory for debugging.
     *
     * @param id the resource location
     * @param subdir the subdirectory (e.g., "recipes", "advancements")
     * @param parent the parent directory
     * @param json the JSON bytes to write
     */
    public static void writeJson(ResourceLocation id, @Nullable String subdir, Path parent, byte[] json) {
        try {
            Path file;
            if (subdir != null) {
                file = parent.resolve(id.getNamespace()).resolve(subdir).resolve(id.getPath() + ".json");
            } else {
                file = parent.resolve(id.getNamespace()).resolve(id.getPath());
            }
            Files.createDirectories(file.getParent());
            try (OutputStream output = Files.newOutputStream(file)) {
                output.write(json);
            }
        } catch (IOException e) {
            TConstruct.LOG.error("Failed to write JSON export for file {}", id, e);
        }
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        if (elements.length > 0 && elements[0].equals("pack.png")) {
            return () -> TConstruct.class.getResourceAsStream("/icon.png");
        }
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type == PackType.SERVER_DATA) {
            return CONTENTS.getResource(location);
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.SERVER_DATA) {
            CONTENTS.listResources(namespace, path, resourceOutput);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? SERVER_DOMAINS : Set.of();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        if (metaReader == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(
                    Component.literal("Tinkers' Construct dynamic data"),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA));
        } else if (metaReader.getMetadataSectionName().equals("filter")) {
            JsonObject filter = new JsonObject();
            JsonArray block = new JsonArray();
            // Add recipe filters - these are recipes that should be blocked from loading
            RECIPE_FILTERS.forEach((id) -> {
                JsonObject entry = new JsonObject();
                entry.addProperty("namespace", "^" + id.getNamespace().replaceAll("[\\W]", "\\\\$0") + "$");
                entry.addProperty("path", "^recipes/" + id.getPath().replaceAll("[\\W]", "\\\\$0") + "\\.json$");
                block.add(entry);
            });
            filter.add("block", block);
            return metaReader.fromJson(filter);
        }
        return null;
    }

    @Override
    public @NotNull String packId() {
        return this.name;
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public void close() {
        // NOOP
    }

    /**
     * Gets the resource location for a recipe file.
     *
     * @param recipeId the recipe ID
     * @return the full resource location including path prefix and .json suffix
     */
    public static ResourceLocation getRecipeLocation(ResourceLocation recipeId) {
        return new ResourceLocation(recipeId.getNamespace(), "recipes/" + recipeId.getPath() + ".json");
    }

    /**
     * Gets the resource location for an advancement file.
     *
     * @param advancementId the advancement ID
     * @return the full resource location including path prefix and .json suffix
     */
    public static ResourceLocation getAdvancementLocation(ResourceLocation advancementId) {
        return new ResourceLocation(advancementId.getNamespace(), "advancements/" + advancementId.getPath() + ".json");
    }

    /**
     * Gets the resource location for a tag file.
     *
     * @param identifier the tag type identifier (e.g., "items", "blocks")
     * @param tagId the tag ID
     * @return the full resource location including path prefix and .json suffix
     */
    public static ResourceLocation getTagLocation(String identifier, ResourceLocation tagId) {
        return new ResourceLocation(tagId.getNamespace(), "tags/" + identifier + "/" + tagId.getPath() + ".json");
    }
}
