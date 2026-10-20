package com.lostfound;

import com.lostfound.enums.Category;
import com.lostfound.matching.MatchEngine;
import com.lostfound.matching.MatchResult;
import com.lostfound.model.FoundItem;
import com.lostfound.model.LostItem;
import com.lostfound.model.Student;
import com.lostfound.notification.ConsoleNotifier;
import com.lostfound.service.ItemService;
import com.lostfound.repository.FileItemRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchEngineTest {
    @Test
    void calculatesKnownScores() {
        Student student = new Student(1, "Asha", "a@x.com", "R1");
        LostItem lost = new LostItem(1, "Calculator", "Black calculator with case", Category.CALCULATOR,
                "Block A", LocalDateTime.of(2026, 1, 2, 14, 0), student, false);
        FoundItem found = new FoundItem(2, "Calculator", "Black calculator", Category.CALCULATOR,
                "Block A", LocalDateTime.of(2026, 1, 2, 15, 0), student, false, LocalDateTime.of(2026, 1, 2, 16, 0));

        MatchEngine engine = new MatchEngine(50);
        int score = engine.calculateScore(lost, found);
        assertTrue(score >= 70);
    }

    @Test
    void matchingItemsRankAboveLessSimilar() {
        Student student = new Student(1, "Asha", "a@x.com", "R1");
        LostItem lost = new LostItem(1, "Calculator", "Scientific calculator", Category.CALCULATOR, "Block A", LocalDateTime.now(), student, false);
        FoundItem close = new FoundItem(2, "Calculator", "Scientific calculator", Category.CALCULATOR, "Block A", LocalDateTime.now().plusHours(1), student, false, LocalDateTime.now());
        FoundItem far = new FoundItem(3, "Umbrella", "Red umbrella", Category.UMBRELLA, "Hostel", LocalDateTime.now().plusHours(10), student, false, LocalDateTime.now());

        MatchEngine engine = new MatchEngine(20);
        List<MatchResult> results = engine.findMatches(lost, List.of(far, close));
        assertEquals(close.getId(), results.get(0).getFoundItem().getId());
    }

    @Test
    void itemsBelowThresholdAreExcluded() {
        Student student = new Student(1, "Asha", "a@x.com", "R1");
        LostItem lost = new LostItem(1, "Calculator", "Scientific calculator", Category.CALCULATOR, "Block A", LocalDateTime.now(), student, false);
        FoundItem found = new FoundItem(2, "Umbrella", "Red", Category.UMBRELLA, "Hostel", LocalDateTime.now().plusHours(30), student, false, LocalDateTime.now());

        MatchEngine engine = new MatchEngine(80);
        List<MatchResult> results = engine.findMatches(lost, List.of(found));
        assertTrue(results.isEmpty());
    }

    @Test
    void topFiveLimitWorks() {
        Student student = new Student(1, "Asha", "a@x.com", "R1");
        LostItem lost = new LostItem(1, "Calculator", "Scientific calculator", Category.CALCULATOR, "Block A", LocalDateTime.now(), student, false);
        MatchEngine engine = new MatchEngine(0);
        List<FoundItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            items.add(new FoundItem(i + 2, "Calculator", "Exact match " + i, Category.CALCULATOR, "Block A", LocalDateTime.now().plusHours(i), student, false, LocalDateTime.now()));
        }
        List<MatchResult> results = engine.findMatches(lost, items);
        assertTrue(results.size() <= 5);
    }

    @Test
    void reportingItemTriggersMatchingAndNotification() {
        FileItemRepository repository = new FileItemRepository("target/test-data/match-items.ser");
        Student studentA = new Student(10, "Asha", "asha@example.com", "R1");
        Student studentB = new Student(11, "Kiran", "kiran@example.com", "R2");

        LostItem lost = new LostItem(1, "Calculator", "Black calculator", Category.CALCULATOR, "Block A", LocalDateTime.of(2026, 2, 1, 14, 0), studentA, false);
        FoundItem found = new FoundItem(2, "Calculator", "Black calculator found", Category.CALCULATOR, "Block A", LocalDateTime.of(2026, 2, 1, 15, 0), studentB, false, LocalDateTime.of(2026, 2, 1, 16, 0));
        repository.save(lost);
        repository.save(found);

        ItemService service = new ItemService(repository, new MatchEngine(20), new ConsoleNotifier());
        List<MatchResult> matches = service.getMatches(1);
        assertFalse(matches.isEmpty());
    }

    @Test
    void matchingWorksInBothReportingDirections() {
        Student studentA = new Student(10, "Asha", "asha@example.com", "R1");
        Student studentB = new Student(11, "Kiran", "kiran@example.com", "R2");
        FileItemRepository repository = new FileItemRepository("target/test-data/both-directions.ser");
        LostItem lost = new LostItem(1, "Calculator", "Black calculator", Category.CALCULATOR, "Block A", LocalDateTime.of(2026, 2, 1, 14, 0), studentA, false);
        FoundItem found = new FoundItem(2, "Calculator", "Black calculator found", Category.CALCULATOR, "Block A", LocalDateTime.of(2026, 2, 1, 15, 0), studentB, false, LocalDateTime.of(2026, 2, 1, 16, 0));
        repository.save(lost);
        repository.save(found);

        ItemService service = new ItemService(repository, new MatchEngine(20), new ConsoleNotifier());
        assertFalse(service.getMatches(1).isEmpty());
        assertFalse(service.getMatches(2).isEmpty());
    }
}
