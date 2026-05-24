package slimeknights.tconstruct.library.addon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.modifiers.Modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Registers addon tag providers and hooks that append to TiC-owned tag providers.
 */
public final class DynamicTagProviderRegistrar implements DynamicProviderRegistrar {
  private final DynamicProviderRegistrar providers;
  private final List<Consumer<FluidTagRegistrar>> fluidTagHooks = new ArrayList<>();
  private final List<Consumer<MaterialTagRegistrar>> materialTagHooks = new ArrayList<>();
  private final List<Consumer<ModifierTagRegistrar>> modifierTagHooks = new ArrayList<>();

  public DynamicTagProviderRegistrar(DynamicProviderRegistrar providers) {
    this.providers = Objects.requireNonNull(providers, "providers");
  }

  @Override
  public void addProvider(String name, java.util.function.Function<net.minecraft.data.PackOutput, ? extends net.minecraft.data.DataProvider> factory) {
    providers.addProvider(name, factory);
  }

  /** Adds content to TiC's fluid tag provider. */
  public void addFluidTags(Consumer<FluidTagRegistrar> hook) {
    fluidTagHooks.add(Objects.requireNonNull(hook, "hook"));
  }

  /** Adds content to TiC's material tag provider. */
  public void addMaterialTags(Consumer<MaterialTagRegistrar> hook) {
    materialTagHooks.add(Objects.requireNonNull(hook, "hook"));
  }

  /** Adds content to TiC's modifier tag provider. */
  public void addModifierTags(Consumer<ModifierTagRegistrar> hook) {
    modifierTagHooks.add(Objects.requireNonNull(hook, "hook"));
  }

  /** Applies addon fluid tag hooks to the passed registrar. */
  public void applyFluidTags(FluidTagRegistrar registrar) {
    fluidTagHooks.forEach(hook -> hook.accept(registrar));
  }

  /** Applies addon material tag hooks to the passed registrar. */
  public void applyMaterialTags(MaterialTagRegistrar registrar) {
    materialTagHooks.forEach(hook -> hook.accept(registrar));
  }

  /** Applies addon modifier tag hooks to the passed registrar. */
  public void applyModifierTags(ModifierTagRegistrar registrar) {
    modifierTagHooks.forEach(hook -> hook.accept(registrar));
  }

  public interface TagRegistrar<T> {
    void add(TagKey<T> tag, ResourceLocation... ids);

    void addOptional(TagKey<T> tag, ResourceLocation... ids);
  }

  public interface FluidTagRegistrar extends AddonSmelteryCompat.FluidTagRegistrar {
  }

  public interface MaterialTagRegistrar extends TagRegistrar<IMaterial> {
  }

  public interface ModifierTagRegistrar extends TagRegistrar<Modifier> {
  }
}
