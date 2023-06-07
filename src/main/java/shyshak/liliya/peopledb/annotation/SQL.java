package shyshak.liliya.peopledb.annotation;

import shyshak.liliya.peopledb.model.CrudOperation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MultiSQL.class)
public @interface SQL {
    String value();
//    int age() default 30;
    CrudOperation operationType();
}
