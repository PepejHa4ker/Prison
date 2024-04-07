package com.pepej.prison.items;

import com.pepej.prison.items.transform.TransformResult;
import com.pepej.prison.items.transform.TransformableItem;
import com.pepej.prison.items.transform.Transformer;

import java.util.Collections;
import java.util.Map;

public class EnergyDustPrisonItem extends PrisonItem implements TransformableItem {
    public EnergyDustPrisonItem() {
        super("energy_dust", "&cEnergy dust");
    }

    @Override
    public <T extends Transformer> Map<Class<? extends Transformer>, ? extends TransformResult<T>> transformers() {
        return Collections.emptyMap();
    }
}
