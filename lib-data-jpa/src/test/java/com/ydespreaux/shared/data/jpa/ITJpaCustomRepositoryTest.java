package com.ydespreaux.shared.data.jpa;import com.ydespreaux.shared.data.jpa.criteria.GroupCriteria;import com.ydespreaux.shared.data.jpa.criteria.ICriteria;import com.ydespreaux.shared.data.jpa.criteria.SimpleCriteria;import com.ydespreaux.shared.testcontainers.mysql.MySQLContainer;import com.ydespreaux.shared.data.jpa.configuration.JpaConfiguration;import com.ydespreaux.shared.data.jpa.configuration.entities.Author;import com.ydespreaux.shared.data.jpa.configuration.entities.Book;import com.ydespreaux.shared.data.jpa.configuration.entities.BookDTO;import com.ydespreaux.shared.data.jpa.configuration.repository.AuthorRepository;import com.ydespreaux.shared.data.jpa.configuration.repository.BookRepository;import org.junit.Before;import org.junit.ClassRule;import org.junit.Test;import org.junit.runner.RunWith;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.boot.test.context.SpringBootTest;import org.springframework.data.domain.Page;import org.springframework.data.domain.PageRequest;import org.springframework.data.domain.Pageable;import org.springframework.data.domain.Sort;import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;import javax.persistence.criteria.Predicate;import java.time.LocalDate;import java.time.ZoneId;import java.util.ArrayList;import java.util.Arrays;import java.util.Date;import java.util.List;import static org.junit.Assert.assertEquals;import static org.junit.Assert.assertNull;@RunWith(SpringJUnit4ClassRunner.class)@SpringBootTest(classes = JpaConfiguration.class)public class ITJpaCustomRepositoryTest {    @ClassRule    public static final MySQLContainer mySqlContainer = new MySQLContainer();    @Autowired    private BookRepository repository;    @Autowired    private AuthorRepository authorRepository;    private Author nicolasBeuglet = null;    private Author elenaFerrante = null;    private Author harlanCoben = null;    private Book leCri = null;    private Book amieProdigieuse = null;    private Book sansDefense = null;    private Book walkingDead28 = null;    private Book walkingDead29 = null;    @Before    public void onSetUp() {        this.repository.deleteAllInBatch();        this.authorRepository.deleteAllInBatch();        nicolasBeuglet = createAuthorAndSave("Nicolas", "Beuglet");        elenaFerrante = createAuthorAndSave("Elena", "Ferrante");        harlanCoben = createAuthorAndSave("Harlan", "Coben");        leCri = this.createBookAndSave(                "Le cri",                "À quelques kilomètres d'Oslo, l'hôpital psychiatrique de Gaustad dresse sa masse sombre parmi les pins enneigés. Appelée sur place pour un suicide, l'inspectrice Sarah Geringën pressent d'emblée que rien ne concorde. Le patient 488, ainsi surnommé suivant les chiffres cicatrisés qu'il porte sur le front, s'est figé dans la mort, un cri muet aux lèvres – un cri de peur primale. Soumise à un compte à rebours implacable, Sarah va découvrir une vérité vertigineuse sur l'une des questions qui hante chacun d'entre nous : la vie...",                Book.Genre.THRILLER,                "Pocket",                nicolasBeuglet,                8.20,                buildPublication(2018, 1, 11));        amieProdigieuse = this.createBookAndSave(                "L’amie Prodigieuse - Tome 3 : Celle qui fuit et celle qui reste",                "Après L'amie prodigieuse et Le nouveau nom, Celle qui fuit et celle qui reste est la suite de la formidable saga dans laquelle Elena Ferrante raconte cinquante ans d'histoire italienne et d'amitié entre ses deux héroïnes, Elena et Lila. Pour Elena, comme pour l'Italie, une période de grands bouleversements s'ouvre. Nous sommes à la fin des années soixante, les événements de 1968 s'annoncent, les mouvements féministes et protestataires s'organisent, et Elena, diplômée de l'Ecole normale de Pise et entourée d'universitaires,...",                Book.Genre.FICTION,                "Gallimard",                elenaFerrante,                8.30,                buildPublication(2018, 1, 25));        sansDefense = this.createBookAndSave(                "Sans défense",                "Deux enfants kidnappés. Un inconnu qui réapparaît. Après dix ans d'angoisse, le cauchemar ne fait que commencer...",                Book.Genre.THRILLER,                "Belfond",                harlanCoben,                21.90,                buildPublication(2018, 3, 1));        walkingDead28 = this.createBookAndSave("Walking Dead - Tome 28 : Walking Dead",                "La Colline a été dévastée et la communauté qui l'habitait a du fuir les lieux, sous l'impulsion de Maggie. Dwight a rejoint Rick, en lui affirmant que les Chuchoteurs ont été anéantis. Malheureusement, même si Beta - qui a pris la tête des Chuchoteurs - a perdu une bataille, il lance une horde de rôdeurs sur Alexandria. La guerre est peut-être terminée, mais la survie d'Alexandria est en jeu...",                Book.Genre.FANTASTIQUE,                "Delcourt",                null,                14.95,                buildPublication(2017, 10, 14));        walkingDead29 = this.createBookAndSave("Walking Dead - Tome 29 : La Ligne blanche",                "Carl ne parvient pas à admettre la mort d'Andrea. Tandis que Rick fait au mieux, Maggie n'accepte pas sa décision de laisser Negan en liberté et le fait étroitement surveillé. Eugene contacte Stephanie par radio et ils conviennent de se rencontrer. À la suite de ces tragiques événements, Rick envisage d'établir une communauté dans l'Ohio. Une nouvelle ère débute pour les survivants de l'apocalypse...",                Book.Genre.FANTASTIQUE,                "Delcourt",                null,                14.95,                buildPublication(2018, 3, 7));    }    @Test    public void searchByTitleWithMultiCriteres(){        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.TITLE).operator(SimpleCriteria.EnumOperator.CONTAINS).value("Walking Dead").build());        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.DESCRIPTION).operator(SimpleCriteria.EnumOperator.CONTAINS).value("La Colline").build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 2));        assertEquals(1, result.getTotalElements());        assertEquals(1, result.getNumberOfElements());        checkBook(this.walkingDead28, result.getContent().get(0));    }    @Test    public void searchByTitleWithMultiCriteres_grouped(){        List<ICriteria> criteres = new ArrayList<>();        criteres.add(GroupCriteria.builder()                .operator(Predicate.BooleanOperator.OR)                .criteriaList(Arrays.asList(                        SimpleCriteria.builder().property(Book.BookPropertyPath.TITLE).operator(SimpleCriteria.EnumOperator.CONTAINS).value("Walking Dead").build(),                        SimpleCriteria.builder().property(Book.BookPropertyPath.DESCRIPTION).operator(SimpleCriteria.EnumOperator.CONTAINS).value("La Colline").build()                ))                .build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 2));        assertEquals(2, result.getTotalElements());        assertEquals(2, result.getNumberOfElements());        checkBook(this.walkingDead28, result.getContent().get(0));        checkBook(this.walkingDead29, result.getContent().get(1));    }    @Test    public void searchByTitleWithEq(){        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.TITLE).operator(SimpleCriteria.EnumOperator.EQ).value("Sans défense").build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 2));        assertEquals(1, result.getTotalElements());        assertEquals(1, result.getNumberOfElements());        checkBook(this.sansDefense, result.getContent().get(0));    }    @Test    public void searchByGenreWithEq(){        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.GENRE).operator(SimpleCriteria.EnumOperator.EQ).value(Book.Genre.THRILLER).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 2));        assertEquals(2, result.getTotalElements());        assertEquals(2, result.getNumberOfElements());        checkBook(this.leCri, result.getContent().get(0));        checkBook(this.sansDefense, result.getContent().get(1));    }    @Test    public void searchByGenreWithNotEq(){        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.GENRE).operator(SimpleCriteria.EnumOperator.NOT_EQ).value(Book.Genre.THRILLER).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 2));        assertEquals(3, result.getTotalElements());        assertEquals(2, result.getNumberOfElements());        checkBook(this.amieProdigieuse, result.getContent().get(0));        checkBook(this.walkingDead28, result.getContent().get(1));        Page<BookDTO> result1 = this.repository.search(criteres, createPageable(1, 2));        assertEquals(3, result1.getTotalElements());        assertEquals(1, result1.getNumberOfElements());        checkBook(this.walkingDead29, result1.getContent().get(0));    }    @Test    public void searchByGenreWithIn(){        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.GENRE).operator(SimpleCriteria.EnumOperator.IN).value(new Book.Genre[]{Book.Genre.THRILLER, Book.Genre.FICTION}).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(3, result.getTotalElements());        assertEquals(3, result.getNumberOfElements());        checkBook(this.leCri, result.getContent().get(0));        checkBook(this.amieProdigieuse, result.getContent().get(1));        checkBook(this.sansDefense, result.getContent().get(2));    }    @Test    public void searchByGenreWithNotIn(){        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.GENRE).operator(SimpleCriteria.EnumOperator.NOT_IN).value(new Book.Genre[]{Book.Genre.THRILLER, Book.Genre.FICTION}).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(2, result.getTotalElements());        assertEquals(2, result.getNumberOfElements());        checkBook(this.walkingDead28, result.getContent().get(0));        checkBook(this.walkingDead29, result.getContent().get(1));    }    @Test    public void searchByAuthorWithNull(){        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.AUTHOR).operator(SimpleCriteria.EnumOperator.NULL).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 2));        assertEquals(2, result.getTotalElements());        assertEquals(2, result.getNumberOfElements());        checkBook(this.walkingDead28, result.getContent().get(0));        checkBook(this.walkingDead29, result.getContent().get(1));    }    @Test    public void searchByAuthorWithNotNull(){        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.AUTHOR).operator(SimpleCriteria.EnumOperator.NOT_NULL).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(3, result.getTotalElements());        assertEquals(3, result.getNumberOfElements());        checkBook(this.leCri, result.getContent().get(0));        checkBook(this.amieProdigieuse, result.getContent().get(1));        checkBook(this.sansDefense, result.getContent().get(2));    }    @Test    public void searchByPriceWithBetween() {        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.PRICE).operator(SimpleCriteria.EnumOperator.BETWEEN).value(new Double[]{10.0, 20.0}).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(2, result.getTotalElements());        assertEquals(2, result.getNumberOfElements());        checkBook(this.walkingDead28, result.getContent().get(0));        checkBook(this.walkingDead29, result.getContent().get(1));    }    @Test    public void searchByPriceWithGreater() {        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.PRICE).operator(SimpleCriteria.EnumOperator.GREATER_THAN).value(21.90).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(0, result.getTotalElements());        assertEquals(0, result.getNumberOfElements());    }    @Test    public void searchByPriceWithGreaterOrEquals() {        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.PRICE).operator(SimpleCriteria.EnumOperator.GREATER_OR_EQUALS).value(21.90).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(1, result.getTotalElements());        assertEquals(1, result.getNumberOfElements());        checkBook(this.sansDefense, result.getContent().get(0));    }    @Test    public void searchByPriceWithLess() {        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.PRICE).operator(SimpleCriteria.EnumOperator.LESS_THAN).value(8.30).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(1, result.getTotalElements());        assertEquals(1, result.getNumberOfElements());        checkBook(this.leCri, result.getContent().get(0));    }    @Test    public void searchByPriceWithLessOrEquals() {        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.PRICE).operator(SimpleCriteria.EnumOperator.LESS_OR_EQUALS).value(8.30).build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(2, result.getTotalElements());        assertEquals(2, result.getNumberOfElements());        checkBook(this.leCri, result.getContent().get(0));        checkBook(this.amieProdigieuse, result.getContent().get(1));    }    @Test    public void searchByDescriptionWithContains() {        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.DESCRIPTION).operator(SimpleCriteria.EnumOperator.CONTAINS).value("Rick").build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(2, result.getTotalElements());        assertEquals(2, result.getNumberOfElements());        checkBook(this.walkingDead28, result.getContent().get(0));        checkBook(this.walkingDead29, result.getContent().get(1));    }    @Test    public void searchByTitleWithStartWith() {        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.TITLE).operator(SimpleCriteria.EnumOperator.START_WITH).value("Walking").build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(2, result.getTotalElements());        assertEquals(2, result.getNumberOfElements());        checkBook(this.walkingDead28, result.getContent().get(0));        checkBook(this.walkingDead29, result.getContent().get(1));    }    @Test    public void searchByTitleWithEndWith() {        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.TITLE).operator(SimpleCriteria.EnumOperator.END_WITH).value("reste").build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(1, result.getTotalElements());        assertEquals(1, result.getNumberOfElements());        checkBook(this.amieProdigieuse, result.getContent().get(0));    }    @Test    public void searchByPublicationWithBetween() {        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.PUBLICATION)                .operator(SimpleCriteria.EnumOperator.BETWEEN)                .value(new Date[]{buildPublication(2018, 1, 1), buildPublication(2018, 2, 1)})                .build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(2, result.getTotalElements());        assertEquals(2, result.getNumberOfElements());        checkBook(this.leCri, result.getContent().get(0));        checkBook(this.amieProdigieuse, result.getContent().get(1));    }    @Test    public void searchByAuthor() {        List<ICriteria> criteres = new ArrayList<>();        criteres.add(SimpleCriteria.builder().property(Book.BookPropertyPath.AUTHOR_LASTNAME)                .operator(SimpleCriteria.EnumOperator.CONTAINS)                .value("coben")                .build());        Page<BookDTO> result = this.repository.search(criteres, createPageable(0, 5));        assertEquals(1, result.getTotalElements());        assertEquals(1, result.getNumberOfElements());        checkBook(this.sansDefense, result.getContent().get(0));    }    /**     *     * @param firstName     * @param lastName     * @return     */    private Author createAuthor(String firstName, String lastName) {        Author author = new Author();        author.setFirstName(firstName);        author.setLastName(lastName);        return author;    }    /**     *     * @param firstName     * @param lastName     * @return     */    private Author createAuthorAndSave(String firstName, String lastName) {        return this.authorRepository.save(createAuthor(firstName, lastName));    }    /**     *     * @param title     * @param description     * @param genre     * @param editor     * @param author     * @param price     * @param publication     * @return     */    private Book createBook(String title, String description, Book.Genre genre, String editor, Author author, Double price, Date publication) {        return Book.builder()                .title(title)                .description(description)                .genre(genre)                .editor(editor)                .author(author)                .price(price)                .publication(publication)                .build();    }    /**     *     * @param title     * @param description     * @param genre     * @param editor     * @param author     * @param price     * @param publication     * @return     */    private Book createBookAndSave(String title, String description, Book.Genre genre, String editor, Author author, Double price, Date publication) {        return this.repository.save(createBook(title, description, genre, editor, author, price, publication));    }    /**     *     * @param reference     * @param other     */    private void checkBook(Book reference, BookDTO other) {        assertEquals(reference.getTitle(), other.getTitle());        assertEquals(reference.getDescription(), other.getDescription());        if (reference.getAuthor() == null) {            assertNull(other.getAuthor());        }else{            assertEquals(reference.getAuthor().getFirstName() + " " + reference.getAuthor().getLastName(), other.getAuthor());        }        assertEquals(reference.getEditor(), other.getEditor());        assertEquals(reference.getPrice(), other.getPrice());        assertEquals(reference.getPublication(), other.getPublication());        assertEquals(reference.getGenre(), other.getGenre());        assertEquals(reference.getId(), other.getId());    }    /**     *     * @param year     * @param month     * @param day     * @return     */    private Date buildPublication(int year, int month, int day){        return Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());    }    /**     *     * @return     */    private Pageable createPageable(int page, int size){        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "title"));    }}