package com.pepej.prison.items.transform;

import java.util.List;

public interface TransformResult<T> {


    List<TransformResultEntity<TransformableItem>> getResult(T transformer, TransformableItem item);

}
