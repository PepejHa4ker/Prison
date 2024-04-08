package com.pepej.prison.container;

import com.pepej.prison.container.slot.TransformerSlot;
import com.pepej.prison.item.transform.TransformableItem;

import java.io.Serializable;
import java.util.List;

public interface TransformerContainer extends Serializable {

    List<TransformerSlot> getInputSlots();

    List<TransformerSlot> getOutputSlots();

    void addInput(final TransformableItem item);

    int getInputSlotsSize();

    int getOutputSlotsSize();

    int getAvailableInputSlots();

    int getAvailableOutputSlots();

    int getTotalSlots();

    int getTotalAvailableSlots();

    byte[] serialize();

    TransformableItem deserialize(final byte[] bytes);


}
