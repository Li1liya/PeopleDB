package shyshak.liliya.peopledb.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import shyshak.liliya.peopledb.model.Address;
import shyshak.liliya.peopledb.model.Person;
import shyshak.liliya.peopledb.model.Region;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class PeopleRepositoryTest {

    private Connection connection;
    private PeopleRepository repo;

    @BeforeEach
    void setUp() throws SQLException {
//        connection = DriverManager.getConnection("jdbc:h2:/Users/Lili/Self-studying/Professional Java Developer Career/PeopleDB/peopleTest31;TRACE_LEVEL_SYSTEM_OUT=0");
        connection = DriverManager.getConnection("jdbc:h2:/Users/Lili/DB/peopleTest31;TRACE_LEVEL_SYSTEM_OUT=0");
        connection.setAutoCommit(false); // не дозволяє додавати записи із тестів
        repo = new PeopleRepository(connection);
//        connection = DriverManager.getConnection("jdbc:h2:~/peopledb".replace("~", System.getProperty("user.home")));
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void canSaveOnePerson() throws SQLException {
        PeopleRepository repo = new PeopleRepository(connection);
        Person john = new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        Person savedPerson = repo.save(john);
        assertThat(savedPerson.getId()).isGreaterThan(0);
    }

    @Test
    public void canSaveTwoPeople() {
        Person john = new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        Person bobby = new Person("Bobby", "Smithy", ZonedDateTime.of(1982, 05, 29, 15, 15, 0, 0, ZoneId.of("-6")));
        Person savedPerson1 = repo.save(john);
        Person savedPerson2 = repo.save(bobby);
        assertThat(savedPerson1.getId()).isNotEqualTo(savedPerson2.getId());
    }

    @Test
    public void canSavePersonWithAddress() {
        Person john = new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        Address address = new Address(null, "123 Beale St.", "Apt. 1A", "Wala Wala", "WA", "90210", "United Stated", "Fulton county", Region.WEST);
        john.setHomeAddress(address);

        Person savedPerson = repo.save(john);
        assertThat(savedPerson.getHomeAddress().get().id()).isGreaterThan(0);
    }

    @Test
    public void canFindPersonById() {
        Person savedPerson = repo.save(new Person("Tommy", "Jameson", ZonedDateTime.of(1993, 8, 10, 8, 43, 0, 0, ZoneId.of("-2"))));
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson).isEqualTo(savedPerson);
    }

    @Test
    public void canSavePersonByIdWithAddress() {
        Person john = new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
        Address address = new Address(null, "123 Beale St.", "Apt. 1A", "Wala Wala", "WA", "90210", "United Stated", "Fulton county", Region.WEST);
        john.setHomeAddress(address);

        Person savedPerson = repo.save(john);
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson.getHomeAddress().get().state()).isEqualTo("WA");
    }

    @Test
    @Disabled
    public void canFindAll() {
        repo.save(new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));
        repo.save(new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));
        repo.save(new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));
        repo.save(new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));
        repo.save(new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));
        repo.save(new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));
        repo.save(new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));
        repo.save(new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));

        List<Person> people = repo.findAll();
        assertThat(people.size()).isGreaterThanOrEqualTo(10);
    }

    @Test
    public void testPersonIdNotFound() {
        Optional<Person> foundPerson = repo.findById(-1L);
        assertThat(foundPerson).isEmpty();
    }

    @Test
    public void canGetCount() {
        long startCount = repo.count();
        repo.save(new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));
        repo.save(new Person("Bobby", "Smithy", ZonedDateTime.of(1982, 05, 29, 15, 15, 0, 0, ZoneId.of("-6"))));
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount + 2);
    }

    @Test
    public void canDelete() {
        Person savedPerson = repo.save(new Person("Tommy", "Jameson", ZonedDateTime.of(1993, 8, 10, 8, 43, 0, 0, ZoneId.of("-2"))));
        long startCount = repo.count();
        repo.delete(savedPerson);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount - 1);
    }

//    @Test
//    public void canDeleteMultiplePeople1() {
//        Person john = new Person("Johnny", "Smithy", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6")));
//        Person bobby = new Person("Bobby", "Smithy", ZonedDateTime.of(1982, 05, 29, 15, 15, 0, 0, ZoneId.of("-6")));
//        repo.delete(john, bobby);
//    }

    @Test
    public void canDeleteMultiplePeople2() {
        Person p1 = new Person(10l, null, null, null);
        Person p2 = new Person(20l, null, null, null);
        Person p3 = new Person(30l, null, null, null);
        Person p4 = new Person(40l, null, null, null);
        Person p5 = new Person(50l, null, null, null);
        Person[] people = Arrays.asList(p1, p2, p3, p4, p5).toArray(new Person[]{});
        String ids = Arrays.stream(people)
                .map(Person::getId)
                .map(String::valueOf)
                .collect(joining(","));
        System.out.println(ids);
    } // not necessary test; just for writing method

    @Test
    public void canUpdate() {
        Person savedPerson = repo.save(new Person("John1", "Smith", ZonedDateTime.of(1980, 11, 15, 15, 15, 0, 0, ZoneId.of("-6"))));

        Person p1 = repo.findById(savedPerson.getId()).get();

        savedPerson.setSalary(new BigDecimal("73000.28"));
        repo.update(savedPerson);

        Person p2 = repo.findById(savedPerson.getId()).get();

        assertThat(p2.getSalary()).isNotEqualTo(p1.getSalary());
    }

    @Test
    @Disabled
    public void loadData() throws IOException, SQLException {
        Files.lines(Path.of("/Users/Lili/Downloads/Hr5m.csv"))
                .skip(1)
                .map(l -> l.split(","))
                .map(a -> {
                    LocalDate dob = LocalDate.parse(a[10], DateTimeFormatter.ofPattern("M/d/yyyy"));
                    LocalTime tob = LocalTime.parse(a[11], DateTimeFormatter.ofPattern("h:mm:s a").localizedBy(Locale.US));
                    LocalDateTime dtob = LocalDateTime.of(dob, tob);
                    ZonedDateTime zdtob = ZonedDateTime.of(dtob, ZoneId.of("+0"));
                    Person person = new Person(a[2], a[4], zdtob);
                    person.setSalary(new BigDecimal(a[25]));
                    person.setEmail(a[6]);
                    return person;
                })
                .forEach(repo::save);
        connection.commit();
        // використовувалося для запису інформації в базу даних
    }
}
