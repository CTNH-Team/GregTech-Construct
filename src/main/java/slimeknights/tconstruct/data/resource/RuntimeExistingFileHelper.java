package slimeknights.tconstruct.data.resource;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.server.ServerLifecycleHooks;
import slimeknights.tconstruct.TConstruct;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RuntimeExistingFileHelper extends ExistingFileHelper {
  public static final RuntimeExistingFileHelper INSTANCE = new RuntimeExistingFileHelper(HashMultimap.create());

  private final Multimap<PackType, ResourceLocation> generated;
  private final List<PathPackResources> resourcePacks = new ArrayList<>();

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
    if (PackType.CLIENT_RESOURCES == packType && (".png".equals(pathSuffix) || ".png.mcmeta".equals(pathSuffix))) {
      return actualExists(loc.withPath(path -> pathPrefix + "/" + path + pathSuffix), packType);
    }
    return super.exists(loc, packType, pathSuffix, pathPrefix);
  }

  @Override
  public Resource getResource(ResourceLocation loc, PackType packType) throws FileNotFoundException {
    Resource runtimeResource = getRuntimeResource(loc, packType);
    if (runtimeResource != null) {
      return runtimeResource;
    }
    for (PathPackResources pack : resourcePacks) {
      IoSupplier<InputStream> supplier = pack.getResource(packType, loc);
      if (supplier != null) {
        return new Resource(pack, supplier);
      }
    }
    IoSupplier<InputStream> classpathResource = getClasspathResource(loc, packType);
    if (classpathResource != null) {
      return new Resource(fallbackPack(), classpathResource);
    }
    throw new FileNotFoundException(loc.toString());
  }

  private boolean actualExists(ResourceLocation loc, PackType packType) {
    if (generated.get(packType).contains(loc)) {
      return true;
    }
    if (getRuntimeResource(loc, packType) != null) {
      return true;
    }
    for (PathPackResources pack : resourcePacks) {
      if (pack.getResource(packType, loc) != null) {
        return true;
      }
    }
    return getClasspathResource(loc, packType) != null;
  }

  private static Resource getRuntimeResource(ResourceLocation loc, PackType packType) {
    ResourceManager manager = getRuntimeManager(packType);
    if (manager == null) {
      return null;
    }
    return manager.getResource(loc).orElse(null);
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
    try {
      Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
      Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
      if (minecraft == null) {
        return null;
      }
      Object manager = minecraftClass.getMethod("getResourceManager").invoke(minecraft);
      return manager instanceof ResourceManager resourceManager ? resourceManager : null;
    } catch (ReflectiveOperationException | LinkageError ignored) {
      return null;
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

  private PathPackResources fallbackPack() {
    if (!resourcePacks.isEmpty()) {
      return resourcePacks.get(0);
    }
    Path path = Path.of(".");
    return new PathPackResources(path.toString(), path, false);
  }

  private void addPack(Path path) {
    if (Files.exists(path)) {
      resourcePacks.add(new PathPackResources(path.toString(), path, false));
    }
  }
}
