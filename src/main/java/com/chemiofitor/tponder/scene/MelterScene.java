package com.chemiofitor.tponder.scene;

import com.chemiofitor.tponder.TinkersPonder;
import com.chemiofitor.tponder.scene.PonderConstants;
import com.chemiofitor.tponder.scene.SceneHelper;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.controller.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.controller.MelterBlock;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.MelterBlockEntity;

public final class MelterScene {
    private MelterScene() {}

    public static void building(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("melter_building", "Building the Melter");

        TinkersPonder.init5x5(builder, util);

        Selection smeltery = util.select().fromTo(2, 1, 2, 2, 2, 2);
        Selection basin = util.select().fromTo(1, 1, 2, 1, 2, 2);
        Selection table = util.select().fromTo(3, 1, 2, 3, 2, 2);
        BlockPos melter = util.grid().at(2, 2, 2);

        builder.idle(5);
        builder.world().showSection(util.select().position(melter.below()), Direction.DOWN);
        builder.idle(5);
        builder.world().showSection(util.select().position(melter), Direction.DOWN);

        builder.idle(20);
        builder.overlay().showOutline(PonderPalette.GREEN, smeltery, util.select().fromTo(2, 1, 2, 2, 2, 2), 80);

        builder.idle(25);
        builder.overlay().showText(40)
                .text("The Melter is your first device as a tinker")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(melter));

        builder.idle(25);
        builder.overlay().showText(40)
                .text("It requires a heater for heat")
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().blockSurface(melter.below(), Direction.WEST));

        builder.idle(60);
        builder.overlay().showText(40)
                .text("The Heater needs fuel to work")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().blockSurface(melter.below(), Direction.WEST));
        builder.idle(45);

        builder.overlay().showControls(util.vector().blockSurface(melter.below(), Direction.NORTH), Pointing.RIGHT, 20)
                .withItem(new ItemStack(Items.COAL));
        builder.world().modifyBlock(melter.below(), block -> block.setValue(ControllerBlock.ACTIVE, true), false);

        builder.idle(40);
        builder.overlay().showText(40)
                .text("Or you can use fuel tank")
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(melter.below(), Direction.WEST));

        builder.idle(25);
        builder.world().destroyBlock(melter.below());
        builder.idle(5);
        builder.world().setBlock(melter.below(), TinkerSmeltery.searedTank.get(SearedTankBlock.TankType.FUEL_TANK).defaultBlockState(), false);

        builder.idle(30);
        builder.overlay().showControls(util.vector().blockSurface(melter.below(), Direction.NORTH), Pointing.RIGHT,
                PonderConstants.CONTROL_TICKS).rightClick()
                .withItem(new ItemStack(Items.LAVA_BUCKET));
        SceneHelper.fillFluid(builder, melter.below(), TankBlockEntity.class,
                Fluids.LAVA, PonderConstants.TANK_CAPACITY);

        builder.overlay().showText(25)
                .text("Also, don’t forget to add fuel such as Lava")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().blockSurface(melter.below(), Direction.WEST));
        builder.idle(60);

        builder.overlay().showText(20)
                .text("Finally, install the casting parts")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().blockSurface(melter, Direction.UP));

        builder.idle(30);
        builder.world().showSection(basin, Direction.DOWN);
        builder.idle(5);
        builder.world().showSection(table, Direction.DOWN);
        builder.idle(40);

        builder.markAsFinished();
    }

    public static void using(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("melter_using", "Melting and Casting");

        TinkersPonder.init5x5(builder, util);

        BlockPos table = util.grid().at(1, 1, 2);
        BlockPos basin = util.grid().at(3, 1, 2);
        BlockPos melter = util.grid().at(2, 2, 2);
        Selection selection = util.select().fromTo(basin, table.above());

        builder.idle(5);
        builder.world().showSection(selection, Direction.DOWN);
        builder.idle(10);
        builder.overlay().showText(20)
                .text("Melting to get molten material")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().blockSurface(melter, Direction.UP));
        builder.idle(30);

        ElementLink<EntityElement> itemLink = builder.world().createItemEntity(
                melter.above().getCenter(),
                util.vector().of(0, 0.1, 0),
                new ItemStack(Items.RAW_GOLD)
        );
        builder.idle(10);
        builder.world().modifyEntity(itemLink, Entity::discard);
        builder.world().modifyBlockEntity(melter, MelterBlockEntity.class, m -> {
            m.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                handler.insertItem(0, new ItemStack(Items.RAW_GOLD), false);
            });
        });
        builder.world().modifyBlock(melter, block -> block.setValue(MelterBlock.ACTIVE, true), false);

        builder.idle(10);
        builder.overlay().showText(20)
                .text("Waiting to melt")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().blockSurface(melter, Direction.NORTH));
        builder.idle(80);

        builder.world().modifyBlockEntity(melter, MelterBlockEntity.class, m -> {
            m.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                handler.extractItem(0, 1, false);
            });
        });
        SceneHelper.fillFluid(builder, melter, MelterBlockEntity.class,
                GTMaterials.Gold.getFluid(), PonderConstants.INGOT_AMOUNT + PonderConstants.FAUCET_PULSE);
        builder.world().modifyBlock(melter, block -> block.setValue(MelterBlock.ACTIVE, false), false);

        builder.idle(20);
        builder.overlay().showText(35)
                .text("Right Click faucet to cast")
                .attachKeyFrame()
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().blockSurface(table.above(), Direction.EAST));

        builder.idle(55);

        builder.overlay().showControls(util.vector().blockSurface(table.above(), Direction.EAST), Pointing.RIGHT,
                PonderConstants.CONTROL_TICKS)
                .rightClick();
        SceneHelper.simulateCasting(builder,
                table.above(), table, melter,
                GTMaterials.Gold.getFluid(),
                PonderConstants.FAUCET_PULSE, PonderConstants.FAUCET_PULSES_FULL,
                PonderConstants.TICK_SHORT,
                MelterBlockEntity.class);
        builder.idle(10);
        builder.idle(40);
        builder.world().modifyBlockEntity(table, CastingBlockEntity.Table.class, t -> {
            t.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                handler.getStackInSlot(0).setCount(0);
            });
        });

        builder.idle(60);
        builder.markAsFinished();
    }
}
