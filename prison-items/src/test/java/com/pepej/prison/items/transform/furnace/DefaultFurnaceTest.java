package com.pepej.prison.items.transform.furnace;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pepej.prison.items.FlashPrisonItem;
import com.pepej.prison.items.PrisonItem;
import com.pepej.prison.items.transform.TransformableItem;
import org.junit.Test;

import java.util.List;

public class DefaultFurnaceTest {


    @Test
    public void testFlashDefaultFurnaceTransform() {
        Furnace furnace = new DefaultFurnace();
        TransformableItem prisonItem = new FlashPrisonItem();
        List<TransformableItem> transformableItems = furnace.transform(prisonItem);
        assertEquals(1, transformableItems.size());
        PrisonItem transformedPrisonItem = (PrisonItem) transformableItems.get(0);
        assertNotNull(transformedPrisonItem);
        assertEquals("energy_dust", transformedPrisonItem.getTag());
        assertEquals(1, transformedPrisonItem.getQuantity());

    }
}