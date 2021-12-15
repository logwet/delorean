package me.logwet.delorean.util.data;

import java.util.TreeMap;
import org.jetbrains.annotations.NotNull;

public class SlotsData {
    private final TreeMap<Integer, String> slots;

    public SlotsData(TreeMap<Integer, String> slots) {
        this.slots = slots;
    }

    public SlotsData() {
        this(new TreeMap<>());
    }

    @NotNull
    public TreeMap<Integer, String> getSlots() {
        return slots;
    }
}
