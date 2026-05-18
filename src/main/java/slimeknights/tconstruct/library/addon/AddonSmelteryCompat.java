package slimeknights.tconstruct.library.addon;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.fluid.texture.AbstractFluidTextureProvider;
import slimeknights.mantle.fluid.texture.FluidTexture;
import slimeknights.mantle.registration.object.FlowingFluidObject;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

import java.util.List;
import java.util.Optional;

import static slimeknights.mantle.Mantle.commonResource;

/**
 * Shared contract for addon-owned smeltery and molten-fluid compat.
 */
public interface AddonSmelteryCompat {
  /** Forces static initialization before TiC deferred registers are attached to the mod bus. */
  default void init() {}

  /** All compat entries owned by this addon. */
  List<Entry> entries();

  /** Adds addon-owned molten fluids to the fluids creative tab. */
  default void addCreativeTabItems(CreativeModeTab.Output output) {
    entries().forEach(entry -> acceptCompat(output, entry.fluid(), entry.material(), entry.name()));
  }

  /** Adds addon-owned molten fluid tags using the passed provider-specific registrar. */
  default void addFluidTags(FluidTagRegistrar registrar) {
    entries().forEach(entry -> registrar.add(entry.fluid()));
  }

  /** Adds addon-owned molten fluid textures using the passed provider. */
  default void addFluidTextures(AbstractFluidTextureProvider provider) {
    entries().forEach(entry -> compatOre(provider, entry.fluid()));
  }

  /** Skips addon-owned molten fluids in a core texture provider that validates a whole mod namespace. */
  default void skipFluidTextures(AbstractFluidTextureProvider provider) {
    entries().forEach(entry -> provider.skip(entry.fluid()));
  }

  /** Creates a builder for a hot fluid with sounds and description. */
  static FluidType.Properties hot(String name) {
    return FluidType.Properties.create().density(2000).viscosity(10000).temperature(1000)
      .descriptionId(TConstruct.makeDescriptionId("fluid", name))
      .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA)
      .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
      .motionScale(0.0023333333333333335D)
      .canSwim(false).canDrown(false)
      .pathType(BlockPathTypes.LAVA).adjacentPathType(null);
  }

  /** Accepts the given item if the material or same named ingot is present. */
  static boolean acceptCompat(CreativeModeTab.Output output, ItemLike item, @Nullable MaterialId material, String name) {
    if (material != null && MaterialRegistry.getMaterial(material) != IMaterial.UNKNOWN) {
      output.accept(item);
      return true;
    }
    String tagName = material == null ? name : material.getPath();
    return acceptIfTag(output, item, ItemTags.create(commonResource("ingots/" + tagName)));
  }

  /** Accepts the given item if the passed tag has items. */
  static boolean acceptIfTag(CreativeModeTab.Output output, ItemLike item, TagKey<Item> tagCondition) {
    Optional<Named<Item>> tag = BuiltInRegistries.ITEM.getTag(tagCondition);
    if (tag.isPresent() && tag.get().size() > 0) {
      output.accept(item);
      return true;
    }
    return false;
  }

  /** Creates a texture in the compat ore folder. */
  static FluidTexture.Builder compatOre(AbstractFluidTextureProvider provider, FluidObject<?> fluid) {
    return named(provider, fluid, "compat_ore/" + fluid.getId().getPath());
  }

  /** Creates a texture using the given fixed name in the fluid folder. */
  static FluidTexture.Builder named(AbstractFluidTextureProvider provider, FluidObject<?> fluid, String name) {
    return provider.texture(fluid).root(TConstruct.getResource("fluid/" + name + "/"))
      .still().flowing().camera().calculateFogColor(true).fog(FogShape.SPHERE, 0.25f, 2);
  }

  record Entry(String name, FlowingFluidObject<ForgeFlowingFluid> fluid, CompatType type, @Nullable MaterialId material) {
    /** Checks if this compat is present for recipe viewer visibility. */
    public boolean isPresent() {
      if (material != null && MaterialRegistry.getMaterial(material) != IMaterial.UNKNOWN) {
        return true;
      }
      return ingotPresent(name);
    }

    /** Checks if the given ingot tag exists. */
    private static boolean ingotPresent(String name) {
      Optional<Named<Item>> tag = BuiltInRegistries.ITEM.getTag(ItemTags.create(commonResource("ingots/" + name)));
      return tag.isPresent() && tag.get().size() > 0;
    }
  }

  @FunctionalInterface
  interface FluidTagRegistrar {
    void add(FlowingFluidObject<?> fluid);
  }

  /** Broad smeltery integration type for shared addon consumers. */
  enum CompatType {
    /** Fluid has ores, and should get support from lustrous. */
    ORE,
    /** Fluid is an alloy, and should get an anvil variant. */
    ALLOY,
    /** Fluid is neither ore nor alloy. */
    NONE
  }
}
