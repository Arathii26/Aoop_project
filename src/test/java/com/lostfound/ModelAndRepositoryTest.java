package com.lostfound;

import com.lostfound.enums.Category;
import com.lostfound.enums.ClaimStatus;
import com.lostfound.model.Admin;
import com.lostfound.model.Claim;
import com.lostfound.model.FoundItem;
import com.lostfound.model.Item;
import com.lostfound.model.LostItem;
import com.lostfound.model.Student;
import com.lostfound.repository.DataStore;
import com.lostfound.repository.FileItemRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ModelAndRepositoryTest {
    @Test
    void constructorsAndGettersWork() {
        Student student = new Student(1, "Asha", "asha@example.com", "2021CS001");
        assertEquals("STUDENT", student.getRole());
        assertEquals("2021CS001", student.getRegNo());

        Admin admin = new Admin(2, "Ravi", "ravi@campus.edu", "STAFF-101");
        assertEquals("ADMIN", admin.getRole());
        assertEquals("STAFF-101", admin.getStaffId());
    }

    @Test
    void lostAndFoundGetSummaryWorks() {
        Student s = new Student(1, "Asha", "a@x.com", "R1");
        LostItem lost = new LostItem(10, "Calculator", "Texas Instruments", Category.CALCULATOR, "Block A", LocalDateTime.of(2026, 1, 2, 14, 0), s, false);
        FoundItem found = new FoundItem(11, "Calculator", "Blue cover", Category.CALCULATOR, "Block A", LocalDateTime.of(2026, 1, 2, 15, 0), s, false, LocalDateTime.of(2026, 1, 2, 16, 0));

        assertTrue(lost.getSummary().contains("Calculator"));
        assertTrue(found.getSummary().contains("Found item"));
    }

    @Test
    void claimApproveRejectTransitionsWork() {
        Student student = new Student(1, "Asha", "a@x.com", "R1");
        LostItem lost = new LostItem(10, "Calculator", "Black", Category.CALCULATOR, "Block A", LocalDateTime.now(), student, false);
        FoundItem found = new FoundItem(11, "Calculator", "Black", Category.CALCULATOR, "Block A", LocalDateTime.now(), student, false, LocalDateTime.now());
        Claim claim = new Claim(1, lost, found, student, ClaimStatus.PENDING, "Please verify");

        claim.approve();
        assertEquals(ClaimStatus.APPROVED, claim.getStatus());

        IllegalStateException ex = assertThrows(IllegalStateException.class, claim::approve);
        assertTrue(ex.getMessage().contains("already approved"));

        Claim rejected = new Claim(2, lost, found, student, ClaimStatus.PENDING, "No");
        rejected.reject();
        assertEquals(ClaimStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void fileRepositorySavesAndReloads() {
        FileItemRepository repository = new FileItemRepository("target/test-data/items.ser");
        Student s = new Student(7, "Asha", "a@x.com", "R1");
        LostItem item = new LostItem(1, "Umbrella", "Blue", Category.UMBRELLA, "Library", LocalDateTime.now(), s, false);

        repository.save(item);
        Item loaded = repository.findById(1);
        assertNotNull(loaded);
        assertEquals("Umbrella", loaded.getName());
    }

    @Test
    void missingItemThrowsItemNotFoundException() {
        FileItemRepository repository = new FileItemRepository("target/test-data/missing.ser");
        assertNull(repository.findById(999));
    }

    @Test
    void singletonReturnsSameInstance() {
        DataStore first = DataStore.getInstance();
        DataStore second = DataStore.getInstance();
        assertSame(first, second);
    }
}
