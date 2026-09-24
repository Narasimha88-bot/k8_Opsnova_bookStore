package tech.opsnova.catalog.service;

import java.util.List;
import org.springframework.stereotype.Service;
import tech.opsnova.catalog.exception.BookNotFoundException;
import tech.opsnova.catalog.model.Book;
import tech.opsnova.catalog.repository.BookRepository;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(Book book) {
        // Ignore any client-supplied id so create never overwrites an existing row.
        book.setId(null);
        return bookRepository.save(book);
    }

    public Book update(Long id, Book incoming) {
        Book book = findById(id);
        book.setTitle(incoming.getTitle());
        book.setAuthor(incoming.getAuthor());
        book.setIsbn(incoming.getIsbn());
        book.setPrice(incoming.getPrice());
        book.setStockCount(incoming.getStockCount());
        return bookRepository.save(book);
    }

    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }
}
