package com.pepej.prison.items.transform.furnace;

public class MegaFurnace extends DefaultFurnace {

    public MegaFurnace(final int fuel) {
        super(fuel);
    }

    @Override
    public String getName() {
        return "mega";
    }

    @Override
    public double getMultiplier() {
        return 3.0;
    }
}
