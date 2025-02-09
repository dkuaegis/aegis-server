package aegis.server.domain.timetable.controller;

import aegis.server.domain.timetable.dto.request.TimetableCreateRequest;
import aegis.server.domain.timetable.service.TimetableService;
import aegis.server.global.security.annotation.LoginUser;
import aegis.server.global.security.oidc.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/timetables")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping
    public ResponseEntity<Void> createTimetable(
            @LoginUser UserDetails userDetails,
            @RequestBody TimetableCreateRequest request
    ) {
        timetableService.createOrUpdateTimetable(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
