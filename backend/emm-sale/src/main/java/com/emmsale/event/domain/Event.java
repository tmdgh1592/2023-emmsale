package com.emmsale.event.domain;

import com.emmsale.base.BaseEntity;
import com.emmsale.comment.domain.Comment;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private String name;
  @Column(nullable = false)
  private String location;
  @Column(nullable = false)
  private LocalDateTime startDate;
  @Column(nullable = false)
  private LocalDateTime endDate;
  @Column(nullable = false)
  private String informationUrl;
  @OneToMany(mappedBy = "event")
  private List<EventTag> tags;
  @OneToMany(mappedBy = "event")
  private List<Comment> comments;

  //@OneToMany
  //private List<Member> participants;

  public Event(
      final String name, final String location,
      final LocalDateTime startDate, final LocalDateTime endDate,
      final String informationUrl, final List<EventTag> tags
  ) {
    this.name = name;
    this.location = location;
    this.startDate = startDate;
    this.endDate = endDate;
    this.informationUrl = informationUrl;
    this.tags = tags;
  }
}
