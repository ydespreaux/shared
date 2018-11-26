package com.ydespreaux.shared.data.jpa.configuration.repository;import com.ydespreaux.shared.data.jpa.configuration.entities.BookDTO;import com.ydespreaux.shared.data.jpa.criteria.ICriteria;import org.springframework.data.domain.Page;import org.springframework.data.domain.Pageable;import java.util.List;import java.util.Optional;public interface BookRepositoryCustom {    /**     * @param criteres     * @param pageable     * @return     */    Page<BookDTO> search(List<ICriteria> criteres, Pageable pageable);    /**     *     * @param bookId     * @return     */    Optional<BookDTO> findBookDTOById(Long bookId);}