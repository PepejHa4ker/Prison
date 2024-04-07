package com.pepej.prison.items.transform.furnace;

import com.pepej.prison.items.PrisonItem;
import com.pepej.prison.items.transform.TransformableItem;
import com.pepej.prison.items.transform.extractor.Extractor;

import java.util.Arrays;
import java.util.List;

public class DefaultFurnace implements Furnace {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public List<TransformableItem> transform(TransformableItem item) {
        return item.transformers().get(Furnace.class).getResult(this, item);
    }


}

