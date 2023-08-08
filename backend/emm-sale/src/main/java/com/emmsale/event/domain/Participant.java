package com.emmsale.event.domain;

import com.emmsale.member.domain.Member;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "event_member")
public class Participant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Member member;

  @ManyToOne
  @JoinColumn(nullable = false)
  private Event event;

  private String content;

  public Participant(final Member member, final Event event, final String content) {
    event.validateAlreadyParticipate(member);
    this.member = member;
    this.event = event;
    this.content = content;
  }

  public boolean isSameMember(final Member member) {
    return this.member.isMe(member);
  }
}
