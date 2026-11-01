package com.chemiofitor.tponder.scene;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.smeltery.block.entity.CastingBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized utility methods for common scene-building patterns.
 * All methods are static; this class cannot be instantiated.
 */
public final class SceneHelper {

    private SceneHelper() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    // --- Fluid manipulation ---

    /**
     * Fill fluid into a block entity via its FLUID_HANDLER capability.
     */
    public static <T extends BlockEntity> void fillFluid(@NotNull SceneBuilder builder, @NotNull BlockPos pos,
                                                          @NotNull Class<T> blockEntityClass, @NotNull Fluid fluid, int amount) {
        builder.world().modifyBlockEntity(pos, blockEntityClass, be -> {
            be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                handler.fill(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
            });
        });
    }

    /**
     * Drain a specific amount of fluid (any fluid) from a block entity via its FLUID_HANDLER capability.
     */
    public static <T extends BlockEntity> void drainFluid(@NotNull SceneBuilder builder, @NotNull BlockPos pos,
                                                           @NotNull Class<T> blockEntityClass, int amount) {
        builder.world().modifyBlockEntity(pos, blockEntityClass, be -> {
            be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                handler.drain(amount, IFluidHandler.FluidAction.EXECUTE);
            });
        });
    }

    /**
     * Drain a specific type and amount of fluid from a block entity via its FLUID_HANDLER capability.
     */
    public static <T extends BlockEntity> void drainFluid(@NotNull SceneBuilder builder, @NotNull BlockPos pos,
                                                           @NotNull Class<T> blockEntityClass,
                                                           @NotNull Fluid fluid, int amount) {
        builder.world().modifyBlockEntity(pos, blockEntityClass, be -> {
            be.getCapability(ForgeCapabilities.FLUID_HANDLER).ifPresent(handler -> {
                handler.drain(new FluidStack(fluid, amount), IFluidHandler.FluidAction.EXECUTE);
            });
        });
    }

    /**
     * Fill fluid into a casting table.
     */
    public static void fillCastingTable(@NotNull SceneBuilder builder, @NotNull BlockPos pos,
                                        @NotNull Fluid fluid, int amount) {
        fillFluid(builder, pos, CastingBlockEntity.Table.class, fluid, amount);
    }

    /**
     * Fill fluid into a casting basin.
     */
    public static void fillCastingBasin(@NotNull SceneBuilder builder, @NotNull BlockPos pos,
                                        @NotNull Fluid fluid, int amount) {
        fillFluid(builder, pos, CastingBlockEntity.Basin.class, fluid, amount);
    }

    // --- Faucet operations ---

    /**
     * Activate a faucet to start pouring the given fluid.
     */
    public static void activateFaucet(@NotNull SceneBuilder builder, @NotNull BlockPos pos,
                                      @NotNull Fluid fluid, int amount) {
        builder.world().modifyBlockEntity(pos, FaucetBlockEntity.class, f ->
                f.onActivationPacket(new FluidStack(fluid, amount), true));
    }

    /**
     * Deactivate a faucet to stop pouring.
     */
    public static void deactivateFaucet(@NotNull SceneBuilder builder, @NotNull BlockPos pos) {
        builder.world().modifyBlockEntity(pos, FaucetBlockEntity.class, f ->
                f.onActivationPacket(FluidStack.EMPTY, false));
    }

    // --- Casting simulation loop ---

    /**
     * Simulate a full faucet casting cycle: activate faucet, pulse N times
     * (filling the table/basin and draining from source each pulse), then deactivate.
     *
     * @param builder      the scene builder
     * @param faucetPos    position of the faucet
     * @param castingPos   position of the casting table or basin
     * @param sourcePos    position of the fluid source (melter/tank)
     * @param fluid        the fluid being cast
     * @param pulseAmount  amount per faucet pulse (typically {@link PonderConstants#FAUCET_PULSE})
     * @param pulses       number of pulses
     * @param tickPerPulse ticks between each pulse
     * @param sourceClass  block entity class of the fluid source (e.g. MelterBlockEntity)
     */
    public static <T extends BlockEntity> void simulateCasting(@NotNull SceneBuilder builder,
                                           @NotNull BlockPos faucetPos,
                                           @NotNull BlockPos castingPos,
                                           @NotNull BlockPos sourcePos,
                                           @NotNull Fluid fluid,
                                           int pulseAmount,
                                           int pulses,
                                           int tickPerPulse,
                                           @NotNull Class<T> sourceClass) {
        for (int i = 0; i < pulses; i++) {
            activateFaucet(builder, faucetPos, fluid, pulseAmount);
            drainFluid(builder, sourcePos, sourceClass, fluid, pulseAmount);
            fillCastingTable(builder, castingPos, fluid, pulseAmount);
            builder.idle(tickPerPulse);
        }
        deactivateFaucet(builder, faucetPos);
    }

    // --- Wall / structure generation ---

    /**
     * Generate a single layer of a rectangular wall perimeter.
     *
     * @param corner1        one corner of the wall (will be normalized to min/max)
     * @param corner2        opposite corner of the wall
     * @param y              the Y-level for this layer
     * @param includeCorners if true, includes the four corner blocks (e.g. Foundry);
     *                       if false, excludes them (e.g. Smeltery)
     * @return list of block positions forming the perimeter
     */
    @NotNull
    public static List<BlockPos> generatePerimeterWallLayer(@NotNull BlockPos corner1,
                                                            @NotNull BlockPos corner2,
                                                            int y, boolean includeCorners) {
        int minX = Math.min(corner1.getX(), corner2.getX());
        int maxX = Math.max(corner1.getX(), corner2.getX());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());

        List<BlockPos> positions = new ArrayList<>();

        // North and South walls (along X axis).
        // When including corners, spans full X range so corners are covered here.
        // When excluding corners, shrinks by 1 on each end.
        int nsStart = minX + (includeCorners ? 0 : 1);
        int nsEnd   = maxX - (includeCorners ? 0 : 1);

        for (int x = nsStart; x <= nsEnd; x++) {
            positions.add(new BlockPos(x, y, minZ));
            positions.add(new BlockPos(x, y, maxZ));
        }

        // West and East walls (along Z axis).
        // Always skip corners — they are already covered by the N/S walls above
        // (when corners are included) or explicitly excluded (when they aren't).
        for (int z = minZ + 1; z <= maxZ - 1; z++) {
            positions.add(new BlockPos(minX, y, z));
            positions.add(new BlockPos(maxX, y, z));
        }

        return positions;
    }

    /**
     * Generate a single layer of a hollow rectangular wall (perimeter only, corners excluded).
     * Convenience method — use for Smeltery-style walls.
     *
     * @deprecated prefer {@link #generatePerimeterWallLayer(BlockPos, BlockPos, int, boolean)} for clarity
     */
    @NotNull
    @Deprecated
    public static List<BlockPos> generateHollowWallLayer(@NotNull BlockPos corner1,
                                                         @NotNull BlockPos corner2, int y) {
        return generatePerimeterWallLayer(corner1, corner2, y, false);
    }

    /**
     * Generate multiple hollow wall layers from startY (inclusive) to endY (inclusive).
     *
     * @param corner1 one corner of the wall
     * @param corner2 opposite corner of the wall
     * @param startY  first Y-level (inclusive)
     * @param endY    last Y-level (inclusive)
     * @return flat list of all block positions across all layers
     */
    @NotNull
    public static List<BlockPos> generateHollowWalls(@NotNull BlockPos corner1,
                                                     @NotNull BlockPos corner2,
                                                     int startY, int endY) {
        List<BlockPos> allPositions = new ArrayList<>();
        for (int y = startY; y <= endY; y++) {
            allPositions.addAll(generateHollowWallLayer(corner1, corner2, y));
        }
        return allPositions;
    }

    /**
     * Show wall layers block-by-block with a 1-tick delay between each.
     * Corners excluded — use for Smeltery walls.
     *
     * @param builder   the scene builder
     * @param util      the scene building utility
     * @param corner1   one corner of the wall area
     * @param corner2   opposite corner of the wall area
     * @param startY    first Y-level (inclusive)
     * @param endY      last Y-level (inclusive)
     * @param direction direction to show sections from
     */
    public static void showWallLayers(@NotNull SceneBuilder builder,
                                      @NotNull SceneBuildingUtil util,
                                      @NotNull BlockPos corner1,
                                      @NotNull BlockPos corner2,
                                      int startY, int endY,
                                      @NotNull Direction direction) {
        showWallLayers(builder, util, corner1, corner2, startY, endY, direction, false);
    }

    /**
     * Show wall layers block-by-block with a 1-tick delay between each.
     *
     * @param builder        the scene builder
     * @param util           the scene building utility
     * @param corner1        one corner of the wall area
     * @param corner2        opposite corner of the wall area
     * @param startY         first Y-level (inclusive)
     * @param endY           last Y-level (inclusive)
     * @param direction      direction to show sections from
     * @param includeCorners if true, includes corner blocks (Foundry);
     *                       if false, excludes them (Smeltery)
     */
    public static void showWallLayers(@NotNull SceneBuilder builder,
                                      @NotNull SceneBuildingUtil util,
                                      @NotNull BlockPos corner1,
                                      @NotNull BlockPos corner2,
                                      int startY, int endY,
                                      @NotNull Direction direction,
                                      boolean includeCorners) {
        for (int y = startY; y <= endY; y++) {
            List<BlockPos> layer = generatePerimeterWallLayer(corner1, corner2, y, includeCorners);
            for (BlockPos pos : layer) {
                builder.world().showSection(util.select().position(pos), direction);
                builder.idle(1);
            }
        }
    }
}
