package com.pepej.prison;

import java.util.Objects;

public class Durability {

    private int max;

    private int current;

    public Durability(int current, int max) {
        this.max = max;
        this.current = current;
    }

    public static Durability of(int max) {
        return new Durability(max, max);
    }

    public static Durability of(int current, int max) {
        return new Durability(current, max);
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    @Override
    public String toString() {
        return "Durability{" +
                "max=" + max +
                ", min=" + current +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Durability that = (Durability) o;
        return max == that.max && current == that.current;
    }

    @Override
    public int hashCode() {
        return Objects.hash(max, current);
    }
}
