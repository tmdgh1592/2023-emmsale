package com.emmsale.event.api;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.emmsale.event.EventFixture;
import com.emmsale.event.application.EventService;
import com.emmsale.event.application.dto.EventDetailRequest;
import com.emmsale.event.application.dto.EventDetailResponse;
import com.emmsale.event.application.dto.EventParticipateRequest;
import com.emmsale.event.application.dto.EventResponse;
import com.emmsale.event.application.dto.ParticipantResponse;
import com.emmsale.event.domain.Event;
import com.emmsale.event.domain.EventStatus;
import com.emmsale.helper.MockMvcTestHelper;
import com.emmsale.tag.TagFixture;
import com.emmsale.tag.application.dto.TagRequest;
import com.emmsale.tag.domain.Tag;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(EventApi.class)
class EventApiTest extends MockMvcTestHelper {

  private static final int QUERY_YEAR = 2023;
  private static final int QUERY_MONTH = 7;

  @MockBean
  private EventService eventService;

  @Test
  @DisplayName("컨퍼런스의 상세정보를 조회할 수 있다.")
  void findEvent() throws Exception {
    //given
    final Long eventId = 1L;
    final EventDetailResponse eventDetailResponse = new EventDetailResponse(
        eventId,
        "인프콘 2023",
        "http://infcon.com",
        LocalDateTime.of(2023, 8, 15, 12, 0),
        LocalDateTime.of(2023, 8, 15, 12, 0),
        "코엑스",
        "예정",
        List.of("코틀린", "백엔드", "안드로이드")
    );

    final ResponseFieldsSnippet responseFields = responseFields(
        fieldWithPath("id").type(JsonFieldType.NUMBER).description("event 식별자"),
        fieldWithPath("name").type(JsonFieldType.STRING).description("envent 이름"),
        fieldWithPath("informationUrl").type(JsonFieldType.STRING).description("상세정보 url"),
        fieldWithPath("startDate").type(JsonFieldType.STRING).description("시작일자"),
        fieldWithPath("endDate").type(JsonFieldType.STRING).description("종료일자"),
        fieldWithPath("location").type(JsonFieldType.STRING).description("장소"),
        fieldWithPath("status").type(JsonFieldType.STRING).description("진행상태"),
        fieldWithPath("tags[]").type(JsonFieldType.ARRAY).description("태그들")
    );

    when(eventService.findEvent(eventId)).thenReturn(eventDetailResponse);

    //when
    mockMvc.perform(get("/events/" + eventId))
        .andExpect(status().isOk())
        .andDo(document("find-event", responseFields));
  }

