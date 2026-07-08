package slimeknights.tconstruct.library.modifiers.modules.capacity;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.common.ColorLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;
import slimeknights.tconstruct.library.modifiers.hook.special.CapacityBarHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 基于动态创建的 tool stat 的 capacity bar。
 *
 * 当通过 JSON 加载时，ModifierId 会自动注入并初始化 capacity stat。
 */
public class StatCapacityBarModule implements CapacityBarHook, ModifierModule, HookProvider {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<StatCapacityBarModule>defaultHooks(ModifierHooks.CAPACITY_BAR);

  public static final RecordLoadable<StatCapacityBarModule> LOADER = RecordLoadable.create(
    ContextKey.ID.requiredField(),
    ColorLoadable.NO_ALPHA.requiredField("color", StatCapacityBarModule::color),
    StatCapacityBarModule::new
  );

  private final ResourceLocation key;
  private final int color;
  @Nullable
  private final FloatToolStat stat;

  public StatCapacityBarModule(int color) {
    this(ModifierManager.EMPTY, color);
  }

  public StatCapacityBarModule(ResourceLocation key, int color) {
    this.key = key;
    this.color = color;
    // 当 key 不是 EMPTY 时（即从 JSON 加载时），立即初始化
    if (key != ModifierManager.EMPTY) {
      ModifierId modifierId = new ModifierId(key);
      this.stat = StatCapacityBarManager.getOrCreateStat(modifierId, color);
      StatCapacityBarManager.register(modifierId, this);
    } else {
      this.stat = null;
    }
  }

  public int color() {
    return color;
  }

  public ResourceLocation getKey() {
    return key;
  }

  @Override
  public void addModules(ModuleHookMap.Builder builder) {
    builder.addModule(new CapacityBarValidator(this));
  }

  @Override
  public RecordLoadable<StatCapacityBarModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public int getAmount(IToolStackView tool) {
    return key != null ? tool.getPersistentData().getInt(key) : 0;
  }

  @Override
  public void setAmount(IToolStackView tool, ModifierEntry entry, int amount) {
    if (key != null && stat != null) {
      int capacity = getCapacity(tool, entry);
      int clamped = Math.max(0, Math.min(amount, capacity));
      if (clamped > 0) {
        tool.getPersistentData().putInt(key, clamped);
      } else {
        tool.getPersistentData().remove(key);
      }
    }
  }

  @Override
  public int getCapacity(IToolStackView tool, ModifierEntry entry) {
    return stat != null ? tool.getStats().getInt(this.stat) : 0;
  }
}
