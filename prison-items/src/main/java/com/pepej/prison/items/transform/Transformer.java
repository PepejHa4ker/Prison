package com.pepej.prison.items.transform;

import java.time.Duration;
import java.util.List;

public interface Transformer {

    List<TransformResultEntity<TransformableItem>> transform(TransformableItem item);

    String getName();

    double getTransformProgressFor(TransformableItem item);

    double getSpeedMultiplier();

    void setTransformProgressFor(TransformableItem item, double progress);

    Duration getTransformDurationFor(TransformableItem item);

    default double getProductionMultiplier() {
        return 1.0;
    }
}
