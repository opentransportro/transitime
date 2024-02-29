/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import org.transitclock.service.dto.IpcActiveBlock;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A route for when outputting active blocks
 *
 * @author SkiBu Smith
 */
@Data
public class ApiActiveBlocksRoute {

    // ID of route
    @XmlAttribute
    private String id;

    // Route short name
    @XmlAttribute
    private String shortName;

    // Name of route
    @XmlAttribute
    private String name;

    // The active blocks for the route
    @XmlElement(name = "block")
    private List<ApiActiveBlock> activeBlocks;

    /********************** Member Functions **************************/

    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiActiveBlocksRoute() {}

    public ApiActiveBlocksRoute(String id, String shortName, String name) {
        this.id = id;
        this.shortName = shortName;
        this.name = name;
        activeBlocks = new ArrayList<>();
    }

    public void add(IpcActiveBlock ipcActiveBlock, String agencyId)
            throws IllegalAccessException, InvocationTargetException {
        activeBlocks.add(new ApiActiveBlock(ipcActiveBlock, agencyId));
    }
}
