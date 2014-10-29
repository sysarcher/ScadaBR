package br.org.scadabr.json;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRemoteProperty {

    boolean read() default true;

    boolean write() default true;

    Class<? extends TypeFactory> typeFactory() default TypeFactory.class;

    Class<?> innerType() default Object.class;

    String alias() default "";
}
