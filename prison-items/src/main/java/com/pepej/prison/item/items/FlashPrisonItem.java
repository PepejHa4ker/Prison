package com.pepej.prison.item.items;

import com.pepej.prison.PrisonItem;
import com.pepej.prison.item.transform.TransformResultEntity;
import com.pepej.prison.item.transform.TransformableItem;
import com.pepej.prison.item.transform.Transformer;
import com.pepej.prison.item.transform.TransformerAdapter;
import com.pepej.prison.item.transform.furnace.Furnace;
import com.pepej.prison.item.transform.registry.TransformersRegistry;

import java.time.Duration;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class FlashPrisonItem extends PrisonItem implements TransformableItem {
    public FlashPrisonItem() {
        super("flash_item", "flash","&cFlash");
        registerTransformers();
    }

    public void registerTransformers() {
        TransformersRegistry.registerTransformerAdapter(this, Furnace.class, new FlashItemFurnaceTransformerAdapter(this));
    }

    @Override
    public <T extends Transformer> TransformerAdapter<T> getAdapterFor(Class<T> transformerClass) {
        return TransformersRegistry.getTransformerAdapterFor(this, transformerClass);
    }

    static final class FlashItemFurnaceTransformerAdapter implements TransformerAdapter<Furnace> {

        private final TransformableItem flash;

        FlashItemFurnaceTransformerAdapter(final TransformableItem flash) {
            this.flash = flash;
        }

        @Override
        public TransformableItem getItem() {
            return flash;
        }

        @Override
        public List<TransformResultEntity<TransformableItem>> getResult(Furnace furnace) {

            final int amount = (int) Math.floor(5 * furnace.getProductionMultiplier());
            if (!furnace.consumeFuel(getFuelCostFor(furnace))) {
                return emptyList();
            }

            return singletonList(
                    TransformResultEntity.of(
                            new EnergyDustPrisonItem(),
                            amount
                    )
            );
        }

        @Override
        public Duration getTransformDuration(final Furnace transformer) {
            return Duration.ofSeconds((long) (15 * transformer.getSpeedMultiplier()));
        }

        @Override
        public int getFuelCostFor(final Furnace transformer) {
            return (int) (3 * transformer.fuelCostMultiplier());
        }
    }


}
