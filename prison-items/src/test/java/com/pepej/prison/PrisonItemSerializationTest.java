package com.pepej.prison;

import com.pepej.prison.item.items.FlashPrisonItem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrisonItemSerializationTest {


    private byte[] serializedItem;

    @Before
    public void setUp() throws Exception {
        PrisonItem flashPrisonItem = new FlashPrisonItem();
        serializedItem = flashPrisonItem.serialize();
    }


    @Test
    public void testDeserialize() {
        PrisonItem flashPrisonItem = new PrisonItem();
        final PrisonItem deserialized = flashPrisonItem.deserialize(serializedItem);
        assertEquals("flash_item", deserialized.getTag());
        assertEquals(Durability.of(-1), deserialized.getDurability());
    }


}