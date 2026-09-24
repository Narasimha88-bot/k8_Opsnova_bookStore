package tech.opsnova.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.opsnova.catalog.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
