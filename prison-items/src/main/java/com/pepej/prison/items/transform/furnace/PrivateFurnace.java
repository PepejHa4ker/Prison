package com.pepej.prison.items.transform.furnace;

public class PrivateFurnace extends DefaultFurnace {

    public static final String ID = "private_furnace";

    public PrivateFurnace(final int fuel) {
        super(fuel);
    }

    @Override
    public int getTankSize() {
        return 256;
    }

    @Override
    public String getName() {
        return ID;
    }

    @Override
    public double getProductionMultiplier() {
        return 2.0;
    }

    @Override
    public double getSpeedMultiplier() {
        return 2.0;
    }
}
