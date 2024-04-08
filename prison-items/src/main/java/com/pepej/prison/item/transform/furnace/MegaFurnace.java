package com.pepej.prison.item.transform.furnace;

public class MegaFurnace extends DefaultFurnace {

    public static final String ID = "mega_furnace";

    public MegaFurnace(final int fuel) {
        super(fuel);
    }

    @Override
    public String getName() {
        return ID;
    }

    @Override
    public double getProductionMultiplier() {
        return 3.0;
    }

    @Override
    public double getSpeedMultiplier() {
        return 3.0;
    }

    @Override
    public double fuelCostMultiplier() {
        return 3.0;
    }
}
