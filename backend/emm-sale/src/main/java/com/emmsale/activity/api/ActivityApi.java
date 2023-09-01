package com.emmsale.activity.api;

import com.emmsale.activity.application.ActivityCommandService;
import com.emmsale.activity.application.ActivityQueryService;
import com.emmsale.activity.application.dto.ActivityAddRequest;
import com.emmsale.activity.application.dto.ActivityResponse;
import com.emmsale.activity.application.dto.ActivityResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityApi {

  private final ActivityQueryService activityQueryService;
  private final ActivityCommandService activityCommandService;

  @GetMapping
  public ResponseEntity<List<ActivityResponses>> findAll() {
    return ResponseEntity.ok(activityQueryService.findAll());
  }

  @PostMapping
  public ResponseEntity<ActivityResponse> addActivity(
      @RequestBody final ActivityAddRequest request) {
    return ResponseEntity.ok(activityCommandService.addActivity(request));
  }
}

