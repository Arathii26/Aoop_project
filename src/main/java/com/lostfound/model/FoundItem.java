package com.lostfound.model;

import com.lostfound.enums.Category;

import java.time.LocalDateTime;

public class FoundItem extends Item {
    private LocalDateTime storedAt;

    public FoundItem() {
        super();
    }

    public FoundItem(int id, String name, String description, Category category,
                    String location, LocalDateTime dateTime, User reporter, boolean resolved, LocalDateTime storedAt) {
        super(id, name, description, category, location, dateTime, reporter, resolved);
        this.storedAt = storedAt;
    }

    public LocalDateTime getStoredAt() {
        return storedAt;
    }

    public void setStoredAt(LocalDateTime storedAt) {
        this.storedAt = storedAt;
    }

    @Override
    public String getSummary() {
        return "Found item: " + getName() + " at " + getLocation() + " and stored at " + storedAt;
    }
}
