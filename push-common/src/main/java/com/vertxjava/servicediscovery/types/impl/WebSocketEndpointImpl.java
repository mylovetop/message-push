package com.vertxjava.servicediscovery.types.impl;

import com.vertxjava.servicediscovery.types.WebSocketEndpoint;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.AbstractServiceReference;
import io.vertx.servicediscovery.types.HttpLocation;
import io.vertx.servicediscovery.types.impl.HttpEndpointImpl;
import io.vertx.servicediscovery.utils.ClassLoaderUtils;

import java.util.Objects;

/**
 * @author Jack
 * @create 2017-12-25 15:41
 **/
public class WebSocketEndpointImpl implements WebSocketEndpoint {
    @Override
    public String name() {
        return WebSocketEndpoint.TYPE;
    }

    @Override
    public ServiceReference get(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject configuration) {
        Objects.requireNonNull(vertx);
        Objects.requireNonNull(record);
        Objects.requireNonNull(discovery);
        return new HttpEndpointReference(vertx, discovery, record, configuration);
    }

    private class HttpEndpointReference extends AbstractServiceReference<HttpClient> {

        private final HttpLocation location;
        private final JsonObject config;

        private Object retrieved;

        HttpEndpointReference(Vertx vertx, ServiceDiscovery discovery, Record record, JsonObject config) {
            super(vertx, discovery, record);
            this.config = config;
            this.location = new HttpLocation(record.getLocation());
        }

        /**
         * Gets a HTTP client to access the service.
         *
         * @return the HTTP client, configured to access the service
         */
        @Override
        public HttpClient retrieve() {
            HttpClientOptions options;
            if (config != null) {
                options = new HttpClientOptions(config);
            } else {
                options = new HttpClientOptions();
            }
            options.setDefaultPort(location.getPort()).setDefaultHost(location.getHost());
            if (location.isSsl()) {
                options.setSsl(true);
            }

            return vertx.createHttpClient(options);
        }

        /**
         * Closes the client.
         */
        @Override
        public synchronized void onClose() {
            service.close();
            retrieved = null;
        }

        @Override
        public synchronized <X> X getAs(Class<X> x) {
            Object svc = get();

            if (x == null || x.isInstance(svc)) {
                retrieved = svc;
                return (X) svc;
            } else {
                X client = ClassLoaderUtils.createWithDelegate(x, svc);
                retrieved = client;
                return client;
            }
        }

        /**
         * GGets the service object if already retrieved. It won't try to acquire the service object if not retrieved yet. Unlike
         * {@link #cached()} this method let you configure the type of object you want to retrieve. This parameter must match
         * the expected service type, and must pass the "polyglot" version of the class.
         *
         * @param x the
         * @return the object to access the service
         */
        @Override
        public synchronized <X> X cachedAs(Class<X> x) {
            Object svc = cached();

            if (svc == null) {
                return null;
            }

            if (retrieved != null  && x.isInstance(retrieved)) {
                return x.cast(retrieved);
            }

            if (x == null || x.isInstance(svc)) {
                return (X) svc;
            } else {
                return ClassLoaderUtils.createWithDelegate(x, svc);
            }
        }

        @Override
        public synchronized boolean isHolding(Object object) {
            // Because some language may use proxy, we compare hashCode and equals
            return service != null  && (object.hashCode() == service.hashCode()  || object.equals(service))
                    || retrieved != null  && (retrieved.hashCode() == object.hashCode()  || object.equals(retrieved));
        }
    }
}
