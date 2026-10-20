package com.lostfound.model;

import com.lostfound.enums.Category;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    private Category category;
    private String location;
    private LocalDateTime dateTime;
    private User reporter;
    private boolean resolved;

    public Item() {
    }

    public Item(int id, String name, String description, Category category,
               String location, LocalDateTime dateTime, User reporter, boolean resolved) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.location = location;
        this.dateTime = dateTime;
        this.reporter = reporter;
        this.resolved = resolved;
    }

    public abstract String getSummary();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}
