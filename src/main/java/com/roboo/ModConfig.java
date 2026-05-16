package com.roboo;

public class ModConfig {

    private static boolean autoWitherBossEnabled = true;
    private static boolean autoDragonBossEnabled = true;
    private static boolean comboAttackEnabled    = true;
    private static boolean autoDodgeEnabled      = true;
    private static boolean autoReconnectEnabled  = true;

    private static String pathfindingMode = "None";

    public static boolean isAutoWitherBossEnabled() { return autoWitherBossEnabled; }
    public static boolean isAutoDragonBossEnabled() { return autoDragonBossEnabled; }
    public static boolean isComboAttackEnabled()    { return comboAttackEnabled; }
    public static boolean isAutoDodgeEnabled()      { return autoDodgeEnabled; }
    public static boolean isAutoReconnectEnabled()  { return autoReconnectEnabled; }
    public static String  getPathfindingMode()      { return pathfindingMode; }

    public static void setAutoWitherBossEnabled(boolean v) { autoWitherBossEnabled = v; }
    public static void setAutoDragonBossEnabled(boolean v) { autoDragonBossEnabled = v; }
    public static void setComboAttackEnabled(boolean v)    { comboAttackEnabled = v; }
    public static void setAutoDodgeEnabled(boolean v)      { autoDodgeEnabled = v; }
    public static void setAutoReconnectEnabled(boolean v)  { autoReconnectEnabled = v; }
    public static void setPathfindingMode(String v)        { pathfindingMode = v; }
}