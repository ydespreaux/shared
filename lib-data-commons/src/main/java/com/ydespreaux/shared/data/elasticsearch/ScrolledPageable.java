package com.ydespreaux.shared.data.elasticsearch;import lombok.Getter;import lombok.Setter;import org.springframework.data.domain.PageRequest;import org.springframework.data.domain.Sort;import java.util.Optional;public class ScrolledPageable extends PageRequest {    private static final Integer DEFAULT_SCROLL_TIME_VALUE = 60;    private static final long serialVersionUID = -5506726303312911158L;    @Getter @Setter    private String scrollId;    @Setter    private Integer scrollTimeInMinutes = DEFAULT_SCROLL_TIME_VALUE;    /**     * Creates a new {@link PageRequest}. Pages are zero indexed, thus providing 0 for {@code page} will return the first     * page.     *     * @deprecated use static method of(...)     * @param size the size of the page to be returned.     */    @Deprecated    public ScrolledPageable(int size) {        super(0, size);    }    /**     * Creates a new {@link PageRequest} with sort parameters applied.     *     * @param size       the size of the page to be returned.     * @param direction  the direction of the {@link Sort} to be specified, can be {@literal null}.     * @param properties the properties to sort by, must not be {@literal null} or empty.     * @deprecated use static method of(...)     */    @Deprecated    public ScrolledPageable(int size, Sort.Direction direction, String... properties) {        super(0, size, direction, properties);    }    /**     * Creates a new {@link PageRequest} with sort parameters applied.     *     * @param size the size of the page to be returned.     * @param sort can be {@literal null}.     * @deprecated use static method of(...)     */    private ScrolledPageable(int size, Sort sort) {        super(0, size, sort);    }    /**     *     * @param size     * @return     */    public static ScrolledPageable of(int size) {        return of(size, Sort.unsorted());    }    /**     *     * @param size     * @param sort     * @return     */    public static ScrolledPageable of(int size, Sort sort) {        return new ScrolledPageable(size, sort == null ? Sort.unsorted() : sort);    }    public static ScrolledPageable of(int size, Sort.Direction direction, String... properties) {        return of(size, Sort.by(direction, properties));    }    public static ScrolledPageable of(int scrollTimeInMinutes, int size) {        return of(scrollTimeInMinutes, size, (Sort)null, null);    }    public static ScrolledPageable of(int scrollTimeInMinutes, int size, Sort sort) {        return of(scrollTimeInMinutes, size, sort, null);    }    public static ScrolledPageable of(int scrollTimeInMinutes, int size, Sort sort, String scrollId) {        ScrolledPageable pageable = of(size, sort);        pageable.setScrollTimeInMinutes(scrollTimeInMinutes);        pageable.setScrollId(scrollId);        return pageable;    }    /**     *     * @return     */    public Integer getScrollTimeInMinutes() {        if (scrollTimeInMinutes == null) {            return DEFAULT_SCROLL_TIME_VALUE;        }        return scrollTimeInMinutes;    }    public Optional<ScrolledPageable> optional() {        return this.isUnpaged() ? Optional.empty() : Optional.of(this);    }}