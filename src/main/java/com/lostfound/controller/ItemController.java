package com.lostfound.controller;

import com.lostfound.enums.Category;
import com.lostfound.matching.MatchResult;
import com.lostfound.model.*;
import com.lostfound.repository.DataStore;
import com.lostfound.repository.FileItemRepository;
import com.lostfound.repository.ItemRepository;
import com.lostfound.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class ItemController {
    private final ItemService itemService;

    public ItemController() {
        DataStore dataStore = DataStore.getInstance();
        ItemRepository repository = new FileItemRepository("data/items.ser");
        dataStore.setItemRepository(repository);
        this.itemService = new ItemService(repository);
    }

    @PostMapping("/lost")
    public ResponseEntity<ItemResponse> reportLost(@RequestBody LostItemRequest request) {
        Student reporter = new Student(request.reporterId, request.reporterName, request.reporterEmail, request.regNo);
        LostItem lostItem = new LostItem(0, request.name, request.description, Category.valueOf(request.category), request.location, LocalDateTime.parse(request.dateTime), reporter, false);
        LostItem saved = itemService.reportLost(lostItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.from(saved));
    }

    @PostMapping("/found")
    public ResponseEntity<ItemResponse> reportFound(@RequestBody FoundItemRequest request) {
        Student reporter = new Student(request.reporterId, request.reporterName, request.reporterEmail, request.regNo);
        FoundItem foundItem = new FoundItem(0, request.name, request.description, Category.valueOf(request.category), request.location, LocalDateTime.parse(request.dateTime), reporter, false, LocalDateTime.now());
        FoundItem saved = itemService.reportFound(foundItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.from(saved));
    }

    @GetMapping("/matches/{id}")
    public ResponseEntity<List<MatchResultResponse>> getMatches(@PathVariable int id) {
        List<MatchResult> results = itemService.getMatches(id);
        List<MatchResultResponse> response = new ArrayList<>();
        for (MatchResult result : results) {
            response.add(new MatchResultResponse(result.getLostItem().getId(), result.getFoundItem().getId(), result.getScore()));
        }
        return ResponseEntity.ok(response);
    }

    public static class LostItemRequest {
        public int reporterId;
        public String reporterName;
        public String reporterEmail;
        public String regNo;
        public String name;
        public String description;
        public String category;
        public String location;
        public String dateTime;
    }

    public static class FoundItemRequest {
        public int reporterId;
        public String reporterName;
        public String reporterEmail;
        public String regNo;
        public String name;
        public String description;
        public String category;
        public String location;
        public String dateTime;
    }

    public static class ItemResponse {
        public int id;
        public String name;
        public String category;
        public String location;
        public String reporterEmail;

        public static ItemResponse from(Item item) {
            ItemResponse response = new ItemResponse();
            response.id = item.getId();
            response.name = item.getName();
            response.category = item.getCategory().name();
            response.location = item.getLocation();
            response.reporterEmail = item.getReporter() != null ? item.getReporter().getEmail() : null;
            return response;
        }
    }

    public static class MatchResultResponse {
        public int lostItemId;
        public int foundItemId;
        public int score;

        public MatchResultResponse(int lostItemId, int foundItemId, int score) {
            this.lostItemId = lostItemId;
            this.foundItemId = foundItemId;
            this.score = score;
        }
    }
}
