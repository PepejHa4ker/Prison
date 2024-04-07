package com.pepej.prison.items.transform.furnace;


import com.pepej.prison.FlashPrisonItem;
import com.pepej.prison.PrisonItem;
import com.pepej.prison.items.transform.TransformResultEntity;
import com.pepej.prison.items.transform.TransformableItem;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultFurnaceTest {


    @Test
    public void testFlashDefaultFurnaceTransform() {
        Furnace furnace = new DefaultFurnace();
        TransformableItem prisonItem = new FlashPrisonItem();
        List<TransformResultEntity<TransformableItem>> transformableItems = furnace.transform(prisonItem);
        assertEquals(1, transformableItems.size());
        TransformResultEntity<TransformableItem> transformResult = transformableItems.get(0);
        PrisonItem transformedPrisonItem = (PrisonItem) transformResult.getResult();
        assertNotNull(transformedPrisonItem);
        assertEquals("energy_dust", transformedPrisonItem.getTag());
        assertEquals(1, transformedPrisonItem.getQuantity());
        assertEquals(5, transformResult.getAmount());

    }
}