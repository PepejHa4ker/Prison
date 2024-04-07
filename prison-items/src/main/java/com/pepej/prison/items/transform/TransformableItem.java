package com.pepej.prison.items.transform;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public interface TransformableItem {


    <T extends Transformer> Map<Class<? extends Transformer>, ? extends TransformResult<T>> transformers();

    default boolean isEmpty() {
        return this == zero();
    }

    static TransformableItem zero() {
        return ZERO.INSTANCE;
    }


    final class ZERO implements TransformableItem {

        public static final TransformableItem INSTANCE = new ZERO();

        private ZERO() {}

        @Override
        public <T extends Transformer> ImmutableMap<Class<? extends Transformer>, TransformResult<T>> transformers() {
            return ImmutableMap.of();
        }
    }
}


