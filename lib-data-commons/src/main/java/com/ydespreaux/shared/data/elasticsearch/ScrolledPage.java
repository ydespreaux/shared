package com.ydespreaux.shared.data.elasticsearch;import java.util.List;public interface ScrolledPage<T> {    String getScrollId();    Long getTotalElements();    List<T> getContent();    Boolean hasContent();}