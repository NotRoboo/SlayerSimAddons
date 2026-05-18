package com.roboo;

public class ModConfig {

    private static boolean autoWitherBossEnabled = true;
    private static boolean autoDragonBossEnabled = true;
    private static boolean comboAttackEnabled    = true;
    private static boolean autoDodgeEnabled      = true;
    private static boolean autoReconnectEnabled  = true;
    private static boolean darkAuctionHudEnabled = true;

    private static String pathfindingMode = "None";

    private static int darkAuctionHudX = 10;
    private static int darkAuctionHudY = 30;

    private static int  autoStoreDelay    = 1200;
    private static boolean autoStoreHudEnabled = true;
    private static int  autoStoreHudX     = 10;
    private static int  autoStoreHudY     = 40;
    private static boolean sprintHudEnabled   = true;
    private static int  sprintHudX        = 10;
    private static int  sprintHudY        = 10;
    private static boolean useHudEnabled      = true;
    private static int  useHudX           = 10;
    private static int  useHudY           = 20;

    // Boss
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

    // Dark Auction HUD
    public static boolean isDarkAuctionHudEnabled() { return darkAuctionHudEnabled; }
    public static int     getDarkAuctionHudX()      { return darkAuctionHudX; }
    public static int     getDarkAuctionHudY()      { return darkAuctionHudY; }

    public static void setDarkAuctionHudEnabled(boolean v) { darkAuctionHudEnabled = v; }
    public static void setDarkAuctionHudX(int v)           { darkAuctionHudX = v; }
    public static void setDarkAuctionHudY(int v)           { darkAuctionHudY = v; }

    // AutoStore
    public static int     getAutoStoreDelay()       { return autoStoreDelay; }
    public static boolean isAutoStoreHudEnabled()   { return autoStoreHudEnabled; }
    public static int     getAutoStoreHudX()        { return autoStoreHudX; }
    public static int     getAutoStoreHudY()        { return autoStoreHudY; }
    public static boolean isSprintHudEnabled()      { return sprintHudEnabled; }
    public static int     getSprintHudX()           { return sprintHudX; }
    public static int     getSprintHudY()           { return sprintHudY; }
    public static boolean isUseHudEnabled()         { return useHudEnabled; }
    public static int     getUseHudX()              { return useHudX; }
    public static int     getUseHudY()              { return useHudY; }

    public static void setAutoStoreDelay(int v)        { autoStoreDelay = v; }
    public static void setAutoStoreHudEnabled(boolean v) { autoStoreHudEnabled = v; }
    public static void setAutoStoreHudX(int v)         { autoStoreHudX = v; }
    public static void setAutoStoreHudY(int v)         { autoStoreHudY = v; }
    public static void setSprintHudEnabled(boolean v)  { sprintHudEnabled = v; }
    public static void setSprintHudX(int v)            { sprintHudX = v; }
    public static void setSprintHudY(int v)            { sprintHudY = v; }
    public static void setUseHudEnabled(boolean v)     { useHudEnabled = v; }
    public static void setUseHudX(int v)               { useHudX = v; }
    public static void setUseHudY(int v)               { useHudY = v; }
}