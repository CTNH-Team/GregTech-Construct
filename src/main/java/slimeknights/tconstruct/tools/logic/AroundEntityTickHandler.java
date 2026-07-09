package slimeknights.tconstruct.tools.logic;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaAreaEffectModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.FormulaRecurrenceModule;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;

@Mod.EventBusSubscriber(modid = TConstruct.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AroundEntityTickHandler {
  private AroundEntityTickHandler() {}

  @SubscribeEvent
  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    Player player = event.player;
    if (player.level().isClientSide || player.isSpectator() || !player.isAlive()) {
      return;
    }

    EquipmentContext context = new EquipmentContext(player);
    if (event.phase == TickEvent.Phase.START) {
      runSelfAndArmorTicks(player, context);
    }
    List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(Config.guardingScanRange()), entity -> entity != player && entity.isAlive());
    if (targets.isEmpty()) {
      PlayerPersistentDataCache.syncFromEntity(player);
      return;
    }
    for (EquipmentSlot slotType : ModifiableArmorMaterial.ARMOR_SLOTS) {
      IToolStackView tool = context.getToolInSlot(slotType);
      if (tool == null || tool.isBroken()) {
        continue;
      }
      for (ModifierEntry entry : tool.getModifierList()) {
        for (LivingEntity target : targets) {
          double distance = player.distanceTo(target);
          if (event.phase == TickEvent.Phase.START) {
            entry.getHook(ModifierHooks.AROUND_ENTITY_TICK).onAroundEntityTickStart(tool, entry, slotType, player, target, distance);
          } else {
            entry.getHook(ModifierHooks.AROUND_ENTITY_TICK).onAroundEntityTickEnd(tool, entry, slotType, player, target, distance);
          }
        }
      }
    }
    PlayerPersistentDataCache.syncFromEntity(player);
    if (event.phase == TickEvent.Phase.END) {
      FormulaAreaEffectModule.flushPending(player);
    }
  }

  private static void runSelfAndArmorTicks(Player player, EquipmentContext context) {
    for (EquipmentSlot slotType : ModifiableArmorMaterial.ARMOR_SLOTS) {
      IToolStackView tool = context.getToolInSlot(slotType);
      if (tool == null || tool.isBroken()) {
        continue;
      }
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.SELF_TICK).onSelfTick(tool, entry, slotType, player);
        entry.getHook(ModifierHooks.ARMOR_TICK).onArmorTick(tool, entry, slotType, player);
      }
    }
    FormulaRecurrenceModule.flushArmorTicks(player);
  }
}
