/* (C)2023 */
package org.transitclock.avl;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.transitclock.configData.AgencyConfig;
import org.transitclock.configData.AvlConfig;
import org.transitclock.db.structs.AvlReport;
import org.transitclock.utils.IntervalTimer;
import org.transitclock.utils.Time;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

/**
 * Subclass of AvlModule to be used when reading AVL data from a feed. Calls the abstract method
 * getAndProcessData() for the subclass to actually get data from the feed. The getAndProcessData()
 * should call processAvlReport(avlReport) for each AVL report read in. If in JMS mode then it
 * outputs the data to the appropriate JMS topic so that it can be read from an AvlClient. If not in
 * JMS mode then uses a BoundedExecutor with multiple threads to directly call AvlClient.run().
 *
 * @author Michael Smith (michael@transitclock.org)
 */
@Slf4j
public abstract class PollUrlAvlModule extends AvlModule {


    // Usually want to use compression when reading data but for some AVL
    // feeds might be binary where don't want additional compression. A
    // superclass can override this value.
    protected boolean useCompression = true;

    protected PollUrlAvlModule(String agencyId) {
        super(agencyId);
    }

    /**
     * Feed specific URL to use when accessing data. Will often be overridden by subclass.
     *
     * @return
     */
    protected String getUrl() {
        return AvlConfig.url.getValue();
    }

    /**
     * Override this method if AVL feed needs to specify header info
     *
     * @param con
     */
    protected void setRequestHeaders(URLConnection con) {}

    /**
     * Actually processes the data from the InputStream. Called by getAndProcessData(). Should be
     * overwritten unless getAndProcessData() is overwritten by superclass.
     *
     * @param in The input stream containing the AVL data
     * @return List of AvlReports read in
     * @throws Exception Throws a generic exception since the processing is done in the abstract
     *     method processData() and it could throw any type of exception since we don't really know
     *     how the AVL feed will be processed.
     */
    protected abstract Collection<AvlReport> processData(InputStream in) throws Exception;

    /**
     * Converts the input stream into a JSON string. Useful for when processing a JSON feed.
     *
     * @param in
     * @return the JSON string
     * @throws IOException
     * @throws JSONException
     */
    protected String getJsonString(InputStream in) throws IOException, JSONException {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = streamReader.readLine()) != null) responseStrBuilder.append(inputStr);

        String responseStr = responseStrBuilder.toString();
        logger.debug("JSON={}", responseStr);
        return responseStr;
    }

    /**
     * Actually reads data from feed and processes it by opening up a URL specified by getUrl() and
     * then reading the contents. Calls the abstract method processData() to actually process the
     * input stream.
     *
     * <p>This method needs to be overwritten if not real data from a URL
     *
     * @throws Exception Throws a generic exception since the processing is done in the abstract
     *     method processData() and it could throw any type of exception since we don't really know
     *     how the AVL feed will be processed.
     */
    protected void getAndProcessData() throws Exception {
        // For logging
        IntervalTimer timer = new IntervalTimer();

        // Get from the AVL feed subclass the URL to use for this feed
        String fullUrl = getUrl();

        // Log what is happening
        logger.info("Getting data from feed using url=" + fullUrl);

        // Create the connection
        URL url = new URL(fullUrl);
        URLConnection con = url.openConnection();

        // Set the timeout so don't wait forever
        int timeoutMsec = AvlConfig.getAvlFeedTimeoutInMSecs();
        con.setConnectTimeout(timeoutMsec);
        con.setReadTimeout(timeoutMsec);

        // Request compressed data to reduce bandwidth used
        if (useCompression) con.setRequestProperty("Accept-Encoding", "gzip,deflate");

        // If authentication being used then set user and password
        if (AvlConfig.authenticationUser.getValue() != null && AvlConfig.authenticationPassword.getValue() != null) {
            String authString = AvlConfig.authenticationUser.getValue() + ":" + AvlConfig.authenticationPassword.getValue();
            byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            con.setRequestProperty("Authorization", "Basic " + authStringEnc);
        }

        // Set any additional AVL feed specific request headers
        setRequestHeaders(con);

        // Create appropriate input stream depending on whether content is
        // compressed or not
        InputStream in = con.getInputStream();
        if ("gzip".equals(con.getContentEncoding())) {
            in = new GZIPInputStream(in);
        }

        // For debugging
        logger.debug("Time to access inputstream {} msec", timer.elapsedMsec());

        // Call the abstract method to actually process the data
        timer.resetTimer();
        Collection<AvlReport> avlReportsReadIn = processData(in);
        in.close();
        logger.debug("Time to parse document {} msec", timer.elapsedMsec());

        // Process all the reports read in
        if (AvlConfig.shouldProcessAvl.getValue()) {
            processAvlReports(avlReportsReadIn);
        }
    }

    /**
     * Does all of the work for the class. Runs forever and reads in AVL data from feed and
     * processes it.
     */
    @Override
    public void run() {
        // Log that module successfully started
        logger.info("Started module {} for agencyId={}", getClass().getName(), getAgencyId());

        while (true) {
            IntervalTimer timer = new IntervalTimer();

            try {
                // Process data
                getAndProcessData();
            } catch (SocketTimeoutException e) {
                logger.error(
                        "Error for agencyId={} accessing AVL feed using URL={} " + "with a timeout of {} msec.",
                        AgencyConfig.getAgencyId(),
                        getUrl(),
                        AvlConfig.getAvlFeedTimeoutInMSecs(),
                        e);
            } catch (Exception e) {
                logger.error("Error accessing AVL feed using URL={}.", getUrl(), e);
            }

            // Wait appropriate amount of time till poll again
            long elapsedMsec = timer.elapsedMsec();
            long sleepTime = AvlConfig.getSecondsBetweenAvlFeedPolling() * Time.MS_PER_SEC - elapsedMsec;
            if (sleepTime < 0) {
                logger.warn("Supposed to have a polling rate of {} msec but processing previous data took {} msec so polling again immediately.", AvlConfig.getSecondsBetweenAvlFeedPolling() * Time.MS_PER_SEC, elapsedMsec);
            } else {
                Time.sleep(sleepTime);
            }
        }
    }
}
