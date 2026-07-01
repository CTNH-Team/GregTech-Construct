package slimeknights.tconstruct.library.addon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.modifiers.Modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Registrar for static tag providers used in datagen (runData command).
 * <p>Tags registered here are written to files during static datagen, NOT dynamically at runtime.
 */
public final class DatagenTagProviderRegistrar {
  private final List<Consumer<BlockTagRegistrar>> blockTagHooks = new ArrayList<>();
  private final List<Consumer<ItemTagRegistrar>> itemTagHooks = new ArrayList<>();
  private final List<Consumer<FluidTagRegistrar>> fluidTagHooks = new ArrayList<>();
  private final List<Consumer<MaterialTagRegistrar>> materialTagHooks = new ArrayList<>();
  private final List<Consumer<ModifierTagRegistrar>> modifierTagHooks = new ArrayList<>();

  public void addBlockTags(Consumer<BlockTagRegistrar> hook) {
    blockTagHooks.add(Objects.requireNonNull(hook, "hook"));
  }

  public void addItemTags(Consumer<ItemTagRegistrar> hook) {
    itemTagHooks.add(Objects.requireNonNull(hook, "hook"));
  }

  public void addFluidTags(Consumer<FluidTagRegistrar> hook) {
    fluidTagHooks.add(Objects.requireNonNull(hook, "hook"));
  }

  public void addMaterialTags(Consumer<MaterialTagRegistrar> hook) {
    materialTagHooks.add(Objects.requireNonNull(hook, "hook"));
  }

  public void addModifierTags(Consumer<ModifierTagRegistrar> hook) {
    modifierTagHooks.add(Objects.requireNonNull(hook, "hook"));
  }

  public void applyBlockTags(BlockTagRegistrar registrar) {
    blockTagHooks.forEach(hook -> hook.accept(registrar));
  }

  public void applyItemTags(ItemTagRegistrar registrar) {
    itemTagHooks.forEach(hook -> hook.accept(registrar));
  }

  public void applyFluidTags(FluidTagRegistrar registrar) {
    fluidTagHooks.forEach(hook -> hook.accept(registrar));
  }

  public void applyMaterialTags(MaterialTagRegistrar registrar) {
    materialTagHooks.forEach(hook -> hook.accept(registrar));
  }

  public void applyModifierTags(ModifierTagRegistrar registrar) {
    modifierTagHooks.forEach(hook -> hook.accept(registrar));
  }

  public interface TagRegistrar<T> {
    void add(TagKey<T> tag, ResourceLocation... ids);

    void addOptional(TagKey<T> tag, ResourceLocation... ids);
  }

  public interface BlockTagRegistrar extends TagRegistrar<Block> {
  }

  public interface ItemTagRegistrar extends TagRegistrar<Item> {
  }

  public interface FluidTagRegistrar extends AddonSmelteryCompat.FluidTagRegistrar {
  }

  public interface MaterialTagRegistrar extends TagRegistrar<IMaterial> {
  }

  public interface ModifierTagRegistrar extends TagRegistrar<Modifier> {
  }
}
