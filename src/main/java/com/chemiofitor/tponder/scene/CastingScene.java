package com.chemiofitor.tponder.scene;

import com.chemiofitor.tponder.TinkersPonder;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.AbstractCastingBlock;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.ChannelBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;
import org.jetbrains.annotations.NotNull;

public final class CastingScene {
    private CastingScene() {}

    public static void cast(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("casting", "Casting");

        TinkersPonder.init5x5(builder, util);

        BlockPos table0 = util.grid().at(2, 1, 1);

        BlockPos table1 = util.grid().at(3, 1, 3);
        BlockPos table2 = util.grid().at(1, 1, 3);

        BlockPos basin1 = util.grid().at(3, 1, 1);
        BlockPos basin2 = util.grid().at(1, 1, 1);

        BlockPos center = util.grid().at(2, 2, 2);

        Fluid moltenIron = GTMaterials.Iron.getFluid();

        builder.idle(PonderConstants.TICK_SHORT);
        builder.world().showSection(util.select().fromTo(table0, center), Direction.NORTH);

        builder.idle(PonderConstants.TICK_EXTENDED - 20);
        builder.addLazyKeyframe();
        builder.overlay().showControls(util.vector().centerOf(table0.above()), Pointing.RIGHT,
                PonderConstants.CONTROL_TICKS).rightClick();
        builder.idle(PonderConstants.TICK_SHORT);

        for (int i = 0; i < PonderConstants.FAUCET_PULSES_FULL; i++) {
            SceneHelper.activateFaucet(builder, table0.above(), moltenIron, PonderConstants.FAUCET_PULSE);
            SceneHelper.fillCastingTable(builder, table0, moltenIron, PonderConstants.FAUCET_PULSE);
            builder.idle(PonderConstants.TICK_SHORT);
        }
        builder.idle(10);
        SceneHelper.deactivateFaucet(builder, table0.above());
        builder.idle(PonderConstants.TICK_EXTENDED);

        builder.world().destroyBlock(table0);
        builder.world().destroyBlock(table0.above());

        builder.idle(PonderConstants.TICK_IDLE);

        builder.world().showSection(util.select().fromTo(table1.above(), basin2), Direction.NORTH);

        builder.idle(PonderConstants.TICK_SHORT);

        TinkersPonder.rotate(builder, PonderConstants.TICK_IDLE, 90);
        builder.idle(PonderConstants.TICK_IDLE);
        builder.addLazyKeyframe();

        // --- East channel casting (table1 = south, basin1 = north) ---
        simulateChannelCasting(builder, util,
                center.east(), center.east().below(),
                table1, basin1, moltenIron);

        builder.idle(200);
        builder.addLazyKeyframe();

        TinkersPonder.rotate(builder, PonderConstants.TICK_IDLE, -180);

        builder.idle(PonderConstants.TICK_IDLE);
        builder.overlay().showText(240)
                .colored(PonderPalette.MEDIUM)
                .text("Scorched casting containers must be used with casts")
                .attachKeyFrame();
        builder.idle(45);

        builder.overlay().showControls(util.vector().topOf(table2), Pointing.RIGHT,
                PonderConstants.CONTROL_TICKS)
                .withItem(new ItemStack(TinkerSmeltery.ingotCast)).rightClick();
        builder.overlay().showControls(util.vector().topOf(basin2), Pointing.RIGHT,
                PonderConstants.CONTROL_TICKS)
                .withItem(new ItemStack(TinkerCommons.goldPlatform)).rightClick();
        builder.world().modifyBlockEntity(table2, CastingBlockEntity.Table.class, f -> {
            f.setItem(0, new ItemStack(TinkerSmeltery.ingotCast));
        });
        builder.world().modifyBlock(table2, b -> b.setValue(AbstractCastingBlock.HAS_ITEM, true), false);
        builder.world().modifyBlockEntity(basin2, CastingBlockEntity.Basin.class, f -> {
            f.setItem(0, new ItemStack(TinkerCommons.goldPlatform));
        });
        builder.world().modifyBlock(basin2, b -> b.setValue(AbstractCastingBlock.HAS_ITEM, true), false);
        builder.idle(45);

        // --- West channel casting (table2 = south, basin2 = north) ---
        simulateChannelCasting(builder, util,
                center.west(), center.west().below(),
                table2, basin2, moltenIron);

        builder.idle(200);
        builder.addLazyKeyframe();

        builder.idle(PonderConstants.TICK_EXTENDED);
        builder.markAsFinished();
    }

