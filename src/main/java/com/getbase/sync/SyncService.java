package com.getbase.sync;

import com.getbase.http.HttpClient;
import com.getbase.http.HttpMethod;
import com.getbase.http.Response;
import com.getbase.serializer.JsonDeserializer;
import com.getbase.serializer.JsonSerializer;
import com.getbase.services.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.getbase.utils.Precondition.*;

public class SyncService extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    public static final String DEVICE_HEADER = "X-Basecrm-Device-UUID";

    public SyncService(HttpClient httpClient) {
        super(httpClient);
    }

    public Session start(String deviceUUID) {
        checkNotNull(deviceUUID, "deviceUUID parameter must not be null");
        checkArgument(!deviceUUID.trim().isEmpty(), "deviceUUID must not be empty");

        String url = "/sync/start";
        Response response = this.httpClient.request(HttpMethod.POST,
                url,
                null,
                buildHeaders(deviceUUID),
                null);

        if (response.getHttpStatus() == 204) return null;
        return SessionDeserializer.deserialize(JsonDeserializer.deserializeRaw(response.getBody()));
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetch(String deviceUUID, String sessionId) {
        checkNotNull(deviceUUID, "deviceUUID parameter must not be null");
        checkArgument(!deviceUUID.trim().isEmpty(), "deviceUUID must not be empty");
        checkNotNull(sessionId, "sessionId parameter must not be null");
        checkArgument(!sessionId.trim().isEmpty(), "sessionId must not be empty");

        String url = String.format(Locale.US, "/sync/%s/queues/main", sessionId);
        Response response = this.httpClient.request(HttpMethod.GET,
                url,
                null,
                buildHeaders(deviceUUID),
                null);

        // nothing new to synchronize
        if (response.getHttpStatus() == 204) {
            return null;
        }

        Map<String, Object> attributes = JsonDeserializer.deserializeRaw(response.getBody());

        // sanity check
        if (attributes == null || attributes.get("items") == null) {
            log.warn("Items missing in response or empty body returned from sync. HTTP status: {}", response.getHttpStatus());
            return Collections.emptyList();
        }

        final List<Map<String, Object>> items = (List<Map<String, Object>>) attributes.get("items");
        if (items.isEmpty()) {
            log.warn("Empty item collection returned from sync. HTTP status: {}", response.getHttpStatus());
        }
        return items;
    }

    public boolean ack(String deviceUUID, List<String> ackKeys) {
        checkNotNull(deviceUUID, "deviceUUID parameter must not be null");
        checkNotNull(ackKeys, "ackKeys parameter must not be null");
        checkArgument(!deviceUUID.trim().isEmpty(), "deviceUUID must not be empty");

        if (ackKeys.isEmpty()) return true;

        String url = "/sync/ack";

        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("ack_keys", ackKeys);
        String serialized = JsonSerializer.serialize(attributes);

        Response response = this.httpClient.request(HttpMethod.POST,
                url,
                null,
                buildHeaders(deviceUUID),
                serialized);

        boolean acked = response.getHttpStatus() == 202;
        if (!acked) {
            log.warn("Failed to ack with status {}", response.getHttpStatus());
        }
        return acked;
    }

    private Map<String, String> buildHeaders(String deviceUUID) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(DEVICE_HEADER, deviceUUID);
        return headers;
    }

    public static final class SessionDeserializer {

        @SuppressWarnings("unchecked")
        public static Session deserialize(Map<String, Object> root) {
            if (root == null || root.get("data") == null) {
                return null;
            }

            Map<String, Object> attributes = (Map<String, Object>)root.get("data");
            String sessionId = (String)attributes.get("id");

            List<Queue> queues = new ArrayList<Queue>();

            for (Map<String, Object> item : (List<Map<String, Object>>)attributes.get("queues")) {
                queues.add(QueueDeserializer.deserialize(item));
            }

            return new Session(sessionId, queues);
        }
    }


    public static final class QueueDeserializer {

        @SuppressWarnings("unchecked")
        public static Queue deserialize(Map<String, Object> root) {
            if (root == null || root.get("data") == null) {
                return null;
            }

            Map<String, Object> attributes = (Map<String, Object>)root.get("data");
            String name = (String)attributes.get("name");
            Integer pages = (Integer)attributes.get("pages");
            Integer totalCount = (Integer)attributes.get("total_count");

            return new Queue(name, pages.longValue(), totalCount.longValue());
        }
    }
}
