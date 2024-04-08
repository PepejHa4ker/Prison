package com.pepej.prison.items.fuel;

public interface FuelConsumer {

    boolean canConsumeFuel(int amount);

    boolean consumeFuel(int amount);

    int remainingFuel();

    boolean fillUp(int amount);

    int getTankSize();


}
