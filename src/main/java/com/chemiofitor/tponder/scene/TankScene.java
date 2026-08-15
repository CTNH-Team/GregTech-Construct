package com.chemiofitor.tponder.scene;

import com.chemiofitor.tponder.TinkersPonder;
import com.chemiofitor.tponder.scene.PonderConstants;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.entity.CastingTankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.ProxyTankBlockEntity;
import org.jetbrains.annotations.NotNull;

public final class TankScene {
    private TankScene() {}

    public static void tank(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("tank", "Use Fluid Containers");

        TinkersPonder.init5x5(builder, util);

        BlockPos center = util.grid().at(2, 2, 2);

        BlockPos gauge = util.grid().at(2, 1, 1);
        BlockPos tank = util.grid().at(2, 1, 3);
        BlockPos proxy = util.grid().at(1, 1, 2);
        BlockPos cast = util.grid().at(3, 1, 2);

        builder.idle(10);
        builder.world().showSection(util.select().layer(1), Direction.NORTH);
        builder.idle(15);

        builder.overlay().showText(45)
                .colored(PonderPalette.MEDIUM)
                .text("Tinker's fluid containers roughly divided into 4 categories");
        builder.idle(55);

        builder.overlay().showOutline(PonderPalette.GREEN, gauge, util.select().position(gauge), 50);
        builder.overlay().showText(55)
                .colored(PonderPalette.GREEN)
                .text("Gauge: holds 48 ingots of fluid, retains liquid when broken")
                .pointAt(util.vector().topOf(gauge))
                .attachKeyFrame();
        builder.idle(60);

        TinkersPonder.rotate(builder, 35, 180);

        builder.overlay().showOutline(PonderPalette.GREEN, tank, util.select().position(tank), 50);
        builder.overlay().showText(55)
                .colored(PonderPalette.GREEN)
                .text("Tank: holds 48 ingots of fluid, retains liquid when broken")
                .pointAt(util.vector().topOf(tank))
                .attachKeyFrame();
        builder.idle(60);

        TinkersPonder.rotate(builder, 35, -90);

        builder.overlay().showOutline(PonderPalette.GREEN, cast, util.select().position(cast), 50);
        builder.overlay().showText(55)
                .colored(PonderPalette.GREEN)
                .text("Casting Tank: interacts with items, retains liquid when broken")
                .pointAt(util.vector().topOf(cast))
                .attachKeyFrame();
        builder.idle(60);

        builder.idle(15);
        builder.world().modifyBlockEntity(cast, CastingTankBlockEntity.class, t -> {
            t.updateFluidTo(new FluidStack(GTMaterials.Iron.getFluid(), 4000));
        });

        builder.idle(30);
        builder.overlay().showControls(util.vector().topOf(cast), Pointing.DOWN, 20)
                .withItem(new ItemStack(TinkerSmeltery.searedTank.get(SearedTankBlock.TankType.INGOT_TANK)))
                .rightClick();

        builder.world().modifyBlockEntity(cast, CastingTankBlockEntity.class, t -> {
            t.updateFluidTo(FluidStack.EMPTY);
            ItemStack stack = new ItemStack(TinkerSmeltery.searedTank.get(SearedTankBlock.TankType.INGOT_TANK));
            CompoundTag fluid =  new CompoundTag();
            fluid.putString("FluidName", ForgeRegistries.FLUIDS.getKey(GTMaterials.Iron.getFluid()).toString());
            fluid.putInt("Amount", 4000);
            stack.getOrCreateTag().put("tank", fluid);
            t.setItem(1, stack);
        });

        builder.idle(30);

        TinkersPonder.rotate(builder, 35, 180);

        builder.overlay().showOutline(PonderPalette.GREEN, proxy, util.select().position(proxy), 50);
        builder.overlay().showText(55)
                .colored(PonderPalette.GREEN)
                .text("Proxy Tank: interacts with items, agents item inside")
                .pointAt(util.vector().topOf(proxy))
                .attachKeyFrame();
        builder.idle(60);

        builder.idle(15);
        builder.overlay().showControls(util.vector().topOf(proxy), Pointing.DOWN, 20)
                .withItem(new ItemStack(TinkerSmeltery.searedTank.get(SearedTankBlock.TankType.INGOT_TANK)))
                .rightClick();

        builder.world().modifyBlockEntity(proxy, ProxyTankBlockEntity.class, t -> {
            ItemStack stack = new ItemStack(TinkerSmeltery.searedTank.get(SearedTankBlock.TankType.INGOT_TANK));
            CompoundTag fluid =  new CompoundTag();
            fluid.putString("FluidName", ForgeRegistries.FLUIDS.getKey(GTMaterials.Iron.getFluid()).toString());
            fluid.putInt("Amount", 4000);
            stack.getOrCreateTag().put("tank", fluid);
            t.getItemTank().setStack(stack);
        });

        builder.idle(60);
        builder.markAsFinished();
    }
}
