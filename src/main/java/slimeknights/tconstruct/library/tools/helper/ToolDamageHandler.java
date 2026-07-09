package slimeknights.tconstruct.library.tools.helper;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TConstruct.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ToolDamageHandler {
  private static final Map<ItemStack,ToolDamageEntry> TOOL_DAMAGE_CACHE = new IdentityHashMap<>();

  private ToolDamageHandler() {}

  public static void accumulate(ItemStack stack, IToolStackView tool, @Nullable LivingEntity holder, int amount, @Nullable EquipmentSlot slot) {
    if (stack.isEmpty() || amount <= 0) {
      return;
    }
    ToolDamageEntry entry = TOOL_DAMAGE_CACHE.computeIfAbsent(stack, key -> new ToolDamageEntry(tool, holder, amount, slot));
    entry.tool = tool;
    entry.holder = holder;
    entry.amount += amount;
    if (entry.slot == null) {
      entry.slot = slot;
    }
  }

  public static void flushPendingDamage() {
    if (TOOL_DAMAGE_CACHE.isEmpty()) {
      return;
    }
    for (Map.Entry<ItemStack,ToolDamageEntry> entry : TOOL_DAMAGE_CACHE.entrySet()) {
      ToolDamageEntry queued = entry.getValue();
      apply(queued.tool, queued.holder, queued.amount, entry.getKey(), queued.slot);
    }
    TOOL_DAMAGE_CACHE.clear();
  }

  public static void clearPendingDamageForTests() {
    TOOL_DAMAGE_CACHE.clear();
  }

  private static void apply(IToolStackView tool, @Nullable LivingEntity holder, int amount, ItemStack stack, @Nullable EquipmentSlot slot) {
    if (amount <= 0 || tool.isBroken() || tool.isUnbreakable()) {
      return;
    }
    amount = ToolDamageUtil.applyDamageHooks(tool, amount, holder, stack);
    if (amount <= 0) {
      return;
    }
    boolean broken = ToolDamageUtil.directDamage(tool, amount, holder, stack);
    if (broken && holder != null && slot != null) {
      holder.broadcastBreakEvent(slot);
    }
  }

  @SubscribeEvent
  public static void onServerTick(TickEvent.ServerTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      flushPendingDamage();
    }
  }

  private static final class ToolDamageEntry {
    private IToolStackView tool;
    private LivingEntity holder;
    private int amount;
    @Nullable
    private EquipmentSlot slot;

    private ToolDamageEntry(IToolStackView tool, @Nullable LivingEntity holder, int amount, @Nullable EquipmentSlot slot) {
      this.tool = tool;
      this.holder = holder;
      this.amount = amount;
      this.slot = slot;
    }
  }
}
