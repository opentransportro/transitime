/* (C)2023 */
package org.transitclock.api.data;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import org.transitclock.service.dto.IpcBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A list of terse blocks, without trip pattern or schedule info
 *
 * @author Michael Smith
 */
@Data
@XmlRootElement(name = "blocks")
public class ApiBlocksTerse {
    @XmlElement(name = "block")
    private List<ApiBlockTerse> blocksData;


    /**
     * Need a no-arg constructor for Jersey. Otherwise get really obtuse "MessageBodyWriter not
     * found for media type=application/json" exception.
     */
    protected ApiBlocksTerse() {}

    public ApiBlocksTerse(Collection<IpcBlock> blocks) {
        blocksData = new ArrayList<>(blocks.size());
        for (IpcBlock block : blocks) {
            blocksData.add(new ApiBlockTerse(block));
        }
    }
}
