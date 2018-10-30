package com.ydespreaux.shared.data.jpa;

import com.ydespreaux.shared.data.jpa.criteria.GroupCriteria;
import com.ydespreaux.shared.data.jpa.criteria.ICriteria;
import com.ydespreaux.shared.data.jpa.criteria.SimpleCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author yoann.despréaux
 * @since 8.0.0
 */
public class SpecificationCriteria<T> implements Specification<T> {

    private static final long serialVersionUID = 3295157927853086841L;

    private List<ICriteria> criteres;

    public SpecificationCriteria(List<ICriteria> criteres) {
        this.criteres = criteres;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder cb) {
        if (this.criteres == null) {
            return null;
        }
        List<Predicate> restrictions = new ArrayList<>();
        for (ICriteria criteria : this.criteres) {
            Predicate predicate = toPredicate(criteria, root, cb);
            if (predicate != null) {
                restrictions.add(predicate);
            }
        }
        if (!restrictions.isEmpty()) {
            return cb.and(restrictions.toArray(new Predicate[restrictions.size()]));
        }
        return null;
    }

    /**
     *
     * @param criteria
     * @param root
     * @param cb
     * @return
     */
    protected Predicate toPredicate(ICriteria criteria, Root<T> root, CriteriaBuilder cb) {
        return criteria.isGrouped() ? toPredicate((GroupCriteria)criteria, root, cb) : toPredicate((SimpleCriteria)criteria, root, cb);
    }

    /**
     *
     * @param criteria
     * @param root
     * @param cb
     * @return
     */
    protected Predicate toPredicate(GroupCriteria criteria, Root<T> root, CriteriaBuilder cb) {
        List<Predicate> restrictions = new ArrayList<>();
        for (ICriteria child : criteria.getCriteriaList()) {
            Predicate predicate = child.isGrouped() ? toPredicate((GroupCriteria)child, root, cb) : toPredicate((SimpleCriteria) child, root, cb);
            if (predicate != null) {
                restrictions.add(predicate);
            }
        }
        if (!restrictions.isEmpty()) {
            return criteria.getOperator() == Predicate.BooleanOperator.AND ?
                    cb.and(restrictions.toArray(new Predicate[restrictions.size()])) :
                    cb.or(restrictions.toArray(new Predicate[restrictions.size()]));
        }
        return null;

    }

    /**
     * @param criteria
     * @param root
     * @param cb
     * @return
     */
    protected Predicate toPredicate(SimpleCriteria criteria, Root<T> root, CriteriaBuilder cb) {
        Path<?> path = getPath(root, splitProperties(criteria.getProperty().getPropertyPath()));
        return toPredicate(cb, path, criteria.getValue(), criteria.getOperator());
    }

    /**
     * @param root
     * @param properties
     * @param <Y>
     * @return
     */
    protected <Y> Path<Y> getPath(From<T, ?> root, String... properties) {
        if (properties.length == 1) {
            return root.get(properties[0]);
        }
        Join<T, ?> join = root.join(properties[0], JoinType.LEFT);
        String[] newProperties = new String[properties.length - 1];
        System.arraycopy(properties, 1, newProperties, 0, newProperties.length);
        return getPath(join, newProperties);
    }

    /**
     * @param builder
     * @param path
     * @param value
     * @param operator
     * @param <Y>
     * @return
     */
    private <Y, V> Predicate toPredicate(CriteriaBuilder builder, Path<Y> path, V value, SimpleCriteria.EnumOperator operator) {
        assertSearchCriteriaValue(operator, value);
        switch (operator) {
            case EQ:
                return builder.equal(path, value);
            case NOT_EQ:
                return builder.notEqual(path, value);
            case NOT_NULL:
                return builder.isNotNull(path);
            case NULL:
                return builder.isNull(path);
            case BETWEEN:
                return builder.between(castPath(path, Comparable.class), ((Comparable[]) value)[0], ((Comparable[]) value)[1]);
            case GREATER_OR_EQUALS:
                return builder.greaterThanOrEqualTo(castPath(path, Comparable.class), (Comparable) value);
            case GREATER_THAN:
                return builder.greaterThan(castPath(path, Comparable.class), (Comparable) value);
            case LESS_OR_EQUALS:
                return builder.lessThanOrEqualTo(castPath(path, Comparable.class), (Comparable) value);
            case LESS_THAN:
                return builder.lessThan(castPath(path, Comparable.class), (Comparable) value);
            case IN:
                if (value instanceof Object[]) {
                    return path.in((Object[]) value);
                } else {
                    return path.in((Collection<?>) value);
                }
            case NOT_IN:
                return builder.not(toPredicate(builder, path, value, SimpleCriteria.EnumOperator.IN));
            case CONTAINS:
            case LIKE:
                return builder.like(builder.upper(castPath(path, String.class)), "%" + ((String) value).toUpperCase() + "%");
            case END_WITH:
                return builder.like(builder.upper(castPath(path, String.class)), "%" + ((String) value).toUpperCase());
            case START_WITH:
                return builder.like(builder.upper(castPath(path, String.class)), ((String) value).toUpperCase() + "%");
        }
        return null;
    }

    /**
     *
     * @param operator
     * @param value
     * @param <V>
     */
//    @SuppressWarnings("unchecked")
    private <V> void assertSearchCriteriaValue(SimpleCriteria.EnumOperator operator, V value) {
        if (operator != SimpleCriteria.EnumOperator.NOT_NULL && operator != SimpleCriteria.EnumOperator.NULL) {
            Objects.requireNonNull(value, "value must not be null!!");
        }
    }

    /**
     *
     * @param path
     * @param valueType
     * @param <T>
     * @param <V>
     * @return
     */
    private <T,V> Expression<V> castPath(Path<T> path, Class<V> valueType) {
        if (valueType.isAssignableFrom(path.getJavaType())) {
            return (Path<V>)path;
        }
        throw new IllegalArgumentException(String.format("Could not convert java type [%s] to [%s]", valueType.getName(), path.getJavaType().getName()));
    }

    /**
     * @param propertyPath
     * @return
     */
    private String[] splitProperties(String propertyPath) {
        return propertyPath.split("\\.");
    }

}