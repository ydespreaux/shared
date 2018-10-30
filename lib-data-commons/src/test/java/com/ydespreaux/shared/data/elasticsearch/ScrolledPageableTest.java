package com.ydespreaux.shared.data.elasticsearch;

import com.ydespreaux.shared.data.elasticsearch.ScrolledPageable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class ScrolledPageableTest {
    @Test
    public void ofPageSize() throws Exception {
        ScrolledPageable pageable = ScrolledPageable.of(50);
        assertThat(pageable.getPageSize(), is(equalTo(50)));
        assertThat(pageable.getScrollId(), is(nullValue()));
        assertThat(pageable.getScrollTimeInMinutes(), is(equalTo(60)));
        assertThat(pageable.getSort(), is(notNullValue()));
        assertThat(pageable.getSort().isSorted(), is(false));
    }

    @Test
    public void ofPageSizeAndSort() throws Exception {
        ScrolledPageable pageable = ScrolledPageable.of(50, Sort.by(Sort.Direction.ASC, "property"));
        assertThat(pageable.getPageSize(), is(equalTo(50)));
        assertThat(pageable.getScrollId(), is(nullValue()));
        assertThat(pageable.getScrollTimeInMinutes(), is(equalTo(60)));
        assertThat(pageable.getSort(), is(notNullValue()));
        assertThat(pageable.getSort().isSorted(), is(true));
        assertThat(pageable.getSort().getOrderFor("property"), is(notNullValue()));
        assertThat(pageable.getSort().getOrderFor("property").getDirection(), is(equalTo(Sort.Direction.ASC)));
    }

    @Test
    public void ofPageSizeAndDirection() throws Exception {
        ScrolledPageable pageable = ScrolledPageable.of(50, Sort.Direction.ASC, "property");
        assertThat(pageable.getPageSize(), is(equalTo(50)));
        assertThat(pageable.getScrollId(), is(nullValue()));
        assertThat(pageable.getScrollTimeInMinutes(), is(equalTo(60)));
        assertThat(pageable.getSort(), is(notNullValue()));
        assertThat(pageable.getSort().isSorted(), is(true));
        assertThat(pageable.getSort().getOrderFor("property"), is(notNullValue()));
        assertThat(pageable.getSort().getOrderFor("property").getDirection(), is(equalTo(Sort.Direction.ASC)));
    }
    @Test(expected = IllegalArgumentException.class)
    public void ofPageSizeAndDirectionNullValue() throws Exception {
        ScrolledPageable.of(50, null, "property");
    }

    @Test
    public void ofPageSizeAndSortNullable() throws Exception {
        ScrolledPageable pageable = ScrolledPageable.of(50, null);
        assertThat(pageable.getPageSize(), is(equalTo(50)));
        assertThat(pageable.getScrollId(), is(nullValue()));
        assertThat(pageable.getScrollTimeInMinutes(), is(equalTo(60)));
        assertThat(pageable.getSort(), is(notNullValue()));
        assertThat(pageable.getSort().isSorted(), is(false));
    }

    @Test
    public void ofScrollTimeAndPageSize() throws Exception {
        ScrolledPageable pageable = ScrolledPageable.of(5000,50);
        assertThat(pageable.getPageSize(), is(equalTo(50)));
        assertThat(pageable.getScrollId(), is(nullValue()));
        assertThat(pageable.getScrollTimeInMinutes(), is(equalTo(5000)));
        assertThat(pageable.getSort(), is(notNullValue()));
        assertThat(pageable.getSort().isSorted(), is(false));
    }

    @Test
    public void ofScrollTimeAndPageSizeAndSort() throws Exception {
        ScrolledPageable pageable = ScrolledPageable.of(5000,50, Sort.by(Sort.Direction.ASC, "property"));
        assertThat(pageable.getPageSize(), is(equalTo(50)));
        assertThat(pageable.getScrollId(), is(nullValue()));
        assertThat(pageable.getScrollTimeInMinutes(), is(equalTo(5000)));
        assertThat(pageable.getSort(), is(notNullValue()));
        assertThat(pageable.getSort().isSorted(), is(true));
        assertThat(pageable.getSort().getOrderFor("property"), is(notNullValue()));
        assertThat(pageable.getSort().getOrderFor("property").getDirection(), is(equalTo(Sort.Direction.ASC)));
    }
}