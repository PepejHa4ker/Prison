package com.pepej.prison.items.transform;

import java.util.List;

public interface Transformer {

    List<TransformableItem> transform(TransformableItem item);

    String getName();
}
