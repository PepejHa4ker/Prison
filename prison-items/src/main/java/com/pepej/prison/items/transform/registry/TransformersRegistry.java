package com.pepej.prison.items.transform.registry;

import com.pepej.prison.items.transform.TransformResult;
import com.pepej.prison.items.transform.TransformableItem;
import com.pepej.prison.items.transform.Transformer;

import java.util.HashMap;
import java.util.Map;

public final class TransformersRegistry {

    private static final Map<TransformableItem, Map<Class<?>, TransformResult<?>>> map = new HashMap<>();


    private TransformersRegistry() {}

    public static <T extends Transformer> void registerTransformer(
            TransformableItem transformableItem,
            Class<? extends T> transformerClass,
            TransformResult<T> transformResult) {
        Map<Class<?>, TransformResult<?>> transformerMap = new HashMap<>();
        transformerMap.put(transformerClass, transformResult);
        map.put(transformableItem, transformerMap);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Transformer> TransformResult<T> getTransformerFor(TransformableItem item, Class<T> transformerClass) {
        Map<Class<?>, TransformResult<?>> classTransformResultMap = map.get(item);
        if (classTransformResultMap != null) {
            TransformResult<?> transformResult = classTransformResultMap.get(transformerClass);
            return (TransformResult<T>) transformResult;
        }
        return null;

    }
}
