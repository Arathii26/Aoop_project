
package com.lostfound;

import com.lostfound.controller.ItemController;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

public class ControllerTest {

    @Test
    void controllerCanCreateLostAndFoundResponses() {

        ItemController controller = new ItemController();

        ItemController.LostItemRequest lost =
                new ItemController.LostItemRequest();

        lost.reporterId = 1;
        lost.reporterName = "Asha";
        lost.reporterEmail = "asha@example.com";
        lost.regNo = "R1";
        lost.name = "Calculator";
        lost.description = "Black calculator";
        lost.category = "CALCULATOR";
        lost.location = "Block A";
        lost.dateTime = "2026-01-02T14:00:00";

        ItemController.FoundItemRequest found =
                new ItemController.FoundItemRequest();

        found.reporterId = 2;
        found.reporterName = "Kiran";
        found.reporterEmail = "kiran@example.com";
        found.regNo = "R2";
        found.name = "Calculator";
        found.description = "Black calculator";
        found.category = "CALCULATOR";
        found.location = "Block A";
        found.dateTime = "2026-01-02T15:00:00";

        ResponseEntity<?> lostResponse =
                controller.reportLost(lost);

        ResponseEntity<?> foundResponse =
                controller.reportFound(found);

        assertEquals(201, lostResponse.getStatusCode().value());
        assertEquals(201, foundResponse.getStatusCode().value());
    }
}
