package com.lostfound.model;

import com.lostfound.enums.ClaimStatus;

public class Claim {
    private int id;
    private LostItem lostItem;
    private FoundItem foundItem;
    private User claimant;
    private ClaimStatus status;
    private String remarks;

    public Claim() {
        this.status = ClaimStatus.PENDING;
    }

    public Claim(int id, LostItem lostItem, FoundItem foundItem, User claimant, ClaimStatus status, String remarks) {
        this.id = id;
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.claimant = claimant;
        this.status = status;
        this.remarks = remarks;
    }

    public void approve() {
        if (this.status == ClaimStatus.APPROVED) {
            throw new IllegalStateException("Claim is already approved.");
        }
        if (this.status == ClaimStatus.REJECTED) {
            throw new IllegalStateException("Claim cannot be approved after rejection.");
        }
        this.status = ClaimStatus.APPROVED;
    }

    public void reject() {
        if (this.status == ClaimStatus.APPROVED) {
            throw new IllegalStateException("Claim cannot be rejected after approval.");
        }
        if (this.status == ClaimStatus.REJECTED) {
            throw new IllegalStateException("Claim is already rejected.");
        }
        this.status = ClaimStatus.REJECTED;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LostItem getLostItem() {
        return lostItem;
    }

    public void setLostItem(LostItem lostItem) {
        this.lostItem = lostItem;
    }

    public FoundItem getFoundItem() {
        return foundItem;
    }

    public void setFoundItem(FoundItem foundItem) {
        this.foundItem = foundItem;
    }

    public User getClaimant() {
        return claimant;
    }

    public void setClaimant(User claimant) {
        this.claimant = claimant;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
