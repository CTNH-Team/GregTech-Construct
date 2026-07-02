package slimeknights.tconstruct.data.pack;

import net.minecraft.data.PackOutput;

import java.nio.file.Path;

public final class DynamicPackOutput {
  private static final PackOutput DUMMY = new PackOutput(Path.of("build", "dynamic-pack-dummy"));

  private DynamicPackOutput() {}

  public static PackOutput dummy() {
    return DUMMY;
  }
}
