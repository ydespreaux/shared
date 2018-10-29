package com.ydespreaux.shared.testcontainers.common;

import org.testcontainers.containers.Container;

/**
 *
 * @since 1.0.0
 * @param <SELF>
 */
public interface IContainer<SELF extends IContainer<SELF>> extends Container<SELF> {


    /**
     *
     * @param registerProperties
     * @return
     */
    SELF withRegisterSpringbootProperties(boolean registerProperties);

    /**
     *
     * @return
     */
    boolean registerSpringbootProperties();

    /**
     *
     * @return
     */
    String getURL();

    /**
     *
     * @return
     */
    String getInternalURL();
}
