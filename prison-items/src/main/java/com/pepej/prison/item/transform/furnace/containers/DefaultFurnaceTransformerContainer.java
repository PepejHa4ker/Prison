package com.pepej.prison.item.transform.furnace.containers;

import com.pepej.prison.container.FuelTransformerContainer;
import com.pepej.prison.container.slot.TransformerSlot;
import com.pepej.prison.item.transform.TransformableItem;

import java.util.Collections;
import java.util.List;

public abstract class DefaultFurnaceTransformerContainer implements FuelTransformerContainer {



    @Override
    public void addFuel(final TransformableItem fuelITem) {
        TransformerSlot availableSlot = findFirstAvailableSlot(getFuelSlots());
        if (availableSlot != null) {
            availableSlot.setItem(fuelITem);
        }
    }

    private static TransformerSlot findFirstAvailableSlot(final List<TransformerSlot> slots) {
        for (final TransformerSlot slot : slots) {
            if (slot.getItem().isEmpty()) {
                return slot;
            }
        }
        return null;
    }

    @Override
    public void addInput(final TransformableItem item) {
        TransformerSlot availableSlot = findFirstAvailableSlot(getFuelSlots());
        if (availableSlot != null) {
            availableSlot.setItem(item);
        }
    }

    @Override
    public List<TransformerSlot> getFuelSlots() {
        return Collections.emptyList();
    }

    @Override
    public int getAvailableFuelSlots() {
        return 1;
    }

    @Override
    public List<TransformerSlot> getInputSlots() {
        return Collections.emptyList();
    }

    @Override
    public List<TransformerSlot> getOutputSlots() {
        return Collections.emptyList();
    }

    @Override
    public int getAvailableInputSlots() {
        return 1;
    }

    @Override
    public int getAvailableOutputSlots() {
        return 1;
    }

    @Override
    public int getFuelSlotsSize() {
        return getFuelSlots().size();
    }

    @Override
    public int getInputSlotsSize() {
        return getInputSlots().size();
    }

    @Override
    public int getOutputSlotsSize() {
        return getOutputSlots().size();
    }

    @Override
    public int getTotalSlots() {
        return getInputSlotsSize() + getOutputSlotsSize() + getFuelSlotsSize();
    }

    @Override
    public int getTotalAvailableSlots() {
        return 3;
    }
}
