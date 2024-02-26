/* (C)2023 */
package org.transitclock.api.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.transitclock.service.dto.IpcBlock;

/**
 * A list of blocks
 *
 * @author Michael Smith
 */
@XmlRootElement(name = "blocks")
public class ApiBlocks {

    @XmlElement(name = "block")
    private List<ApiBlock> blocksData;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiBlocks() {}

    public ApiBlocks(Collection<IpcBlock> blocks) {
        blocksData = new ArrayList<ApiBlock>(blocks.size());
        for (IpcBlock block : blocks) {
            blocksData.add(new ApiBlock(block));
        }
    }
}
