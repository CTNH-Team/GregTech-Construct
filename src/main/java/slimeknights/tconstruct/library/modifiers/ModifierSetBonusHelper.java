package slimeknights.tconstruct.library.modifiers;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ModifierSetBonusHelper {
  public static final int FULL_SET_COUNT = 4;
  private static final EquipmentSlot[] ARMOR_SLOTS = {
    EquipmentSlot.HEAD,
    EquipmentSlot.CHEST,
    EquipmentSlot.LEGS,
    EquipmentSlot.FEET
  };

  private static final Map<ModifierId,ModifierId> BONUS_DISPLAY_MODIFIERS = new HashMap<>();

  private ModifierSetBonusHelper() {}

  public static void register(ModifierId baseModifier, ModifierId bonusDisplayModifier) {
    Objects.requireNonNull(baseModifier, "baseModifier");
    Objects.requireNonNull(bonusDisplayModifier, "bonusDisplayModifier");
    ModifierId existing = BONUS_DISPLAY_MODIFIERS.putIfAbsent(baseModifier, bonusDisplayModifier);
    if (existing != null) {
      throw new IllegalArgumentException("Duplicate set bonus registration for " + baseModifier + ": " + existing + " already registered");
    }
  }

  @Nullable
  public static ModifierId getBonusDisplayModifier(ModifierId baseModifier) {
    return BONUS_DISPLAY_MODIFIERS.get(baseModifier);
  }

  public static int getEquippedSetCount(Player player, ModifierId baseModifier) {
    int count = 0;
    for (EquipmentSlot slot : ARMOR_SLOTS) {
      if (hasModifier(player.getItemBySlot(slot), baseModifier)) {
        count++;
      }
    }
    return count;
  }

  public static boolean hasFullSet(Player player, ModifierId baseModifier) {
    return getEquippedSetCount(player, baseModifier) == FULL_SET_COUNT;
  }

  public static boolean isEquippedArmorStack(Player player, @Nullable ItemStack stack) {
    if (stack == null || stack.isEmpty() || !stack.is(TinkerTags.Items.MODIFIABLE)) {
      return false;
    }
    for (EquipmentSlot slot : ARMOR_SLOTS) {
      if (player.getItemBySlot(slot) == stack) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasModifier(@Nullable ItemStack stack, ModifierId modifier) {
    return stack != null && ModifierUtil.getModifierLevel(stack, modifier) > 0;
  }

  static void clearForTesting() {
    BONUS_DISPLAY_MODIFIERS.clear();
  }
}
