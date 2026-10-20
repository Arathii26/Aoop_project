package com.lostfound;

import com.lostfound.enums.Category;
import com.lostfound.exception.InvalidClaimException;
import com.lostfound.model.*;
import com.lostfound.notification.AsyncNotificationService;
import com.lostfound.notification.ConsoleNotifier;
import com.lostfound.notification.Notifier;
import com.lostfound.notification.NotifierFactory;
import com.lostfound.repository.FileItemRepository;
import com.lostfound.service.ClaimService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationClaimServiceTest {
    @Test
    void notifierFactoryReturnsCorrectType() {
        Notifier console = NotifierFactory.getNotifier("CONSOLE");
        assertTrue(console instanceof ConsoleNotifier);
    }

    @Test
    void asyncNotificationCompletesAndCreatesLog() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        AsyncNotificationService service = new AsyncNotificationService(executorService, "target/test-data/notifications.log");
        Student student = new Student(1, "Asha", "asha@example.com", "R1");
        Future<Boolean> result = service.sendAsync(student, "Hello", new ConsoleNotifier());
        assertTrue(result.get());
        File logFile = new File("target/test-data/notifications.log");
        assertTrue(logFile.exists());
        service.shutdown();
    }

    @Test
    void unauthorizedApprovalFails() {
        FileItemRepository repository = new FileItemRepository("target/test-data/claim-auth.ser");
        Student student = new Student(1, "Asha", "asha@example.com", "R1");
        Student other = new Student(2, "John", "john@example.com", "R2");
        LostItem lost = new LostItem(1, "Calculator", "Black", Category.CALCULATOR, "Block A", LocalDateTime.now(), student, false);
        FoundItem found = new FoundItem(2, "Calculator", "Black", Category.CALCULATOR, "Block A", LocalDateTime.now(), other, false, LocalDateTime.now());
        repository.save(lost);
        repository.save(found);

        ClaimService claimService = new ClaimService(repository, new AsyncNotificationService(Executors.newFixedThreadPool(2), "target/test-data/claim.log"), new ConsoleNotifier());
        Claim claim = claimService.createClaim(lost, found, student, "Please verify");
        assertThrows(InvalidClaimException.class, () -> claimService.approveClaim(claim.getId(), student));
    }

    @Test
    void duplicateActiveClaimsAreRejected() {
        FileItemRepository repository = new FileItemRepository("target/test-data/duplicate-claims.ser");
        Student student = new Student(1, "Asha", "asha@example.com", "R1");
        Student other = new Student(2, "John", "john@example.com", "R2");

        LostItem lost = new LostItem(1, "Calculator", "Black", Category.CALCULATOR, "Block A", LocalDateTime.now(), student, false);
        FoundItem found = new FoundItem(2, "Calculator", "Black", Category.CALCULATOR, "Block A", LocalDateTime.now(), other, false, LocalDateTime.now());
        repository.save(lost);
        repository.save(found);

        ClaimService claimService = new ClaimService(repository, new AsyncNotificationService(Executors.newFixedThreadPool(2), "target/test-data/duplicate.log"), new ConsoleNotifier());
        claimService.createClaim(lost, found, student, "Claim 1");
        assertThrows(InvalidClaimException.class, () -> claimService.createClaim(lost, found, student, "Claim 2"));
    }

    @Test
    void approvalResolvesBothItemsAndRejectionDoesNot() {
        FileItemRepository repository = new FileItemRepository("target/test-data/resolve-claim.ser");
        Student student = new Student(1, "Asha", "asha@example.com", "R1");
        Student other = new Student(2, "John", "john@example.com", "R2");
        LostItem lost = new LostItem(1, "Calculator", "Black", Category.CALCULATOR, "Block A", LocalDateTime.now(), student, false);
        FoundItem found = new FoundItem(2, "Calculator", "Black", Category.CALCULATOR, "Block A", LocalDateTime.now(), other, false, LocalDateTime.now());
        repository.save(lost);
        repository.save(found);

        ClaimService claimService = new ClaimService(repository, new AsyncNotificationService(Executors.newFixedThreadPool(2), "target/test-data/resolve.log"), new ConsoleNotifier());
        Claim claim = claimService.createClaim(lost, found, student, "Please verify");
        Admin admin = new Admin(3, "Harish", "harish@campus.edu", "ADMIN-1");
        claimService.approveClaim(claim.getId(), admin);
        assertTrue(lost.isResolved());
        assertTrue(found.isResolved());

        LostItem lost2 = new LostItem(3, "Umbrella", "Blue", Category.UMBRELLA, "Library", LocalDateTime.now(), student, false);
        FoundItem found2 = new FoundItem(4, "Umbrella", "Blue", Category.UMBRELLA, "Library", LocalDateTime.now(), other, false, LocalDateTime.now());
        repository.save(lost2);
        repository.save(found2);
        Claim claim2 = claimService.createClaim(lost2, found2, student, "Reject this");
        claimService.rejectClaim(claim2.getId(), admin);
        assertFalse(lost2.isResolved());
        assertFalse(found2.isResolved());
    }
}
