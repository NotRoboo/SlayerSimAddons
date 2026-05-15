package com.roboo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class WitherBossHelper {

    private static final Minecraft mc = Minecraft.getInstance();

    
    // POSITIONS
    private static final double FORWARD_X = -112;
    private static final double SAFE_X    = -106;

    private static final double SUMMON_X = -109;
    private static final double SUMMON_Y =  102;
    private static final double SUMMON_Z =   42;

    // Shared death-respawn summon position (same as Dragon)
    private static final double DEATH_SUMMON_X = -29;
    private static final double DEATH_SUMMON_Y = 107;
    private static final double DEATH_SUMMON_Z = -57;

    private static final double SUMMON_TOLERANCE = 1.0;

    
    // STATE
    private static boolean rotateToBoss = false;
    private static boolean moveForward  = false;
    private static boolean moveToSafe   = false;
    private static boolean holdClick    = false;
    private static boolean wasHolding   = false;

    private static boolean comboAttack  = false;
    private static boolean witherMagic  = false;
    private static boolean demonMagic   = false;
    private static boolean dodgeTriggered       = false;
    private static boolean waitingToStopDodge   = false;

    private static long entryTime = 0;
    private static long safeTime  = 0;

    private static boolean atSummonPos      = false;
    private static long    summonPosEntryTime = 0;
    private static int     summonAttemptCount = 0;
    private static boolean summonFailed       = false;

    
    // INIT
    public static void init() {
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> handleChat(msg.getString()));
        ClientReceiveMessageEvents.CHAT.register((msg, signed, sender, params, ts) -> handleChat(msg.getString()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> onTick(client));
    }

    
    // TICK
    private static void onTick(Minecraft client) {
        if (!ModConfig.isAutoWitherBossEnabled() || !AutoWither.isEnabled()) return;
        if (mc.player == null || mc.level == null) return;
        if (rotateToBoss) {
            if (System.currentTimeMillis() - entryTime < 1500) {
                if (RotationHelper.lookAt(90f, 0f)) {
                    rotateToBoss = false;
                    moveForward  = true;
                }
            }
        }

        // Move to attack position
        if (moveForward) {
            if (MovementHelper.moveToX(FORWARD_X)) {
                moveForward = false;
                holdClick   = true;
            }
        }

        // Move to safe zone
        if (moveToSafe) {
            if (MovementHelper.moveToX(SAFE_X)) {
                moveToSafe = false;
                if (demonMagic && !dodgeTriggered && ModConfig.isAutoDodgeEnabled()) {
                    DodgeHelper.setRequiredSafes(true);
                    DodgeHelper.start();
                    ParryHelper.trigger();
                    dodgeTriggered = true;
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

        // Delayed dodge stop
        if (waitingToStopDodge && System.currentTimeMillis() - safeTime > 100) {
            DodgeHelper.stop();
            dodgeTriggered      = false;
            waitingToStopDodge  = false;
        }

        // Combo attack dodge
        if (comboAttack && ModConfig.isAutoDodgeEnabled()) {
            holdClick = false;
            if (!dodgeTriggered) {
                DodgeHelper.setRequiredSafes(true);
                DodgeHelper.start();
                ParryHelper.trigger();
                dodgeTriggered = true;
            }
        }

        // Auto summon position check
        handleSummonPosition(client);
    }

    
    // SUMMON POSITION LOGIC
    private static void handleSummonPosition(Minecraft client) {
        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        boolean nearSummonPos =
                (Math.abs(px - SUMMON_X) <= SUMMON_TOLERANCE &&
                        Math.abs(py - SUMMON_Y) <= SUMMON_TOLERANCE &&
                        Math.abs(pz - SUMMON_Z) <= SUMMON_TOLERANCE)
                        ||
                        (Math.abs(px - DEATH_SUMMON_X) <= SUMMON_TOLERANCE &&
                                Math.abs(py - DEATH_SUMMON_Y) <= SUMMON_TOLERANCE &&
                                Math.abs(pz - DEATH_SUMMON_Z) <= SUMMON_TOLERANCE);

        if (nearSummonPos) {
            if (!atSummonPos) {
                atSummonPos        = true;
                summonPosEntryTime = System.currentTimeMillis();
                summonAttemptCount = 1;
                summonFailed       = false;
                InventoryHelper.useSummonItem();
            } else if (!summonFailed) {
                long elapsed = System.currentTimeMillis() - summonPosEntryTime;
                if (summonAttemptCount == 1 && elapsed >= 1000) {
                    summonAttemptCount = 2;
                    InventoryHelper.useSummonItem();
                } else if (summonAttemptCount == 2 && elapsed >= 5000) {
                    summonAttemptCount = 3;
                    InventoryHelper.useSummonItem();
                } else if (summonAttemptCount == 3 && elapsed >= 10000) {
                    summonFailed = true;
                    client.gui.getChat().addMessage(Component.literal(
                            "§e[WitherBoss] §cSummon failed — still at summon position after 15s!"));
                }
            }
        } else {
            atSummonPos        = false;
            summonAttemptCount = 0;
            summonFailed       = false;
        }
    }

    
    // CHAT
    public static void handleChat(String msg) {
        if (!ModConfig.isAutoWitherBossEnabled() || !AutoWither.isEnabled()) return;
        if (msg == null) return;

        String lower = msg.toLowerCase();

        if (lower.contains("invited you to his palace")) {
            rotateToBoss = true;
            entryTime    = System.currentTimeMillis();
            resetCombatState();
        }
        else if (lower.contains("combo attack") && ModConfig.isComboAttackEnabled()) {
            comboAttack    = true;
            dodgeTriggered = false;
        }
        else if (lower.contains("wither magic")) {
            // Wither magic: move to safe, no dodge
            witherMagic    = true;
            demonMagic     = false;
            moveToSafe     = true;
            holdClick      = false;
            dodgeTriggered = false;
        }
        else if (lower.contains("demon magic")) {
            // Demon magic: move to safe then dodge
            demonMagic     = true;
            witherMagic    = false;
            moveToSafe     = true;
            holdClick      = false;
            dodgeTriggered = false;
        }
        else if (lower.contains("safe!!!")) {
            holdClick          = true;
            moveForward        = true;
            moveToSafe         = false;
            witherMagic        = false;
            demonMagic         = false;
            comboAttack        = false;
            safeTime           = System.currentTimeMillis();
            waitingToStopDodge = true;
        }
        else if (lower.contains("uncommon drop! wither bone")) {
            resetCombatState();
        }
        else if (lower.contains("you died")) {
            InputHelper.stopAll();
            DodgeHelper.stop();
            ParryHelper.reset();
            resetAllState();
        }
    }

    
    // RESET
    private static void resetCombatState() {
        moveForward        = false;
        moveToSafe         = false;
        MovementHelper.stopMovement();
        holdClick          = false;
        wasHolding         = false;
        comboAttack        = false;
        witherMagic        = false;
        demonMagic         = false;
        dodgeTriggered     = false;
        waitingToStopDodge = false;
    }

    private static void resetAllState() {
        rotateToBoss = false;
        resetCombatState();
    }

    public static void fullReset() {
        resetAllState();
        atSummonPos        = false;
        summonAttemptCount = 0;
        summonFailed       = false;
        InputHelper.stopAll();
    }
}