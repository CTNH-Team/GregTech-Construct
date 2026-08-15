package com.chemiofitor.tponder.scene;

import com.chemiofitor.tponder.TinkersPonder;
import com.chemiofitor.tponder.scene.PonderConstants;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public final class HeaterScene {
    private HeaterScene() {}

    public static void using(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("heater_using", "Use the Heater");

        TinkersPonder.init5x5(builder, util);

        BlockPos center = util.grid().at(2, 1, 2);

        BlockPos alloyer = center.above().east();
        BlockPos melter = center.above().west();

        Selection all = util.select().fromTo(melter.below(), alloyer);

        builder.idle(PonderConstants.TICK_SHORT);
        builder.world().showSection(util.select().position(center), Direction.NORTH);
        builder.idle(PonderConstants.TICK_IDLE);
        builder.overlay().showText(PonderConstants.TEXT_MEDIUM)
                .colored(PonderPalette.GREEN)
                .text("The Heater can burn solid fuel to provide a temperature of 800℃")
                .pointAt(util.vector().topOf(center))
                .attachKeyFrame();
        builder.idle(45);

        builder.idle(PonderConstants.TICK_IDLE);
        builder.world().destroyBlock(center);
        builder.idle(PonderConstants.TICK_IDLE);
        builder.world().showSection(all, Direction.NORTH);

        builder.idle(PonderConstants.TICK_MEDIUM);

        builder.addLazyKeyframe();
        builder.overlay().showControls(util.vector().blockSurface(alloyer.below(), Direction.NORTH), Pointing.RIGHT,
                PonderConstants.CONTROL_TICKS)
                .withItem(new ItemStack(Items.COAL));
        builder.overlay().showControls(util.vector().blockSurface(melter.below(), Direction.NORTH), Pointing.RIGHT,
                PonderConstants.CONTROL_TICKS)
                .withItem(new ItemStack(Items.COAL));

        builder.idle(PonderConstants.TICK_EXTENDED);
        builder.markAsFinished();
    }
}
