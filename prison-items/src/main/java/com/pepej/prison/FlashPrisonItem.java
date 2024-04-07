package com.pepej.prison;

import com.pepej.prison.items.transform.TransformResult;
import com.pepej.prison.items.transform.TransformResultEntity;
import com.pepej.prison.items.transform.TransformableItem;
import com.pepej.prison.items.transform.furnace.Furnace;
import com.pepej.prison.items.transform.registry.TransformersRegistry;

import java.util.Collections;
import java.util.List;

public class FlashPrisonItem extends PrisonItem implements TransformableItem {
    public FlashPrisonItem() {
        super("flash_item", "&cFlash");
        registerTransformers();
    }

    @SuppressWarnings("unchecked")
    public void registerTransformers() {
        TransformersRegistry.registerTransformer(this, Furnace.class, new FlashItemFurnaceTransformResult());
    }


    static class FlashItemFurnaceTransformResult implements TransformResult<Furnace> {
        @Override
        public List<TransformResultEntity<TransformableItem>> getResult(Furnace furnace, TransformableItem item) {

            if (furnace.getName().equals("default")) {
                return Collections.singletonList(
                        TransformResultEntity.of(
                                new EnergyDustPrisonItem(),
                                5
                        )
                );
            }
            return Collections.emptyList();
        }
    }
}
