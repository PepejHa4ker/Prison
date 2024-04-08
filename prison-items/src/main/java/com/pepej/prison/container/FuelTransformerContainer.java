package com.pepej.prison.container;

import com.pepej.prison.container.slot.TransformerSlot;

import java.util.List;

public interface FuelTransformerContainer extends TransformerContainer {


    List<TransformerSlot> getFuelSlots();

}
