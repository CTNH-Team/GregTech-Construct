package com.chemiofitor.tponder.scene;

import com.chemiofitor.tponder.TinkersPonder;
import com.chemiofitor.tponder.scene.PonderConstants;
import com.chemiofitor.tponder.scene.SceneHelper;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public final class FoundryScene {
    private FoundryScene() {}

    public static void building(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("foundry_building", "Building the Foundry");

        TinkersPonder.init9x9(builder, util);

        BlockPos bottomCenter = util.grid().at(4, 1, 4);

        Selection second = util.select().fromTo(2, 2, 2, 6, 2, 6);
        Selection bottom = util.select().fromTo(2, 1, 2, 6, 1, 6);
        Selection foundry = second.copy().add(bottom);
        Selection cast = util.select().fromTo(1, 1, 4, 1, 2, 5);

        builder.idle(5);
        builder.world().showSection(foundry, Direction.DOWN);

        builder.idle(20);
        builder.overlay().showOutline(PonderPalette.GREEN, foundry, foundry, 130);
        builder.overlay().showText(35)
                .colored(PonderPalette.GREEN)
                .text("The Foundry is very similar to The Smeltery")
                .pointAt(util.vector().topOf(bottomCenter.above()))
                .attachKeyFrame();
        builder.idle(45);
        builder.overlay().showText(35)
                .colored(PonderPalette.GREEN)
                .text("But it uses scorched blocks and its edges must be filled")
                .pointAt(util.vector().topOf(bottomCenter.above()))
                .attachKeyFrame();
        builder.idle(60);

        TinkersPonder.rotateAround(builder, 60, 90);

        builder.idle(20);

        builder.overlay().showText(100)
                .colored(PonderPalette.GREEN)
                .text("The outer wall can also be extended upwards by up to 63 blocks")
                .attachKeyFrame();

        builder.idle(10);

        SceneHelper.showWallLayers(builder, util,
                util.grid().at(2, 0, 2), util.grid().at(6, 0, 6),
                3, 9, Direction.DOWN, true);
        builder.idle(20);
        builder.addLazyKeyframe();
        builder.world().showSection(cast, Direction.NORTH);

        builder.idle(30);

        builder.overlay().showText(100)
                .text("The Foundry has unique features:\nIt will produce some by-products when smelting\nAnd it can't be used to make alloys")
                .colored(PonderPalette.MEDIUM);
        builder.idle(120);

        builder.idle(60);
        builder.markAsFinished();
    }
}
