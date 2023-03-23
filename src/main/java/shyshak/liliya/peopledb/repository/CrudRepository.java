package shyshak.liliya.peopledb.repository;

import shyshak.liliya.peopledb.annotation.Id;
import shyshak.liliya.peopledb.annotation.MultiSQL;
import shyshak.liliya.peopledb.annotation.SQL;
import shyshak.liliya.peopledb.model.CrudOperation;

import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

abstract class CrudRepository<T> {

    protected Connection connection;

    public CrudRepository(Connection connection) {
        this.connection = connection;
    }

    private String getSQLByAnnotation(CrudOperation operationType, Supplier<String> sqlGetter) {
        Stream<SQL> multiSqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(MultiSQL.class))
                .map(m -> m.getAnnotation(MultiSQL.class))
                .flatMap(msql -> Arrays.stream(msql.value()));

        Stream<SQL> sqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
//                .filter(m -> methodName.contentEquals(m.getName()))
                .filter(m -> m.isAnnotationPresent(SQL.class))
                .map(m -> m.getAnnotation(SQL.class));

        return Stream.concat(multiSqlStream, sqlStream)
                .filter(a -> a.operationType().equals(operationType))
                .map(SQL::value)
                .findFirst().orElseGet(sqlGetter);
    }

    public T save(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSQLByAnnotation(CrudOperation.SAVE, this::getSaveSQL)/*getSaveSQL()*/, Statement.RETURN_GENERATED_KEYS);
            mapForSave(entity, ps);
            int recordsAffected = ps.executeUpdate(); // auto-commited
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                long id = rs.getLong(1);
                setIdByAnnotation(id, entity);
//                System.out.println(entity);
            }
//            System.out.printf("Records affected: %d%n", recordsAffected);
        } catch (SQLException e) {
            e.getStackTrace();
        }
        return entity;
    }

    /**
     * @param id
     * @return Returns a String that represents the SQL needed to retrieve one entity.
     * TheSQL must contain one SQL parameter, i.e. (тобто) "?", that will bind to the entity's ID
     */

    public Optional<T> findById(Long id) {
        T entity = null;
        try {
            PreparedStatement ps = connection.prepareStatement(getSQLByAnnotation(CrudOperation.FIND_BY_ID, this::getFindByIdSQL)/*getFindByIdSQL()*/);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();// returns a result set
            while (rs.next()) {
                entity = extractEntityFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(entity);
    }

    public List<T> findAll() {
        List<T> entities = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(getSQLByAnnotation(CrudOperation.FIND_ALL, this::getFindAllSQL));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entities.add(extractEntityFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entities;
    }

    public long count() {
        long count = 0;
        try {
            PreparedStatement ps = connection.prepareStatement(getSQLByAnnotation(CrudOperation.COUNT, this::getCountSql)/*getCountSql()*/);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public void delete(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSQLByAnnotation(CrudOperation.DELETE_ONE, this::getDeleteSQL));
            ps.setLong(1, getIdByAnnotation(entity));
//            ps.setLong(1, entity.getId());
            int affectedRecordCount = ps.executeUpdate();
            System.out.println(affectedRecordCount);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Long getIdByAnnotation(T entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .map(f -> {
                    f.setAccessible(true);
                    Long id = null;
                    try {
                        id = (long)f.get(entity);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return id;
                })
                .findFirst().orElseThrow(() -> new RuntimeException("No ID annotated file found"));
        // метод написаний для заміни виклику поля getId()
    }

    private void setIdByAnnotation(Long id, T entity) {
        Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .forEach(f -> {
                    f.setAccessible(true);
                    try {
                        f.set(entity, id);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Unable to set Id field value");
                    }
                });
    }

    public void delete(T... entities) {
        try {
            Statement statement = connection.createStatement();
            String ids = Arrays.stream(entities)
//                    .map(T::getId)
                    .map(e -> getIdByAnnotation(e))
                    .map(String::valueOf)
                    .collect(joining(","));
            int affectedRecordsCount = statement.executeUpdate(getSQLByAnnotation(CrudOperation.DELETE_MANY, this::getDeleteInSQL).replace(":ids", ids));
            System.out.println(affectedRecordsCount);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSQLByAnnotation(/*"mapForUpdate"*/ CrudOperation.UPDATE, this::getUpdateSql)/*getUpdateSql()*/);
            mapForUpdate(entity, ps);
            ps.setLong(5, getIdByAnnotation(entity));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Should return a SQL string like
     * "DELETE FROM PEOPLE WHERE ID IN (:ids)"
     * Be sure to include the '(:ids)' named parameter & call it 'ids'
     */
//    protected abstract String getDeleteInSQL();
    protected String getDeleteInSQL() {
        throw  new RuntimeException("SQL not defined");
    }
    protected String getDeleteSQL() {
        throw  new RuntimeException("SQL not defined");
    }
    protected String getCountSql() {
        throw  new RuntimeException("SQL not defined");
    }
    protected String getFindAllSQL() {
        throw  new RuntimeException("SQL not defined");
    }
    protected String getFindByIdSQL() {
        throw  new RuntimeException("SQL not defined");
    }
    //    protected abstract String getUpdateSql();
    protected String getUpdateSql() {
        throw  new RuntimeException("SQL not defined");
    }
    protected String getSaveSQL() {
        throw  new RuntimeException("SQL not defined");
    }

    abstract T extractEntityFromResultSet(ResultSet rs) throws SQLException;

    abstract void mapForSave(T entity, PreparedStatement ps) throws SQLException;

    abstract void mapForUpdate(T entity, PreparedStatement ps) throws SQLException;

}
