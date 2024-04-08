package com.pepej.prison.container;

import com.pepej.prison.container.slot.TransformerSlot;
import com.pepej.prison.item.transform.TransformableItem;

import java.util.List;

public interface FuelTransformerContainer extends TransformerContainer {

    List<TransformerSlot> getFuelSlots();

    int getFuelSlotsSize();

    void addFuel(final TransformableItem fuelITem);

    int getAvailableFuelSlots();

    @Override
    int getTotalSlots();
}
