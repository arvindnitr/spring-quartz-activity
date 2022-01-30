package com.arvind.quartz.scheduler.activity;

public enum ResourceType {

    TIME("TIME"),
    MT_DB("MT_DB"),
    CADW_PASSWORD("CADW_PASSWORD"),
    CADW_WALLET("CADW_WALLET"),
    ODI_DB_PASSWORD("ODI_DB_PASSWORD"),
    GS_PASSWORD("GS_PASSWORD"),
    NSGW_ATP_PASSWORD("NSGW_ATP_PASSWORD"),
    NSGW_CADW_PASSWORD("NSGW_CADW_PASSWORD"),
    NSGW_WALLET("NSGW_WALLET"),
    NS_INSTANCE("NS_INSTANCE");

    private final String type;

    ResourceType(final String type) {
        this.type = type;
    }

    public static ResourceType safeValueOf(String literal) {
        try {
            return ResourceType.valueOf(literal);
        } catch(Exception ex) {
        }

        return null;
    }
}
