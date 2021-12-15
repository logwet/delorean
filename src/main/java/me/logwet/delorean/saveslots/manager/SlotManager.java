package me.logwet.delorean.saveslots.manager;

import me.logwet.delorean.data.SlotsData;

public interface SlotManager {
    SlotsData getSlotsData();

    boolean save(String id);

    boolean save(int slot);

    boolean save();

    boolean load(String id);

    boolean load(int slot);

    boolean load();

    boolean delete(String id);

    boolean delete(int slot);

    boolean delete();

    boolean deleteAll();
}
