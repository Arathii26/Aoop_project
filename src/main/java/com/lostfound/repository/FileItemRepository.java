package com.lostfound.repository;

import com.lostfound.model.Item;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileItemRepository implements ItemRepository {
    private static final String DEFAULT_FILE_PATH = "data/items.ser";
    private final String filePath;
    private final Map<Integer, Item> storage;

    public FileItemRepository() {
        this(DEFAULT_FILE_PATH);
    }

    public FileItemRepository(String filePath) {
        this.filePath = filePath;
        this.storage = new HashMap<>();
        load();
    }

    @Override
    public Item save(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        storage.put(item.getId(), item);
        persist();
        return item;
    }

    @Override
    public Item findById(int id) {
        return storage.get(id);
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(storage.values());
    }

    private void load() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0L) {
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            if (obj instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() instanceof Integer key && entry.getValue() instanceof Item item) {
                        storage.put(key, item);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            storage.clear();
            file.delete();
        }
    }

    private void persist() {
        File parent = new File(filePath).getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(storage);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save repository data to file: " + filePath, e);
        }
    }
}
