package com.pepej.prison.items.transform.furnace;

import com.pepej.prison.items.transform.TransformResult;
import com.pepej.prison.items.transform.TransformResultEntity;
import com.pepej.prison.items.transform.TransformableItem;
import com.pepej.prison.items.transform.registry.TransformersRegistry;

import java.util.Collections;
import java.util.List;

public class DefaultFurnace implements Furnace {

    private int fuel;

    public DefaultFurnace(int fuel) {
        this.fuel = fuel;
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public List<TransformResultEntity<TransformableItem>> transform(TransformableItem item) {
        TransformResult<Furnace> furnaceTransformResult = TransformersRegistry.getTransformerFor(item, Furnace.class);
        if (furnaceTransformResult == null) {
            return Collections.emptyList();
        }
        return furnaceTransformResult.getResult(this, item);
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

