package com.pepej.prison.items.transform.furnace;

import com.pepej.prison.items.transform.TransformResult;
import com.pepej.prison.items.transform.TransformResultEntity;
import com.pepej.prison.items.transform.TransformableItem;
import com.pepej.prison.items.transform.registry.TransformersRegistry;

import java.util.Collections;
import java.util.List;

public class DefaultFurnace implements Furnace {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public List<TransformResultEntity<TransformableItem>> transform(TransformableItem item) {
        TransformResult<Furnace> furnaceTransformResult = TransformersRegistry.getTransformerFor(item, Furnace.class);
        if (furnaceTransformResult == null) {
            return Collections.emptyList();
        }
        return furnaceTransformResult.getResult(this, item);

    }


}

