package slimeknights.tconstruct.library.events;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

/**
 * 在资源重载时触发，此时可以安全访问 ResourceManager 和纹理资源。
 * 用于生成需要读取现有资源的动态内容（如材质渲染信息、blockstates、models 等）。
 */
public class RegisterDynamicResourcesEvent extends Event implements IModBusEvent {
  public RegisterDynamicResourcesEvent() {}
}
