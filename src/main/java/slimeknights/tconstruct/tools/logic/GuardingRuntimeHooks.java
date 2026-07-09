package slimeknights.tconstruct.tools.logic;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

@Mod.EventBusSubscriber(modid = TConstruct.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuardingRuntimeHooks {
  private GuardingRuntimeHooks() {}

  @SubscribeEvent
  public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    Player player = event.getEntity();
    EquipmentContext context = new EquipmentContext(player);
    for (EquipmentSlot slotType : EquipmentSlot.values()) {
      if (!ModifierUtil.validArmorSlot(player, slotType)) {
        continue;
      }
      IToolStackView tool = context.getToolInSlot(slotType);
      if (tool == null || tool.isBroken()) {
        continue;
      }
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.PLAYER_LOGIN).onPlayerLogin(tool, entry, player);
      }
    }
  }

  @SubscribeEvent
  public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    GuardingCache.removePlayer(event.getEntity().getUUID());
    GuardingCache.clearHostilityFor(event.getEntity().getUUID());
    PlayerPersistentDataCache.remove(event.getEntity().getUUID());
  }
}
