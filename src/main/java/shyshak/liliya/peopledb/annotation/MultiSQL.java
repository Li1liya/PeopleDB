package shyshak.liliya.peopledb.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MultiSQL{
    SQL[] value();
}

// інтерфейс створений спеціально для того, щоб можна було додавати кілька анотацій до кожного з методів