package slimeknights.tconstruct.tools.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import oftenoviour.util.formula.FormulaBuilder;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.addon.DynamicDataRegistrar;
import slimeknights.tconstruct.library.data.RuntimeDataProvider;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class ArmorFormulaProvider implements RuntimeDataProvider {
  private static final String ROOT = "formula";
  private static final String DEFAULT_ROOT = ROOT + "/default";

  public ArmorFormulaProvider(PackOutput output) {}

  @Override
  public void addToDynamicPack(DynamicDataRegistrar registrar) {
        write(registrar, DEFAULT_ROOT + "/generic", "per_armor_ratio",
            f("armor_count", "result = 1 / armor_count"));
        write(registrar, DEFAULT_ROOT + "/generic", "total_level_formula",
            f("value", "total_level", "result = value"));
        write(registrar, DEFAULT_ROOT + "/generic", "level_add",
            f("value", "base", "per_level", "level", "result = value + base + level * per_level"));
        write(registrar, DEFAULT_ROOT + "/generic", "level_max",
            f("stored_value", "base", "per_level", "level", "result = max(stored_value, base + level * per_level)"));
        write(registrar, DEFAULT_ROOT + "/tool_damage", "formula",
            f("level", "amount", "result = amount"));
        write(registrar, DEFAULT_ROOT + "/armor_absorption_cap", "formula",
            f("cap", "level", "result = cap"));
        write(registrar, DEFAULT_ROOT + "/armor_absorption_cap/accumulator", "formula",
            f("stored_value", "level", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/armor_absorption_cap/finalizer", "formula",
            f("cap", "stored_value", "total_level", "result = max(cap, stored_value)"));
        write(registrar, DEFAULT_ROOT + "/area_effect", "range_formula",
            f("level", "result = 8"));
        write(registrar, DEFAULT_ROOT + "/area_effect/accumulator", "duration",
            f("stored_duration", "level", "distance", "result = stored_duration"));
        write(registrar, DEFAULT_ROOT + "/area_effect/accumulator", "amplifier",
            f("stored_amplifier", "level", "distance", "result = stored_amplifier"));
        write(registrar, DEFAULT_ROOT + "/area_effect/finalizer", "duration",
            f("duration", "stored_duration", "total_level", "result = max(duration, stored_duration)"));
        write(registrar, DEFAULT_ROOT + "/area_effect/finalizer", "amplifier",
            f("amplifier", "stored_amplifier", "total_level", "result = max(amplifier, stored_amplifier)"));
        write(registrar, DEFAULT_ROOT + "/self_effect/accumulator", "duration",
            f("stored_duration", "level", "result = stored_duration"));
        write(registrar, DEFAULT_ROOT + "/self_effect/accumulator", "amplifier",
            f("stored_amplifier", "level", "result = stored_amplifier"));
        write(registrar, DEFAULT_ROOT + "/self_effect/finalizer", "duration",
            f("duration", "stored_duration", "total_level", "result = max(duration, stored_duration)"));
        write(registrar, DEFAULT_ROOT + "/self_effect/finalizer", "amplifier",
            f("amplifier", "stored_amplifier", "total_level", "result = max(amplifier, stored_amplifier)"));
        write(registrar, DEFAULT_ROOT + "/share_damage", "distance_factor_formula",
            f("distance", "effective_range", "full_effect_range", "result = 1"));
        write(registrar, DEFAULT_ROOT + "/share_damage", "share_ratio_formula",
            f("stored_value", "level", "result = 0"));
        write(registrar, DEFAULT_ROOT + "/share_damage", "extra_protection_formula",
            f("stored_value", "level", "result = 0"));
        write(registrar, DEFAULT_ROOT + "/share_damage/accumulator", "share_ratio_formula",
            f("stored_value", "level", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/share_damage/accumulator", "extra_protection_formula",
            f("stored_value", "level", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/share_damage/finalizer", "share_ratio_formula",
            f("stored_value", "total_level", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/share_damage/finalizer", "extra_protection_formula",
            f("stored_value", "total_level", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/damage_limit", "cap_formula",
            f("level", "original_damage", "result = 4"));
        write(registrar, DEFAULT_ROOT + "/damage_limit", "condition_formula",
            f("level", "original_damage", "result = 0"));
        write(registrar, DEFAULT_ROOT + "/damage_limit/accumulator", "armor_damage_formula",
            f("stored_value", "level", "original_damage", "capacity", "amount", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/damage_limit/accumulator", "overshield_damage_formula",
            f("stored_value", "level", "original_damage", "capacity", "amount", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/damage_limit/finalizer", "armor_damage_formula",
            f("stored_value", "total_level", "original_damage", "overflow", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/damage_limit/finalizer", "overshield_damage_formula",
            f("stored_value", "total_level", "original_damage", "overflow", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/regenerate", "regenerate_formula",
            f("level", "amount", "capacity", "last_amount", "result = 0.1 * level"));
        write(registrar, DEFAULT_ROOT + "/regenerate", "dura_consume_formula",
            f("level", "amount", "capacity", "gain", "result = 0"));
        write(registrar, DEFAULT_ROOT + "/regenerate", "cool_down_formula",
            f("level", "amount", "capacity", "last_amount", "result = 200"));
        write(registrar, DEFAULT_ROOT + "/conditional_armor_stats", "formula",
            f("value", "base", "per_level", "level", "result = value"));
        write(registrar, DEFAULT_ROOT + "/conditional_armor_stats/accumulator", "formula",
            f("stored_value", "value", "base", "per_level", "level", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/conditional_armor_stats/finalizer", "formula",
            f("value", "stored_value", "total_level", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/persistent_armor_stat", "formula",
            f("tool_old_val", "level", "result = tool_old_val"));
        write(registrar, DEFAULT_ROOT + "/persistent_armor_stat/accumulator", "formula",
            f("stored_value", "persistent_value", "level", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/persistent_armor_stat/finalizer", "formula",
            f("persistent_value", "stored", "total_level", "result = persistent_value"));
        write(registrar, DEFAULT_ROOT + "/damage_to_persistent", "formula",
            f("tool_old_val", "level", "result = tool_old_val"));
        write(registrar, DEFAULT_ROOT + "/damage_to_persistent/accumulator", "formula",
            f("stored_value", "persistent_value", "level", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/damage_to_persistent/finalizer", "formula",
            f("persistent_value", "stored", "total_level", "result = persistent_value"));
        write(registrar, DEFAULT_ROOT + "/persistent_tick", "formula",
            f("value", "level", "result = value"));
        write(registrar, DEFAULT_ROOT + "/persistent_tick/accumulator", "formula",
            f("stored_value", "persistent_value", "level", "result = stored_value"));
        write(registrar, DEFAULT_ROOT + "/persistent_tick/finalizer", "formula",
            f("persistent_value", "stored", "total_level", "result = persistent_value"));
        write(registrar, DEFAULT_ROOT + "/formula_slot", "formula",
                f("level", "result = 1"));
        write(registrar, ROOT + "/plating", "stat_bonus",
                f("value", "level", "capacity", "amount", "result = 0.1 * amount / capacity"));
        write(registrar, ROOT + "/plating", "pre_damage",
                FormulaBuilder.inputs("level", "amount", "capacity", "bar_amount").caches("result").output("result").
                        ifelse("bar_amount > 0", "result = amount - 1", "result = amount"));
        write(registrar, ROOT + "/plating", "tool_damage",
                f("level", "amount", "capacity", "bar_amount", "result = max(0, amount - bar_amount)"));
        write(registrar, ROOT + "/plating", "damage_capacity_pre",
                FormulaBuilder.inputs("level", "amount", "capacity", "bar_amount").caches("result").output("result").
                        ifelse("bar_amount > 0", "result = 1", "result = 0"));
        write(registrar, ROOT + "/plating", "damage_capacity",
                f("level", "amount", "capacity", "bar_amount", "result = min(amount, bar_amount)"));
        write(registrar, ROOT + "/crystal_lattice", "stat_bonus",
                f("value", "level", "capacity", "amount", "result = pow(amount / capacity, 2)"));
        write(registrar, ROOT + "/crystal_lattice", "damage_capacity",
                f("level", "amount", "capacity", "bar_amount", "result = amount - 1"));
        write(registrar, ROOT + "/crystal_solidity", "cap_formula",
                f("level", "original_damage", "capacity", "amount", "result = 4"));
        write(registrar, ROOT + "/crystal_solidity", "condition_formula",
                f("level", "original_damage", "capacity", "amount", "result = amount / capacity - 0.9"));
        write(registrar, ROOT + "/crystal_solidity", "per_armor_ratio",
                f("armor_count", "result = 2 / (1 + armor_count)"));
        write(registrar, ROOT + "/crystal_solidity/finalizer", "armor_damage_formula",
                f("stored_value", "total_level", "original_damage", "overflow", "result = overflow * (2 + log(16, overflow))"));
        write(registrar, ROOT + "/crystal_solidity/finalizer", "overshield_damage_formula",
                f("stored_value", "total_level", "original_damage", "overflow", "result = (4 + log(2, original_damage))"));
        write(registrar, ROOT + "/crystalizing", "regenerate_formula",
                f("level", "amount", "capacity", "last_amount", "result = 0.025 * level * pow((1 + min(100, amount) / 100), 2)"));
        write(registrar, ROOT + "/crystalizing", "dura_consume_formula",
                f("level", "gain", "capacity", "amount", "result = 0"));
        write(registrar, ROOT + "/crystalizing", "cool_down_formula",
                f("level", "amount", "capacity", "last_amount", "result = 160 - 10 * level"));
        write(registrar, ROOT + "/cushion", "formula",
                f("level", "amount", "result = max(0, amount - 0.5)"));
        write(registrar, ROOT + "/guarding", "distance_factor_formula",
                f("distance", "effective_range", "full_effect_range", "result = 1 - (distance - full_effect_range) / (effective_range - full_effect_range)"));
        write(registrar, ROOT + "/guarding", "share_ratio_formula",
                f("stored_value", "level", "capacity", "amount", "result = 0.25 + 0.25 * amount / capacity"));
        write(registrar, ROOT + "/guarding", "extra_protection_formula",
                f("stored_value", "level", "capacity", "amount", "result = 0.1 * amount / capacity"));
        write(registrar, ROOT + "/hardening", "regenerate_formula",
                f("level", "amount", "capacity", "last_amount", "result = 0.15 * level"));
        write(registrar, ROOT + "/hardening", "dura_consume_formula",
                f("level", "amount", "capacity", "gain", "result = 0.1 * level"));
        write(registrar, ROOT + "/hardening", "cool_down_formula",
                f("level", "amount", "capacity", "last_amount", "result = 250 - 10 * level"));
        write(registrar, ROOT + "/tanned", "formula",
            FormulaBuilder.inputs("level", "amount").caches("result").output("result")
                .ifelse("amount > 1", "result = floor(amount / 2)", "result = amount"));
        write(registrar, ROOT + "/totem", "range_formula",
            f("level", "result = 16"));
        write(registrar, ROOT + "/totem/accumulator", "duration_formula",
            f("stored_duration", "level", "distance", "result = 30"));
        write(registrar, ROOT + "/totem/accumulator", "level_formula",
            f("stored_amplifier", "level", "distance", "result = max(stored_amplifier, level)"));
        write(registrar, ROOT + "/totem/finalizer", "duration_formula",
            f("duration", "stored_duration", "total_level", "result = max(duration, stored_duration)"));
        write(registrar, ROOT + "/totem/finalizer", "level_formula",
            f("amplifier", "stored_amplifier", "total_level", "result = max(amplifier, stored_amplifier)"));
        write(registrar, ROOT + "/recurrence/persistent_armor_stat", "finalizer",
            FormulaBuilder.inputs("persistent_value", "stored", "total_level", "reduced")
                .caches("result").output("result")
                .mixed().flat("result = persistent_value - min(persistent_value, reduced) / (1 + total_level / 8)")
                .cond("result < 0.1", "result = 0").exit());
        write(registrar, ROOT + "/recurrence/damage_to_persistent", "accumulator",
            f("stored_value", "persistent_value", "level", "reduced", "result = max(stored_value, level)"));
        write(registrar, ROOT + "/recurrence/damage_to_persistent", "finalizer",
            f("persistent_value", "stored", "total_level", "reduced", "result = persistent_value + reduced * (0.4 + stored * 0.1)"));
        write(registrar, ROOT + "/recurrence/persistent_tick", "finalizer",
            FormulaBuilder.inputs("persistent_value", "stored", "total_level")
                .caches("result").output("result")
                .ifelse("persistent_value > 1", "result = persistent_value * (1 - 0.25 / (1 + total_level / 8))", "result = max(0, persistent_value - 0.25 / (1 + total_level / 8))"));
        write(registrar, ROOT + "/malleability", "cap_formula",
                f("cap", "level", "result = cap - 0.1"));
        write(registrar, ROOT + "/malleability", "slot_formula",
                f("level", "material_count", "result = max(0, material_count - 1) * level"));
  }

  private static FormulaBuilder f(String... args) {
    String[] inputs = Arrays.copyOf(args, args.length - 1);
    return FormulaBuilder.inputs(inputs)
      .caches("result").output("result")
      .flat(args[args.length - 1]);
  }

  private static void write(DynamicDataRegistrar registrar, String dir, String name, FormulaBuilder builder) {
    registrar.addData(ResourceLocation.tryBuild(TConstruct.MOD_ID, dir + "/" + name + ".json"), builder.build().getBytes(StandardCharsets.UTF_8));
  }
}
