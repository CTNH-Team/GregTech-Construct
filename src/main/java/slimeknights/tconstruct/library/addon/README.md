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
public class ExampleTiCAddon implements ITiCAddon, ITiCStaticModifierAddon {
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
- `registerDynamicTagProviders` for standalone tag providers and TiC-owned
  fluid/material/modifier tag appends

For TiC-owned fluid/material/modifier tags, prefer
`DynamicTagProviderRegistrar#addFluidTags`, `addMaterialTags`, and
`addModifierTags` from `registerDynamicTagProviders` instead of replacing
entire tag providers.

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

- addon-owned ID holder classes for integration-owned material/modifier IDs
- truly shared entries in core `MaterialIds` or `ModifierIds`
- addon-owned placeholder holders for integration-owned static modifiers
- addon-owned smeltery compat via `AddonSmelteryCompat` for
  integration-owned fluids
- shared/core fluid or `SmelteryCompat` definitions that other TiC systems
  still depend on
- base client metadata such as colors, translations, or modifier display config

Keep only the shared registry primitives in core when other TiC systems depend
on them; integration-owned IDs should stay with the addon holder class.

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
- addon-owned ID holder classes may own stable IDs for integration-owned
  materials/modifiers
- core owns shared registry primitives and symbols used across multiple TiC
  systems, which may still reference those stable IDs

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
public class ExampleTiCAddon implements ITiCAddon, ITiCStaticModifierAddon {
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
- 使用 `registerDynamicTagProviders` 注册独立 tag provider，以及追加 TiC 自有
  fluid/material/modifier tag 条目

对于 TiC 自有的 fluid/material/modifier tag，优先在
`registerDynamicTagProviders` 中通过
`DynamicTagProviderRegistrar#addFluidTags`、`addMaterialTags` 和
`addModifierTags` 追加条目，而不是替换整个 tag provider。

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

- 用 addon 自有 ID holder 类保存联动自有的材质/modifier ID
- core `MaterialIds` 或 `ModifierIds` 中真正共享的条目
- 联动自有静态 modifier 的 addon-owned 占位 holder
- 对联动自有流体，通过 `AddonSmelteryCompat` 持有的 addon 冶炼兼容定义
- 其他 TiC 系统仍然依赖的共享/core 流体或 `SmelteryCompat` 定义
- 基础客户端元数据，例如颜色、翻译、modifier 显示配置

如果其他 TiC 系统仍然依赖某些共享注册级原语，就把这些原语留在 core；联动自有 ID 则应留在 addon 的 holder 类中。

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
  - integration-owned smeltery compat, molten fluids, fluid tags, and fluid
    textures routed through `AddonSmelteryCompat`
- Core-owned:
  - shared registry primitives that may reference addon-owned IDs
  - runtime modifier placeholders for truly core or shared modifiers
  - shared/core molten fluid registrations
  - shared/core smeltery compat enum entries
  - base client metadata such as colors/translations/modifier model mapping

This means addonization is about ownership, not just moving files. Botania owns
its optional behavior, generated content, IDs, placeholders, and smeltery compat
entries; core still owns shared registry primitives and generic hooks that other
TiC systems depend on.

## Why Ownership Still Matters

### 1. IDs Must Be Stable And Globally Addressable

Stable material and modifier IDs do not automatically belong in core
`MaterialIds` or `ModifierIds`. When an integration owns those IDs, they can
live in addon-owned ID holder classes while still using stable
`ResourceLocation` values.

Current Botania examples:

- `BotaniaMaterialIds`: `manaSteel`, `terraSteel`
- `BotaniaModifierIds`: `manafix`, `terrarecover`

Those constants resolve to stable `tconstruct:*` IDs owned by the Botania
integration. Shared core registry primitives may still reference those IDs when
they participate in common TiC systems.

If a shared core system needs one of these IDs, it should depend on the
addon-owned holder class instead of:

- start reconstructing raw strings such as `"tconstruct:manasteel"` in many
  places, or
- moving the ID back into core `MaterialIds`/`ModifierIds` just because a core
  registry primitive references it.

Neither option is desirable.

### 2. Runtime Placeholder Modifiers Must Exist Before Addon Binding

Integration-owned `StaticModifier` placeholders should live in the addon
package beside the addon-owned modifier IDs, while the concrete
implementations are registered through `ITiCStaticModifierAddon`.

This is necessary because code and JSON-facing systems still need a stable
runtime handle for those modifier IDs before static registration completes.
The addon owns both that placeholder identity and the implementation binding.

For Botania:

- `BotaniaModifiers.manafix`
- `BotaniaModifiers.terrarecover`

The important distinction is:

- Botania-specific IDs and placeholders are addon-owned
- the addon provides the concrete modifier binding
- core `TinkerModifiers` placeholders are only for truly core or shared
  modifiers

### 3. Integration Fluids Can Be Addon-Owned

Integration-owned molten compat metals should live behind
`AddonSmelteryCompat` when they do not need a core `SmelteryCompat` enum entry.
The compat implementation owns the whole optional fluid path:

- fluid object registration
- creative tab insertion
- tags
- generated textures and camera metadata
- bucket/block registration
- material/compat metadata entries

For Botania this includes:

- `moltenManaSteel`
- `moltenTerraSteel`

Those fields live in `BotaniaSmelteryCompat`, which implements
`AddonSmelteryCompat`. Core `TinkerFluids` only calls the generic addon hook for
creative tab items, and the core fluid tag/texture providers do not mention
Botania fluids directly.

If a future fluid is truly shared by multiple core systems, keeping that fluid
in `TinkerFluids` can still be correct. The key rule is to document whether the
owner is the addon compat implementation or a shared core registry.

### 4. Smeltery Compat Uses Core And Addon Tables

`SmelteryCompat` remains the core-owned table for shared smeltery compat
definitions. Integration-owned entries can instead be represented by
`AddonSmelteryCompat.Entry` and supplied by the addon.

For Botania, `BotaniaSmelteryCompat.INSTANCE.entries()` replaces the old core
enum entries. This keeps Botania-specific names, materials, and fluids out of
`SmelteryCompat` while preserving a single addon hook for smeltery-related
metadata.

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

> The core no longer contains any shared registry primitive, fluid, enum, or
> metadata entry related to that compat.

That stronger form requires every affected system to have an addon-facing
contract, such as `AddonSmelteryCompat` for integration-owned smeltery fluids
and metadata.

## Guidance For Future Internal Addons

When adding another internal addon like Botania, use this rule:

- move optional content generation and registration wiring into the addon
- use addon-owned ID holder classes for integration-owned stable
  materials/modifiers
- use addon-owned compat contracts, such as `AddonSmelteryCompat`, for
  integration-owned fluids and smeltery metadata
- keep shared registry-level primitives in core only when other TiC systems
  already depend on them, even if those primitives reference addon-owned IDs

Before deciding a compat piece can leave the core path entirely, verify whether
it is required by any of the following:

- addon-owned or shared ID constants
- runtime placeholder references
- addon-owned or shared fluid registration
- addon-owned or core smeltery compat tables
- base client metadata

If yes, choose the narrowest owner that fits the shared behavior and document
that choice. For integration-owned fluid compat, prefer an addon contract before
falling back to core ownership.

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
  - 通过 `AddonSmelteryCompat` 接入的联动自有冶炼兼容、熔融流体、fluid tag 和流体贴图
- 由 core 持有：
  - 可能引用 addon 自有 ID 的共享注册级原语
  - 真正属于 core 或共享系统的运行时 modifier 占位
  - 共享/core 熔融流体注册
  - 共享/core 冶炼兼容枚举项
  - 基础客户端元数据，例如颜色、翻译、modifier 模型映射

这意味着，addon 化关注的是所有权，而不只是移动文件。Botania 持有自己的可选行为、生成内容、ID、占位和冶炼兼容条目；core 仍然持有其他 TiC 系统依赖的共享注册级原语和通用 hook。

## 为什么所有权仍然重要

### 1. ID 必须稳定且可被全局引用

稳定的材质和 modifier ID 并不一定要放在 core 的 `MaterialIds` 或 `ModifierIds` 中。当某个联动拥有这些 ID 时，它们可以放在 addon 自有的 ID holder 类里，同时继续使用稳定的 `ResourceLocation` 值。

当前 Botania 对应的例子有：

- `BotaniaMaterialIds`：`manaSteel`、`terraSteel`
- `BotaniaModifierIds`：`manafix`、`terrarecover`

这些常量解析出来的仍然是稳定的 `tconstruct:*` ID，只是所有权属于 Botania 联动。共享的 core 注册级原语在参与公共 TiC 系统时，仍然可以引用这些 ID。

如果某个共享 core 系统需要这些 ID，应该依赖 addon 自有的 holder 类，而不是：

- 到处手写 `"tconstruct:manasteel"` 这类字符串，或者
- 仅仅因为某个 core 注册级原语引用了这个 ID，就把它移回 core 的 `MaterialIds`/`ModifierIds`

### 2. 运行时 modifier 占位必须先于 addon 绑定存在

联动自有的 `StaticModifier` 占位应与联动自有的 modifier ID 一起放在 addon 包中，而具体实现仍然通过 `ITiCStaticModifierAddon` 注册。

原因是代码和面向 JSON 的系统在静态注册完成前仍然需要一个稳定的运行时引用来表示这些 modifier ID。addon 同时持有这个占位身份和具体实现绑定。

对于 Botania：

- `BotaniaModifiers.manafix`
- `BotaniaModifiers.terrarecover`

这里真正重要的区别是：

- Botania 专属 ID 和占位都由 addon 持有
- addon 负责提供具体 modifier 绑定
- core `TinkerModifiers` 的占位只用于真正属于 core 或共享系统的 modifier

### 3. 联动流体可以由 addon 持有

联动自有的熔融兼容金属，如果不需要 core `SmelteryCompat` 枚举项，就应通过 `AddonSmelteryCompat` 持有。这个兼容实现负责整条可选流体链路：

- 流体对象注册
- 创造模式栏插入
- tag
- 贴图和 camera 元数据生成
- 桶/方块注册
- 材质/兼容元数据条目

对于 Botania，这包括：

- `moltenManaSteel`
- `moltenTerraSteel`

这些字段现在位于实现 `AddonSmelteryCompat` 的 `BotaniaSmelteryCompat` 中。core `TinkerFluids` 只调用通用 addon hook 来插入创造模式栏物品，core 的 fluid tag/texture provider 不再直接提到 Botania 流体。

如果未来某个流体确实被多个 core 系统共享，继续放在 `TinkerFluids` 中仍然可以是正确选择。关键是明确记录它的所有者是 addon 兼容实现，还是共享 core 注册表。

### 4. 冶炼兼容分为 core 表和 addon 表

`SmelteryCompat` 仍然是 core 持有的共享冶炼兼容定义表。联动自有条目可以改由 `AddonSmelteryCompat.Entry` 表示，并由 addon 提供。

对于 Botania，`BotaniaSmelteryCompat.INSTANCE.entries()` 取代了原来的 core 枚举条目。这样可以让 Botania 专属名称、材质和流体离开 `SmelteryCompat`，同时保留一个统一的 addon hook 来暴露冶炼相关元数据。

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

> core 中从此不再包含任何与该联动有关的共享注册级原语、流体、枚举项或元数据。

后者要求每个相关系统都有面向 addon 的 contract，例如 `AddonSmelteryCompat` 负责联动自有冶炼流体和元数据。

## 对未来内部 addon 的建议

以后继续添加类似 Botania 的内部 addon 时，建议遵循这个规则：

- 把可选内容生成与注册接线迁入 addon
- 对联动自有的稳定材质/modifier 使用 addon 自有 ID holder 类
- 对联动自有流体和冶炼元数据，优先使用 `AddonSmelteryCompat` 这类 addon 自有兼容 contract
- 只有当某些共享注册级原语已经被多个 TiC 系统依赖时，才继续保留在 core，即使这些原语引用 addon 自有 ID

在决定某个联动内容是否可以彻底离开主链路前，先确认它是否被以下任一项需要：

- addon 自有或共享 ID 常量
- 运行时占位引用
- addon 自有或共享流体注册
- addon 自有或 core 冶炼兼容表
- 基础客户端元数据

如果答案是需要，就选择能承载该共享行为的最窄所有者，并把这个选择写清楚。对于联动自有流体兼容，优先尝试 addon contract，再退回 core 持有。
