package tech.opsnova.catalog.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Data;

/**
 * A book in the catalog. Plain JPA entity - the only Lombok used is @Data,
 * which gives us getters, setters, toString, equals/hashCode and a no-arg
 * constructor (all that JPA needs).
 */
@Entity
@Table(name = "books")
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;      // tool name
    private String author;     // category (e.g. "Container Runtime")
    private String isbn;        // licence (e.g. "Apache-2.0")
    private BigDecimal price;
    private Integer stockCount;
    private String logo;       // logo slug -> /img/<logo>.svg
}
