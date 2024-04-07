package com.pepej.prison.items.transform;

import java.util.List;

public interface Transformer {

    List<TransformResultEntity<TransformableItem>> transform(TransformableItem item);

    String getName();

    default double getMultiplier() {
        return 1.0;
    }
}
