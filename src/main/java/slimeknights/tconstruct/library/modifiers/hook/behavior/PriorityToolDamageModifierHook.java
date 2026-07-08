package slimeknights.tconstruct.library.modifiers.hook.behavior;

/**
 * 扩展 ToolDamageModifierHook 以支持优先级排序。
 * 高优先级的 hook 会先执行。
 */
public interface PriorityToolDamageModifierHook extends ToolDamageModifierHook {
  /**
   * 获取 tool damage 处理的优先级。
   * @return 优先级值，数值越大优先级越高。默认为 0。
   */
  default int getToolDamagePriority() {
    return 0;
  }
}
