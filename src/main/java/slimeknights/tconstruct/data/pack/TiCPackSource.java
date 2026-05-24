package slimeknights.tconstruct.data.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * TiC 动态包源，用于将动态数据包/资源包注入 Minecraft 的包系统
 */
public class TiCPackSource implements RepositorySource {
    private final String name;
    private final PackType type;
    private final Pack.Position position;
    private final Function<String, PackResources> resources;

    public TiCPackSource(String name, PackType type, Pack.Position position, Function<String, PackResources> resources) {
        this.name = name;
        this.type = type;
        this.position = position;
        this.resources = resources;
    }

    @Override
    public void loadPacks(Consumer<Pack> onLoad) {
        onLoad.accept(readMetaAndCreate(name,
                Component.literal(name),
                true,
                resources::apply,
                type,
                position,
                PackSource.BUILT_IN));
    }

    /**
     * 读取包元数据并创建 Pack 实例
     *
     * @param id              包 ID
     * @param title           包标题
     * @param required        是否必需
     * @param resources       资源供应器
     * @param packType        包类型（数据包或资源包）
     * @param defaultPosition 默认位置
     * @param packSource      包来源
     * @return Pack 实例，如果无法读取元数据则返回 null
     */
    public static Pack readMetaAndCreate(String id, Component title, boolean required, Pack.ResourcesSupplier resources,
                                         PackType packType, Pack.Position defaultPosition, PackSource packSource) {
        Pack.Info info = Pack.readPackInfo(id, resources);
        return info != null ? Pack.create(id, title, required, resources,
                info, packType, defaultPosition, true, packSource) : null;
    }
}
