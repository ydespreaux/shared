package com.ydespreaux.shared.data.jpa;

import com.ydespreaux.shared.data.ModelConverter;
import org.hibernate.annotations.QueryHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.Assert;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.*;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

/**
 * @param <T>
 * @author yoann.despréaux
 * @since 8.0.0
 */
public class JpaCustomRepository<T, K extends Serializable> extends SimpleJpaRepository<T, K> {

    protected final EntityManager em;

    /**
     * @param entityInformation
     * @param em
     */
    public JpaCustomRepository(JpaEntityInformation<T, ?> entityInformation, final EntityManager em) {
        super(entityInformation, em);
        this.em = em;
    }

    /**
     * @param spec
     * @param sort
     * @param fetchAssociations
     * @return
     */
    protected TypedQuery<T> getTypedQuery(Specification<T> spec, Sort sort, String... fetchAssociations) {
        return this.getTypedQuery(spec, getDomainClass(), sort, fetchAssociations);
    }

    /**
     * @param spec
     * @param domainClass
     * @param sort
     * @param fetchAssociations
     * @return
     */
    protected <S extends T> TypedQuery<S> getTypedQuery(Specification<S> spec, Class<S> domainClass, Sort sort, String... fetchAssociations) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<S> criteriaQuery = builder.createQuery(domainClass);
        Root<S> root = applySpecification(spec, domainClass, criteriaQuery);
        criteriaQuery.select(root);
        if (sort != null) {
            criteriaQuery.orderBy(toOrders(sort, root, builder));
        }
        TypedQuery<S> query = em.createQuery(criteriaQuery);
        if (fetchAssociations != null && fetchAssociations.length > 0) {
            query.setHint(QueryHints.LOADGRAPH, applyFetchAssociations(domainClass, fetchAssociations));
        }
        return query;
    }
    /**
     *
     * @param domainClass
     * @param associations
     * @param <S>
     * @return
     */
    private <S extends T> EntityGraph<S> applyFetchAssociations(Class<S> domainClass, String... associations){
        final EntityGraph<S> fetchGraph = em.createEntityGraph(domainClass);
        Map<String, Subgraph<?>> joinMap = new HashMap<>();
        for (String association : associations) {
            applyFetchAssociation(joinMap, fetchGraph, association, null, new StringBuilder());
        }
        return fetchGraph;
    }
    /**
     *
     * @param joinMap
     * @param root
     * @param association
     * @param currentSubGraph
     * @param path
     */
    private void applyFetchAssociation(Map<String, Subgraph<?>> joinMap, EntityGraph<?> root, String association, Subgraph<?> currentSubGraph, StringBuilder path){
        String[] fields = association.split("\\.");
        path.append(path.length() == 0 ? "" : ".").append(fields[0]);
        if (!joinMap.containsKey(path)){
            joinMap.put(path.toString(), currentSubGraph != null ? currentSubGraph.addSubgraph(fields[0]) : root.addSubgraph(fields[0]));
        }
        if (fields.length == 1){
            return;
        }
        applyFetchAssociation(joinMap, root, association.substring(fields[0].length() + 1), joinMap.get(path), path);
    }
    /**
     * Reads the given {@link TypedQuery} into a {@link Page} applying the given {@link Pageable} and
     * {@link Specification}.
     *
     * @param query    must not be {@literal null}.
     * @param spec     can be {@literal null}.
     * @param pageable can be {@literal null}.
     * @return
     */
    protected <S extends T, D> Page<D> readPage(TypedQuery<S> query, final Specification<S> spec, final Class<S> domainClass, ModelConverter<D, S> converter, Pageable pageable) {
        query.setFirstResult((int)pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return PageableExecutionUtils.getPage(transformResult(query, converter), pageable, () -> executeCountQuery(getCountQuery(spec, domainClass)));
    }

    /**
     * @param spec
     * @param domainClass
     * @param query
     * @param <S>
     * @param <U>
     * @return
     */
    private <S, U extends T> Root<U> applySpecification(Specification<U> spec, Class<U> domainClass,
                                                        CriteriaQuery<S> query) {
        Assert.notNull(domainClass, "Domain class must not be null!");
        Assert.notNull(query, "CriteriaQuery must not be null!");
        Root<U> root = query.from(domainClass);
        if (spec == null) {
            return root;
        }
        CriteriaBuilder builder = em.getCriteriaBuilder();
        Predicate predicate = spec.toPredicate(root, query, builder);
        if (predicate != null) {
            query.where(predicate);
        }
        return root;
    }

    /**
     * @param query
     * @return
     */
    private static Long executeCountQuery(TypedQuery<Long> query) {

        Assert.notNull(query, "TypedQuery must not be null!");

        List<Long> totals = query.getResultList();
        Long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }

        return total;
    }

    /**
     * @param query
     * @param converter
     * @return
     */
    protected <S extends T, D> List<D> transformResult(TypedQuery<S> query, ModelConverter<D, S> converter) {
        final List<D> transformers = new ArrayList<>();
        final List<S> entities = query.getResultList();
        entities.forEach(entity -> transformers.add(converter.convertToDTO(entity)));
        return transformers;
    }

    /**
     *
     * @param query
     * @param converter
     * @param <S>
     * @param <D>
     * @return
     */
    protected <S extends T, D> Optional<D> transformSingleResult(TypedQuery<S> query, ModelConverter<D, S> converter) {
        final List<S> entities = query.getResultList();
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(converter.convertToDTO(entities.get(0)));
    }

}
