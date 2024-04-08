package com.pepej.prison.container.slot;

import com.pepej.prison.item.transform.TransformableItem;

public interface TransformerSlot {

    int getIndex();

    int getColumn();

    int getRow();

    TransformableItem getItem();

    boolean setItem(final TransformableItem item);

    TransformerSlotFunction getFunction();
}
