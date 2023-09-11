package com.emmsale.message_room.application;

import static com.emmsale.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static com.emmsale.member.exception.MemberExceptionType.NOT_MATCHING_TOKEN_AND_LOGIN_MEMBER;
import static com.emmsale.message_room.exception.MessageRoomExceptionType.FORBIDDEN_NOT_INTERLOCUTORS;
import static com.emmsale.message_room.exception.MessageRoomExceptionType.NOT_FOUND_MESSAGE_ROOM;

import com.emmsale.member.domain.Member;
import com.emmsale.member.domain.MemberRepository;
import com.emmsale.member.exception.MemberException;
import com.emmsale.message_room.application.dto.MessageResponse;
import com.emmsale.message_room.application.dto.RoomResponse;
import com.emmsale.message_room.domain.MessageRepository;
import com.emmsale.message_room.domain.Room;
import com.emmsale.message_room.domain.RoomRepository;
import com.emmsale.message_room.exception.MessageRoomException;
import com.emmsale.message_room.infrastructure.persistence.MessageDao;
import com.emmsale.message_room.infrastructure.persistence.dto.MessageOverview;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomQueryService {

  private final RoomRepository roomRepository;
  private final MessageDao messageDao;
  private final MemberRepository memberRepository;
  private final MessageRepository messageRepository;

  public List<RoomResponse> findAll(final Member loginMember, final Long memberId) {
    validateSameMember(loginMember, memberId);

    final Long loginMemberId = loginMember.getId();

    final List<MessageOverview> messageOverviews = messageDao.findRecentlyMessages(loginMemberId);

    final Map<String, Long> interlocutorIdPerRoomExceptMe = groupingPartnerIdByRoom(
        messageOverviews,
        loginMemberId
    );

    return messageOverviews.stream()
        .map(messageOverview ->
            RoomResponse.from(
                messageOverview,
                findInterlocutor(interlocutorIdPerRoomExceptMe.get(messageOverview.getRoomUUID()))
            ))
        .collect(Collectors.toList());
  }

  private Map<String, Long> groupingPartnerIdByRoom(
      final List<MessageOverview> messageOverviews,
      final Long loginMemberId
  ) {
    return messageOverviews.stream()
        .flatMap(messageOverview -> findInterlocutorInRoom(messageOverview, loginMemberId))
        .collect(Collectors.toMap(
            room -> room.getRoomId().getUuid(),
            room -> room.getRoomId().getMemberId())
        );
  }

  private Stream<Room> findInterlocutorInRoom(
      final MessageOverview messageOverview,
      final Long loginMemberId
  ) {
    return roomRepository.findByUUID(messageOverview.getRoomUUID())
        .stream()
        .filter(room -> room.isInterlocutorWith(loginMemberId));
  }

  private void validateSameMember(final Member loginMember, final Long memberId) {
    if (loginMember.isNotMe(memberId)) {
      throw new MemberException(NOT_MATCHING_TOKEN_AND_LOGIN_MEMBER);
    }
  }

  private Member findInterlocutor(final Long interlocutorId) {
    return memberRepository.findById(interlocutorId)
        .orElseThrow(() -> new MemberException(NOT_FOUND_MEMBER));
  }

  public List<MessageResponse> findByRoomId(
      final Member loginMember,
      final String roomId,
      final Long memberId
  ) {
    validateSameMember(loginMember, memberId);

    return findMessageByRoomUUID(roomId);
  }

  private List<MessageResponse> findMessageByRoomUUID(final String roomId) {
    return messageRepository.findByRoomUUID(roomId)
        .stream()
        .map(MessageResponse::from)
        .sorted(Comparator.comparing(MessageResponse::getCreatedAt))
        .collect(Collectors.toList());
  }

  public List<MessageResponse> findByInterlocutorIds(
      final Long senderId,
      final Long receiverId,
      final Long memberId,
      final Member loginMember
  ) {
    validateSameMember(loginMember, memberId);
    validateRoomInterlocutors(senderId, receiverId, loginMember);

    final Room room = roomRepository.findByInterlocutorIds(senderId, receiverId)
        .orElseThrow(() -> new MessageRoomException(NOT_FOUND_MESSAGE_ROOM));

    return findMessageByRoomUUID(room.getRoomId().getUuid());
  }

  private void validateRoomInterlocutors(
      final Long senderId,
      final Long receiverId,
      final Member loginMember
  ) {
    final Long memberId = loginMember.getId();
    if (!(senderId.equals(memberId) || receiverId.equals(memberId))) {
      throw new MessageRoomException(FORBIDDEN_NOT_INTERLOCUTORS);
    }
  }
}
