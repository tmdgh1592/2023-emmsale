package com.emmsale.report.application;


import static com.emmsale.event.EventFixture.eventFixture;

import com.emmsale.comment.domain.Comment;
import com.emmsale.comment.domain.CommentRepository;
import com.emmsale.event.domain.Event;
import com.emmsale.event.domain.repository.EventRepository;
import com.emmsale.helper.ServiceIntegrationTestHelper;
import com.emmsale.member.domain.Member;
import com.emmsale.member.domain.MemberRepository;
import com.emmsale.report.application.dto.ReportCreateRequest;
import com.emmsale.report.application.dto.ReportCreateResponse;
import com.emmsale.report.application.dto.ReportFindResponse;
import com.emmsale.report.domain.ReportType;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ReportQueryServiceTest extends ServiceIntegrationTestHelper {

  private static Long 신고자_ID;
  private static Long 신고_대상자_ID;
  @Autowired
  private ReportQueryService reportQueryService;
  @Autowired
  private ReportCommandService reportCommandService;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private EventRepository eventRepository;
  @Autowired
  private CommentRepository commentRepository;

  @BeforeEach
  void init() {
    final Event event = eventRepository.save(eventFixture());
    final Member 신고자 = memberRepository.findById(1L).get();
    final Member 신고_대상자 = memberRepository.findById(2L).get();
    신고자_ID = 신고자.getId();
    신고_대상자_ID = 신고_대상자.getId();
    commentRepository.save(Comment.createRoot(event, 신고_대상자, "상대방에게 불쾌감을 줄 수 있는 내용"));
    commentRepository.save(Comment.createRoot(event, 신고자, "그냥 댓글"));
  }

  @Test
  @DisplayName("모든 신고 목록을 조회할 수 있다.")
  void findReports() {
    // given
    final Long abusingContentId = 1L;
    final Member reporter = memberRepository.findById(신고자_ID).get();
    final ReportCreateRequest request = new ReportCreateRequest(신고자_ID, 신고_대상자_ID,
        ReportType.COMMENT,
        abusingContentId);
    final ReportCreateResponse report = reportCommandService.create(request, reporter);
    final List<ReportFindResponse> expected = List.of(
        new ReportFindResponse(report.getId(), report.getReporterId(), report.getReportedId(),
            report.getType(), report.getContentId(), report.getCreatedAt()));

    // when
    List<ReportFindResponse> actual = reportQueryService.findReports();

    // then
    Assertions.assertThat(actual)
        .usingRecursiveComparison()
        .ignoringFields("createdAt")
        .isEqualTo(expected);

  }
}