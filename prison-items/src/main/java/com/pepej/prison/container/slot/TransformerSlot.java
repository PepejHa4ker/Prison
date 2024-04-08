package com.pepej.prison.container.slot;

public interface TransformerSlot {

    int getIndex();

    int getColumn();

    int getRow();

    TransformerSlotFunction getFunction();
}
