package com.emmsale.event.domain.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.emmsale.event.EventFixture;
import com.emmsale.event.domain.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Sql("/data-test.sql")
class EventRepositoryTest {

  @Autowired
  private EventRepository eventRepository;

  @Test
  @DisplayName("existsById() : eventId로 해당 event 가 있는지 조회할 수 있다.")
  void test_existsById() throws Exception {
    //given
    final Event savedEvent = eventRepository.save(EventFixture.eventFixture());

    //when & then
    assertTrue(eventRepository.existsById(savedEvent.getId()));
  }
}
