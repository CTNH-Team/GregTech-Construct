package slimeknights.tconstruct.data.pack;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraftforge.fml.loading.FMLPaths;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.addon.ITiCAddon;
import slimeknights.tconstruct.library.addon.TiCAddonFinder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Collectors;

/**
 * Dynamic resource pack for client-side resources (models, blockstates, textures, etc.).
 * Uses {@link TiCDynamicPackContents} for in-memory storage of generated resources.
 *
 * @author based on GTDynamicResourcePack by GTCEu
 */
@ParametersAreNonnullByDefault
public class TiCDynamicResourcePack implements PackResources {

    /** Set of resource domains that are considered client-side resources */
    protected static final ObjectSet<String> CLIENT_DOMAINS = new ObjectOpenHashSet<>();

    /** Shared contents between resource pack instances */
    protected static final TiCDynamicPackContents CONTENTS = new TiCDynamicPackContents();

    /** Converter for blockstate file locations */
    public static final FileToIdConverter BLOCKSTATE_ID_CONVERTER = FileToIdConverter.json("blockstates");

    /** Converter for model file locations */
    public static final FileToIdConverter MODEL_ID_CONVERTER = FileToIdConverter.json("models");

    /** The name of this pack */
    private final String name;

    static {
        CLIENT_DOMAINS.addAll(Sets.newHashSet(TConstruct.MOD_ID, "minecraft", "forge", "c"));
    }

    public TiCDynamicResourcePack(String name) {
        this(name, TiCAddonFinder.getAddons().stream().map(ITiCAddon::addonModId).collect(Collectors.toSet()));
    }

    public TiCDynamicResourcePack(String name, Collection<String> additionalDomains) {
        this.name = name;
        CLIENT_DOMAINS.addAll(additionalDomains);
    }

    /**
     * Clears all client-side resources from the pack contents.
     * Should be called on resource reload or client disconnect.
     */
    public static void clearClient() {
        CLIENT_DOMAINS.clear();
        CLIENT_DOMAINS.addAll(Sets.newHashSet(TConstruct.MOD_ID, "minecraft", "forge", "c"));
        CONTENTS.clearData();
    }

    /**
     * Adds a JSON resource to the pack contents.
     *
     * @param location the resource location
     * @param obj the JSON element to store
     */
    public static void addResource(ResourceLocation location, JsonElement obj) {
        addResource(location, obj.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Adds raw bytes as a resource to the pack contents.
     *
     * @param location the resource location
     * @param data the data bytes to store
     */
    public static void addResource(ResourceLocation location, byte[] data) {
        CLIENT_DOMAINS.add(location.getNamespace());
        if (shouldDumpAssets()) {
            TiCDynamicDataPack.writeBytes(location, getDumpAssetsRoot(), data);
        }
        CONTENTS.addToData(location, data);
    }

    /**
     * Adds a texture to the pack. Automatically prepends "textures/" to the path if not present
     * and appends ".png" if missing.
     *
     * @param loc   the texture location (without "textures/" prefix or ".png" suffix)
     * @param data  the texture bytes to store
     */
    public static void addTexture(ResourceLocation loc, byte[] data) {
        String path = loc.getPath();
        if (!path.startsWith("textures/")) {
            loc = loc.withPrefix("textures/");
            path = loc.getPath();
        }
        if (!path.endsWith(".png")) {
            loc = loc.withSuffix(".png");
        }
        addResource(loc, data);
    }

    /**
     * Adds a block model to the pack. Automatically prepends "block/" to the path if not present.
     *
     * @param loc the model location (without "block/" prefix)
     * @param obj the JSON element representing the model
     */
    public static void addBlockModel(ResourceLocation loc, JsonElement obj) {
        if (!loc.getPath().startsWith("block/")) {
            loc = loc.withPrefix("block/");
        }
        addModel(loc, obj);
    }

    /**
     * Adds an item model to the pack. Automatically prepends "item/" to the path if not present.
     *
     * @param loc the model location (without "item/" prefix)
     * @param obj the JSON element representing the model
     */
    public static void addItemModel(ResourceLocation loc, JsonElement obj) {
        if (!loc.getPath().startsWith("item/")) {
            loc = loc.withPrefix("item/");
        }
        addModel(loc, obj);
    }

    /**
     * Adds a model to the pack. Converts the location to a file path under "models/" directory.
     *
     * @param loc the model location
     * @param obj the JSON element representing the model
     */
    public static void addModel(ResourceLocation loc, JsonElement obj) {
        loc = MODEL_ID_CONVERTER.idToFile(loc);
        addResource(loc, obj);
    }

    /**
     * Adds a blockstate definition to the pack. Converts the location to a file path under "blockstates/" directory.
     *
     * @param loc the blockstate location
     * @param stateJson the JSON element representing the blockstate
     */
    public static void addBlockState(ResourceLocation loc, JsonElement stateJson) {
        loc = BLOCKSTATE_ID_CONVERTER.idToFile(loc);
        addResource(loc, stateJson);
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
        if (type == PackType.CLIENT_RESOURCES) {
            return CONTENTS.getResource(location);
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.CLIENT_RESOURCES) {
            CONTENTS.listResources(namespace, path, resourceOutput);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? CLIENT_DOMAINS : Set.of();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        if (metaReader == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(
                    Component.literal("Tinkers' Construct dynamic assets"),
                    SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));
        }
        return null;
    }

    @Override
    public String packId() {
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

    /** Dumps all currently captured dynamic client resources to disk if the config is enabled. */
    public static void dumpAllAssetsIfConfigured() {
        if (!shouldDumpAssets()) {
            return;
        }
        Path parent = getDumpAssetsRoot();
        for (String namespace : new LinkedHashSet<>(CLIENT_DOMAINS)) {
            CONTENTS.listResources(namespace, "", (location, supplier) -> {
                try (InputStream input = supplier.get()) {
                    TiCDynamicDataPack.writeBytes(location, parent, input.readAllBytes());
                } catch (IOException exception) {
                    TConstruct.LOG.error("Failed to dump dynamic asset {}", location, exception);
                }
            });
        }
    }

    private static Path getDumpAssetsRoot() {
        return FMLPaths.GAMEDIR.get().resolve(TConstruct.MOD_ID).resolve("dumped").resolve("assets");
    }

    private static boolean shouldDumpAssets() {
        try {
            return Config.COMMON.dumpAssets.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}