  @Test
  @DisplayName("Event에 사용자를 참여자로 추가할 수 있다.")
  void participateEvent() throws Exception {
    //given
    final Long eventId = 1L;
    final Long memberId = 2L;
    final Long participantId = 3L;
    final EventParticipateRequest request = new EventParticipateRequest(memberId);
    final String fakeAccessToken = "Bearer accessToken";

    final RequestFieldsSnippet requestFields = requestFields(
        fieldWithPath("memberId").type(JsonFieldType.NUMBER).description("멤버 식별자")
    );

    when(eventService.participate(any(), any(), any()))
        .thenReturn(participantId);

    //when
    mockMvc.perform(post("/events/{eventId}/participants", eventId)
            .header("Authorization", fakeAccessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(
            header().string("Location",
                format("/events/%s/participants/%s", eventId, participantId))
        )
        .andDo(document("participate-event", requestFields));
  }

  @Test
  @DisplayName("특정 카테고리의 행사 목록을 조회할 수 있으면 200 OK를 반환한다.")
  void findEvents() throws Exception {
    // given
    final RequestParametersSnippet requestParameters = requestParameters(
        parameterWithName("category").description("행사 카테고리(CONFERENCE, COMPETITION)"),
        parameterWithName("year").description("조회하고자 하는 연도(2015 이상의 값)(option)").optional(),
        parameterWithName("month").description("조회하고자 하는 월(1~12)(option)").optional(),
        parameterWithName("tag").description("필터링하려는 태그(option)").optional(),
        parameterWithName("status").description("필터링하려는 상태(option)").optional()
    );

    final ResponseFieldsSnippet responseFields = responseFields(
        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("행사 id"),
        fieldWithPath("[].name").type(JsonFieldType.STRING).description("행사명"),
        fieldWithPath("[].startDate").type(JsonFieldType.STRING)
            .description("행사 시작일(yyyy:MM:dd:HH:mm:ss)"),
        fieldWithPath("[].endDate").type(JsonFieldType.STRING)
            .description("행사 종료일(yyyy:MM:dd:HH:mm:ss)"),
        fieldWithPath("[].tags[]").type(JsonFieldType.ARRAY)
            .description("행사 태그 목록"),
        fieldWithPath("[].status").type(JsonFieldType.STRING).description("행사 진행 상황"),
        fieldWithPath("[].remainingDays").type(JsonFieldType.STRING).description("행사 시작일까지 남은 일 수"),
        fieldWithPath("[].imageUrl").type(JsonFieldType.STRING).description("행사 이미지 URL")
    );

    final List<EventResponse> eventResponses = List.of(
        new EventResponse(1L, "인프콘 2023", LocalDateTime.parse("2023-06-03T12:00:00"),
            LocalDateTime.parse("2023-09-03T12:00:00"),
            List.of("백엔드", "프론트엔드", "안드로이드", "IOS", "AI"), "진행 중",
            "https://biz.pusan.ac.kr/dext5editordata/2022/08/20220810_160546511_10103.jpg",
            3),
        new EventResponse(5L, "웹 컨퍼런스", LocalDateTime.parse("2023-07-03T12:00:00"),
            LocalDateTime.parse("2023-08-03T12:00:00"), List.of("백엔드", "프론트엔드"), "진행 중",
            null,
            3),
        new EventResponse(2L, "AI 컨퍼런스", LocalDateTime.parse("2023-07-22T12:00:00"),
            LocalDateTime.parse("2023-07-30T12:00:00"), List.of("AI"), "진행 예정",
            "https://biz.pusan.ac.kr/dext5editordata/2022/08/20220810_160546511_10103.jpg",
            3),
        new EventResponse(4L, "안드로이드 컨퍼런스", LocalDateTime.parse("2023-06-29T12:00:00"),
            LocalDateTime.parse("2023-07-16T12:00:00"), List.of("백엔드", "프론트엔드"), "종료된 행사",
            "https://biz.pusan.ac.kr/dext5editordata/2022/08/20220810_160546511_10103.jpg",
            3)

    );

    when(eventService.findEvents(any(), any(LocalDate.class), eq(QUERY_YEAR), eq(QUERY_MONTH),
        eq(null), eq(null))).thenReturn(eventResponses);

    // when & then
    mockMvc.perform(get("/events")
            .param("category", "CONFERENCE")
            .param("year", "2023")
            .param("month", "7")
        )
        .andExpect(status().isOk())
        .andDo(document("find-events", requestParameters, responseFields));
  }

  @Test
  @DisplayName("행사의 참여자를 전체 조회할 수 있다.")
  void findParticipants() throws Exception {
    //given
    final Long eventId = 1L;
    final ResponseFieldsSnippet responseFields = responseFields(
        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("참여자 식별자"),
        fieldWithPath("[].memberId").type(JsonFieldType.NUMBER).description("member의 식별자"),
        fieldWithPath("[].name").type(JsonFieldType.STRING).description("member 이름"),
        fieldWithPath("[].imageUrl").type(JsonFieldType.STRING).description("프로필 이미지 url"),
        fieldWithPath("[].description").type(JsonFieldType.STRING).description("한줄 자기 소개")
    );
    final List<ParticipantResponse> responses = List.of(
        new ParticipantResponse(1L, 1L, "스캇", "imageUrl",
            "토마토 던지는 사람"),
        new ParticipantResponse(2L, 2L, "홍실", "imageUrl",
            "토마토 맞는 사람")
    );

    when(eventService.findParticipants(eventId))
        .thenReturn(responses);

    //when && then
    mockMvc.perform(get(format("/events/%s/participants", eventId)))
        .andExpect(status().isOk())
        .andDo(document("find-participants", responseFields));
  }

  @Test
  @DisplayName("이벤트를 성공적으로 업데이트하면 204, NO_CONTENT를 반환한다.")
  void updateEventTest() throws Exception {
    //given
    final long eventId = 1L;
    final Event event = EventFixture.인프콘_2023();

    final List<TagRequest> tags = Stream.of(TagFixture.백엔드(), TagFixture.안드로이드())
        .map(tag -> new TagRequest(tag.getName()))
        .collect(Collectors.toList());

    final EventDetailRequest request = new EventDetailRequest(
        event.getName(),
        event.getLocation(),
        event.getInformationUrl(),
        event.getStartDate(),
        event.getEndDate(),
        tags
    );

    final EventDetailResponse response = new EventDetailResponse(eventId, request.getName(),
        request.getInformationUrl(), request.getStartDateTime(), request.getEndDateTime(),
        request.getLocation(), EventStatus.IN_PROGRESS.getValue(),
        tags.stream().map(TagRequest::getName).collect(Collectors.toList()));

    when(eventService.updateEvent(any(), any())).thenReturn(response);

    final RequestFieldsSnippet requestFields = requestFields(
        fieldWithPath("name").type(JsonFieldType.STRING).description("행사(Event) 이름"),
        fieldWithPath("location").type(JsonFieldType.STRING).description("행사(Event) 장소"),
        fieldWithPath("startDateTime").type(JsonFieldType.STRING).description("행사(Event) 시작일시"),
        fieldWithPath("endDateTime").type(JsonFieldType.STRING).description("행사(Event) 종료일시"),
        fieldWithPath("informationUrl").type(JsonFieldType.STRING)
            .description("행사(Event) 상세 정보 URL"),
        fieldWithPath("tags[].name").type(JsonFieldType.STRING).description("연관 태그명")
    );

    final ResponseFieldsSnippet responseFields = responseFields(
        fieldWithPath("id").type(JsonFieldType.NUMBER).description("행사(Event) id"),
        fieldWithPath("name").type(JsonFieldType.STRING).description("행사(Event) 이름"),
        fieldWithPath("informationUrl").type(JsonFieldType.STRING)
            .description("행사(Event) 상세 정보 URL"),
        fieldWithPath("startDate").type(JsonFieldType.STRING).description("행사(Event) 시작일시"),
        fieldWithPath("endDate").type(JsonFieldType.STRING).description("행사(Event) 종료일시"),
        fieldWithPath("location").type(JsonFieldType.STRING).description("행사(Event) 장소"),
        fieldWithPath("status").type(JsonFieldType.STRING).description("행사(Event) 진행 상태"),
        fieldWithPath("tags[]").type(JsonFieldType.ARRAY).description("행사(Event) 연관 태그 목록")
    );

    //when
    final ResultActions result = mockMvc.perform(put("/events/" + eventId)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(objectMapper.writeValueAsString(request)));

    //then
    result.andExpect(status().isNoContent())
        .andDo(print())
        .andDo(document("update-event", requestFields, responseFields));
  }

  @Test
  @DisplayName("이벤트를 성공적으로 삭제하면 204, NO_CONTENT를 반환한다.")
  void deleteEventTest() throws Exception {
    //given
    final long eventId = 1L;
    final Event event = EventFixture.인프콘_2023();

    final List<String> tags = Stream.of(TagFixture.백엔드(), TagFixture.안드로이드())
        .map(Tag::getName).collect(Collectors.toList());

    final EventDetailResponse response = new EventDetailResponse(eventId, event.getName(),
        event.getInformationUrl(), event.getStartDate(), event.getEndDate(),
        event.getLocation(), EventStatus.IN_PROGRESS.getValue(), tags);

    when(eventService.deleteEvent(eventId)).thenReturn(response);

    final ResponseFieldsSnippet responseFields = responseFields(
        fieldWithPath("id").type(JsonFieldType.NUMBER).description("행사(Event) id"),
        fieldWithPath("name").type(JsonFieldType.STRING).description("행사(Event) 이름"),
        fieldWithPath("informationUrl").type(JsonFieldType.STRING)
            .description("행사(Event) 상세 정보 URL"),
        fieldWithPath("startDate").type(JsonFieldType.STRING).description("행사(Event) 시작일시"),
        fieldWithPath("endDate").type(JsonFieldType.STRING).description("행사(Event) 종료일시"),
        fieldWithPath("location").type(JsonFieldType.STRING).description("행사(Event) 장소"),
        fieldWithPath("status").type(JsonFieldType.STRING).description("행사(Event) 진행 상태"),
        fieldWithPath("tags[]").type(JsonFieldType.ARRAY).description("행사(Event) 연관 태그 목록")
    );

    //when
    final ResultActions result = mockMvc.perform(delete("/events/" + eventId));

    //then
    result.andExpect(status().isNoContent())
        .andDo(print())
        .andDo(document("delete-event", responseFields));
  }

  @Nested
  class AddEvent {

    @Test
    @DisplayName("이벤트를 성공적으로 추가하면 201, CREATED 를 반환한다.")
    void addEventTest() throws Exception {
      //given
      final Event event = EventFixture.인프콘_2023();

      final List<TagRequest> tags = Stream.of(TagFixture.백엔드(), TagFixture.안드로이드())
          .map(tag -> new TagRequest(tag.getName()))
          .collect(Collectors.toList());

      final EventDetailRequest request = new EventDetailRequest(
          event.getName(),
          event.getLocation(),
          event.getInformationUrl(),
          event.getStartDate(),
          event.getEndDate(),
          tags
      );

      final EventDetailResponse response = new EventDetailResponse(1L, request.getName(),
          request.getInformationUrl(),
          request.getStartDateTime(), request.getEndDateTime(), request.getLocation(),
          EventStatus.IN_PROGRESS.getValue(),
          tags.stream().map(TagRequest::getName).collect(Collectors.toList()));

      when(eventService.addEvent(any())).thenReturn(response);

      final RequestFieldsSnippet requestFields = requestFields(
          fieldWithPath("name").type(JsonFieldType.STRING).description("행사(Event) 이름"),
          fieldWithPath("location").type(JsonFieldType.STRING).description("행사(Event) 장소"),
          fieldWithPath("startDateTime").type(JsonFieldType.STRING).description("행사(Event) 시작일시"),
          fieldWithPath("endDateTime").type(JsonFieldType.STRING).description("행사(Event) 종료일시"),
          fieldWithPath("informationUrl").type(JsonFieldType.STRING)
              .description("행사(Event) 상세 정보 URL"),
          fieldWithPath("tags[].name").type(JsonFieldType.STRING).description("연관 태그명")
      );

      final ResponseFieldsSnippet responseFields = responseFields(
          fieldWithPath("id").type(JsonFieldType.NUMBER).description("행사(Event) id"),
          fieldWithPath("name").type(JsonFieldType.STRING).description("행사(Event) 이름"),
          fieldWithPath("informationUrl").type(JsonFieldType.STRING)
              .description("행사(Event) 상세 정보 URL"),
          fieldWithPath("startDate").type(JsonFieldType.STRING).description("행사(Event) 시작일시"),
          fieldWithPath("endDate").type(JsonFieldType.STRING).description("행사(Event) 종료일시"),
          fieldWithPath("location").type(JsonFieldType.STRING).description("행사(Event) 장소"),
          fieldWithPath("status").type(JsonFieldType.STRING).description("행사(Event) 진행 상태"),
          fieldWithPath("tags[]").type(JsonFieldType.ARRAY).description("행사(Event) 연관 태그 목록")
      );

      //when
      final ResultActions result = mockMvc.perform(post("/events")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(request)));

      //then
      result.andExpect(status().isCreated())
          .andDo(print())
          .andDo(document("add-event", requestFields, responseFields));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @DisplayName("이름에 빈 값이 들어올 경우 400 BAD_REQUEST를 반환한다.")
    void addEventWithEmptyNameTest(final String eventName) throws Exception {
      //given
      final Event event = EventFixture.인프콘_2023();

      final List<TagRequest> tags = Stream.of(TagFixture.백엔드(), TagFixture.안드로이드())
          .map(tag -> new TagRequest(tag.getName()))
          .collect(Collectors.toList());

      final EventDetailRequest request = new EventDetailRequest(
          eventName,
          event.getLocation(),
          event.getInformationUrl(),
          event.getStartDate(),
          event.getEndDate(),
          tags
      );

      //when
      final ResultActions result = mockMvc.perform(post("/events")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(request)));

      //then
      result.andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @DisplayName("장소에 빈 값이 들어올 경우 400 BAD_REQUEST를 반환한다.")
    void addEventWithEmptyLocationTest(final String eventLocation) throws Exception {
      //given
      final Event event = EventFixture.인프콘_2023();

      final List<TagRequest> tags = Stream.of(TagFixture.백엔드(), TagFixture.안드로이드())
          .map(tag -> new TagRequest(tag.getName()))
          .collect(Collectors.toList());

      final EventDetailRequest request = new EventDetailRequest(
          event.getName(),
          eventLocation,
          event.getInformationUrl(),
          event.getStartDate(),
          event.getEndDate(),
          tags
      );

      //when
      final ResultActions result = mockMvc.perform(post("/events")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(request)));

      //then
      result.andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"httpexample.com", "http:example.com", "http:/example.com",
        "httpsexample.com", "https:example.com", "https:/example.com"})
    @NullSource
    @DisplayName("상세 URL에 http:// 혹은 https://로 시작하지 않는 값이 들어올 경우 400 BAD_REQUEST를 반환한다.")
    void addEventWithInvalidInformationUrlTest(final String informationUrl) throws Exception {
      //given
      final Event event = EventFixture.인프콘_2023();

      final List<TagRequest> tags = Stream.of(TagFixture.백엔드(), TagFixture.안드로이드())
          .map(tag -> new TagRequest(tag.getName()))
          .collect(Collectors.toList());

      final EventDetailRequest request = new EventDetailRequest(
          event.getName(),
          event.getLocation(),
          informationUrl,
          event.getStartDate(),
          event.getEndDate(),
          tags
      );

      //when
      final ResultActions result = mockMvc.perform(post("/events")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(objectMapper.writeValueAsString(request)));

      //then
      result.andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"23-01-01T12:00:00", "2023-1-01T12:00:00", "2023-01-1T12:00:00",
        "2023-01-01T2:00:00", "2023-01-01T12:0:00", "2023-01-01T12:00:0"})
    @NullSource
    @DisplayName("시작 일시에 null 혹은 다른 형식의 일시 값이 들어올 경우 400 BAD_REQUEST를 반환한다.")
    void addEventWithUnformattedStartDateTimeTest(final String startDateTime) throws Exception {
      //given
      final Event event = EventFixture.인프콘_2023();

      final List<TagRequest> tags = Stream.of(TagFixture.백엔드(), TagFixture.안드로이드())
          .map(tag -> new TagRequest(tag.getName()))
          .collect(Collectors.toList());

      final String request = "{"
          + "\"name\":\"인프콘 2023\","
          + "\"location\":\"코엑스\","
          + "\"informationUrl\":\"https://~~~\","
          + "\"startDateTime\":" + startDateTime + ","
          + "\"endDateTime\":\"2023-01-02T12:00:00\""
          + ",\"tags\":[{\"name\":\"백엔드\"},{\"name\":\"안드로이드\"}]"
          + "}";

      //when
      final ResultActions result = mockMvc.perform(post("/events")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(request));

      //then
      result.andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"23-01-02T12:00:00", "2023-1-02T12:00:00", "2023-01-2T12:00:00",
        "2023-01-02T2:00:00", "2023-01-02T12:0:00", "2023-01-02T12:00:0"})
    @NullSource
    @DisplayName("종료 일시에 null 혹은 다른 형식의 일시 값이 들어올 경우 400 BAD_REQUEST를 반환한다.")
    void addEventWithUnformattedEndDateTimeTest(final String endDateTime) throws Exception {
      //given
      final Event event = EventFixture.인프콘_2023();

      final List<TagRequest> tags = Stream.of(TagFixture.백엔드(), TagFixture.안드로이드())
          .map(tag -> new TagRequest(tag.getName()))
          .collect(Collectors.toList());

      final String request = "{"
          + "\"name\":\"인프콘 2023\","
          + "\"location\":\"코엑스\","
          + "\"informationUrl\":\"https://~~~\","
          + "\"startDateTime\":\"2023-01-01T12:00:00\""
          + "\"endDateTime\":" + endDateTime + ","
          + ",\"tags\":[{\"name\":\"백엔드\"},{\"name\":\"안드로이드\"}]"
          + "}";

      //when
      final ResultActions result = mockMvc.perform(post("/events")
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .content(request));

      //then
      result.andExpect(status().isBadRequest());
    }
  }
}
