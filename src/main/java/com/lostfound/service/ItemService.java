package com.lostfound.service;

import com.lostfound.exception.ItemNotFoundException;
import com.lostfound.matching.MatchEngine;
import com.lostfound.matching.MatchResult;
import com.lostfound.model.FoundItem;
import com.lostfound.model.Item;
import com.lostfound.model.LostItem;
import com.lostfound.notification.ConsoleNotifier;
import com.lostfound.notification.Notifier;
import com.lostfound.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;

public class ItemService {
    private final ItemRepository repository;
    private final MatchEngine matchEngine;
    private final Notifier notifier;

    public ItemService(ItemRepository repository) {
        this(repository, new MatchEngine(50), new ConsoleNotifier());
    }

    public ItemService(ItemRepository repository, MatchEngine matchEngine, Notifier notifier) {
        this.repository = repository;
        this.matchEngine = matchEngine;
        this.notifier = notifier;
    }

    public LostItem reportLost(LostItem item) {
        validateItem(item);
        item.setId(nextId());
        repository.save(item);
        notifyMatchesForNewLostItem(item);
        return item;
    }

    public FoundItem reportFound(FoundItem item) {
        validateItem(item);
        item.setId(nextId());
        repository.save(item);
        notifyMatchesForNewFoundItem(item);
        return item;
    }

    public List<MatchResult> getMatches(int itemId) {
        Item item = repository.findById(itemId);
        if (item == null) {
            throw new ItemNotFoundException("Item with id " + itemId + " was not found.");
        }

        if (item instanceof LostItem lostItem) {
            return matchEngine.findMatches(lostItem, findFoundItems());
        }
        if (item instanceof FoundItem foundItem) {
            List<MatchResult> results = new ArrayList<>();
            for (LostItem lostItem : findLostItems()) {
                int score = matchEngine.calculateScore(lostItem, foundItem);
                if (score >= 50) {
                    results.add(new MatchResult(lostItem, foundItem, score));
                }
            }
            results.sort(MatchResult.byScoreDescending());
            return results.stream().limit(5).toList();
        }
        throw new IllegalArgumentException("Unsupported item type");
    }

    private void notifyMatchesForNewLostItem(LostItem lostItem) {
        List<FoundItem> foundItems = findFoundItems();
        List<MatchResult> results = matchEngine.findMatches(lostItem, foundItems);
        for (MatchResult result : results) {
            if (result.getFoundItem().getReporter() != null) {
                notifier.notify(result.getFoundItem().getReporter(), "Potential match found for lost item: " + lostItem.getName());
            }
        }
    }

    private void notifyMatchesForNewFoundItem(FoundItem foundItem) {
        List<LostItem> lostItems = findLostItems();
        for (LostItem lostItem : lostItems) {
            int score = matchEngine.calculateScore(lostItem, foundItem);
            if (score >= 50) {
                if (lostItem.getReporter() != null) {
                    notifier.notify(lostItem.getReporter(), "Potential match found for found item: " + foundItem.getName());
                }
            }
        }
    }

    private void validateItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (item.getLocation() == null || item.getLocation().isBlank()) {
            throw new IllegalArgumentException("Item location is required.");
        }
        if (item.getReporter() == null) {
            throw new IllegalArgumentException("Reporter is required.");
        }
    }

    private int nextId() {
        return repository.findAll().stream().mapToInt(Item::getId).max().orElse(0) + 1;
    }

    private List<FoundItem> findFoundItems() {
        List<FoundItem> result = new ArrayList<>();
        for (Item item : repository.findAll()) {
            if (item instanceof FoundItem foundItem && !foundItem.isResolved()) {
                result.add(foundItem);
            }
        }
        return result;
    }

    private List<LostItem> findLostItems() {
        List<LostItem> result = new ArrayList<>();
        for (Item item : repository.findAll()) {
            if (item instanceof LostItem lostItem && !lostItem.isResolved()) {
                result.add(lostItem);
            }
        }
        return result;
    }
}
