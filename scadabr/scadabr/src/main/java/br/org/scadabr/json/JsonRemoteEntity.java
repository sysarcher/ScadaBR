package br.org.scadabr.json;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRemoteEntity {

    Class<? extends TypeFactory> typeFactory() default TypeFactory.class;

    Class<? extends TypeConverter> typeConverter() default TypeConverter.class;
}
