package com.pepej.prison.item.items;

import com.pepej.prison.PrisonItem;
import com.pepej.prison.item.transform.TransformResultEntity;
import com.pepej.prison.item.transform.TransformableItem;
import com.pepej.prison.item.transform.Transformer;
import com.pepej.prison.item.transform.TransformerAdapter;
import com.pepej.prison.item.transform.crusher.Crusher;
import com.pepej.prison.item.transform.registry.TransformersRegistry;

import java.time.Duration;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class BatteryPrisonItem extends PrisonItem implements TransformableItem {

    public BatteryPrisonItem() {
        super("battery", "iron_button", "&aBattery");
    }

    @Override
    public void registerTransformers() {
        TransformersRegistry.registerTransformerAdapter(this, Crusher.class, new BatteryCrusherTransformerAdapter(this));
    }

    @Override
    public <T extends Transformer> TransformerAdapter<T> getAdapterFor(final Class<T> transformClass) {
        return TransformableItem.super.getAdapterFor(transformClass);
    }

    static final class BatteryCrusherTransformerAdapter implements TransformerAdapter<Crusher> {

        private final BatteryPrisonItem batteryPrisonItem;

        private BatteryCrusherTransformerAdapter(final BatteryPrisonItem batteryPrisonItem) {
            this.batteryPrisonItem = batteryPrisonItem;
        }

        @Override
        public TransformableItem getItem() {
            return this.batteryPrisonItem;
        }

        @Override
        public List<TransformResultEntity<TransformableItem>> getResult(Crusher crusher) {

            final int amount = (int) Math.floor(10 * crusher.getProductionMultiplier());
            if (!crusher.consumeFuel(getFuelCostFor(crusher))) {
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
        public Duration getTransformDuration(final Crusher transformer) {
            return Duration.ofSeconds((long) (8 * transformer.getSpeedMultiplier()));

        }

        @Override
        public int getFuelCostFor(final Crusher transformer) {
            return (int) (10 * transformer.fuelCostMultiplier());
        }
    }
}
