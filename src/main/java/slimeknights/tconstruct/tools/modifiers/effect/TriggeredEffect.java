package slimeknights.tconstruct.tools.modifiers.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerEffect;
import slimeknights.tconstruct.shared.TinkerEffects;

@EventBusSubscriber(modid = TConstruct.MOD_ID, bus = Bus.FORGE)
public class TriggeredEffect extends TinkerEffect {
  public TriggeredEffect() {
    super(MobEffectCategory.BENEFICIAL, 0x7E6059, true);
    addAttributeModifier(Attributes.ATTACK_DAMAGE, "c72d8a1e-3f5b-4a2c-9e81-7f6d1b4a3c52", 5.0, Operation.ADDITION);
    addAttributeModifier(Attributes.ARMOR, "a1e5f8d2-6c3b-4f9a-8d7e-2b5c1f3a4e69", 5.0, Operation.ADDITION);
    addAttributeModifier(Attributes.MOVEMENT_SPEED, "d4b2e7f1-9a3c-4f6d-8c1e-5a2b3d7f9e14", 0.2, Operation.MULTIPLY_BASE);
  }

  @SubscribeEvent
  static void onHurt(LivingHurtEvent event) {
    LivingEntity entity = event.getEntity();
    if (entity instanceof TamableAnimal && entity.hasEffect(TinkerEffects.triggered.get())) {
      int level = TinkerEffect.getLevel(entity, TinkerEffects.triggered.get());
      event.setAmount((float)(event.getAmount() * Math.pow(0.8, level)));
    }
  }
}
