package com.pepej.prison.items.transform.furnace;

public class PrivateFurnace extends DefaultFurnace {

    @Override
    public String getName() {
        return "private";
    }

    @Override
    public double getMultiplier() {
        return 2.0;
    }
}
