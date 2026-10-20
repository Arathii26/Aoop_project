
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class ItemController {

    private final ItemService itemService;

    public ItemController() {
        DataStore dataStore = DataStore.getInstance();

        ItemRepository repository = dataStore.getItemRepository();

        if (repository == null) {
            repository = new FileItemRepository("data/items.ser");
            dataStore.setItemRepository(repository);
        }

        this.itemService = new ItemService(repository);
    }

    // Homepage: GET http://localhost:8080/
    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.ok(
                "Lost and Found Management API is running!"
        );
    }

    // Report a lost item
    @PostMapping("/lost")
    public ResponseEntity<?> reportLost(
            @RequestBody LostItemRequest request) {

        ResponseEntity<String> validationError =
                validateRequest(
                        request.name,
                        request.category,
                        request.location,
                        request.dateTime
                );

        if (validationError != null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(validationError.getBody()));
        }

        try {
            Student reporter = new Student(
                    request.reporterId,
                    request.reporterName,
                    request.reporterEmail,
                    request.regNo
            );

            LostItem lostItem = new LostItem(
                    0,
                    request.name.trim(),
                    request.description,
                    Category.valueOf(request.category.trim()),
                    request.location.trim(),
                    LocalDateTime.parse(request.dateTime.trim()),
                    reporter,
                    false
            );

            LostItem saved = itemService.reportLost(lostItem);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ItemResponse.from(saved));

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(
                            "Invalid dateTime format. Use yyyy-MM-ddTHH:mm:ss."
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(
                            "Invalid category. Please use a valid category."
                    ));
        }
    }

    // Report a found item
    @PostMapping("/found")
    public ResponseEntity<?> reportFound(
            @RequestBody FoundItemRequest request) {

        ResponseEntity<String> validationError =
                validateRequest(
                        request.name,
                        request.category,
                        request.location,
                        request.dateTime
                );

        if (validationError != null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(validationError.getBody()));
        }

        try {
            Student reporter = new Student(
                    request.reporterId,
                    request.reporterName,
                    request.reporterEmail,
                    request.regNo
            );

            FoundItem foundItem = new FoundItem(
                    0,
                    request.name.trim(),
                    request.description,
                    Category.valueOf(request.category.trim()),
                    request.location.trim(),
                    LocalDateTime.parse(request.dateTime.trim()),
                    reporter,
                    false,
                    LocalDateTime.now()
            );

            FoundItem saved = itemService.reportFound(foundItem);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ItemResponse.from(saved));

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(
                            "Invalid dateTime format. Use yyyy-MM-ddTHH:mm:ss."
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(
                            "Invalid category. Please use a valid category."
                    ));
        }
    }

    // Validate the required item fields
    private ResponseEntity<String> validateRequest(
            String name,
            String category,
            String location,
            String dateTime) {

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Item name is required.");
        }

        if (location == null || location.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Item location is required.");
        }

        if (category == null || category.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Item category is required.");
        }

        if (dateTime == null || dateTime.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Item date and time is required.");
        }

        return null;
    }

    // Find matches for an item
    @GetMapping("/matches/{id}")
    public ResponseEntity<List<MatchResultResponse>> getMatches(
            @PathVariable int id) {

        List<MatchResult> results = itemService.getMatches(id);

        List<MatchResultResponse> response = new ArrayList<>();

        for (MatchResult result : results) {
            response.add(
                    new MatchResultResponse(
                            result.getLostItem().getId(),
                            result.getFoundItem().getId(),
                            result.getScore()
                    )
            );
        }

        return ResponseEntity.ok(response);
    }

    // Request class for lost items
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

    // Request class for found items
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

    // Response class for item details
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

            response.reporterEmail = item.getReporter() != null
                    ? item.getReporter().getEmail()
                    : null;

            return response;
        }
    }

    // Response class for matching results
    public static class MatchResultResponse {
        public int lostItemId;
        public int foundItemId;
        public int score;

        public MatchResultResponse(
                int lostItemId,
                int foundItemId,
                int score) {

            this.lostItemId = lostItemId;
            this.foundItemId = foundItemId;
            this.score = score;
        }
    }

    // Error response class
    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
