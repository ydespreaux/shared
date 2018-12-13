package com.ydespreaux.shared.data.elasticsearch.core.converter.adapter;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.*;
import java.util.Objects;

public class TimeTypeAdapterRegistry {

    /**
     * The specific genericized type for {@code LocalDate}.
     */
    public static final Type LOCAL_DATE_TYPE = new TypeToken<LocalDate>() {
    }.getType();

    /**
     * The specific genericized type for {@code LocalDateTime}.
     */
    public static final Type LOCAL_DATE_TIME_TYPE = new TypeToken<LocalDateTime>() {
    }.getType();

    /**
     * The specific genericized type for {@code LocalTime}.
     */
    public static final Type LOCAL_TIME_TYPE = new TypeToken<LocalTime>() {
    }.getType();

    /**
     * The specific genericized type for {@code OffsetDateTime}.
     */
    public static final Type OFFSET_DATE_TIME_TYPE = new TypeToken<OffsetDateTime>() {
    }.getType();

    /**
     * The specific genericized type for {@code OffsetTime}.
     */
    public static final Type OFFSET_TIME_TYPE = new TypeToken<OffsetTime>() {
    }.getType();

    /**
     * The specific genericized type for {@code ZonedDateTime}.
     */
    public static final Type ZONED_DATE_TIME_TYPE = new TypeToken<ZonedDateTime>() {
    }.getType();

    /**
     * The specific genericized type for {@code Instant}.
     */
    public static final Type INSTANT_TYPE = new TypeToken<Instant>() {
    }.getType();

    /**
     * Registers all the Java Time converters.
     *
     * @param builder The GSON builder to register the converters with.
     * @return A reference to {@code builder}.
     */
    public static GsonBuilder registerAll(GsonBuilder builder) {
        Objects.requireNonNull(builder, "builder cannot be null");
        registerLocalDate(builder);
        registerLocalDateTime(builder);
        registerLocalTime(builder);
        registerOffsetDateTime(builder);
        registerOffsetTime(builder);
        registerZonedDateTime(builder);
        registerInstant(builder);
        return builder;
    }

    /**
     * Registers the {@link LocalDateTypeAdapter} converter.
     *
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    public static GsonBuilder registerLocalDate(GsonBuilder builder) {
        return builder.registerTypeAdapter(LOCAL_DATE_TYPE, new LocalDateTypeAdapter());
    }

    /**
     * Registers the {@link LocalDateTimeTypeAdapter} converter.
     *
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    public static GsonBuilder registerLocalDateTime(GsonBuilder builder) {
        return builder.registerTypeAdapter(LOCAL_DATE_TIME_TYPE, new LocalDateTimeTypeAdapter());
    }

    /**
     * Registers the {@link LocalTimeTypeAdapter} converter.
     *
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    public static GsonBuilder registerLocalTime(GsonBuilder builder) {
        return builder.registerTypeAdapter(LOCAL_TIME_TYPE, new LocalTimeTypeAdapter());
    }

    /**
     * Registers the {@link OffsetDateTimeTypeAdapter} converter.
     *
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    public static GsonBuilder registerOffsetDateTime(GsonBuilder builder) {
        return builder.registerTypeAdapter(OFFSET_DATE_TIME_TYPE, new OffsetDateTimeTypeAdapter());
    }

    /**
     * Registers the {@link OffsetTimeTypeAdapter} converter.
     *
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    public static GsonBuilder registerOffsetTime(GsonBuilder builder) {
        return builder.registerTypeAdapter(OFFSET_TIME_TYPE, new OffsetTimeTypeAdapter());
    }

    /**
     * Registers the {@link ZonedDateTimeTypeAdapter} converter.
     *
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    public static GsonBuilder registerZonedDateTime(GsonBuilder builder) {
        return builder.registerTypeAdapter(ZONED_DATE_TIME_TYPE, new ZonedDateTimeTypeAdapter());
    }

    /**
     * Registers the {@link InstantTypeAdapter} converter.
     *
     * @param builder The GSON builder to register the converter with.
     * @return A reference to {@code builder}.
     */
    public static GsonBuilder registerInstant(GsonBuilder builder) {
        return builder.registerTypeAdapter(INSTANT_TYPE, new InstantTypeAdapter());
    }
}
