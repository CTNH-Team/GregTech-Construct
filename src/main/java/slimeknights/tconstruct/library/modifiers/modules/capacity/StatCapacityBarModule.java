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
 * 注意：capacity stat 不是在构造时创建，而是在 ModifiersLoadedEvent 时
 * 由 TinkerModifiers 的事件监听器统一初始化。
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
  private FloatToolStat stat; // 延迟初始化

  public StatCapacityBarModule(int color) {
    this(ModifierManager.EMPTY, color);
  }

  public StatCapacityBarModule(ResourceLocation key, int color) {
    this.key = key;
    this.color = color;
    this.stat = null; // 等待事件初始化
  }

  public int color() {
    return color;
  }

  public ResourceLocation getKey() {
    return key;
  }

  /**
   * 由 TinkerModifiers 的 ModifiersLoadedEvent 监听器调用。
   * 动态创建并注册 capacity stat。
   */
  public void initialize(ModifierId modifierId) {
    if (stat == null) {
      stat = StatCapacityBarManager.getOrCreateStat(modifierId, color);
      StatCapacityBarManager.register(modifierId, this);
    }
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
