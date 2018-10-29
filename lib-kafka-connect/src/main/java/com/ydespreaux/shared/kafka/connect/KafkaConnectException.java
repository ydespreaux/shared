package com.ydespreaux.shared.kafka.connect;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Define a generic exception for kafka connect
 *
 * @since 1.0.0
 */
public class KafkaConnectException extends RuntimeException{

    /**
     * http code error
     */
    @Getter
    private final HttpStatus errorCode;


    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public KafkaConnectException(String message) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     *
     * @param errorCode
     * @param message
     */
    public KafkaConnectException(HttpStatus errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public KafkaConnectException(String message, Throwable cause) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }

    /**
     *
     * @param errorCode
     * @param message
     * @param cause
     */
    public KafkaConnectException(HttpStatus errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new runtime exception with the specified cause and a
     * detail message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>).  This constructor is useful for runtime exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public KafkaConnectException(Throwable cause) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }

    /**
     *
     * @param errorCode
     * @param cause
     */
    public KafkaConnectException(HttpStatus errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

}
