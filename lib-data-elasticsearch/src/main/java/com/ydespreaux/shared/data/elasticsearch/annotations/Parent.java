package com.ydespreaux.shared.data.elasticsearch.annotations;

import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * Parent
 *
 * @since 1.1.0
 * @author Yoann Despréaux
 */

@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parent {
}
