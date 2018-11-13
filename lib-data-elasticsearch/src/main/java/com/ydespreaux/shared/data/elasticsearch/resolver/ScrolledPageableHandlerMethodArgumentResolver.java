package com.ydespreaux.shared.data.elasticsearch.resolver;


import com.ydespreaux.shared.data.elasticsearch.ScrolledPageable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

@Slf4j
public class ScrolledPageableHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    static final ScrolledPageable DEFAULT_PAGE_REQUEST = ScrolledPageable.of(20);
    private static final SortHandlerMethodArgumentResolver DEFAULT_SORT_RESOLVER = new SortHandlerMethodArgumentResolver();
    private static final String INVALID_DEFAULT_PAGE_SIZE = "Invalid default page size configured for method %s! Must not be less than one!";
    private static final String DEFAULT_SIZE_PARAMETER = "pageSize";
    private static final String DEFAULT_SCROLL_ID_PARAMETER = "scrollId";
    private static final String DEFAULT_SCROLL_TIME_PARAMETER = "scrollTimeInMinutes";
    private static final String DEFAULT_PREFIX = "";
    private static final String DEFAULT_QUALIFIER_DELIMITER = "_";
    private static final int DEFAULT_MAX_PAGE_SIZE = 2000;
    private ScrolledPageable fallbackPageable;
    private SortArgumentResolver sortResolver;
    private String sizeParameterName;
    private String scrollIdParameterName;
    private String scrollTimeParameterName;
    private String prefix;
    private String qualifierDelimiter;
    private int maxPageSize;
    private boolean oneIndexedParameters;

    public ScrolledPageableHandlerMethodArgumentResolver() {
        this((SortArgumentResolver) null);
    }

//    @Nullable
//    @Override
//    public ScrolledPageable resolveArgument(MethodParameter methodParameter, @Nullable ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, @Nullable WebDataBinderFactory webDataBinderFactory) throws Exception {
//        return ScrolledPageable.of(20);
//    }

    public ScrolledPageableHandlerMethodArgumentResolver(SortHandlerMethodArgumentResolver sortResolver) {
        this((SortArgumentResolver) sortResolver);
    }

    public ScrolledPageableHandlerMethodArgumentResolver(@Nullable SortArgumentResolver sortResolver) {
        this.fallbackPageable = DEFAULT_PAGE_REQUEST;
        this.sizeParameterName = DEFAULT_SIZE_PARAMETER;
        this.scrollIdParameterName = DEFAULT_SCROLL_ID_PARAMETER;
        this.scrollTimeParameterName = DEFAULT_SCROLL_TIME_PARAMETER;
        this.prefix = "";
        this.qualifierDelimiter = "_";
        this.maxPageSize = 2000;
        this.oneIndexedParameters = false;
        this.sortResolver = sortResolver == null ? DEFAULT_SORT_RESOLVER : sortResolver;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return ScrolledPageable.class.equals(methodParameter.getParameterType());
    }

    public void setFallbackPageable(ScrolledPageable fallbackPageable) {
        Assert.notNull(fallbackPageable, "Fallback Pageable must not be null!");
        this.fallbackPageable = fallbackPageable;
    }

    public boolean isFallbackPageable(Pageable pageable) {
        return this.fallbackPageable != null && this.fallbackPageable.equals(pageable);
    }

    protected int getMaxPageSize() {
        return this.maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    protected String getSizeParameterName() {
        return this.sizeParameterName;
    }

    public void setSizeParameterName(String sizeParameterName) {
        Assert.hasText(sizeParameterName, "Size parameter name must not be null or empty!");
        this.sizeParameterName = sizeParameterName;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }

    public void setQualifierDelimiter(String qualifierDelimiter) {
        this.qualifierDelimiter = qualifierDelimiter == null ? "_" : qualifierDelimiter;
    }

    protected boolean isOneIndexedParameters() {
        return this.oneIndexedParameters;
    }

    public void setOneIndexedParameters(boolean oneIndexedParameters) {
        this.oneIndexedParameters = oneIndexedParameters;
    }

    public ScrolledPageable resolveArgument(MethodParameter methodParameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
//        Optional<ScrolledPageable> defaultOrFallback = this.getDefaultFromAnnotationOrFallback(methodParameter).toOptional();
        Optional<ScrolledPageable> defaultOrFallback = this.fallbackPageable.optional();
        String pageSizeString = webRequest.getParameter(this.getParameterNameToUse(this.sizeParameterName, methodParameter));
        String scrollTimeString = webRequest.getParameter(this.getParameterNameToUse(this.scrollTimeParameterName, methodParameter));
        String scrollId = webRequest.getParameter(this.getParameterNameToUse(this.scrollIdParameterName, methodParameter));
        Optional<Integer> pageSize = this.parseAndApplyBoundaries(pageSizeString, this.maxPageSize, false);
        Optional<Integer> scrollTime = !StringUtils.hasText(scrollTimeString) ? Optional.empty() : Optional.of(Integer.parseInt(scrollTimeString));

        int size = pageSize.orElseGet(() -> defaultOrFallback.map(Pageable::getPageSize).orElseThrow(IllegalStateException::new));
        size = size < 1 ? defaultOrFallback.map(Pageable::getPageSize).orElseThrow(IllegalStateException::new) : size;
        size = size > this.maxPageSize ? this.maxPageSize : size;

        int time = scrollTime.orElseGet(() -> defaultOrFallback.map(ScrolledPageable::getScrollTimeInMinutes).orElseThrow(IllegalStateException::new));
        Sort sort = this.sortResolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
        return ScrolledPageable.of(time, size, sort.isSorted() ? sort : defaultOrFallback.map(Pageable::getSort).orElseGet(Sort::unsorted), scrollId);
    }

//    private ScrolledPageable getDefaultFromAnnotationOrFallback(MethodParameter methodParameter) {
//        PageableDefault defaults = (PageableDefault)methodParameter.getParameterAnnotation(PageableDefault.class);
//        return defaults != null ? getDefaultPageRequestFrom(methodParameter, defaults) : this.fallbackPageable;
//    }

    protected String getParameterNameToUse(String source, @Nullable MethodParameter parameter) {
        StringBuilder builder = new StringBuilder(this.prefix);
        Qualifier qualifier = parameter == null ? null : parameter.getParameterAnnotation(Qualifier.class);
        if (qualifier != null) {
            builder.append(qualifier.value());
            builder.append(this.qualifierDelimiter);
        }
        return builder.append(source).toString();
    }

    private Optional<Integer> parseAndApplyBoundaries(@Nullable String parameter, int upper, boolean shiftIndex) {
        if (!StringUtils.hasText(parameter)) {
            return Optional.empty();
        } else {
            try {
                int parsed = Integer.parseInt(parameter) - (this.oneIndexedParameters && shiftIndex ? 1 : 0);
                return Optional.of(parsed < 0 ? 0 : (parsed > upper ? upper : parsed));
            } catch (NumberFormatException var5) {
                return Optional.of(0);
            }
        }
    }

}

