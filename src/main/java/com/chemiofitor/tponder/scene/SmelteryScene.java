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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.DuctBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;
import slimeknights.tconstruct.smeltery.item.CopperCanItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class SmelteryScene {
    private  SmelteryScene() {}

    public static void building(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("smeltery_building", "Build the Smeltery");

        TinkersPonder.init9x9(builder, util);

        BlockPos bottomCenter = util.grid().at(4, 1, 4);
        BlockPos controllerPos = util.grid().at(4, 2, 2);

        Selection bottom = util.select().fromTo(5, 1, 3, 3, 1, 5);
        Selection second = util.select().fromTo(6, 1, 2, 2, 2, 6);
        Selection controllers = util.select().fromTo(5, 2, 2, 3, 2, 2);
        Selection fluidFuelTank = util.select().position(5, 2, 2);
        Selection controller = util.select().position(4, 2, 2);
        Selection drain = util.select().position(3, 2, 2);

        builder.idle(5);
        builder.world().showSection(bottom, Direction.DOWN);

        builder.idle(20);
        builder.overlay().showOutline(PonderPalette.GREEN, bottom, bottom, 100);
        builder.overlay().showText(35)
                .colored(PonderPalette.GREEN)
                .text("The bottom of the Smelter is a rectangle composed of seared blocks")
                .pointAt(util.vector().topOf(bottomCenter))
                .attachKeyFrame();
        builder.idle(50);
        builder.overlay().showText(35)
                .colored(PonderPalette.GREEN)
                .text("The maximum size of a rectangle is 14x14 blocks")
                .pointAt(util.vector().topOf(bottomCenter));
        builder.idle(100);

        builder.world().showSection(controllers, Direction.SOUTH);
        builder.idle(20);

        builder.overlay().showOutline(PonderPalette.BLUE, controller, controller, 30);
        builder.overlay().showText(35)
                .colored(PonderPalette.BLUE)
                .text("You need place a controller outside the second layer")
                .pointAt(util.vector().topOf(controllerPos))
                .attachKeyFrame();
        builder.idle(40);

        builder.overlay().showOutline(PonderPalette.BLUE, fluidFuelTank, fluidFuelTank, 30);
        builder.overlay().showText(35)
                .colored(PonderPalette.BLUE)
                .text("You need to place at least one tank for fuel")
                .pointAt(util.vector().topOf(controllerPos.east()))
                .attachKeyFrame();
        builder.idle(40);

        builder.overlay().showOutline(PonderPalette.BLUE, drain, drain, 30);
        builder.overlay().showText(35)
                .colored(PonderPalette.BLUE)
                .text("To cast, you need plate a drain")
                .pointAt(util.vector().topOf(controllerPos.west()))
                .attachKeyFrame();
        builder.idle(40);

        builder.idle(20);
        List<BlockPos> bricks0 = List.of(
                util.grid().at(2, 2, 3), util.grid().at(2, 2, 4), util.grid().at(2, 2, 5),
                util.grid().at(3, 2, 6), util.grid().at(4, 2, 6), util.grid().at(5, 2, 6),
                util.grid().at(6, 2, 5), util.grid().at(6, 2, 4), util.grid().at(6, 2, 3)
        );
        for (BlockPos brick : bricks0) {
            builder.world().showSection(util.select().position(brick), Direction.DOWN);
            builder.idle(1);
        }
        builder.idle(20);

        builder.overlay().showText(35)
                .colored(PonderPalette.GREEN)
                .text("The second layer should be closed")
                .pointAt(util.vector().topOf(util.grid().at(4, 2, 6)));
        builder.idle(45);

        builder.overlay().showOutline(PonderPalette.GREEN, second, second, 40);
        builder.overlay().showText(45)
                .colored(PonderPalette.GREEN)
                .text("Forming a open-top hollow cuboid")
                .pointAt(util.vector().topOf(bottomCenter.above()))
                .attachKeyFrame();
        builder.idle(60);

        TinkersPonder.rotateAround(builder, 60, 90);

        builder.idle(30);

        BlockPos basin = util.grid().at(2, 1, 2);
        BlockPos table = util.grid().at(3, 1, 1);

        builder.world().showSection(util.select().position(basin.above()), Direction.EAST);
        builder.world().showSection(util.select().position(table.above()), Direction.SOUTH);
        builder.idle(10);
        builder.world().showSection(util.select().position(basin), Direction.DOWN);
        builder.world().showSection(util.select().position(table), Direction.DOWN);
        builder.idle(40);

        builder.overlay().showText(100)
                .colored(PonderPalette.GREEN)
                .text("The outer wall can be extended upwards by up to 63 blocks")
                .attachKeyFrame();

        builder.idle(10);

        SceneHelper.showWallLayers(builder, util,
                util.grid().at(2, 0, 2), util.grid().at(6, 0, 6),
                3, 9, Direction.DOWN);

        builder.idle(30);
        builder.overlay().showText(30)
                .colored(PonderPalette.MEDIUM)
                .text("Don’t forget to add fuel such as Lava")
                .pointAt(util.vector().blockSurface(controllerPos.east(), Direction.NORTH))
                .attachKeyFrame();

        builder.idle(45);
        builder.overlay().showControls(util.vector().blockSurface(controllerPos.east(), Direction.NORTH), Pointing.RIGHT,
                PonderConstants.CONTROL_TICKS).rightClick()
                .withItem(new ItemStack(Items.LAVA_BUCKET));
        SceneHelper.fillFluid(builder, controllerPos.east(), TankBlockEntity.class,
                Fluids.LAVA, PonderConstants.TANK_CAPACITY);

        builder.idle(60);
        builder.markAsFinished();
    }

    public static void using(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("smeltery_using", "Use the Smeltery");

        TinkersPonder.init9x9(builder, util);

        BlockPos controllerPos = util.grid().at(4, 2, 2);
        BlockPos tankPos = util.grid().at(3, 2, 2);

        BlockPos chutePos = util.grid().at(2, 2, 3);
        BlockPos ductPos = util.grid().at(2, 2, 4);
        BlockPos drainPos = util.grid().at(2, 2, 5);

        Selection smeltery = util.select().fromTo(2, 1, 2, 6, 4, 6);
        Selection chute = util.select().position(chutePos);
        Selection duct = util.select().position(ductPos);
        Selection drain = util.select().position(drainPos);

        builder.idle(5);
        builder.world().showSection(smeltery, Direction.NORTH);

        builder.idle(30);
        builder.overlay().showText(30)
                .text("Right Click to open the GUI of the Smeltery")
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().blockSurface(controllerPos, Direction.NORTH));
        builder.idle(45);
        builder.overlay().showText(30)
                .text("You can put items in the GUI to start melting")
                .colored(PonderPalette.MEDIUM)
                .pointAt(util.vector().blockSurface(controllerPos, Direction.NORTH))
                .attachKeyFrame();
        builder.idle(45);
        builder.overlay().showText(45)
                .text("And next, some functional blocks are introduced")
                .colored(PonderPalette.MEDIUM);
        builder.idle(15);

        TinkersPonder.rotate(builder, 15, -90);

        builder.idle(30);
        builder.overlay().showOutline(PonderPalette.GREEN, chute, chute, 30);
        builder.idle(5);
        builder.overlay().showText(45)
                .text("The Chute can input and output items to and from the Smeltery")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(chutePos, Direction.WEST))
                .attachKeyFrame();
        builder.idle(60);

        builder.idle(20);
        builder.world().showSection(util.select().position(chutePos.west()), Direction.NORTH);
        builder.idle(15);
        ElementLink<EntityElement> itemLink = builder.world().createItemEntity(
                chutePos.west().above().getCenter(),
                util.vector().of(0, 0.1, 0),
                new ItemStack(Items.RAW_GOLD)
        );
        builder.idle(10);
        builder.world().modifyEntity(itemLink, Entity::discard);

        builder.idle(30);
        builder.overlay().showOutline(PonderPalette.GREEN, drain, drain.add(duct), 30);
        builder.idle(5);
        builder.overlay().showText(45)
                .text("The Drain and Duct can input and output fluids to and from the Smeltery")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(ductPos, Direction.WEST))
                .attachKeyFrame();

        builder.idle(80);
        builder.overlay().showOutline(PonderPalette.GREEN, duct, duct, 30);
        builder.idle(5);
        builder.overlay().showText(45)
                .text("The Duct has a GUI with a slot that can filter fluids")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(ductPos, Direction.WEST))
                .attachKeyFrame();
        builder.idle(60);
        builder.overlay().showText(45)
                .text("Try put a Can with Molten Gold")
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(ductPos, Direction.WEST));
        builder.idle(60);
        ItemStack can = new ItemStack(TinkerSmeltery.copperCan);
        CopperCanItem.setFluid(can, new FluidStack(GTMaterials.Gold.getFluid(), 90));
        builder.overlay().showControls(util.vector().blockSurface(ductPos, Direction.WEST), Pointing.RIGHT, 20)
                .withItem(can.copy());
        builder.world().modifyBlockEntityNBT(util.select().position(ductPos), DuctBlockEntity.class, tag -> {
            tag.put("item", can.save(new CompoundTag()));
        }, true);

        builder.idle(35);
        builder.world().showSection(util.select().position(ductPos.west()), Direction.NORTH);
        builder.world().showSection(util.select().position(ductPos.west().below()), Direction.NORTH);
        builder.world().showSection(util.select().position(drainPos.west()), Direction.NORTH);
        builder.world().showSection(util.select().position(drainPos.west().below()), Direction.NORTH);

        builder.idle(35);

        builder.addLazyKeyframe();
        builder.overlay().showControls(util.vector().blockSurface(drainPos.west(), Direction.SOUTH), Pointing.LEFT, 20).rightClick();
        builder.overlay().showControls(util.vector().blockSurface(ductPos.west(), Direction.NORTH), Pointing.RIGHT, 20).rightClick();

        builder.idle(5);

        for (int i = 0; i < 4; i++) {
            builder.world().modifyBlockEntity(drainPos.west(), FaucetBlockEntity.class, f -> {
                f.onActivationPacket(new FluidStack(GTMaterials.Iron.getFluid(), 30), true);
            });
            builder.world().modifyBlockEntity(ductPos.west(), FaucetBlockEntity.class, f -> {
                f.onActivationPacket(new FluidStack(GTMaterials.Gold.getFluid(), 30), true);
            });
            builder.world().modifyBlockEntity(drainPos.west().below(), CastingBlockEntity.Table.class, t -> {
                t.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                    handler.fill(new FluidStack(GTMaterials.Iron.getFluid(), 30), IFluidHandler.FluidAction.EXECUTE);
                });
            });
            builder.world().modifyBlockEntity(ductPos.west().below(), CastingBlockEntity.Table.class, t -> {
                t.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                    handler.fill(new FluidStack(GTMaterials.Gold.getFluid(), 30), IFluidHandler.FluidAction.EXECUTE);
                });
            });
            builder.idle(5);
        }
        builder.idle(10);
        builder.world().modifyBlockEntity(drainPos.west(), FaucetBlockEntity.class, f -> {
            f.onActivationPacket(FluidStack.EMPTY, false);
        });
        builder.world().modifyBlockEntity(ductPos.west(), FaucetBlockEntity.class, f -> {
            f.onActivationPacket(FluidStack.EMPTY, false);
        });
        builder.idle(40);
        builder.world().modifyBlockEntity(drainPos.west().below(), CastingBlockEntity.Table.class, t -> {
            t.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                handler.insertItem(1, new ItemStack(Items.IRON_BARS), false);
            });
        });
        builder.world().modifyBlockEntity(ductPos.west().below(), CastingBlockEntity.Table.class, t -> {
            t.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                handler.insertItem(1, new ItemStack(TinkerCommons.goldBars), false);
            });
        });

        builder.idle(60);
        builder.markAsFinished();
    }

    public static void mini(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("smeltery_mini", "Mini Smeltery");

        TinkersPonder.init7x7(builder, util);

        builder.idle(5);
        builder.world().showSection(util.select().layers(1, 2), Direction.UP);

        builder.idle(25);

        TinkersPonder.rotateAround(builder, 100, 90);

        builder.idle(60);
        builder.markAsFinished();
    }
}
