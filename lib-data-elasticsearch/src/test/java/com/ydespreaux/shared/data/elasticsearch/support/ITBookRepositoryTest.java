package com.ydespreaux.shared.data.elasticsearch.support;import com.ydespreaux.shared.data.elasticsearch.ScrolledPage;import com.ydespreaux.shared.data.elasticsearch.ScrolledPageable;import com.ydespreaux.shared.data.autoconfigure.elasticsearch.JestElasticsearchAutoConfiguration;import com.ydespreaux.shared.data.autoconfigure.elasticsearch.JestElasticsearchDataAutoConfiguration;import com.ydespreaux.shared.data.elasticsearch.ElasticsearchOperations;import com.ydespreaux.shared.data.elasticsearch.configuration.ElasticsearchBookConfiguration;import com.ydespreaux.shared.data.elasticsearch.entities.Book;import com.ydespreaux.shared.data.elasticsearch.repositories.BookRepository;import org.elasticsearch.index.query.QueryBuilders;import org.junit.Before;import org.junit.Test;import org.junit.runner.RunWith;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.data.domain.Sort;import org.springframework.test.annotation.DirtiesContext;import org.springframework.test.context.junit4.SpringRunner;import java.util.ArrayList;import java.util.List;import java.util.Optional;import java.util.concurrent.ExecutionException;import static org.junit.Assert.*;@DirtiesContext@RunWith(SpringRunner.class)@SpringBootTest(classes = {JestElasticsearchAutoConfiguration.class, JestElasticsearchDataAutoConfiguration.class, ElasticsearchBookConfiguration.class})public class ITBookRepositoryTest {    @Autowired    private BookRepository repository;    @Autowired    private ElasticsearchOperations elasticsearchOperations;    @Before    public void setUp(){        this.repository.deleteAll();    }    @Test    public void findById() {        Book bookIndexed = indexBook("Livre1", "Description du livre 1", 10.5d);        this.repository.refresh();        Optional<Book> optionalBook = this.repository.findById(bookIndexed.getDocumentId());        assertTrue(optionalBook.isPresent());        Book bookLoaded = optionalBook.get();        assertEquals(bookIndexed.getDocumentId(), bookLoaded.getDocumentId());        assertEquals(bookIndexed.getTitle(), bookLoaded.getTitle());        assertEquals(bookIndexed.getDescription(), bookLoaded.getDescription());        assertEquals(bookIndexed.getPrice(), bookLoaded.getPrice());        assertEquals(new Integer(1), bookLoaded.getVersion());    }    @Test    public void save() {        Book book = createBook("Livre1", "Description du livre 1", 10.5d);        Book bookIndexed = this.repository.save(book);        this.repository.refresh();        assertNotNull(bookIndexed.getDocumentId());        assertEquals(bookIndexed.getTitle(), book.getTitle());        assertEquals(bookIndexed.getDescription(), book.getDescription());        assertEquals(bookIndexed.getPrice(), book.getPrice());        assertEquals(new Integer(1), bookIndexed.getVersion());    }    @Test    public void save_bulk()  {        List<Book> books = new ArrayList<>();        books.add(createBook("Livre1", "Description du livre 1", 10.5d));        books.add(createBook("Livre2", "Description du livre 2", 8d));        books.add(createBook("Livre3", "Description du livre 3", 20d));        books.add(createBook("Livre4", "Description du livre 4", 5d));        books.add(createBook("Livre5", "Description du livre 5", 8.5d));        List<Book> booksIndexed =this.repository.save(books);        this.repository.refresh();        assertEquals(books.size(), booksIndexed.size());        for (Book bookIndexed : booksIndexed) {            assertNotNull(bookIndexed.getDocumentId());            assertEquals(new Integer(1), bookIndexed.getVersion());        }    }    @Test    public void deleteById() throws ExecutionException {        Book bookIndexed = indexBook("Livre1", "Description du livre 1", 10.5d);        this.repository.refresh();        this.repository.deleteById(bookIndexed.getDocumentId());        this.repository.refresh();        Optional<Book> optional = this.repository.findById(bookIndexed.getDocumentId());        assertFalse(optional.isPresent());    }    @Test    public void delete() throws ExecutionException {        Book bookIndexed = indexBook("Livre1", "Description du livre 1", 10.5d);        this.repository.refresh();        this.repository.delete(bookIndexed);        this.repository.refresh();        Optional<Book> optional = this.repository.findById(bookIndexed.getDocumentId());        assertFalse(optional.isPresent());    }    @Test    public void deleteAll() throws ExecutionException {        List<Book> books = new ArrayList<>();        books.add(indexBook("Livre1", "Description du livre 1", 10.5d));        books.add(indexBook("Livre2", "Description du livre 2", 10.5d));        books.add(indexBook("Livre3", "Description du livre 3", 10.5d));        this.repository.refresh();        this.repository.deleteAll(books);        this.repository.refresh();        for (Book bookIndexed : books) {            Optional<Book> optional = this.repository.findById(bookIndexed.getDocumentId());            assertFalse(optional.isPresent());        }    }    @Test    public void searchByPrice_withoutSort(){        List<Book> books = new ArrayList<>();        books.add(indexBook("Livre1", "Description du livre 1", 5d));        books.add(indexBook("Livre2", "Description du livre 2", 8d));        books.add(indexBook("Livre3", "Description du livre 3", 12.5d));        books.add(indexBook("Livre4", "Description du livre 4", 20d));        books.add(indexBook("Livre5", "Description du livre 5", 6d));        this.repository.refresh();        List<Book> result = this.repository.search(QueryBuilders.rangeQuery("price").from(5).to(10), (Sort)null);        assertEquals(3, result.size());        for (Book book : result) {            assertNotNull(book.getDocumentId());            assertNotNull(book.getVersion());        }    }    @Test    public void searchByPrice_withSort(){        List<Book> books = new ArrayList<>();        books.add(indexBook("Livre1", "Description du livre 1", 5d));        books.add(indexBook("Livre2", "Description du livre 2", 8d));        books.add(indexBook("Livre3", "Description du livre 3", 12.5d));        books.add(indexBook("Livre4", "Description du livre 4", 20d));        books.add(indexBook("Livre5", "Description du livre 5", 6d));        this.repository.refresh();        List<Book> result = this.repository.search(QueryBuilders.rangeQuery("price").from(5).to(10), Sort.by(Sort.Direction.DESC, "price"));        assertEquals(3, result.size());        for (Book book : result) {            assertNotNull(book.getDocumentId());            assertNotNull(book.getVersion());        }        assertEquals("Livre2", result.get(0).getTitle());        assertEquals("Livre5", result.get(1).getTitle());        assertEquals("Livre1", result.get(2).getTitle());    }    @Test    public void searchByTitle_withScroll() {        List<Book> books = new ArrayList<>();        books.add(indexBook("Livre1", "Description du livre 1", 5d));        books.add(indexBook("Livre2", "Description du livre 2", 8d));        books.add(indexBook("Livre3", "Description du livre 3", 12.5d));        books.add(indexBook("Livre4", "Description du livre 4", 20d));        books.add(indexBook("Livre5", "Description du livre 5", 6d));        this.repository.refresh();        ScrolledPageable pageable = ScrolledPageable.of(2, Sort.by(Sort.Direction.ASC, "title"));        pageable.setScrollTimeInMinutes(1);        ScrolledPage<Book> result = this.repository.search(QueryBuilders.matchAllQuery(), pageable);        String scrollId = result.getScrollId();        assertNotNull(scrollId);        assertEquals(new Integer(5), result.getTotalElements());        assertEquals(2, result.getContent().size());        assertEquals("Livre1", result.getContent().get(0).getTitle());        assertEquals("Livre2", result.getContent().get(1).getTitle());        pageable.setScrollId(scrollId);        result = this.repository.search(pageable);        assertEquals(new Integer(5), result.getTotalElements());        assertEquals(2, result.getContent().size());        assertEquals("Livre3", result.getContent().get(0).getTitle());        assertEquals("Livre4", result.getContent().get(1).getTitle());        result = this.repository.search(pageable);        assertEquals(new Integer(5), result.getTotalElements());        assertEquals(1, result.getContent().size());        assertEquals("Livre5", result.getContent().get(0).getTitle());        result = this.repository.search(pageable);        assertEquals(new Integer(5), result.getTotalElements());        assertEquals(0, result.getContent().size());    }    /**     *     * @param title     * @param description     * @param price     * @return     */    private Book createBook(String title, String description, Double price) {        return Book.builder()                .title(title)                .description(description)                .price(price)                .build();    }    private Book indexBook(String title, String description, Double price) {        return this.elasticsearchOperations.index(createBook(title, description, price), Book.class);    }}