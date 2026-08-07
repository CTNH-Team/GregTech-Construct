# TABLES DOMAIN

## OVERVIEW
Tinker Station, tool tables (including the crafting station), menus, screens, block entities, slots, and their network packets.

## WHERE TO LOOK
- Main registration module: `TinkerTables.java`.
- Table blocks and block entities: `block/` (crafting station: `block/CraftingStationBlock.java`, `block/entity/table/CraftingStationBlockEntity.java`).
- Menus and slot rules: `menu/` (crafting station: `menu/CraftingStationContainerMenu.java`).
- Client screens and inventory views: `client/` (crafting station: `client/inventory/CraftingStationScreen.java`).
- Packets and synchronization: `network/` (crafting station: `network/UpdateCraftingRecipePacket.java`).
- Table integration tests: `src/test/java/slimeknights/tconstruct/tables`.

## CONVENTIONS
- Keep authoritative inventory/menu decisions on the common side; screens mirror server state.
- Packet handlers must validate the sender, menu, slot, and requested action before mutation.
- Put shared slot/layout rules in table or library layout classes, not in screen rendering.
- Use existing tool build and modifier APIs when tables apply changes to a tool.

## ANTI-PATTERNS
- Do not trust client packet values for item, slot, or material changes.
- Do not mutate server inventories from client screen code.
- Do not put table-specific registration into `TinkerTools` just because tools are displayed there.
- Do not share mutable worktable NBT; return a copy where the API requires it.

## NOTES
- Table registration is performed before complete tool registration, so menu suppliers may reference tool types indirectly.
- `client/` screens are consumers of menu state, not owners of authoritative recipe or inventory rules.
- Network packet tests live beside table tests and are useful for catching serialization drift.
- Reuse `library/tools/layout` for slot layout contracts instead of duplicating slot indexes.
- The crafting station menu syncs stacks through `common/network/HighStackCountSynchronizer` (channel version `"3"`) so counts beyond vanilla's byte field stay correct.

## CHANGE IMPACT
- Menu slot order is a compatibility surface for packets and saved screen state.
- Any packet schema change needs serialization and interaction coverage.

## TESTING
- Use the existing table packet and menu tests as the contract for client/server interaction.

## SCOPE
Applies to `src/main/java/slimeknights/tconstruct/tables`, including common menus, client screens, slots, block entities, and packets.

## READ WHEN
- Changing table inventory rules, slot layouts, menu state, screen behavior, or packet payloads.

## SOURCE OF TRUTH
- Server authority: menus, block entities, recipes, and common packet handlers.
- Client projection: `client/` screens and inventory views.
- Compatibility layout rules: `library/tools/layout`.

## WORKFLOW
1. Update or inspect the authoritative common-side rule first.
2. Lock packet and menu behavior with focused tests.
3. Run `runClient` for screen changes; use a server-backed test for inventory mutations.
