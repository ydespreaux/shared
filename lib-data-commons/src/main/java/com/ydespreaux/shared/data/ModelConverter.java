package com.ydespreaux.shared.data;

import org.springframework.core.convert.converter.Converter;

public interface ModelConverter<D, T> extends Converter<D, T> {

    /**
     * @param source
     * @return
     */
    D convertToDTO(T source);
}
