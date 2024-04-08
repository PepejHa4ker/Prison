package com.pepej.prison.items.transform;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public interface TransformableItem {


    default void registerTransformers() {}

    default boolean isEmpty() {
        return this == zero();
    }

    default <T extends Transformer> TransformerAdapter<T> getAdapterFor(Class<T> transformClass) {
        return NOOPAdapter.INSTANCE;
    }


    static TransformableItem zero() {
        return ZERO.INSTANCE;
    }


    final class ZERO implements TransformableItem {

        static final TransformableItem INSTANCE = new ZERO();

        private ZERO() {}

    }

    final class NOOPAdapter<T extends Transformer> implements TransformerAdapter<T> {

        static final NOOPAdapter INSTANCE = new NOOPAdapter();

        private NOOPAdapter() {}

        @Override
        public TransformableItem getItem() {
            return null;
        }

        @Override
        public List<TransformResultEntity<TransformableItem>> getResult(final T transformer) {
            return Collections.emptyList();
        }

        @Override
        public Duration getTransformDuration(final T transformer) {
            return Duration.ZERO;
        }
    }
}


