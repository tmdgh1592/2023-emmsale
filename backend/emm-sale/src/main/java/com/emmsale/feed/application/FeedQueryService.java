package com.emmsale.feed.application;

import com.emmsale.event.domain.repository.EventRepository;
import com.emmsale.event.exception.EventException;
import com.emmsale.event.exception.EventExceptionType;
import com.emmsale.feed.application.dto.FeedDetailResponse;
import com.emmsale.feed.application.dto.FeedListResponse;
import com.emmsale.feed.domain.Feed;
import com.emmsale.feed.domain.repository.FeedRepository;
import com.emmsale.feed.exception.FeedException;
import com.emmsale.feed.exception.FeedExceptionType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FeedQueryService {

  private final FeedRepository feedRepository;
  private final EventRepository eventRepository;

  public FeedListResponse findAllFeeds(final Long eventId) {
    validateEvent(eventId);

    final List<Feed> feeds = feedRepository.findAllByEventIdAndNotDeleted(eventId);

    return FeedListResponse.from(eventId, feeds);
  }

  public FeedDetailResponse findFeed(final Long id) {
    final Feed feed = feedRepository.findById(id)
        .orElseThrow(() -> new FeedException(FeedExceptionType.NOT_FOUND_FEED));

    validateDeletedFeed(feed);

    return FeedDetailResponse.from(feed);
  }

  private void validateEvent(final Long eventId) {
    if (!eventRepository.existsById(eventId)) {
      throw new EventException(EventExceptionType.NOT_FOUND_EVENT);
    }
  }

  private void validateDeletedFeed(final Feed feed) {
    if (feed.isDeleted()) {
      throw new FeedException(FeedExceptionType.FORBIDDEN_DELETED_FEED);
    }
  }
}
