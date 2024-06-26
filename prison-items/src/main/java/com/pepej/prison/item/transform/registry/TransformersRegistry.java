package com.pepej.prison.item.transform.registry;

import com.pepej.prison.item.transform.TransformerAdapter;
import com.pepej.prison.item.transform.TransformableItem;
import com.pepej.prison.item.transform.Transformer;

import java.util.HashMap;
import java.util.Map;

public final class TransformersRegistry {

    private static final Map<TransformableItem, Map<Class<?>, TransformerAdapter<?>>> map = new HashMap<>();


    private TransformersRegistry() {}

    public static <T extends Transformer> void registerTransformerAdapter(
            TransformableItem transformableItem,
            Class<? extends T> transformerClass,
            TransformerAdapter<T> transformResult) {
        Map<Class<?>, TransformerAdapter<?>> transformerMap = new HashMap<>();
        transformerMap.put(transformerClass, transformResult);
        map.put(transformableItem, transformerMap);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Transformer> TransformerAdapter<T> getTransformerAdapterFor(TransformableItem item, Class<T> transformerClass) {
        Map<Class<?>, TransformerAdapter<?>> classTransformResultMap = map.get(item);
        if (classTransformResultMap != null) {
            TransformerAdapter<?> transformResult = classTransformResultMap.get(transformerClass);
            return (TransformerAdapter<T>) transformResult;
        }
        return null;

    }
}
