package com.pepej.prison.items;

import com.google.common.collect.ImmutableMap;
import com.pepej.prison.items.transform.TransformResult;
import com.pepej.prison.items.transform.TransformableItem;
import com.pepej.prison.items.transform.Transformer;
import com.pepej.prison.items.transform.furnace.Furnace;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FlashPrisonItem extends PrisonItem implements TransformableItem {
    public FlashPrisonItem() {
        super("flash_item", "&cFlash");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Transformer> Map<Class<? extends Transformer>, ? extends TransformResult<T>> transformers() {
        return ImmutableMap.<Class<? extends Transformer>, TransformResult<T>>builder()
                .put(Furnace.class, (TransformResult<T>) new FlashItemFurnaceTransformResult())
                .build();
    }


    static class FlashItemFurnaceTransformResult implements TransformResult<Furnace> {
        @Override
        public List<TransformableItem> getResult(Furnace transformer, TransformableItem item) {

            if (transformer.getName().equals("default")) {
                return Collections.singletonList(new EnergyDustPrisonItem());
            }
            return Collections.emptyList();
        }
    }
}
