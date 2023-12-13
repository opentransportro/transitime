/* (C)2023 */
package org.transitclock.web;

import org.transitclock.config.StringConfigValue;

/**
 * Contains Java properties use by web server. These parameters are read in using ReadConfigListener
 * class.
 *
 * @author Michael Smith
 */
public class WebConfigParams {
    public static String getMapTileUrl() {
        return mapTileUrl.getValue();
    }

    private static StringConfigValue mapTileUrl = new StringConfigValue(
            "transitclock.web.mapTileUrl",
            "http://otile4.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png",
            "Specifies the URL used by Leaflet maps to fetch map " + "tiles.");

    public static String getMapTileCopyright() {
        return mapTileCopyright.getValue();
    }

    private static StringConfigValue mapTileCopyright = new StringConfigValue(
            "transitclock.web.mapTileCopyright",
            "MapQuest",
            "For displaying as map attributing for the where map tiles " + "from.");
}
