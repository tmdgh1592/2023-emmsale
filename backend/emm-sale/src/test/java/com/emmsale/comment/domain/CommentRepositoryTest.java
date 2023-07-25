package com.emmsale.comment.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.emmsale.event.domain.Event;
import com.emmsale.event.domain.repository.EventRepository;
import com.emmsale.member.domain.Member;
import com.emmsale.member.domain.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Sql("/data.sql")
class CommentRepositoryTest {

  @Autowired
  private CommentRepository commentRepository;
  @Autowired
  private EventRepository eventRepository;
  @Autowired
  private MemberRepository memberRepository;

  @Test
  @DisplayName("findByEventId() : 행사에 존재하는 모든 댓글들을 조회할 수 있다.")
  void test_findByEventId() throws Exception {
    //given
    final Event event1 = eventRepository.save(new Event(
        "event", "location",
        LocalDateTime.now(), LocalDateTime.now(),
        "url", null
    ));

    final Event event2 = eventRepository.save(new Event(
        "event", "location",
        LocalDateTime.now(), LocalDateTime.now(),
        "url", null
    ));

    final Member member = memberRepository.findById(1L).get();

    commentRepository.save(
        new Comment(event1, null, member, "부모댓글2")
    );

    commentRepository.save(
        new Comment(event1, null, member, "부모댓글1")
    );

    final Comment savedComment = commentRepository.save(
        new Comment(event2, null, member, "부모댓글1")
    );

    final long eventId = 2L;

    //when
    final List<Comment> savedComments = commentRepository.findByEventId(eventId);

    //then
    assertAll(
        () -> Assertions.assertEquals(1, savedComments.size()),
        () -> assertEquals(savedComment.getId(), savedComments.get(0).getId())
    );
  }
}
