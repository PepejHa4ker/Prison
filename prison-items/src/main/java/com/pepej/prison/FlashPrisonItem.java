package com.pepej.prison;

import com.pepej.prison.items.transform.TransformResult;
import com.pepej.prison.items.transform.TransformResultEntity;
import com.pepej.prison.items.transform.TransformableItem;
import com.pepej.prison.items.transform.Transformer;
import com.pepej.prison.items.transform.furnace.Furnace;
import com.pepej.prison.items.transform.registry.TransformersRegistry;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class FlashPrisonItem extends PrisonItem implements TransformableItem {
    public FlashPrisonItem() {
        super("flash_item", "flash","&cFlash");
        registerTransformers();
    }

    @SuppressWarnings("unchecked")
    public void registerTransformers() {
        TransformersRegistry.registerTransformer(this, Furnace.class, new FlashItemFurnaceTransformResult());
    }

    @Override
    public int getFuelCostFor(final Transformer transformer) {
        return 3;
    }

    static class FlashItemFurnaceTransformResult implements TransformResult<Furnace> {
        @Override
        public List<TransformResultEntity<TransformableItem>> getResult(Furnace furnace, TransformableItem item) {

            final int amount = (int) Math.floor(5 * furnace.getMultiplier());
            if (!furnace.consumeFuel(item.getFuelCostFor(furnace))) {
                return emptyList();
            }

            return singletonList(
                    TransformResultEntity.of(
                            new EnergyDustPrisonItem(),
                            amount
                    )
            );
        }
    }
}
