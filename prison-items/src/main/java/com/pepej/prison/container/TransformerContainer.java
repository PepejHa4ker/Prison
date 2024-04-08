package com.pepej.prison.container;

import com.pepej.prison.container.slot.TransformerSlot;

import java.util.List;

public interface TransformerContainer {


    List<TransformerSlot> getInputSlots();

    List<TransformerSlot> getOutputSlots();

    int getInputSlotsSize();

    int getOutputSlotsSize();

    int getAvailableInputSlots();

    int getAvailableOutputSlots();

    int getTotalSlots();

    int getTotalAvailableSlots();

}
