package com.lostfound.repository;

import com.lostfound.model.Item;

import java.util.List;

public interface ItemRepository {
    Item save(Item item);
    Item findById(int id);
    List<Item> findAll();
}
