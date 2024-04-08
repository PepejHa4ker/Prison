package com.pepej.prison.items.transform.furnace;

public class PrivateFurnace extends DefaultFurnace {

    public PrivateFurnace(final int fuel) {
        super(fuel);
    }

    @Override
    public String getName() {
        return "private";
    }

    @Override
    public double getMultiplier() {
        return 2.0;
    }
}
