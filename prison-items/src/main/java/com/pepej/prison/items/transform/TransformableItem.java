package com.pepej.prison.items.transform;

public interface TransformableItem {


    default void registerTransformers() {}

    default boolean isEmpty() {
        return this == zero();
    }

    static TransformableItem zero() {
        return ZERO.INSTANCE;
    }


    final class ZERO implements TransformableItem {

        public static final TransformableItem INSTANCE = new ZERO();

        private ZERO() {}

    }
}


