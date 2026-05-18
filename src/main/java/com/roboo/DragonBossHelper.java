package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class DragonBossHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    private static final double SPAWN_STRAFE_Z       = -13.5;
    private static final double STRAFE_Z_TOLERANCE   = 0.175;

    private static final double ATTACK_X             = -106.0;

    private static final double SAFE_X               = -102.5;

    private static final double SUMMON_X             = -119.5;
    private static final double SUMMON_Y             =  106.0;
    private static final double SUMMON_Z             =  -50.9;
    private static final double SUMMON_TOLERANCE     =    1.5;

    private static final double DEATH_SUMMON_X       =  -29.0;
    private static final double DEATH_SUMMON_Y       =  107.0;
    private static final double DEATH_SUMMON_Z       =  -57.0;
    private static final double DEATH_SUMMON_TOLERANCE = 1.5;

    private static final float BOSS_YAW   = 90f;
    private static final float BOSS_PITCH =  0f;

    private static final double ARENA_MIN_X = -112.0;
    private static final double ARENA_MAX_X =  -87.0;
    private static final double ARENA_MIN_Y =   85.0;
    private static final double ARENA_MAX_Y =  100.0;
    private static final double ARENA_MIN_Z =  -20.0;
    private static final double ARENA_MAX_Z =   -5.0;

    private static final double TELEPORT_X         = -90.656;
    private static final double TELEPORT_Y         =  96.0;
    private static final double TELEPORT_Z         = -14.351;
    private static final double TELEPORT_TOLERANCE =   0.2;

    private enum Phase {
        IDLE,
        ROTATE_TO_BOSS,
        STRAFE_LEFT,
        MOVE_FORWARD
    }

    private static Phase phase         = Phase.IDLE;
    private static boolean holdClick   = false;
    private static boolean moveToSafe  = false;
    private static boolean dodgeQueued = false;
    private static boolean parryQueued = false;
    private static boolean dodgeTriggered      = false;
    private static boolean waitingToStopDodge  = false;
    private static boolean comboActive         = false;

    private static long entryTime  = 0;
    private static long safeTime   = 0;

    private static boolean atSummonPos        = false;
    private static long    summonPosEntryTime = 0;
    private static int     summonAttemptCount = 0;
    private static boolean summonFailed       = false;

    private static boolean wasInArena = false;
    private static boolean wasHolding = false;
    private static boolean optionsApplied = false;


    public static void init() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> handleChat(msg.getString()));
        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, ts) -> handleChat(msg.getString()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick(client));
    }

    private static void onTick(Minecraft client) {
        if (!ModConfig.isAutoDragonBossEnabled() || !AutoWither.isEnabled()) return;
        if (mc.player == null || mc.level == null) return;

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        boolean inArena = px >= ARENA_MIN_X && px <= ARENA_MAX_X
                && py >= ARENA_MIN_Y && py <= ARENA_MAX_Y
                && pz >= ARENA_MIN_Z && pz <= ARENA_MAX_Z;

        if (inArena && !optionsApplied) {
            OptionsHelper.enableAutoJump();
            OptionsHelper.disableToggleSneak();
            optionsApplied = true;
        } else if (!inArena && optionsApplied) {
            OptionsHelper.restoreAutoJump();
            OptionsHelper.restoreToggleSneak();
            optionsApplied = false;
        }

        if (phase != Phase.IDLE) {
            if (!inArena && wasInArena) {
                wasInArena = false;
                fullReset();
                return;
            }
            wasInArena = inArena;
        }

        long now = System.currentTimeMillis();

        if (phase == Phase.IDLE) {
            boolean nearTeleport =
                    Math.abs(px - TELEPORT_X) <= TELEPORT_TOLERANCE &&
                            Math.abs(py - TELEPORT_Y) <= TELEPORT_TOLERANCE &&
                            Math.abs(pz - TELEPORT_Z) <= TELEPORT_TOLERANCE;

            if (nearTeleport) {
                InventoryHelper.restoreSlotAfterSummon();
                phase     = Phase.ROTATE_TO_BOSS;
                entryTime = System.currentTimeMillis();
                resetCombatState();
            }
        }

        switch (phase) {
            case ROTATE_TO_BOSS -> {
                boolean rotDone = RotationHelper.lookAt(BOSS_YAW, BOSS_PITCH);
                if (rotDone || now - entryTime >= 1500) {
                    phase = Phase.STRAFE_LEFT;
                }
            }
            case STRAFE_LEFT -> {
                if (MovementHelper.moveToZ(SPAWN_STRAFE_Z, STRAFE_Z_TOLERANCE)) {
                    phase = Phase.MOVE_FORWARD;
                }
            }
            case MOVE_FORWARD -> {
                if (MovementHelper.moveToX(ATTACK_X)) {
                    phase     = Phase.IDLE;
                    holdClick = true;
                }
            }
            default -> {}
        }

        if (moveToSafe) {
            if (MovementHelper.moveToX(SAFE_X)) {
                moveToSafe = false;
                if (dodgeQueued && !dodgeTriggered && ModConfig.isAutoDodgeEnabled()) {
                    DodgeHelper.setRequiredSafes(parryQueued);
                    DodgeHelper.start();
                    if (parryQueued) ParryHelper.trigger();
                    dodgeTriggered = true;
                    dodgeQueued    = false;
                    parryQueued    = false;
                }
            }
        }

        if (holdClick && !wasHolding) {
            InputHelper.holdRightClick(true);
            wasHolding = true;
        } else if (!holdClick && wasHolding) {
            InputHelper.holdRightClick(false);
            wasHolding = false;
        }

        if (waitingToStopDodge && now - safeTime > 100) {
            DodgeHelper.stop();
            dodgeTriggered     = false;
            waitingToStopDodge = false;
        }

        if (comboActive && ModConfig.isAutoDodgeEnabled()) {
            holdClick = false;
            if (!dodgeTriggered) {
                DodgeHelper.setRequiredSafes(true);
                DodgeHelper.start();
                ParryHelper.trigger();
                dodgeTriggered = true;
            }
        }

        handleSummonPosition(client);
    }

    private static void handleSummonPosition(Minecraft client) {
        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        boolean nearSummonPos =
                (Math.abs(px - SUMMON_X) <= SUMMON_TOLERANCE &&
                        Math.abs(py - SUMMON_Y) <= SUMMON_TOLERANCE &&
                        Math.abs(pz - SUMMON_Z) <= SUMMON_TOLERANCE)
                        ||
                        (Math.abs(px - DEATH_SUMMON_X) <= DEATH_SUMMON_TOLERANCE &&
                                Math.abs(py - DEATH_SUMMON_Y) <= DEATH_SUMMON_TOLERANCE &&
                                Math.abs(pz - DEATH_SUMMON_Z) <= DEATH_SUMMON_TOLERANCE);

        if (nearSummonPos) {
            if (!atSummonPos) {
                atSummonPos        = true;
                summonPosEntryTime = System.currentTimeMillis();
                summonAttemptCount = 0;
                summonFailed       = false;
            } else if (!summonFailed) {
                long elapsed = System.currentTimeMillis() - summonPosEntryTime;
                if (summonAttemptCount == 0 && elapsed >= 1000) {
                    summonAttemptCount = 1;
                    boolean used = InventoryHelper.useDragonKey();
                    if (!used) {
                        client.gui.getChat().addMessage(Component.literal(
                                "§e[DragonBoss] §cNo Dragon Key found in hotbar!"));
                    }
                } else if (summonAttemptCount == 1 && elapsed >= 3000) {
                    summonAttemptCount = 2;
                    InventoryHelper.useDragonKey();
                } else if (summonAttemptCount == 2 && elapsed >= 6000) {
                    summonAttemptCount = 3;
                    InventoryHelper.useDragonKey();
                } else if (summonAttemptCount == 3 && elapsed >= 11000) {
                    summonFailed = true;
                    client.gui.getChat().addMessage(Component.literal(
                            "§e[DragonBoss] §cSummon failed — still at summon position after 15s!"));
                }
            }
        } else {
            atSummonPos        = false;
            summonAttemptCount = 0;
            summonFailed       = false;
        }
    }

    public static void handleChat(String msg) {
        if (!ModConfig.isAutoDragonBossEnabled() || !AutoWither.isEnabled()) return;
        if (msg == null) return;

        String lower = msg.toLowerCase();

        if (lower.contains("dark slash") && ModConfig.isComboAttackEnabled()) {
            comboActive    = true;
            dodgeTriggered = false;
        }

        else if (lower.contains("void storm")) {
            moveToSafe     = true;
            holdClick      = false;
            dodgeQueued    = true;
            parryQueued    = false;
            dodgeTriggered = false;
        }

        else if (lower.contains("heroic strike")) {
            ParryHelper.trigger();
        }

        else if (lower.contains("death bless")) {
            moveToSafe     = true;
            holdClick      = false;
            dodgeQueued    = true;
            parryQueued    = true;
            dodgeTriggered = false;
        }

        else if (lower.contains("safe!!!")) {
            holdClick          = true;
            moveToSafe         = false;
            comboActive        = false;
            dodgeQueued        = false;
            parryQueued        = false;
            safeTime           = System.currentTimeMillis();
            waitingToStopDodge = true;
            phase = Phase.MOVE_FORWARD;
        }

        else if (lower.contains("you died")) {
            InputHelper.stopAll();
            DodgeHelper.stop();
            ParryHelper.reset();
            fullReset();
        }
    }

    private static void resetCombatState() {
        moveToSafe         = false;
        holdClick          = false;
        wasHolding         = false;
        comboActive        = false;
        dodgeQueued        = false;
        parryQueued        = false;
        dodgeTriggered     = false;
        waitingToStopDodge = false;
        MovementHelper.stopMovement();
    }

    public static void fullReset() {
        phase              = Phase.IDLE;
        wasInArena         = false;
        resetCombatState();
        atSummonPos        = false;
        summonAttemptCount = 0;
        summonFailed       = false;

        if (optionsApplied) {
            OptionsHelper.restoreAutoJump();
            OptionsHelper.restoreToggleSneak();
            optionsApplied = false;
        }

        InputHelper.stopAll();
    }
}