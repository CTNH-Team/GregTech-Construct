# TiC Addon Notes

## How To Add A New Internal Addon

This section is a practical checklist for adding a new internal addon similar
to Botania.

### 1. Create The Addon Package

Create a package under `slimeknights.tconstruct.plugin.<modid>`, for example:

- `slimeknights.tconstruct.plugin.botania`

Keep the package self-contained. The addon entry class should only wire things
together; the actual providers and runtime logic should live in separate files.

### 2. Add The Addon Entrypoint

Create one concrete zero-argument class annotated with `@TiCAddon`, for
example:

```java
@TiCAddon(requiredMods = "examplemod")
public class ExampleTiCAddon implements ITiCAddon, ITiCStaticModifierAddon, ITiCTagAddon {
  public static final String MOD_ID = "examplemod";

  @Override
  public String addonModId() {
    return MOD_ID;
  }
}
```

Use `requiredMods` whenever the addon references classes from an optional mod.
That prevents the addon from being instantiated when the target mod is absent.

### 3. Move Optional Content Generation Into The Addon

Register addon-owned providers from the entrypoint:

- `registerDynamicRecipeProviders`
- `registerDynamicMaterialProviders`
- `registerDynamicResourceProviders`
- `registerDynamicTinkeringProviders` if needed
- `registerDynamicTagProviders` only if you truly need standalone tag providers

For TiC-owned material/modifier tags, prefer `ITiCTagAddon` append hooks instead
of replacing entire tag providers.

### 4. Register Static Modifier Implementations Through The Addon

If the compat needs runtime static modifiers, bind the concrete implementation
from `ITiCStaticModifierAddon`:

```java
@Override
public void registerStaticModifiers(StaticModifierRegistrar registrar) {
  registrar.register("example_modifier", ExampleModifier::new);
}
```

This moves the optional class binding into the addon instead of hardcoding it in
`TinkerModifiers`.

### 5. Decide Whether Core-Owned Symbols Are Still Required

Moving generation logic into the addon does **not** automatically mean every
related symbol can leave the core path.

Before removing anything from core, check whether the compat also needs:

- stable entries in `MaterialIds`
- stable entries in `ModifierIds`
- placeholder references in `TinkerModifiers`
- molten fluid registration in `TinkerFluids`
- entries in `SmelteryCompat`
- base client metadata such as colors, translations, or modifier display config

If another shared TiC system depends on those symbols, keep them in core.

### 6. Resource Provider Rule

Addon resource providers must generate only addon-owned content.

Do **not** subclass a core provider and call `super` if that would regenerate
core files. If the addon only owns a few materials, prefer deriving directly
from the lower-level abstract provider and emitting only those files.

### 7. Add Tests

At minimum, add integration tests that verify:

- the addon is discovered
- the addon registers expected providers
- TiC-owned tags receive the addon entries
- addon resource/material providers only generate addon-owned files
- addon-generated render info does not overwrite core-owned files

### 8. Keep The Entrypoint Thin

The addon entry class should act as a wiring layer only. Avoid putting full
generation logic, modifier behavior, or large helper methods directly in the
entrypoint.

### 9. Recommended Mental Model

Use this rule when deciding where code belongs:

- addon owns optional behavior and generated compat content
- core owns shared IDs, registry primitives, and symbols used across multiple
  TiC systems

If you are unsure whether something belongs in addon or core, read the section
below: **Why Some Compat Pieces Still Live In The Core Path**.

---

## 如何添加新的内部 Addon

这一节是一份实操清单，用来指导以后新增一个类似 Botania 的内部 addon。

### 1. 创建 Addon 包

在 `slimeknights.tconstruct.plugin.<modid>` 下创建一个包，例如：

- `slimeknights.tconstruct.plugin.botania`

尽量让这个包保持自洽。addon 入口类只负责接线，具体的 provider 和运行时逻辑应拆到独立文件中。

### 2. 添加 Addon 入口类

创建一个带 `@TiCAddon` 注解、具有无参构造的具体类，例如：

```java
@TiCAddon(requiredMods = "examplemod")
public class ExampleTiCAddon implements ITiCAddon, ITiCStaticModifierAddon, ITiCTagAddon {
  public static final String MOD_ID = "examplemod";

  @Override
  public String addonModId() {
    return MOD_ID;
  }
}
```

