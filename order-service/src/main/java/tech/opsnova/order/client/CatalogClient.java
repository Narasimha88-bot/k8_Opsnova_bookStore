package tech.opsnova.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tech.opsnova.order.dto.CatalogBook;
import tech.opsnova.order.dto.StockResponse;
import tech.opsnova.order.exception.BookNotFoundException;
import tech.opsnova.order.exception.CatalogUnavailableException;

/**
 * Talks to catalog-service over HTTP.
 *
 * The base URL comes from the externalised property {@code catalog.service.url}
 * (env {@code CATALOG_SERVICE_URL}), defaulting to the Kubernetes ClusterIP DNS
 * name {@code http://catalog-service:8080}. It is NEVER hardcoded - proving that
 * this DNS name resolves is exactly what this call demonstrates in class.
 *
 * Timeouts are short so that an unreachable catalog fails fast into a 503 rather
 * than hanging. Any transport failure becomes {@link CatalogUnavailableException};
 * we never let it crash the service.
 */
@Component
public class CatalogClient {

    private final RestClient restClient;
    private final String baseUrl;

    public CatalogClient(@Value("${catalog.service.url:http://catalog-service:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /** GET /api/books/{id}/stock - the named inter-service call used to verify availability. */
    public StockResponse getStock(Long bookId) {
        try {
            return restClient.get()
                    .uri("/api/books/{id}/stock", bookId)
                    .retrieve()
                    .body(StockResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new BookNotFoundException(bookId);
        } catch (RestClientException e) {
            throw new CatalogUnavailableException("catalog-service unreachable at " + baseUrl, e);
        }
    }

    /** GET /api/books/{id} - full book, used to read the price for the order total. */
    public CatalogBook getBook(Long bookId) {
        try {
            return restClient.get()
                    .uri("/api/books/{id}", bookId)
                    .retrieve()
                    .body(CatalogBook.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new BookNotFoundException(bookId);
        } catch (RestClientException e) {
            throw new CatalogUnavailableException("catalog-service unreachable at " + baseUrl, e);
        }
    }
}
