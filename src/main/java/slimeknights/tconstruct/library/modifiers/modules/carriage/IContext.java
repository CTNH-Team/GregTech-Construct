package slimeknights.tconstruct.library.modifiers.modules.carriage;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IContext {
    @Nullable
    default LivingEntity holder() { return null; }

    @Nullable
    default Damage damage() { return null; }

    @Nullable
    default ItemStack stack() { return null; }
}
