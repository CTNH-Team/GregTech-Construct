# GADGETS DOMAIN

## OVERVIEW
Standalone gadget blocks, entities, items, capabilities, client behavior, and gadget data.

## WHERE TO LOOK
- Module and registry objects: `TinkerGadgets.java`.
- Gadget blocks and block entities: `block/`.
- Gadget entities and behavior: `entity/`.
- Capability/state integration: `capability/`.
- Gadget-specific data and item behavior: `data/` and `item/`.

## CONVENTIONS
- Register gadget objects through the module and shared registration helpers.
- Keep client-only renderers and screens under `client/`.
- Entity behavior belongs with its entity type; capability state belongs under `capability/`.
- Reuse library hooks and tool interfaces instead of reaching into implementation classes.

## ANTI-PATTERNS
- Do not make gadget registration depend on optional integration modules.
- Do not access client classes from common entity or item construction.
- Do not duplicate shared tool/material logic that belongs in `library`.

## NOTES
- `TinkerGadgets` is one of the base modules constructed directly by `TConstruct`.
- Gadget capabilities should expose stable interfaces to items and entities.
- Client package classes must be loaded only from client setup or client event subscribers.
- Generated gadget data belongs with the relevant provider, not in runtime entity constructors.

## CHANGE IMPACT
- Entity or capability changes should include a registry-backed test where practical.
- Block/item ids affect authored and generated resource paths.

## TESTING
- Keep common behavior tests independent of client rendering classes.
