package slimeknights.tconstruct.library.addon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Optional addon hook for appending entries to TiC-owned tag providers without replacing the full tag files.
 */
@SuppressWarnings("unused")
public interface ITiCTagAddon {
  default void registerMaterialTags(MaterialTagRegistrar registrar) {}

  default void registerModifierTags(ModifierTagRegistrar registrar) {}

  interface TagRegistrar<T> {
    void add(TagKey<T> tag, ResourceLocation... ids);

    void addOptional(TagKey<T> tag, ResourceLocation... ids);
  }

  interface MaterialTagRegistrar extends TagRegistrar<IMaterial> {
  }

  interface ModifierTagRegistrar extends TagRegistrar<Modifier> {
  }
}
