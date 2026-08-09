package slimeknights.tconstruct.plugin.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.ModList;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.registration.CompatMaterialFluidObject;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.fluids.fluids.PotionFluidType;
import slimeknights.tconstruct.library.addon.TiCAddonRegistry;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.smeltery.data.SmelteryCompat;
import slimeknights.tconstruct.smeltery.item.CopperCanItem;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.item.ModifierCrystalItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@EmiEntrypoint
public final class TConstructEmiFilterPlugin implements EmiPlugin {
  private static final List<ResourceLocation> HIDDEN_RECIPES = List.of(
      TConstruct.getResource("tables/tinkers_forge"),
      TConstruct.getResource("tables/scorched_forge"),
      TConstruct.getResource("tables/seared_forge_material"),
      TConstruct.getResource("tables/scorched_forge_material"));

  @Override
  public void register(EmiRegistry registry) {
    registerPartComparisons(registry);
    if (!Config.CLIENT.showFilledFluidTanks.get()) {
      hideFilledFluidTanks(registry);
    }
    filterToolMaterials(registry);
    filterPartMaterials(registry);
    hideAbsentCompatibilityFluids(registry);
    filterPotionFluids(registry);
    hideModifierItems(registry);
    HIDDEN_RECIPES.forEach(registry::removeRecipes);
  }

  /**
   * 部件按材料 NBT 区分。EMI 的配方来源/用途索引默认只按物品 id 匹配,
   * 不注册比较会把这个部件的所有材料变体配方全部返回,故为 parts 标签下的
   * 部件注册 NBT 严格比较。所有涉及部件的配方输入/输出都带材料 NBT
   * (具体部件或 MaterialIngredient 展开的变体),不会误伤通配配方。
   */
  private static void registerPartComparisons(EmiRegistry registry) {
    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.TOOL_PARTS)) {
      registry.setDefaultComparison(holder.get(), Comparison.compareNbt());
    }
  }

  private static void hideModifierItems(EmiRegistry registry) {
    Set<EmiStack> hiddenStacks = new HashSet<>();
    hiddenStacks.add(EmiStack.of(new ItemStack(TinkerModifiers.modifierCrystal)));
    ModifierCrystalItem.addVariants(stack -> hiddenStacks.add(EmiStack.of(stack)));
    hiddenStacks.add(EmiStack.of(new ItemStack(TinkerModifiers.creativeSlotItem)));
    TinkerModifiers.creativeSlotItem.get().addVariants(stack -> hiddenStacks.add(EmiStack.of(stack)));
    registry.removeEmiStacks(hiddenStacks::contains);
  }

  private static void hideFilledFluidTanks(EmiRegistry registry) {
    List<EmiStack> filledStacks = new ArrayList<>();
    CopperCanItem.addFilledVariants(stack -> filledStacks.add(EmiStack.of(stack)));
    TankItem.addFilledVariants(stack -> filledStacks.add(EmiStack.of(stack)));

    List<EmiStack> fuelStacks = List.of(
        EmiStack.of(TankItem.fillTank(TinkerSmeltery.searedTank, TankType.FUEL_TANK, Fluids.LAVA)),
        EmiStack.of(TankItem.fillTank(TinkerSmeltery.searedTank, TankType.FUEL_TANK, TinkerFluids.blazingBlood.get())),
        EmiStack.of(TankItem.fillTank(TinkerSmeltery.scorchedTank, TankType.FUEL_TANK, Fluids.LAVA)),
        EmiStack.of(TankItem.fillTank(TinkerSmeltery.scorchedTank, TankType.FUEL_TANK, TinkerFluids.blazingBlood.get())));
    Set<EmiStack> fuelSet = Set.copyOf(fuelStacks);
    Set<EmiStack> filledSet = Set.copyOf(filledStacks);

    registry.removeEmiStacks(stack -> filledSet.contains(stack) && !fuelSet.contains(stack));
    fuelStacks.forEach(registry::addEmiStack);
  }

  private static void filterToolMaterials(EmiRegistry registry) {
    String material = Config.CLIENT.showOnlyToolMaterial.get();
    if (material.isEmpty()) {
      return;
    }
    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.MODIFIABLE)) {
      if (holder.get() instanceof IModifiable tool) {
        restrictVariants(registry,
            collectVariants(output -> ToolBuildHandler.addVariants(output, tool, "")),
            collectVariants(output -> ToolBuildHandler.addVariants(output, tool, material)));
      }
    }
  }

  private static void filterPartMaterials(EmiRegistry registry) {
    String material = Config.CLIENT.showOnlyPartMaterial.get();
    if (material.isEmpty()) {
      return;
    }
    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.TOOL_PARTS)) {
      if (holder.get() instanceof IMaterialItem part) {
        restrictVariants(registry,
            collectVariants(output -> part.addVariants(output, "")),
            collectVariants(output -> part.addVariants(output, material)));
      }
    }
  }

  private static List<EmiStack> collectVariants(Consumer<Consumer<ItemStack>> variantProvider) {
    List<EmiStack> variants = new ArrayList<>();
    variantProvider.accept(stack -> variants.add(EmiStack.of(stack)));
    return variants;
  }

  private static void restrictVariants(EmiRegistry registry, List<EmiStack> all, List<EmiStack> selected) {
    Set<EmiStack> allSet = new HashSet<>(all);
    registry.removeEmiStacks(stack -> allSet.contains(stack) && !selected.contains(stack));
    selected.forEach(registry::addEmiStack);
  }

  private static void hideAbsentCompatibilityFluids(EmiRegistry registry) {
    Set<Fluid> hiddenFluids = new HashSet<>();
    for (SmelteryCompat compat : SmelteryCompat.values()) {
      if (!compat.isPresent() && !(compat.getFluid() instanceof CompatMaterialFluidObject)) {
        hiddenFluids.add(compat.getFluid().get());
      }
    }
    TiCAddonRegistry.collectSmelteryCompat(compat -> compat.entries().stream()
        .filter(entry -> !entry.isPresent())
        .filter(entry -> !(entry.fluid() instanceof CompatMaterialFluidObject))
        .map(entry -> entry.fluid().get())
        .forEach(hiddenFluids::add));
    if (!ModList.get().isLoaded("ceramics")) {
      hiddenFluids.add(TinkerFluids.moltenPorcelain.get());
    }
    if (!hiddenFluids.isEmpty()) {
      registry.removeEmiStacks(stack -> stack.getKey() instanceof Fluid fluid && hiddenFluids.contains(fluid));
    }
  }

  private static void filterPotionFluids(EmiRegistry registry) {
    Fluid potionFluid = TinkerFluids.potion.get();
    registry.removeEmiStacks(stack -> stack.getKey() == potionFluid && !stack.hasNbt());

    if (Config.CLIENT.showPotionFluidInEMI.get()) {
      BuiltInRegistries.POTION.holders()
          .filter(holder -> {
            Potion potion = holder.get();
            return potion != Potions.EMPTY && potion != Potions.WATER && !holder.is(TinkerTags.Potions.HIDDEN_FLUID);
          })
          .forEach(holder -> {
            var fluid = PotionFluidType.potionFluid(holder.key(), FluidType.BUCKET_VOLUME);
            registry.addEmiStack(EmiStack.of(fluid.getFluid(), fluid.getTag(), fluid.getAmount()));
          });
    }
  }
}