    /**
     * Simulate a channel-based casting setup: faucet above channel, channel
     * flowing fluid north and south into two casting containers.
     * The south container fills for 3 pulses (flag1 stops at i=2),
     * the north for 27 pulses (flag2 stops at i=26).
     */
    private static void simulateChannelCasting(@NotNull SceneBuilder builder,
                                               @NotNull SceneBuildingUtil util,
                                               @NotNull BlockPos faucetPos,
                                               @NotNull BlockPos channelPos,
                                               @NotNull BlockPos southTarget,
                                               @NotNull BlockPos northTarget,
                                               @NotNull Fluid fluid) {
        // Show controls and activate faucet
        builder.overlay().showControls(util.vector().centerOf(faucetPos), Pointing.RIGHT,
                PonderConstants.CONTROL_TICKS).rightClick();
        builder.idle(PonderConstants.TICK_IDLE);

        SceneHelper.activateFaucet(builder, faucetPos, fluid, PonderConstants.FAUCET_PULSE);
        builder.idle(1);

        // Set up channel fluid and initial flow directions
        builder.world().modifyBlockEntity(channelPos, ChannelBlockEntity.class, f -> {
            f.updateFluidTo(new FluidStack(fluid, PonderConstants.FAUCET_PULSE));
            f.setFlow(Direction.NORTH, true);
            f.setFlow(Direction.SOUTH, true);
        });

        boolean southDone = false;
        boolean northDone = false;

        for (int i = 0; i < 27; i++) {
            if (!southDone) {
                SceneHelper.fillCastingTable(builder, southTarget, fluid, PonderConstants.FAUCET_PULSE);
                if (i == 2) {
                    southDone = true;
                    builder.world().modifyBlockEntity(channelPos, ChannelBlockEntity.class, f ->
                            f.setFlow(Direction.SOUTH, false));
                }
            }
            if (!northDone) {
                SceneHelper.fillCastingBasin(builder, northTarget, fluid, PonderConstants.FAUCET_PULSE);
                if (i == 26) {
                    northDone = true;
                    builder.world().modifyBlockEntity(channelPos, ChannelBlockEntity.class, f ->
                            f.setFlow(Direction.NORTH, false));
                }
            }
            builder.idle(PonderConstants.TICK_SHORT);
        }
        SceneHelper.deactivateFaucet(builder, faucetPos);
    }

    public static void sand(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("sand_casting", "Use Sand Cast");

        TinkersPonder.init5x5(builder, util);

        BlockPos table = util.grid().at(2, 1, 1);
        BlockPos center = util.grid().at(2, 2, 2);

        Selection cast = util.select().fromTo(table, center);
        Fluid moltenIron = GTMaterials.Iron.getFluid();

        builder.idle(PonderConstants.TICK_SHORT);
        builder.world().showSection(cast, Direction.NORTH);

        builder.idle(PonderConstants.TICK_IDLE);
        builder.overlay().showText(PonderConstants.TEXT_LONG)
                .colored(PonderPalette.GREEN)
                .text("Sand Cast can only be used once")
                .pointAt(util.vector().topOf(table))
                .attachKeyFrame();
        builder.idle(PonderConstants.TICK_EXTENDED);
        builder.overlay().showText(PonderConstants.TEXT_MEDIUM)
                .colored(PonderPalette.GREEN)
                .text("Right Click with item to make shape")
                .pointAt(util.vector().topOf(table));
        builder.idle(PonderConstants.TEXT_LONG);

        builder.overlay().showControls(util.vector().topOf(table), Pointing.DOWN,
                PonderConstants.CONTROL_TICKS)
                .rightClick().withItem(new ItemStack(Items.BRICK));
        builder.idle(PonderConstants.TICK_SHORT);
        builder.world().modifyBlockEntity(table, CastingBlockEntity.Table.class, t -> {
            t.setItem(0, new ItemStack(TinkerSmeltery.ingotCast.getSand()));
            t.setItem(1, new ItemStack(Items.BRICK));
        });
        builder.idle(25);

        builder.overlay().showControls(util.vector().topOf(table), Pointing.DOWN,
                PonderConstants.CONTROL_TICKS)
                .rightClick();
        builder.idle(PonderConstants.TICK_SHORT);
        builder.world().modifyBlockEntity(table, CastingBlockEntity.Table.class, t -> {
            t.setItem(1, ItemStack.EMPTY);
        });

        builder.idle(25);
        builder.addLazyKeyframe();
        builder.overlay().showControls(util.vector().centerOf(table.above()), Pointing.RIGHT,
                PonderConstants.CONTROL_TICKS)
                .rightClick();
        builder.idle(PonderConstants.TICK_SHORT);
        builder.world().modifyBlockEntity(table.above(), FaucetBlockEntity.class, t -> {
            t.onActivationPacket(new FluidStack(moltenIron, PonderConstants.FAUCET_PULSE), true);
        });
        builder.idle(PonderConstants.TICK_SHORT);
        for (int i = 0; i < 3; i++) {
            SceneHelper.fillCastingTable(builder, table, moltenIron, PonderConstants.FAUCET_PULSE);
            builder.idle(PonderConstants.TICK_SHORT);
        }
        SceneHelper.deactivateFaucet(builder, table.above());
        builder.idle(PonderConstants.TICK_EXTENDED);
        builder.markAsFinished();
    }
}
