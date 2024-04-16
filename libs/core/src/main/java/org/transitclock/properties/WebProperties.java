package org.transitclock.properties;

import lombok.Data;

@Data
public class WebProperties {
    // config param: transitclock.web.mapTileUrl
    // Specifies the URL used by Leaflet maps to fetch map tiles.
    private String mapTileUrl = "http://otile4.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";

    // config param: transitclock.web.mapTileCopyright
    // For displaying as map attributing for the where map tiles from.
    private String mapTileCopyright = "MapQuest";

}
