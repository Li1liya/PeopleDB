package shyshak.liliya.peopledb.model;

import java.time.ZonedDateTime;

public class Person {
    private String firstName;
    private String lastName;
    private ZonedDateTime dob;
    private Long id;

    public Person(String firstName, String lastName, ZonedDateTime dob){
    }

    public Long getId() {
        return 1L;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public ZonedDateTime getDob() {
        return dob;
    }

    public void setDob(ZonedDateTime dob) {
        this.dob = dob;
    }
}
