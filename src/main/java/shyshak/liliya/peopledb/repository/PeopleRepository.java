package shyshak.liliya.peopledb.repository;

import shyshak.liliya.peopledb.annotation.SQL;
import shyshak.liliya.peopledb.model.Address;
import shyshak.liliya.peopledb.model.CrudOperation;
import shyshak.liliya.peopledb.model.Person;
import shyshak.liliya.peopledb.model.Region;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class PeopleRepository extends CrudRepository<Person> {

    private AddressRepository addressRepository = null;
    public static final String SAVE_PERSON_SQL = """
            INSERT INTO PEOPLE 
            (FIRST_NAME, LAST_NAME, DOB, SALARY, EMAIL, HOME_ADDRESS) 
            VALUES(?, ?, ?, ?, ?, ?)""";
    public static final String FIND_BY_ID_SQL = """
            SELECT 
            P.ID, P.FIRST_NAME, P.LAST_NAME, P.DOB, P.SALARY, P.HOME_ADDRESS, 
            A.ID AS A_ID, A.STREET_ADDRESS, A.ADDRESS2, A.CITY, A.STATE, A.POSTCODE, A.COUNTY, A.REGION, A.COUNTRY 
            FROM PEOPLE AS P 
            LEFT OUTER JOIN ADDRESSES AS A ON P.HOME_ADDRESS = A.ID 
            WHERE P.ID = ?""";
    public static final String FIND_ALL_SQL = "SELECT ID, FIRST_NAME, LAST_NAME,DOB, SALARY FROM PEOPLE";
    public static final String SELECT_COUNT_SQL = "SELECT COUNT(*) FROM PEOPLE";
    public static final String DELETE_SQL = "DELETE FROM PEOPLE WHERE ID=?";
    public static final String DELETE_IN_SQL = "DELETE FROM PEOPLE WHERE ID IN (:ids)";
    public static final String UPDATE_SQL = "UPDATE PEOPLE SET FIRST_NAME=?, LAST_NAME=?, DOB=?, SALARY=? WHERE ID=?";
    //    private Connection connection; // це поле наслідується із батьківського класу, тому не є потрібним в написанні тут

    public PeopleRepository(Connection connection) {
        super(connection);
        addressRepository = new AddressRepository(connection);
    }

//    @Override
//    String getSaveSQL() {
//        return SAVE_PERSON_SQL;
//    }

    @Override
    @SQL(value = SAVE_PERSON_SQL, operationType = CrudOperation.SAVE)
    void mapForSave(Person entity, PreparedStatement ps) throws SQLException {
        Address savedAddress = null;
        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setTimestamp(3, convertDobToTimestamp(entity.getDob()));
        ps.setBigDecimal(4, entity.getSalary());
        ps.setString(5, entity.getEmail());
        if (entity.getHomeAddress().isPresent()) {
            savedAddress = addressRepository.save(entity.getHomeAddress().get());
            ps.setLong(6, savedAddress.id());
        } else {
            ps.setObject(6, null);
        }
    }

    @Override
    @SQL(value = UPDATE_SQL, operationType = CrudOperation.UPDATE)
    void mapForUpdate(Person entity, PreparedStatement ps) throws SQLException {
        ps.setString(1, entity.getFirstName());
        ps.setString(2, entity.getLastName());
        ps.setTimestamp(3, convertDobToTimestamp(entity.getDob()));
        ps.setBigDecimal(4, entity.getSalary());
    }

    @Override
    @SQL(value = "SELECT ID, FIRST_NAME, LAST_NAME,DOB, SALARY FROM PEOPLE WHERE ID=?", operationType = CrudOperation.FIND_BY_ID)
    @SQL(value = FIND_BY_ID_SQL, operationType = CrudOperation.FIND_BY_ID) //два варіанти правильні
    @SQL(value = FIND_ALL_SQL, operationType = CrudOperation.FIND_ALL)
    @SQL(value = SELECT_COUNT_SQL, operationType = CrudOperation.COUNT)
    @SQL(value = DELETE_SQL, operationType = CrudOperation.DELETE_ONE)
    @SQL(value = DELETE_IN_SQL, operationType = CrudOperation.DELETE_MANY)
    Person extractEntityFromResultSet(ResultSet rs) throws SQLException {
        long periodId = rs.getLong("ID");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = rs.getBigDecimal("SALARY");
        long homeAddressId = rs.getLong("HOME_ADDRESS");
        Address address = extractAddress(rs);

        Person person = new Person(periodId, firstName, lastName, dob, salary);
        person.setHomeAddress(address);

        return person;
    }

    private Address extractAddress(ResultSet rs) throws SQLException {
//        long addId = rs.getLong("A_ID");
//        if (rs.getObject("A_ID") == null) return null;
        Long addId = getValueByAlias("A_ID", rs, Long.class);
        if (addId == null) return null;
        String streetAddress = rs.getString("STREET_ADDRESS");
        String address2 = rs.getString("ADDRESS2");
        String city = rs.getString("CITY");
        String state = rs.getString("STATE");
        String postcode = rs.getString("POSTCODE");
        Region region = Region.valueOf(rs.getString("REGION").toUpperCase());
        String county = rs.getString("COUNTY");
        String country = rs.getString("COUNTRY");
        Address address = new Address(addId, streetAddress, address2, city, state, postcode, country, county, region);
        return address;
    }

    private <T> T getValueByAlias(String alias, ResultSet rs, Class<T> clazz /*data type */) throws SQLException {
        int columnCount = rs.getMetaData().getColumnCount();
        for(int colIdx = 1; colIdx<= columnCount; colIdx++) {
            if(alias.equals(rs.getMetaData().getColumnLabel(colIdx))) {
                return (T) rs.getObject(colIdx);
            }
        }
        throw  new SQLException(String.format("Column not found for alias: %s", alias));
    }


    private static Timestamp convertDobToTimestamp(ZonedDateTime dob) {
        return Timestamp.valueOf(dob.withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
    }
}


//    @Override
//    protected String getFindByIdSQL() {
//        return FIND_BY_ID_SQL;
//    }
//
//    @Override
//    protected String getFindAllSQL() {
//        return FIND_ALL_SQL;
//    }
//
//    @Override
//    protected String getCountSql() {
//        return SELECT_COUNT_SQL;
//    }
//
//    @Override
//    protected String getDeleteSQL() {
//        return DELETE_SQL;
//    }
//
//    @Override
//    protected String getDeleteInSQL() {
//        return DELETE_IN_SQL;
//    }

//    @Override
//    protected String getUpdateSql() {
//        return UPDATE_SQL;
//    }


//    public Person save(Person entity) throws UnableToSaveException {
////        String sql = String.format("INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME, DOB) " +
////                        "VALUES('%s', '%s', %s", person.getFirstName(), person.getLastName(), person.getDob());
////        + createStatement
//        try {
//            PreparedStatement ps = connection.prepareStatement(SAVE_PERSON_SQL, Statement.RETURN_GENERATED_KEYS);
//            ps.setString(1, entity.getFirstName());
//            ps.setString(2, entity.getLastName());
//            ps.setTimestamp(3, convertDobToTimestamp(entity.getDob()));
//            int recordsAffected = ps.executeUpdate(); // auto-commited
//            ResultSet rs = ps.getGeneratedKeys();
//            while (rs.next()) {
//                long id = rs.getLong(1);
//                entity.setId(id);
//                System.out.println(entity);
//            }
//            System.out.printf("Records affected: %d%n", recordsAffected);
//        } catch (SQLException e) {
//            e.getStackTrace();
//            throw new UnableToSaveException("Tried to save person: " + entity);
//        }
//        return entity;
// цей метод перенесено в CRUDRepository (generic class) + видозмінено його частини із додаванням 2 інших абстрактних методів
//    }

//    public Optional<Person> findById(Long id) {
//        Person person = null;
//        try {
//            PreparedStatement ps = connection.prepareStatement(FIND_BY_ID_SQL);
//            ps.setLong(1, id);
//            ResultSet rs = ps.executeQuery();// returns a result set
//            while (rs.next()) {
//                person = extractEntityFromResultSet(rs);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return Optional.ofNullable(person);
//    }

//    public List<Person> findAll() {
//        List<Person> people = new ArrayList<>();
//        try{
//            PreparedStatement ps = connection.prepareStatement(FIND_ALL_SQL);
//            ResultSet rs = ps.executeQuery();
//            while (rs.next()) {
//                people.add(extractEntityFromResultSet(rs));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return people;
//    }

//    private Person extractPersonFromResultSet(ResultSet rs) throws SQLException {
//        long periodId = rs.getLong("ID");
//        String firstName = rs.getString("FIRST_NAME");
//        String lastName = rs.getString("LAST_NAME");
//        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
//        BigDecimal salary = rs.getBigDecimal("SALARY");
//        return new Person(periodId, firstName, lastName, dob, salary);
//    }

//    public long count() {
//        long count = 0;
//        try {
//            PreparedStatement ps = connection.prepareStatement(SELECT_COUNT_SQL);
//            ResultSet rs = ps.executeQuery();
//            if (rs.next()) {
//                count = rs.getLong(1);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return count;
//    }

//    public void delete(Person person) {
//        try {
//            PreparedStatement ps = connection.prepareStatement(DELETE_SQL);
//            ps.setLong(1, person.getId());
//            int affectedRecordCount = ps.executeUpdate();
//            System.out.println(affectedRecordCount);
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void delete(Person... people) {
//        try {
//            Statement statement = connection.createStatement();
//            String ids = Arrays.stream(people)
//                    .map(Person::getId)
//                    .map(String::valueOf)
//                    .collect(joining(","));
//            int affectedRecordsCount = statement.executeUpdate(DELETE_IN_SQL.replace(":ids", ids));
//            //використовується для того, щоб можна було видалити будь-яку кількість записів
//            System.out.println(affectedRecordsCount);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
////        for (Person person : people) {
////            delete(people);
////        }
//    }

//    public void update(Person person) {
//        try {
//            PreparedStatement ps = connection.prepareStatement(UPDATE_SQL);
//            ps.setString(1, person.getFirstName());
//            ps.setString(2, person.getLastName());
//            ps.setTimestamp(3, convertDobToTimestamp(person.getDob()));
//            ps.setBigDecimal(4, person.getSalary());
//            ps.setLong(5, person.getId());
//            ps.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }