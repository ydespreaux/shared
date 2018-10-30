package com.ydespreaux.shared.data.jpa.criteria;

import lombok.*;

import javax.persistence.criteria.Predicate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCriteria implements ICriteria{

    /**
     *
     */
    private Predicate.BooleanOperator operator;

    /**
     *
     */
    private List<ICriteria> criteriaList;

    @Override
    public boolean isGrouped() {
        return true;
    }
}