只要 addon 会直接引用可选模组的类，就应该使用 `requiredMods`。这样在目标模组不存在时，这个 addon 不会被实例化。

### 3. 把可选内容生成迁入 Addon

在入口类中注册 addon 自己拥有的 provider：

- `registerDynamicRecipeProviders`
- `registerDynamicMaterialProviders`
- `registerDynamicResourceProviders`
- 如果需要，再用 `registerDynamicTinkeringProviders`
- 只有确实需要独立 tag provider 时，才使用 `registerDynamicTagProviders`

对于 TiC 自有的材质/modifier tag，优先使用 `ITiCTagAddon` 的追加 hook，而不是去替换整个 tag provider。

### 4. 通过 Addon 注册静态 Modifier 实现

如果该联动需要运行时静态 modifier，应通过 `ITiCStaticModifierAddon` 绑定其具体实现：

```java
@Override
public void registerStaticModifiers(StaticModifierRegistrar registrar) {
  registrar.register("example_modifier", ExampleModifier::new);
}
```

这样可以把可选类绑定从 `TinkerModifiers` 中挪出，改为由 addon 负责。

### 5. 判断哪些符号仍然必须留在 Core

把生成逻辑迁入 addon，**不代表** 所有关联符号都能一起离开主链路。

在删除 core 中的内容之前，先确认该联动是否还需要：

- `MaterialIds` 中的稳定条目
- `ModifierIds` 中的稳定条目
- `TinkerModifiers` 中的占位引用
- `TinkerFluids` 中的熔融流体注册
- `SmelteryCompat` 中的条目
- 基础客户端元数据，例如颜色、翻译、modifier 显示配置

如果其他共享 TiC 系统仍然依赖这些符号，那它们就应该继续保留在 core。

### 6. 资源 Provider 规则

Addon 的资源 provider 只能生成 addon 自己拥有的内容。

不要通过继承 core provider 再调用 `super` 的方式去重新生成 core 文件。如果 addon 只拥有少量材质，更推荐直接继承底层抽象 provider，只输出这些材质对应的文件。

### 7. 添加测试

至少应补上以下集成测试：

- addon 能被发现
- addon 会注册预期的 providers
- TiC 自有 tag 能收到 addon 条目
- addon 的 resource/material provider 只生成 addon 自己拥有的文件
- addon 生成的 render info 不会覆盖 core 自有文件

### 8. 保持入口类轻量

Addon 入口类只应充当接线层。不要把完整的生成逻辑、modifier 行为实现、或大量辅助方法都堆在入口类中。

### 9. 推荐的判断模型

可以用下面这条规则来决定代码该放在哪：

- addon 持有可选行为和生成出来的联动内容
- core 持有共享 ID、注册级原语，以及多个 TiC 系统都会用到的符号

如果你不确定某个内容应该放 addon 还是 core，请继续阅读下面这一节：**Why Some Compat Pieces Still Live In The Core Path**。

## Why Some Compat Pieces Still Live In The Core Path

The `TiCAddon` system is responsible for wiring optional integrations into TiC's
dynamic generators and selected runtime extension points. Its goal is to move
compat _content logic_ out of the main providers, not to fully erase all
knowledge of a compat material or modifier from the core codebase.

For integrations such as Botania, the following split is intentional:

- Addon-owned:
  - material data providers
  - material stats/traits providers
  - recipe providers
  - resource/render providers
  - TiC-owned tag appends
  - static modifier implementation binding
- Core-owned:
  - stable material IDs
  - stable modifier IDs
  - runtime modifier placeholders
  - molten fluid registrations
  - smeltery compat enum entries
  - base client metadata such as colors/translations/modifier model mapping

This means Botania compat is only partially "moved out of the main path" by
design. The addon owns the optional behavior and generated content, while the
core still owns the symbols and registries that other TiC systems depend on.

## Why The Core-Owned Pieces Are Necessary

### 1. IDs Must Be Stable And Globally Addressable

`MaterialIds` and `ModifierIds` remain in core because a large part of TiC
communicates through stable `ResourceLocation` identifiers. Once a compat
material or modifier participates in shared systems, multiple places need to be
able to refer to that ID without requiring the addon package itself.

