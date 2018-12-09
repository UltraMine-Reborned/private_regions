package org.ultramine.mods.privreg;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class RegionConfig {

    private static final String CATEGORY_CLIENT = "Client config";
    private static final String CATEGORY_SERVER = "Server config";

    public static boolean enableDragonModel;

    public static int DefaultBlockMaxCharge = 1000;
    //public static int DefaultItemMaxCharge = 1000;
    public static int DefaultMaxTacts = 50;
    public static int ChargerMaxCharge = 1000;
    public static double moneyPerEA = 1d;
    public static int CheckDistance = 15;
    public static long inactiveRegionTimeout = 1000 * 60 * 60 * 24 * 7; //one week
    public static float defaultRegionCost = 20.0F;

    public static void loadConfig(File configFile, boolean isClient) {
        Configuration config = new Configuration(configFile);
        config.load();

        if (isClient) {
            enableDragonModel = Boolean.parseBoolean(config.get(CATEGORY_CLIENT, "enableDragonModel", false, "If true, simple textured region block will been replaced by awesome Endergradon model").getString());
        }

        DefaultBlockMaxCharge = Integer.parseInt(config.get(CATEGORY_SERVER, "defaultRegionBlockMaxCharge", 1000, "Default maximum charge value for Region Block").getString());

        if (config.hasChanged())
            config.save();
    }
}
