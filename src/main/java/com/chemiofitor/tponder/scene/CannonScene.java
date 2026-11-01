package com.chemiofitor.tponder.scene;

import com.chemiofitor.tponder.TinkersPonder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.smeltery.block.entity.FluidCannonBlockEntity;

public final class CannonScene {
    private CannonScene() {}

    public static void using(@NotNull SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        builder.title("fluid_cannon_using", "Using Fluid Cannons");

        TinkersPonder.init5x5(builder, util);

        BlockPos copper = util.grid().at(1, 1, 2);
        BlockPos cobalt = util.grid().at(3, 1, 2);

        builder.idle(PonderConstants.TICK_IDLE);
        builder.world().showSection(util.select().fromTo(cobalt, cobalt.above()), Direction.NORTH);
        builder.world().showSection(util.select().fromTo(copper, copper.above()), Direction.NORTH);

        builder.idle(PonderConstants.TICK_LONG);

        builder.world().modifyBlockEntity(copper, FluidCannonBlockEntity.class, t -> {
            t.updateFluidTo(new FluidStack(Fluids.LAVA, 2000));
        });

        builder.world().modifyBlockEntity(cobalt, FluidCannonBlockEntity.class, t -> {
            t.updateFluidTo(new FluidStack(Fluids.WATER, 2000));
        });

        builder.idle(PonderConstants.TICK_MEDIUM);
        builder.overlay().showText(PonderConstants.TEXT_LONG)
                .text("Fluid Cannon can shoot fluid and different fluids have different effects")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame();
        builder.idle(60);
        builder.overlay().showText(PonderConstants.TEXT_MEDIUM)
                .text("Check the Tinker's Guide for specific fluid effects")
                .colored(PonderPalette.BLUE)
                .attachKeyFrame();
        builder.idle(PonderConstants.TICK_IDLE);

        // Power the levers
        builder.world().modifyBlock(copper.above(), s -> s.setValue(LeverBlock.POWERED, true), false);
        builder.world().modifyBlock(cobalt.above(), s -> s.setValue(LeverBlock.POWERED, true), false);
        builder.idle(10);

        // Show redstone signal
        builder.effects().indicateRedstone(copper.above());
        builder.effects().indicateRedstone(cobalt.above());
        builder.idle(15);

        // --- Lava cannon fires ---
        shootCannon(builder, copper, Fluids.LAVA, ParticleTypes.LAVA, ParticleTypes.FLAME);

        // --- Water cannon fires ---
        shootCannon(builder, cobalt, Fluids.WATER, ParticleTypes.SPLASH, ParticleTypes.BUBBLE);

        builder.idle(50);
        builder.markAsFinished();
    }

    /**
     * Simulate a fluid cannon shooting using the PonderLevel's particle system.
     * <p>
     * We cannot call {@code FluidCannonBlockEntity.shoot()} directly because it requires
     * {@code ServerLevel}, and Ponder's {@code PonderLevel} extends {@code SchematicLevel}
     * (which is client-side only). Instead we replicate the visual effects:
     * drain fluid and spawn particles in the cannon's facing direction.
     */
    private static void shootCannon(@NotNull SceneBuilder builder, @NotNull BlockPos pos,
                                    @NotNull net.minecraft.world.level.material.Fluid fluid,
                                    @NotNull net.minecraft.core.particles.ParticleOptions primaryParticle,
                                    @NotNull net.minecraft.core.particles.ParticleOptions secondaryParticle) {
        builder.world().modifyBlockEntity(pos, FluidCannonBlockEntity.class, cannon -> {
            // Drain fluid (same as shoot() would)
            cannon.updateFluidTo(new FluidStack(fluid, 1500));

            Level level = cannon.getLevel();
            if (level == null) return;

            // Read the facing direction from the cannon's block state
            Direction facing = cannon.getBlockState().getValue(net.minecraft.world.level.block.DirectionalBlock.FACING);
            int dx = facing.getStepX();
            int dy = facing.getStepY();
            int dz = facing.getStepZ();

            RandomSource r = level.getRandom();
            double originX = pos.getX() + 0.5 + dx * 0.6;
            double originY = pos.getY() + 0.5 + dy * 0.6;
            double originZ = pos.getZ() + 0.5 + dz * 0.6;

            for (int i = 0; i < 15; i++) {
                double spreadX = (r.nextDouble() - 0.5) * 0.3;
                double spreadY = (r.nextDouble() - 0.5) * 0.3;
                double spreadZ = (r.nextDouble() - 0.5) * 0.3;
                // Perpendicular offsets shrink along facing axis to form a cone
                if (dx != 0) { spreadX = 0; }
                if (dy != 0) { spreadY = 0; }
                if (dz != 0) { spreadZ = 0; }

                level.addParticle(primaryParticle,
                        originX + spreadX, originY + spreadY, originZ + spreadZ,
                        dx * 0.3 + (r.nextDouble() - 0.5) * 0.1,
                        dy * 0.3 + (r.nextDouble() - 0.5) * 0.1,
                        dz * 0.3 + (r.nextDouble() - 0.5) * 0.1);
            }
            for (int i = 0; i < 8; i++) {
                level.addParticle(secondaryParticle,
                        originX + (r.nextDouble() - 0.5) * 0.3,
                        originY + (r.nextDouble() - 0.5) * 0.3,
                        originZ + (r.nextDouble() - 0.5) * 0.3,
                        dx * 0.15 + (r.nextDouble() - 0.5) * 0.05,
                        dy * 0.15 + (r.nextDouble() - 0.5) * 0.05,
                        dz * 0.15 + (r.nextDouble() - 0.5) * 0.05);
            }
        });
    }
}
