package slimeknights.tconstruct.data.resource;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import slimeknights.tconstruct.TConstruct;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RuntimeExistingFileHelper extends ExistingFileHelper {
  public static final RuntimeExistingFileHelper INSTANCE = new RuntimeExistingFileHelper(HashMultimap.create());

  private static final PackResources CLASSPATH_PACK = new ClasspathPackResources();

  private final Multimap<PackType, ResourceLocation> generated;
  private final List<PathPackResources> resourcePacks = new ArrayList<>();
  private Active activeHelper;

  protected RuntimeExistingFileHelper(Multimap<PackType, ResourceLocation> generated) {
    super(Collections.emptySet(), Collections.emptySet(), false, null, null);
    this.generated = generated;
    addPack(Path.of("src", "main", "resources"));
    addPack(Path.of("src", "generated", "resources"));
  }

  @Override
  public boolean exists(ResourceLocation loc, PackType packType) {
    return true;
  }

  public Active activeHelper() {
    if (activeHelper == null) {
      activeHelper = new Active(generated);
    }
    return activeHelper;
  }

  @Override
  public void trackGenerated(ResourceLocation loc, IResourceType type) {
    trackGenerated(loc, type.getPackType(), type.getSuffix(), type.getPrefix());
  }

  @Override
  public void trackGenerated(ResourceLocation loc, PackType packType, String suffix, String prefix) {
    generated.put(packType, loc.withPath(path -> prefix + "/" + path + suffix));
  }

  @Override
  public Resource getResource(ResourceLocation loc, PackType packType, String pathSuffix, String pathPrefix) throws FileNotFoundException {
    return getResource(loc.withPath(path -> pathPrefix + "/" + path + pathSuffix), packType);
  }

  @Override
  public boolean exists(ResourceLocation loc, PackType packType, String pathSuffix, String pathPrefix) {
    return true;
  }

  @Override
  public Resource getResource(ResourceLocation loc, PackType packType) throws FileNotFoundException {
    Resource runtimeResource = getRuntimeResource(loc, packType);
    if (runtimeResource != null) {
      return runtimeResource;
    }
    Resource vanillaResource = getVanillaResource(loc, packType);
    if (vanillaResource != null) {
      return vanillaResource;
    }
    for (PathPackResources pack : resourcePacks) {
      IoSupplier<InputStream> supplier = pack.getResource(packType, loc);
      if (supplier != null) {
        return new Resource(pack, supplier);
      }
    }
    IoSupplier<InputStream> classpathResource = getClasspathResource(loc, packType);
    if (classpathResource != null) {
      return new Resource(CLASSPATH_PACK, classpathResource);
    }
    throw new FileNotFoundException(loc.toString());
  }

  protected boolean actualExists(ResourceLocation loc, PackType packType) {
    if (generated.get(packType).contains(loc)) {
      return true;
    }
    return getRuntimeResource(loc, packType) != null;
  }

  private static Resource getRuntimeResource(ResourceLocation loc, PackType packType) {
    ResourceManager manager = getRuntimeManager(packType);
    if (manager == null) {
      return null;
    }
    return manager.getResource(loc).orElse(null);
  }

  private static Resource getVanillaResource(ResourceLocation loc, PackType packType) {
    if (packType != PackType.CLIENT_RESOURCES || !isClient()) {
      return null;
    }
    try {
      return ClientResources.getVanillaResource(loc, packType);
    } catch (RuntimeException | LinkageError ignored) {
      return null;
    }
  }

  private static ResourceManager getRuntimeManager(PackType packType) {
    try {
      if (packType == PackType.CLIENT_RESOURCES) {
        return getClientResourceManager();
      }
      if (packType == PackType.SERVER_DATA && ServerLifecycleHooks.getCurrentServer() != null) {
        return ServerLifecycleHooks.getCurrentServer().getResourceManager();
      }
    } catch (RuntimeException | LinkageError ignored) {
      return null;
    }
    return null;
  }

  private static ResourceManager getClientResourceManager() {
    if (!isClient()) {
      return null;
    }
    return ClientResources.getResourceManager();
  }

  private static boolean isClient() {
    return FMLEnvironment.dist == Dist.CLIENT;
  }

  @OnlyIn(Dist.CLIENT)
  private static final class ClientResources {
    private static ResourceManager getResourceManager() {
      return Minecraft.getInstance().getResourceManager();
    }

    private static Resource getVanillaResource(ResourceLocation loc, PackType packType) {
      PackResources pack = Minecraft.getInstance().getVanillaPackResources();
      IoSupplier<InputStream> supplier = pack.getResource(packType, loc);
      return supplier != null ? new Resource(pack, supplier) : null;
    }
  }

  private static IoSupplier<InputStream> getClasspathResource(ResourceLocation loc, PackType packType) {
    String path = "/" + packType.getDirectory() + "/" + loc.getNamespace() + "/" + loc.getPath();
    if (TConstruct.class.getResource(path) == null) {
      return null;
    }
    return () -> {
      InputStream input = TConstruct.class.getResourceAsStream(path);
      if (input == null) {
        throw new FileNotFoundException(path);
      }
      return input;
    };
  }

  private void addPack(Path path) {
    if (Files.exists(path)) {
      resourcePacks.add(new PathPackResources(path.toString(), path, false));
    }
  }

  private static class ClasspathPackResources implements PackResources {
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
      return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation loc) {
      return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput output) {}

    @Override
    public Set<String> getNamespaces(PackType packType) {
      return Set.of();
    }

    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
      return null;
    }

    @Override
    public String packId() {
      return "tconstruct/classpath";
    }

    @Override
    public void close() {}
  }

  public static class Active extends RuntimeExistingFileHelper implements AutoCloseable {
    private Active(Multimap<PackType, ResourceLocation> generated) {
      super(generated);
    }

    @Override
    public Active activeHelper() {
      return this;
    }

    @Override
    public boolean exists(ResourceLocation loc, PackType packType) {
      return this.actualExists(loc, packType);
    }

    @Override
    public boolean exists(ResourceLocation loc, PackType packType, String pathSuffix, String pathPrefix) {
      return this.actualExists(loc.withPath(path -> pathPrefix + "/" + path + pathSuffix), packType);
    }

    @Override
    public void close() {}
  }
}
