package com.pepej.prison.item.fuel;

public interface FuelConsumer {

    boolean canConsumeFuel(int amount);

    double fuelCostMultiplier();

    boolean consumeFuel(int amount);

    int remainingFuel();

    boolean fillUp(int amount);

    int getTankSize();


}
