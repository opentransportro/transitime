/* (C)2023 */
package org.transitclock.avl.amigocloud;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For getting the results of a http request.
 *
 * @author AmigoCloud
 */
@SuppressWarnings("deprecation")
public class AmigoRest {
    private static final Logger logger = LoggerFactory.getLogger(AmigoRest.class);

    /********************** Member Functions **************************/
    private static DefaultHttpClient getThreadSafeClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
        return client;
    }

    private HttpClient httpclient = getThreadSafeClient();

    private String apiToken;

    /**
     * Constructor
     *
     * @param token
     */
    public AmigoRest(String token) {
        apiToken = token;
    }

    /**
     * Gets result of HTTP request to the URL and returns it as a string.
     *
     * @param url
     * @return The resulting string from the HTTP request. Will be null if there was a problem
     *     connecting.
     */
    public String get(String url) {
        String uri;
        uri = url + "?token=" + apiToken;
        HttpGet httpget = new HttpGet(uri);
        try {
            HttpResponse response = httpclient.execute(httpget);

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Queried url \"{}\" and it returned \"{}|\"", url, result.toString());
            }
            return result.toString();
        } catch (Exception e) {
            logger.error("Exception when requesting URI {} . {}", uri, e.getMessage(), e);
        }
        return null;
    }
}
