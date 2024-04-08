package com.pepej.prison.items.transform.furnace;


import com.pepej.prison.FlashPrisonItem;
import com.pepej.prison.PrisonItem;
import com.pepej.prison.items.transform.TransformResultEntity;
import com.pepej.prison.items.transform.TransformableItem;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FurnaceTest {


    @Test
    public void testFlashDefaultFurnaceTransform() {
        Furnace furnace = new DefaultFurnace(5);
        TransformableItem prisonItem = new FlashPrisonItem();
        assertEquals(5, furnace.remainingFuel());
        List<TransformResultEntity<TransformableItem>> transformableItems = furnace.transform(prisonItem);
        assertEquals(1, transformableItems.size());
        TransformResultEntity<TransformableItem> transformResult = transformableItems.get(0);
        PrisonItem transformedPrisonItem = (PrisonItem) transformResult.getResult();
        assertNotNull(transformedPrisonItem);
        assertEquals(2, furnace.remainingFuel());
        assertEquals(128, furnace.getTankSize());
        assertEquals("default", furnace.getName());
        assertEquals(1.0, furnace.getMultiplier(), 0.001);
        assertEquals("energy_dust", transformedPrisonItem.getTag());
        assertEquals(1, transformedPrisonItem.getQuantity());
        assertEquals(5, transformResult.getAmount());

    }


    @Test
    public void testFlashPrivateFurnaceTransform() {
        Furnace furnace = new PrivateFurnace(10);
        TransformableItem prisonItem = new FlashPrisonItem();
        List<TransformResultEntity<TransformableItem>> transformableItems = furnace.transform(prisonItem);
        assertEquals(1, transformableItems.size());
        TransformResultEntity<TransformableItem> transformResult = transformableItems.get(0);
        PrisonItem transformedPrisonItem = (PrisonItem) transformResult.getResult();
        assertNotNull(transformedPrisonItem);
        assertEquals("private", furnace.getName());
        assertEquals(2.0, furnace.getMultiplier(), 0.001);
        assertEquals("energy_dust", transformedPrisonItem.getTag());
        assertEquals(1, transformedPrisonItem.getQuantity());
        assertEquals(10, transformResult.getAmount());
    }

    @Test
    public void testFlashMegaFurnaceTransform() {
        Furnace furnace = new MegaFurnace(20);
        TransformableItem prisonItem = new FlashPrisonItem();
        List<TransformResultEntity<TransformableItem>> transformableItems = furnace.transform(prisonItem);
        assertEquals(1, transformableItems.size());
        TransformResultEntity<TransformableItem> transformResult = transformableItems.get(0);
        PrisonItem transformedPrisonItem = (PrisonItem) transformResult.getResult();
        assertNotNull(transformedPrisonItem);
        assertEquals(128,furnace.getTankSize());
        assertEquals("mega", furnace.getName());
        assertEquals(3.0, furnace.getMultiplier(), 0.001);
        assertEquals("energy_dust", transformedPrisonItem.getTag());
        assertEquals(1, transformedPrisonItem.getQuantity());
        assertEquals(15, transformResult.getAmount());
    }
}