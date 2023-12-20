/* (C)2023 */
package org.transitclock.avl;

import java.io.InputStream;
import java.util.Collection;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.transitclock.core.domain.AvlReport;

/**
 * Polls XML feed for AVL data. To be overridden with subclass that specifies URL to use and how to
 * process the data.
 *
 * <p>If in playback then feed will not be run since will be getting AVL data from db instead of
 * from AVL feed.
 *
 * @author SkiBu Smith
 */
public abstract class XmlPollingAvlModule extends PollUrlAvlModule {

    /**
     * Constructor
     *
     * @param agencyId
     */
    public XmlPollingAvlModule(String agencyId) {
        super(agencyId);
    }

    /**
     * Extracts the AVL data from the XML document. Uses JDOM to parse the XML because it makes the
     * Java code much simpler.
     *
     * @param doc
     * @return Collection of AvlReports
     * @throws NumberFormatException
     */
    protected abstract Collection<AvlReport> extractAvlData(Document doc) throws NumberFormatException;

    /**
     * Reads in the XML document using the specified input stream.
     *
     * @param in The input stream containing the AVL data
     * @return Collection of AvlReports
     * @throws Exception Throws a generic exception since the processing is done in the abstract
     *     method processData() and it could throw any type of exception since we don't really know
     *     how the AVL feed will be processed.
     */
    @Override
    protected Collection<AvlReport> processData(InputStream in) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(in);

        // Have the AVL feed subclass processes the document and extract the AVL data
        return extractAvlData(doc);
    }
}
