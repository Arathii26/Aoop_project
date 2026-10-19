package com.lostfound.service;

import com.lostfound.enums.ClaimStatus;
import com.lostfound.exception.InvalidClaimException;
import com.lostfound.exception.ItemNotFoundException;
import com.lostfound.model.*;
import com.lostfound.notification.AsyncNotificationService;
import com.lostfound.notification.Notifier;
import com.lostfound.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;

public class ClaimService {
    private final ItemRepository itemRepository;
    private final AsyncNotificationService asyncNotificationService;
    private final Notifier notifier;
    private final List<Claim> claims;

    public ClaimService(ItemRepository itemRepository, AsyncNotificationService asyncNotificationService, Notifier notifier) {
        this.itemRepository = itemRepository;
        this.asyncNotificationService = asyncNotificationService;
        this.notifier = notifier;
        this.claims = new ArrayList<>();
    }

    public Claim createClaim(LostItem lostItem, FoundItem foundItem, User claimant, String remarks) {
        validateClaimInputs(lostItem, foundItem, claimant);
        if (lostItem.isResolved() || foundItem.isResolved()) {
            throw new InvalidClaimException("Cannot create a claim for already resolved items.");
        }
        if (hasDuplicateActiveClaim(lostItem, foundItem, claimant)) {
            throw new InvalidClaimException("Duplicate active claim exists for the same item pair.");
        }
        Claim claim = new Claim(nextId(), lostItem, foundItem, claimant, ClaimStatus.PENDING, remarks);
        claims.add(claim);
        return claim;
    }

    public Claim approveClaim(int claimId, User admin) {
        if (!(admin instanceof Admin)) {
            throw new InvalidClaimException("Only an admin may approve claims.");
        }
        Claim claim = findClaimById(claimId);
        if (claim.getStatus() == ClaimStatus.APPROVED) {
            throw new InvalidClaimException("Claim already approved.");
        }
        if (claim.getStatus() == ClaimStatus.REJECTED) {
            throw new InvalidClaimException("Claim cannot be approved after rejection.");
        }
        if (claim.getLostItem() == null || claim.getFoundItem() == null) {
            throw new InvalidClaimException("Claim references invalid items.");
        }
        claim.approve();
        claim.getLostItem().setResolved(true);
        claim.getFoundItem().setResolved(true);
        notifier.notify(claim.getClaimant(), "Your claim for " + claim.getLostItem().getName() + " was approved.");
        asyncNotificationService.sendAsync(claim.getClaimant(), "Claim approved for " + claim.getLostItem().getName(), notifier);
        return claim;
    }

    public Claim rejectClaim(int claimId, User admin) {
        if (!(admin instanceof Admin)) {
            throw new InvalidClaimException("Only an admin may reject claims.");
        }
        Claim claim = findClaimById(claimId);
        if (claim.getStatus() == ClaimStatus.REJECTED) {
            throw new InvalidClaimException("Claim already rejected.");
        }
        claim.reject();
        notifier.notify(claim.getClaimant(), "Your claim for " + claim.getLostItem().getName() + " was rejected.");
        asyncNotificationService.sendAsync(claim.getClaimant(), "Claim rejected for " + claim.getLostItem().getName(), notifier);
        return claim;
    }

    private void validateClaimInputs(LostItem lostItem, FoundItem foundItem, User claimant) {
        if (lostItem == null || foundItem == null) {
            throw new InvalidClaimException("Lost and found item are required.");
        }
        if (claimant == null) {
            throw new InvalidClaimException("Claimant is required.");
        }
        if (itemRepository.findById(lostItem.getId()) == null) {
            throw new ItemNotFoundException("Lost item not found: " + lostItem.getId());
        }
        if (itemRepository.findById(foundItem.getId()) == null) {
            throw new ItemNotFoundException("Found item not found: " + foundItem.getId());
        }
    }

    private boolean hasDuplicateActiveClaim(LostItem lostItem, FoundItem foundItem, User claimant) {
        for (Claim c : claims) {
            if (c.getStatus() == ClaimStatus.PENDING && c.getLostItem().getId() == lostItem.getId()
                    && c.getFoundItem().getId() == foundItem.getId() && c.getClaimant().getId() == claimant.getId()) {
                return true;
            }
        }
        return false;
    }

    private Claim findClaimById(int claimId) {
        for (Claim claim : claims) {
            if (claim.getId() == claimId) {
                return claim;
            }
        }
        throw new ItemNotFoundException("Claim with id " + claimId + " was not found.");
    }

    private int nextId() {
        return claims.stream().mapToInt(Claim::getId).max().orElse(0) + 1;
    }
}
