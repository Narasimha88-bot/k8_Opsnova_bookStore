package tech.opsnova.catalog.config;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tech.opsnova.catalog.model.Book;
import tech.opsnova.catalog.repository.BookRepository;

/**
 * Seeds the DevOps tools & technologies catalog at startup, but only when the
 * table is empty. Each row is one tool: {@code author} holds its category,
 * {@code isbn} its licence, and {@code logo} the slug of its logo SVG under /img.
 * (The JPA entity keeps its original field names so the /api/books contract and
 * order-service integration are unchanged.)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final BookRepository bookRepository;

    public DataSeeder(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) {
        long existing = bookRepository.count();
        if (existing > 0) {
            log.info("Catalog already contains {} tools, skipping seed", existing);
            return;
        }

        // name, category, licence, price (INR), stock, logo slug (/img/<slug>.svg)
        List<Book> seed = List.of(
                tool("Docker", "Container Runtime", "Apache-2.0", "4999", 25, "docker"),
                tool("Kubernetes", "Container Orchestration", "Apache-2.0", "8499", 18, "kubernetes"),
                tool("Terraform", "Infrastructure as Code", "BUSL-1.1", "6299", 12, "terraform"),
                tool("Ansible", "Configuration Management", "GPL-3.0", "5499", 20, "ansible"),
                tool("Jenkins", "CI/CD Automation", "MIT", "3999", 15, "jenkins"),
                tool("Prometheus", "Monitoring", "Apache-2.0", "4299", 9, "prometheus"),
                tool("Grafana", "Observability", "AGPL-3.0", "5999", 7, "grafana"),
                tool("Helm", "Kubernetes Packaging", "Apache-2.0", "2999", 22, "helm"),
                tool("Git", "Version Control", "GPL-2.0", "1999", 30, "git"),
                tool("Linux", "Operating System", "GPL-2.0", "3499", 40, "linux")
        );

        bookRepository.saveAll(seed);
        log.info("Seeded {} tools into the catalog", seed.size());
    }

    private Book tool(String name, String category, String licence, String price, int stock, String logo) {
        Book b = new Book();
        b.setTitle(name);
        b.setAuthor(category);
        b.setIsbn(licence);
        b.setPrice(new BigDecimal(price));
        b.setStockCount(stock);
        b.setLogo(logo);
        return b;
    }
}
