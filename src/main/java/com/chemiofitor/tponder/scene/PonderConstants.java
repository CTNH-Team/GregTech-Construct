package com.chemiofitor.tponder.scene;

/**
 * Centralized constants for Tinkers' Ponder scene building.
 * Replaces magic numbers scattered across scene classes.
 */
public final class PonderConstants {

    private PonderConstants() {
        throw new UnsupportedOperationException("Utility class - do not instantiate");
    }

    // --- Fluid amounts (mB, matching Tinkers' Construct conventions) ---
    /** 1 ingot = 90 mB in TCon */
    public static final int INGOT_AMOUNT = 90;
    /** 1 nugget = 10 mB (1/9 ingot) */
    public static final int NUGGET_AMOUNT = 10;
    /** 1 block = 810 mB (9 ingots) */
    public static final int BLOCK_AMOUNT = 810;
    /** 1 faucet pulse = 30 mB (1/3 ingot) */
    public static final int FAUCET_PULSE = 30;
    /** Standard tank / melter capacity = 4000 mB */
    public static final int TANK_CAPACITY = 4000;

    // --- Common tick durations ---
    /** Minimal pause between sequential operations */
    public static final int TICK_SHORT = 5;
    /** Brief pause, used after keyframe transitions */
    public static final int TICK_IDLE = 15;
    /** Moderate pause for reading context text */
    public static final int TICK_MEDIUM = 20;
    /** Longer pause before attaching a keyframe */
    public static final int TICK_LONG = 30;
    /** Extended pause for comprehensive text */
    public static final int TICK_EXTENDED = 60;

    // --- Text display durations (ticks) ---
    public static final int TEXT_SHORT = 25;
    public static final int TEXT_MEDIUM = 35;
    public static final int TEXT_LONG = 55;
    public static final int TEXT_EXTENDED = 100;

    // --- Scene scale factors ---
    public static final float SCALE_5x5 = 0.9f;
    public static final float SCALE_7x7 = 0.75f;
    public static final float SCALE_9x9 = 0.6f;

    // --- Outline display durations ---
    public static final int OUTLINE_SHORT = 30;
    public static final int OUTLINE_MEDIUM = 50;
    public static final int OUTLINE_LONG = 130;

    // --- Control display durations ---
    public static final int CONTROL_TICKS = 20;

    // --- Faucet / casting pulse count ---
    /** Standard number of faucet pulses for a full cast (4 × 30 = 120 mB, slightly over 1 ingot at 90 mB) */
    public static final int FAUCET_PULSES_FULL = 4;
}
