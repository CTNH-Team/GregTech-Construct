# TABLES DOMAIN

## OVERVIEW
Tinker Station, tool tables, menus, screens, block entities, slots, and their network packets.

## WHERE TO LOOK
- Main registration module: `TinkerTables.java`.
- Table blocks and block entities: `block/`.
- Menus and slot rules: `menu/`.
- Client screens and inventory views: `client/`.
- Packets and synchronization: `network/`.
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

## CHANGE IMPACT
- Menu slot order is a compatibility surface for packets and saved screen state.
- Any packet schema change needs serialization and interaction coverage.

## TESTING
- Use the existing table packet and menu tests as the contract for client/server interaction.
