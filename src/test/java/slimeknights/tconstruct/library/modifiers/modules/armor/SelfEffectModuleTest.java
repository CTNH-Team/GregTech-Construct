package slimeknights.tconstruct.library.modifiers.modules.armor;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.tconstruct.library.json.LevelingValue;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.DummyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.test.BaseMcTest;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SelfEffectModuleTest extends BaseMcTest {
  @Test
  void selfTickAppliesEffectToHolder() {
    LivingEntity holder = mock(LivingEntity.class);
    net.minecraft.world.level.Level level = mock(net.minecraft.world.level.Level.class);
    when(holder.level()).thenReturn(level);
    when(level.isClientSide()).thenReturn(false);
    holder.tickCount = 20;

    SelfEffectModule.selfEffect(MobEffects.GLOWING, LevelingValue.flat(40), LevelingValue.flat(1))
      .onSelfTick(new TestToolStack(), new ModifierEntry(slimeknights.tconstruct.tools.data.ModifierIds.insulation, 2), EquipmentSlot.CHEST, holder);

    verify(holder).addEffect(argThat((MobEffectInstance effect) ->
      effect.getEffect() == MobEffects.GLOWING && effect.getDuration() == 40 && effect.getAmplifier() == 1));
  }

  private static class TestToolStack extends DummyToolStack {
    private TestToolStack() {
      super(Items.AIR, ModifierNBT.EMPTY, new ModDataNBT());
    }
  }
}
