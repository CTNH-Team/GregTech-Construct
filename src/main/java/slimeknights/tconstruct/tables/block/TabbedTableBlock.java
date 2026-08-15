package slimeknights.tconstruct.tables.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import slimeknights.mantle.inventory.BaseContainerMenu;
import slimeknights.tconstruct.shared.block.TableBlock;
import slimeknights.tconstruct.tables.menu.TabbedContainerMenu;

public abstract class TabbedTableBlock extends TableBlock implements ITabbedBlock {

  public TabbedTableBlock(Properties builder) {
    super(builder);
  }

  @Override
  public boolean openGui(Player player, Level world, BlockPos pos) {
    if (!world.isClientSide()) {
      MenuProvider container = this.getMenuProvider(world.getBlockState(pos), world, pos);
      if (container != null && player instanceof ServerPlayer serverPlayer) {
        // send the merged side inventory slot count along with the tile position so the client
        // menu mirrors the server layout instead of re-detecting adjacent containers
        int sideSlotCount = TabbedContainerMenu.detectSideInventories(world, pos, player).slotCount();
        NetworkHooks.openScreen(serverPlayer, container, buf -> {
          buf.writeBlockPos(pos);
          buf.writeVarInt(sideSlotCount);
        });
        if (player.containerMenu instanceof BaseContainerMenu<?> menu) {
          menu.syncOnOpen(serverPlayer);
        }
      }
    }

    return true;
  }
}