Current Botania examples:

- `MaterialIds.manaSteel`
- `MaterialIds.terraSteel`
- `ModifierIds.manafix`
- `ModifierIds.terrarecover`

If these IDs only existed inside the addon package, every core system that
needs to refer to them would either:

- gain a hard dependency on addon classes, or
- start reconstructing raw strings such as `"tconstruct:manasteel"` in many
  places.

Neither option is desirable.

### 2. Runtime Placeholder Modifiers Must Exist Before Addon Binding

`TinkerModifiers` keeps `StaticModifier` placeholders for optional compat
modifiers even though the concrete implementations are now registered through
`ITiCStaticModifierAddon`.

This is necessary because core code and JSON-facing systems still need a stable
runtime handle for those modifier IDs. The addon supplies the implementation;
the core keeps the placeholder identity.

For Botania:

- `TinkerModifiers.manafix`
- `TinkerModifiers.terrarecover`

The important distinction is:

- the core no longer directly binds these to Botania classes
- the addon now provides that binding
- the placeholder remains core-owned so the modifier identity is still stable

### 3. Fluids Are Registered In Core Registries

Molten compat metals currently live in `TinkerFluids` because they participate
in TiC's normal fluid registration pipeline:

- fluid object registration
- tags
- generated textures
- bucket/block registration
- lookups by fluid name

For Botania this includes:

- `moltenManaSteel`
- `moltenTerraSteel`

As long as compat molten metals are treated as first-class TiC fluids, the core
registry layer still needs to own them.

### 4. Smeltery Compat Uses A Shared Core Table

`SmelteryCompat` is still core-owned because it acts as a shared compat
definition table for smeltery-related logic. Botania entries still appear there
for the same reason molten fluids do: they participate in a common core system
instead of a fully isolated addon-local pipeline.

### 5. Client Metadata Still Needs Core Visibility

Some resource files still mention compat materials/modifiers from the base
resource pack, for example:

- `assets/tconstruct/tinkering/modifiers.json`
- `assets/tconstruct/mantle/colors.json`
- language files

These are not addon generator wiring; they are shared client metadata that the
core pack exposes under stable IDs.

## What "Addonized" Means Here

Within this codebase, "moved to TiCAddon" should be read as:

> The optional compat behavior and generated content are routed through addon
> entrypoints instead of being hardcoded into the main providers.

It does **not** necessarily mean:

> The core no longer contains any ID, placeholder, fluid, enum, or metadata
> entry related to that compat.

That stronger form would require a deeper architectural change, such as a
single compat contract/manifest that can drive IDs, fluids, placeholders, and
shared metadata from one source of truth.

## Guidance For Future Internal Addons

When adding another internal addon like Botania, use this rule:

- move optional content generation and registration wiring into the addon
- keep shared identifiers and registry-level primitives in core when other TiC
  systems already depend on them

Before deciding a compat piece can leave the core path entirely, verify whether
it is required by any of the following:

- shared ID constants
- runtime placeholder references
- fluid registration
- smeltery compat tables
- base client metadata

If yes, keeping that piece in core is intentional and should be documented
rather than treated as an incomplete migration.

---

# TiC Addon 说明

## 为什么有些联动内容仍然保留在主链路中

`TiCAddon` 系统的职责，是把可选联动接入到 TiC 的动态生成链路和少量运行时扩展点中。它的目标是把联动的**内容逻辑**从主 provider 中迁出去，而不是让核心代码库彻底不再认识某个联动材质或 modifier。

以 Botania 为例，当前这种拆分是有意为之：

- 由 addon 持有：
  - 材质数据 provider
  - 材质数值/特性 provider
  - 配方 provider
  - 资源/渲染 provider
  - 对 TiC 自有 tag 的追加
  - 静态 modifier 的具体实现绑定
- 由 core 持有：
  - 稳定的材质 ID
  - 稳定的 modifier ID
  - 运行时 modifier 占位
  - 熔融流体注册
  - 冶炼兼容枚举项
  - 基础客户端元数据，例如颜色、翻译、modifier 模型映射

