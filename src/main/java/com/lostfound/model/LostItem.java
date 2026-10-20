package com.lostfound.model;

import com.lostfound.enums.Category;

import java.time.LocalDateTime;

public class LostItem extends Item {
    public LostItem() {
        super();
    }

    public LostItem(int id, String name, String description, Category category,
                   String location, LocalDateTime dateTime, User reporter, boolean resolved) {
        super(id, name, description, category, location, dateTime, reporter, resolved);
    }

    @Override
    public String getSummary() {
        return "Lost item: " + getName() + " in " + getLocation() + " on " + getDateTime();
    }
}
