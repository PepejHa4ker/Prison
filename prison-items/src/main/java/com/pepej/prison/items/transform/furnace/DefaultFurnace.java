package com.pepej.prison.items.transform.furnace;

import com.pepej.prison.items.transform.TransformerAdapter;
import com.pepej.prison.items.transform.TransformResultEntity;
import com.pepej.prison.items.transform.TransformableItem;
import com.pepej.prison.items.transform.registry.TransformersRegistry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class DefaultFurnace implements Furnace {

    public static final String ID = "default_furnace";
    private int fuel;

    private double progress;

    public DefaultFurnace(int fuel) {
        this.fuel = fuel;
    }

    @Override
    public String getName() {
        return ID;
    }

    @Override
    public List<TransformResultEntity<TransformableItem>> transform(TransformableItem item) {
        TransformerAdapter<Furnace> furnaceTransformResult = TransformersRegistry.getTransformerFor(item, Furnace.class);
        if (furnaceTransformResult == null) {
            return Collections.emptyList();
        }
        return furnaceTransformResult.getResult(this);
    }

    @Override
    public boolean fillUp(final int amount) {
        if (this.remainingFuel() + amount > this.getTankSize()) {
            return false;
        }

        this.fuel += amount;
        return true;
    }


    @Override
    public Duration getTransformDurationFor(final TransformableItem item) {
        return item.getAdapterFor(Furnace.class).getTransformDuration(this);
    }

    @Override
    public double getTransformProgressFor(final TransformableItem item) {
        return progress;
    }

    @Override
    public double getSpeedMultiplier() {
        return 1.0;
    }

    @Override
    public void setTransformProgressFor(final TransformableItem item, final double progress) {
        this.progress = progress;
    }

    @Override
    public int getTankSize() {
        return 128;
    }

    @Override
    public boolean consumeFuel(final int amount) {
        if (!canConsumeFuel(amount)) {
            return false;
        }
        this.fuel -= amount;
        return true;
    }

    @Override
    public int remainingFuel() {
        return this.fuel;
    }

    @Override
    public boolean canConsumeFuel(int amount) {
        return this.remainingFuel() > 0 && this.remainingFuel() >= amount;
    }
}