这意味着，按设计来说，Botania 联动只是**部分脱离主链路**。addon 持有可选行为和生成内容，而 core 仍然持有其他 TiC 系统依赖的符号和注册项。

## 为什么这些 core 持有的部分仍然是必要的

### 1. ID 必须稳定且可被全局引用

`MaterialIds` 和 `ModifierIds` 仍然保留在 core 中，因为 TiC 很多系统都是通过稳定的 `ResourceLocation` 标识符协作的。一旦某个联动材质或 modifier 参与共享系统，多个位置都需要能引用这个 ID，而不能要求它们反向依赖 addon 包。

当前 Botania 对应的例子有：

- `MaterialIds.manaSteel`
- `MaterialIds.terraSteel`
- `ModifierIds.manafix`
- `ModifierIds.terrarecover`

如果这些 ID 只存在于 addon 包中，那么所有需要引用它们的 core 系统最终只能走两条路：

- 直接对 addon 类形成硬依赖，或者
- 到处手写 `"tconstruct:manasteel"` 这类字符串

这两种都不是理想方案。

### 2. 运行时 modifier 占位必须先于 addon 绑定存在

`TinkerModifiers` 仍然保留这些可选联动 modifier 的 `StaticModifier` 占位，即使具体实现现在已经通过 `ITiCStaticModifierAddon` 注册。

原因是 core 代码和面向 JSON 的系统仍然需要一个稳定的运行时引用来表示这些 modifier ID。addon 提供实现，core 保留占位身份。

对于 Botania：

- `TinkerModifiers.manafix`
- `TinkerModifiers.terrarecover`

这里真正重要的区别是：

- core 不再直接把它们绑定到 Botania 类
- addon 现在负责提供这种绑定
- 但占位仍然由 core 持有，以保证 modifier 身份稳定

### 3. 流体仍然注册在核心流体注册表中

熔融兼容金属目前仍然放在 `TinkerFluids` 中，因为它们参与的是 TiC 标准流体注册链：

- 流体对象注册
- tag
- 贴图生成
- 桶/方块注册
- 按流体名进行查找

对于 Botania，这包括：

- `moltenManaSteel`
- `moltenTerraSteel`

只要这些兼容熔融金属仍然被视为 TiC 的一等流体，core 注册层就仍然需要持有它们。

### 4. 冶炼兼容仍然依赖共享的 core 表

`SmelteryCompat` 仍然由 core 持有，因为它本质上是冶炼相关逻辑的共享兼容定义表。Botania 项目还出现在这里，原因和熔融流体一样：它们参与的是公共核心系统，而不是一个完全隔离的 addon 本地流程。

### 5. 客户端基础元数据仍然需要 core 可见

有些资源文件仍然会在基础资源包中直接提到联动材质或 modifier，例如：

- `assets/tconstruct/tinkering/modifiers.json`
- `assets/tconstruct/mantle/colors.json`
- 各语言文件

这些不是 addon 的生成接线逻辑，而是 core 资源包对稳定 ID 暴露的共享客户端元数据。

## 这里说的 “Addon 化” 到底是什么意思

在这个代码库里，“迁移到 TiCAddon” 更准确的含义应该是：

> 可选联动行为和生成内容，改为通过 addon 入口进行接入，而不是继续硬编码在主 provider 中。

它**不一定**表示：

> core 中从此不再包含任何与该联动有关的 ID、占位、流体、枚举项或元数据。

如果想达到后者那种更彻底的形态，就需要更深的架构调整，例如引入单一 compat contract/manifest，由一处事实源统一驱动 ID、流体、占位和共享元数据。

## 对未来内部 addon 的建议

以后继续添加类似 Botania 的内部 addon 时，建议遵循这个规则：

- 把可选内容生成与注册接线迁入 addon
- 如果某些共享标识或注册级原语已经被多个 TiC 系统依赖，则继续保留在 core

在决定某个联动内容是否可以彻底离开主链路前，先确认它是否被以下任一项需要：

- 共享 ID 常量
- 运行时占位引用
- 流体注册
- 冶炼兼容表
- 基础客户端元数据

如果答案是需要，那么把它保留在 core 是有意设计，应该被文档化说明，而不应简单视为“迁移没做完”。
