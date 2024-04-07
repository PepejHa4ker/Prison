package com.pepej.prison.items.transform;

import java.util.Objects;

public class TransformResultEntity<T> {

    private final T result;
    private final int amount;

    public TransformResultEntity(T result, int amount) {
        this.result = result;
        this.amount = amount;
    }

    public T getResult() {
        return result;
    }

    public int getAmount() {
        return amount;
    }

    public static <T> TransformResultEntity<T> of(T result, int quantity) {
        Objects.requireNonNull(result, "result must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        return new TransformResultEntity<>(result, quantity);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformResultEntity<?> that = (TransformResultEntity<?>) o;
        return amount == that.amount && Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, amount);
    }

    @Override
    public String toString() {
        return "TransformResultEntity{" +
                "result=" + result +
                ", quantity=" + amount +
                '}';
    }
}
