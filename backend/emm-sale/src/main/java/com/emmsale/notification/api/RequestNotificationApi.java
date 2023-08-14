package com.emmsale.notification.api;

import com.emmsale.member.domain.Member;
import com.emmsale.notification.application.RequestNotificationCommandService;
import com.emmsale.notification.application.RequestNotificationQueryService;
import com.emmsale.notification.application.dto.RequestNotificationExistedRequest;
import com.emmsale.notification.application.dto.RequestNotificationModifyRequest;
import com.emmsale.notification.application.dto.RequestNotificationRequest;
import com.emmsale.notification.application.dto.RequestNotificationResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RequestNotificationApi {

  private final RequestNotificationCommandService requestNotificationCommandService;
  private final RequestNotificationQueryService requestNotificationQueryService;

  @PostMapping("/request-notifications")
  @ResponseStatus(HttpStatus.CREATED)
  public RequestNotificationResponse create(
      @RequestBody final RequestNotificationRequest requestNotificationRequest) {
    return requestNotificationCommandService.create(requestNotificationRequest);
  }

  @PatchMapping("/request-notifications/{request-notification-id}/status")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void modify(
      @RequestBody final RequestNotificationModifyRequest requestNotificationModifyRequest,
      @PathVariable("request-notification-id") final Long notificationId
  ) {
    requestNotificationCommandService.modify(requestNotificationModifyRequest, notificationId);
  }

  @PatchMapping("/request-notifications/{request-notification-id}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void read(
      @PathVariable("request-notification-id") final Long notificationId,
      final Member member
  ) {
    requestNotificationCommandService.read(notificationId, member);
  }

  @GetMapping("/request-notifications/{request-notification-id}")
  public RequestNotificationResponse find(
      @PathVariable("request-notification-id") final Long notificationId
  ) {
    return requestNotificationQueryService.findNotificationBy(notificationId);
  }

  @GetMapping("/request-notifications")
  @ResponseStatus(HttpStatus.OK)
  public List<RequestNotificationResponse> findAll(final Member member) {
    return requestNotificationCommandService.findAllNotifications(member);
  }

  @DeleteMapping("/request-notifications/{request-notification-id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      final Member member,
      @PathVariable("request-notification-id") final Long notificationId
  ) {
    requestNotificationCommandService.delete(member, notificationId);
  }

  @GetMapping("/request-notifications/existed")
  public boolean isAlreadyExisted(
      final Member member,
      final RequestNotificationExistedRequest existedRequest
  ) {
    return requestNotificationCommandService.isAlreadyExisted(member, existedRequest);
  }
}
