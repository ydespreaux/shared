package com.ydespreaux.shared.data.jpa.criteria;

import lombok.*;

/**
 * Specification criteria in the sense of Domain Driven Design.
 *
 * @author yoann.despreaux
 * @since 8.0.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleCriteria implements ICriteria {

    @Override
    public boolean isGrouped() {
        return false;
    }

    /**
     * Enul for kind fo Operator.
     */
    public enum EnumOperator {
        EQ, LIKE, START_WITH, END_WITH, CONTAINS, NOT_EQ, NULL, NOT_NULL, BETWEEN, GREATER_THAN, GREATER_OR_EQUALS, LESS_THAN, LESS_OR_EQUALS, IN, NOT_IN
    }

    public interface EnumPropertyPath {
        /**
         * @return
         */
        String getPropertyPath();
    }

    /**
     * Property name
     */
    private EnumPropertyPath property;
    /**
     * Criteria value
     */
    private Object value;
    /**
     * Operator
     */
    private EnumOperator operator;

}
