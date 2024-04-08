package com.pepej.prison;

import com.pepej.prison.items.transform.TransformResultEntity;
import com.pepej.prison.items.transform.TransformableItem;
import com.pepej.prison.items.transform.Transformer;
import com.pepej.prison.items.transform.TransformerAdapter;
import com.pepej.prison.items.transform.furnace.DefaultFurnace;
import com.pepej.prison.items.transform.furnace.Furnace;
import com.pepej.prison.items.transform.furnace.MegaFurnace;
import com.pepej.prison.items.transform.furnace.PrivateFurnace;
import com.pepej.prison.items.transform.registry.TransformersRegistry;

import java.time.Duration;
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
        TransformersRegistry.registerTransformer(this, Furnace.class, new FlashItemFurnaceTransformerAdapter(this));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Transformer> TransformerAdapter<T> getAdapterFor(Class<T> transformerClass) {
        if (transformerClass == Furnace.class) {
            return (TransformerAdapter<T>) new FlashItemFurnaceTransformerAdapter(this);
        }
        return null;
    }

    static class FlashItemFurnaceTransformerAdapter implements TransformerAdapter<Furnace> {

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
            switch (transformer.getName()) {
                case DefaultFurnace.ID:
                    return 3;
                case PrivateFurnace.ID:
                    return 5;
                case MegaFurnace.ID:
                    return 8;
            }
            return 0;
        }
    }


}
