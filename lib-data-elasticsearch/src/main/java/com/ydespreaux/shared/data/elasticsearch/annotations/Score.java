package com.ydespreaux.shared.data.elasticsearch.annotations;

import org.springframework.data.annotation.ReadOnlyProperty;

import java.lang.annotation.*;

/**
 * Specifies that this field is used for storing the document score.
 *
 * @since 1.1.0
 * @author Yoann Despréaux
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Inherited
@ReadOnlyProperty
public @interface Score {}
