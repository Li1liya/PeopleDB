package shyshak.liliya.peopledb.model;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    @Test
    public void testForEquality() {
        Person p1 = new Person("Tommy", "Jameson", ZonedDateTime.of(1993, 8, 10, 8, 43, 0, 0, ZoneId.of("-2")));
        Person p2 = new Person("Tommy", "Jameson", ZonedDateTime.of(1993, 8, 10, 8, 43, 0, 0, ZoneId.of("-2")));
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void testForInequality() {
        Person p1 = new Person("Tommy", "Jameson", ZonedDateTime.of(1993, 8, 10, 8, 43, 0, 0, ZoneId.of("-2")));
        Person p2 = new Person("Toffie", "Jameson", ZonedDateTime.of(1993, 8, 10, 8, 43, 0, 0, ZoneId.of("-2")));
        assertThat(p1).isNotEqualTo(p2);
    }

}