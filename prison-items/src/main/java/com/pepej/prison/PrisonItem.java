package com.pepej.prison;

import com.pepej.prison.enchantment.Enchantment;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PrisonItem implements Serializable {

    public byte[] serialize() {
        return SerializationUtils.serialize(this);
    }

    public PrisonItem deserialize(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    private final UUID id;
    private final String itemName;
    private final String tag;
    private final String displayName;
    private final Set<Enchantment> enchantments;

    private int quantity;
    private Durability durability;

    protected PrisonItem(String tag, final String itemName, String displayName) {
        this.id = UUID.randomUUID();
        this.itemName = itemName;
        this.tag = tag;
        this.quantity = 1;
        this.displayName = displayName;
        this.durability = Durability.of(-1);
        this.enchantments = new HashSet<>();
    }

    PrisonItem() {
        this.id = null;
        this.itemName = null;
        this.tag = null;
        this.quantity = 0;
        this.displayName = null;
        this.enchantments = new HashSet<>();

    }

    public UUID getId() {
        return id;
    }

    public String getTag() {
        return tag;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Durability getDurability() {
        return durability;
    }

    public Set<Enchantment> getEnchantments() {
        return enchantments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrisonItem that = (PrisonItem) o;
        return quantity == that.quantity && Objects.equals(id, that.id) && Objects.equals(tag, that.tag) && Objects.equals(displayName, that.displayName) && Objects.equals(durability, that.durability);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tag, displayName, quantity, durability);
    }

    @Override
    public String toString() {
        return "PrisonItem{" +
                "id=" + id +
                ", tag='" + tag + '\'' +
                ", displayName='" + displayName + '\'' +
                ", quantity=" + quantity +
                ", durability=" + durability +
                '}';
    }

    public String getItemName() {
        return itemName;
    }
}
