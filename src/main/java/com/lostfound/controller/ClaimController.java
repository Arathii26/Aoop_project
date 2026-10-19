package com.lostfound.controller;

import com.lostfound.enums.Category;
import com.lostfound.exception.InvalidClaimException;
import com.lostfound.model.Admin;
import com.lostfound.model.Claim;
import com.lostfound.model.FoundItem;
import com.lostfound.model.LostItem;
import com.lostfound.model.Student;
import com.lostfound.notification.AsyncNotificationService;
import com.lostfound.notification.ConsoleNotifier;
import com.lostfound.repository.DataStore;
import com.lostfound.repository.FileItemRepository;
import com.lostfound.repository.ItemRepository;
import com.lostfound.service.ClaimService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/claims")
public class ClaimController {
    private final ClaimService claimService;

    public ClaimController() {
        DataStore dataStore = DataStore.getInstance();
        ItemRepository repository = dataStore.getItemRepository() != null ? dataStore.getItemRepository() : new FileItemRepository("data/items.ser");
        this.claimService = new ClaimService(repository, new AsyncNotificationService(), new ConsoleNotifier());
    }

    @PostMapping
    public ResponseEntity<ClaimResponse> createClaim(@RequestBody ClaimRequest request) {
        LostItem lostItem = new LostItem(request.lostItemId, "Lost item", "", Category.CALCULATOR, "Block A", LocalDateTime.now(), new Student(), false);
        FoundItem foundItem = new FoundItem(request.foundItemId, "Found item", "", Category.CALCULATOR, "Block A", LocalDateTime.now(), new Student(), false, LocalDateTime.now());
        Student claimant = new Student(request.claimantId, request.claimantName, request.claimantEmail, request.regNo);
        Claim claim = claimService.createClaim(lostItem, foundItem, claimant, request.remarks);
        return ResponseEntity.status(HttpStatus.CREATED).body(ClaimResponse.from(claim));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ClaimResponse> approve(@PathVariable int id, @RequestHeader(value = "X-Admin-Id", required = false) String adminId) {
        Admin admin = new Admin(1, "Admin", "admin@campus.edu", "ADM001");
        if (adminId == null || !adminId.equals("ADM001")) {
            throw new InvalidClaimException("Unauthorized action.");
        }
        return ResponseEntity.ok(ClaimResponse.from(claimService.approveClaim(id, admin)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ClaimResponse> reject(@PathVariable int id, @RequestHeader(value = "X-Admin-Id", required = false) String adminId) {
        Admin admin = new Admin(1, "Admin", "admin@campus.edu", "ADM001");
        if (adminId == null || !adminId.equals("ADM001")) {
            throw new InvalidClaimException("Unauthorized action.");
        }
        return ResponseEntity.ok(ClaimResponse.from(claimService.rejectClaim(id, admin)));
    }

    public static class ClaimRequest {
        public int lostItemId;
        public int foundItemId;
        public int claimantId;
        public String claimantName;
        public String claimantEmail;
        public String regNo;
        public String remarks;
    }

    public static class ClaimResponse {
        public int id;
        public int lostItemId;
        public int foundItemId;
        public int claimantId;
        public String status;

        public static ClaimResponse from(Claim claim) {
            ClaimResponse response = new ClaimResponse();
            response.id = claim.getId();
            response.lostItemId = claim.getLostItem().getId();
            response.foundItemId = claim.getFoundItem().getId();
            response.claimantId = claim.getClaimant().getId();
            response.status = claim.getStatus().name();
            return response;
        }
    }
}
