package com.pepej.prison.item.transform;

import java.time.Duration;
import java.util.List;

public interface TransformerAdapter<T extends Transformer> {

    TransformableItem getItem();

    List<TransformResultEntity<TransformableItem>> getResult(T transformer);

    default int getFuelCostFor(T transformer) {
        return 0;
    }

    Duration getTransformDuration(T transformer);


}
