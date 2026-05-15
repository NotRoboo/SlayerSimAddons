package com.roboo;

public class ModConfig {

    private static boolean autoWitherBossEnabled    = true;
    private static boolean autoDragonBossEnabled    = true;

    private static boolean comboAttackEnabled       = true;  // "Combo Attack / Dark Slash"
    private static boolean autoDodgeEnabled         = true;  // "Auto Dodge / Parry"

    private static boolean autoReconnectEnabled     = true;
    private static boolean hexPathfindingEnabled    = false; // was crescentTower
    private static boolean fishingPathfindingEnabled = false; // was volcano

    public static boolean isAutoWitherBossEnabled()      { return autoWitherBossEnabled; }
    public static boolean isAutoDragonBossEnabled()      { return autoDragonBossEnabled; }
    public static boolean isComboAttackEnabled()         { return comboAttackEnabled; }
    public static boolean isAutoDodgeEnabled()           { return autoDodgeEnabled; }
    public static boolean isAutoReconnectEnabled()       { return autoReconnectEnabled; }
    public static boolean isHexPathfindingEnabled()      { return hexPathfindingEnabled; }
    public static boolean isFishingPathfindingEnabled()  { return fishingPathfindingEnabled; }

    public static boolean isCrescentTowerEnabled()       { return hexPathfindingEnabled; }
    public static boolean isVolcanoEnabled()             { return fishingPathfindingEnabled; }

    public static void setAutoWitherBossEnabled(boolean v)      { autoWitherBossEnabled = v; }
    public static void setAutoDragonBossEnabled(boolean v)      { autoDragonBossEnabled = v; }
    public static void setComboAttackEnabled(boolean v)         { comboAttackEnabled = v; }
    public static void setAutoDodgeEnabled(boolean v)           { autoDodgeEnabled = v; }
    public static void setAutoReconnectEnabled(boolean v)       { autoReconnectEnabled = v; }
    public static void setHexPathfindingEnabled(boolean v)      { hexPathfindingEnabled = v; }
    public static void setFishingPathfindingEnabled(boolean v)  { fishingPathfindingEnabled = v; }

    public static void setCrescentTowerEnabled(boolean v)       { hexPathfindingEnabled = v; }
    public static void setVolcanoEnabled(boolean v)             { fishingPathfindingEnabled = v; }
}