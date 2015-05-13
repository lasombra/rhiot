package zed.service.document.sdk;

import com.github.camellabs.iot.cloudlet.sdk.HealthCheck;
import com.github.camellabs.iot.cloudlet.sdk.ServiceDiscoveryException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.util.List;

import static com.github.camellabs.iot.cloudlet.sdk.Discoveries.discoverServiceUrl;
import static com.github.camellabs.iot.cloudlet.sdk.RestTemplates.defaultRestTemplate;
import static com.github.camellabs.iot.utils.Reflections.classOfArrayOfClass;
import static com.github.camellabs.iot.utils.Reflections.writeField;
import static java.lang.String.format;
import static zed.service.document.sdk.Pojos.pojoClassToCollection;

public class RestDocumentService<T> implements DocumentService<T> {

    private static final Logger LOG = LoggerFactory.getLogger(RestDocumentService.class);

    // Constants

    public static final int DEFAULT_DOCUMENT_SERVICE_PORT = 15001;

    public static final String DEFAULT_DOCUMENT_SERVICE_URL = "http://localhost:" + DEFAULT_DOCUMENT_SERVICE_PORT;

    // Configuration members

    @VisibleForTesting
    final String baseUrl;

    // Collaborators members

    private final RestOperations restClient;

    // Constructors

    public RestDocumentService(String baseUrl, RestOperations restClient) {
        this.baseUrl = baseUrlWithContextPath(baseUrl);
        this.restClient = restClient;
    }

    public RestDocumentService(String baseUrl) {
        this(baseUrl, defaultRestTemplate());
    }

    public RestDocumentService(int restApiPort) {
        this("http://localhost:" + restApiPort);
    }

    // Factory methods

    public static RestDocumentService discover() {
        String serviceUrl = discoverServiceUrl("document", DEFAULT_DOCUMENT_SERVICE_PORT, new HealthCheck() {
            @Override
            public void check(String serviceUrl) {
                new RestDocumentService<>(serviceUrl).count(RestDocumentServiceConnectivityTest.class);
            }
        });
        return new RestDocumentService<>(serviceUrl);
    }

    public static RestDocumentService discoverOrDefault() {
        try {
            return discover();
        } catch (ServiceDiscoveryException ex) {
            LOG.debug("Can't discover document service. Falling back to default URL {}.", DEFAULT_DOCUMENT_SERVICE_URL);
            return new RestDocumentService(DEFAULT_DOCUMENT_SERVICE_PORT);
        }
    }

    // Overridden

    @Override
    public T save(T document) {
        String id = restClient.postForObject(format("%s/save/%s", baseUrl, pojoClassToCollection(document.getClass())), document, String.class);
        writeField(document, "id", id);
        return document;
    }

    @Override
    public T findOne(Class<T> documentClass, String id) {
        return restClient.getForObject(format("%s/findOne/%s/%s", baseUrl, pojoClassToCollection(documentClass), id), documentClass);
    }

    @Override
    public List<T> findMany(Class<T> documentClass, String... ids) {
        T[] results = restClient.postForObject(format("%s/findMany/%s", baseUrl, pojoClassToCollection(documentClass)), new Ids(ids), classOfArrayOfClass(documentClass));
        return ImmutableList.copyOf(results);
    }

    @Override
    public long count(Class<?> documentClass) {
        return restClient.getForObject(format("%s/count/%s", baseUrl, pojoClassToCollection(documentClass)), Long.class);
    }

    @Override
    public List<T> findByQuery(Class<T> documentClass, QueryBuilder queryBuilder) {
        String collection = pojoClassToCollection(documentClass);
        T[] documents = restClient.postForObject(format("%s/findByQuery/%s", baseUrl, collection), queryBuilder, classOfArrayOfClass(documentClass));
        return ImmutableList.copyOf(documents);
    }

    @Override
    public long countByQuery(Class<T> documentClass, QueryBuilder queryBuilder) {
        return restClient.postForObject(format("%s/countByQuery/%s", baseUrl, pojoClassToCollection(documentClass)), queryBuilder, Long.class);
    }

    @Override
    public void remove(Class<T> documentClass, String id) {
        restClient.delete(format("%s/remove/%s/%s", baseUrl, pojoClassToCollection(documentClass), id));
    }

    // Helpers

    static String baseUrlWithContextPath(String baseUrl) {
        baseUrl = baseUrl.trim();
        return baseUrl + "/api/document";
    }

    private static final class RestDocumentServiceConnectivityTest {
    }

}

class Ids {

    String[] ids;

    public Ids(String[] ids) {
        this.ids = ids;
    }

    public Ids() {
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }

}