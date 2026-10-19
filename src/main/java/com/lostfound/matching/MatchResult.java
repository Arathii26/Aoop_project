package com.lostfound.matching;

import com.lostfound.model.FoundItem;
import com.lostfound.model.LostItem;

public class MatchResult implements Comparable<MatchResult> {
    private LostItem lostItem;
    private FoundItem foundItem;
    private int score;

    public MatchResult(LostItem lostItem, FoundItem foundItem, int score) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.score = score;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(MatchResult other) {
        return Integer.compare(other.score, this.score);
    }

    public static java.util.Comparator<MatchResult> byScoreDescending() {
        return (left, right) -> Integer.compare(right.getScore(), left.getScore());
    }
}
