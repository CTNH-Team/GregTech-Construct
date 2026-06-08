/*
 * Adapted from TinkersGears by Chemiofitor, licensed under the MIT License.
 */
package slimeknights.tconstruct.plugin.create.modifier;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierId;

public final class CreateModifierIds {

    public static final ModifierId CRUSHING = id("crushing");
    public static final ModifierId EXTENDO = id("extendo");
    public static final ModifierId GOGGLES = id("goggles");
    public static final ModifierId WRENCH = id("wrench");
    public static final ModifierId DIVING_WEIGHTS = id("diving_weights");

    private CreateModifierIds() {}

    private static ModifierId id(String name) {
        return new ModifierId(TConstruct.MOD_ID, name);
    }
}
