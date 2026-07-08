package slimeknights.tconstruct.library.modifiers.modules.build;

import net.minecraft.resources.ResourceLocation;
import oftenoviour.util.formula.FormulaManager;
import oftenoviour.util.formula.IFormula;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ToolDataNBT;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Adds modifier slots from a Formula Loader expression using modifier level and unique material count. */
public record FormulaModifierSlotModule(SlotType type, ResourceLocation formula, ModifierCondition<IToolContext> condition)
  implements VolatileDataModifierHook, ModifierModule, ConditionalModule<IToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FormulaModifierSlotModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<FormulaModifierSlotModule> LOADER = RecordLoadable.create(
    SlotType.LOADABLE.requiredField("name", FormulaModifierSlotModule::type),
    Loadables.RESOURCE_LOCATION.requiredField("formula", FormulaModifierSlotModule::formula),
    ModifierCondition.CONTEXT_FIELD,
    FormulaModifierSlotModule::new);

  @Internal
  public FormulaModifierSlotModule {}

  public static FormulaModifierSlotModule slot(SlotType type, ResourceLocation formula) {
    return new FormulaModifierSlotModule(type, formula, ModifierCondition.ANY_CONTEXT);
  }

  @Override
  public Integer getPriority() {
    return 50;
  }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    if (!condition.matches(context, modifier)) {
      return;
    }
    IFormula resolved = FormulaManager.getOrNull(formula);
    if (resolved == null) {
      return;
    }
    int slots = (int)Math.round(resolved.accept(modifier.getEffectiveLevel(), uniqueMaterialCount(context)));
    if (slots > 0) {
      volatileData.addSlots(type, slots);
    }
  }

  private static int uniqueMaterialCount(IToolContext context) {
    Set<MaterialId> materials = new HashSet<>();
    context.getMaterials().forEach(material -> materials.add(material.getVariant().getId()));
    return materials.size();
  }

  @Override
  public RecordLoadable<FormulaModifierSlotModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
