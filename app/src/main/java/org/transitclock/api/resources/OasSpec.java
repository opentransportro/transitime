package org.transitclock.api.resources;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info =
        @Info(
                title = "TrasnsitClockAPI",
                version = "1.0",
                description = "TheTransitClock is an open source transit information system. It’s"
                        + " core function is to provide and analyze arrival predictions"
                        + " for transit systems.<br>Here you will find the detailed"
                        + " description of The Transit Clock API.<br>For more"
                        + " information visit <a"
                        + " href=\"https://thetransitclock.github.io/\">thetransitclock.github.io.</a><br>"
                        + " The original documentation can be found in <a"
                        + " href=\"https://github.com/Transitime/core/wiki/API\">Api"
                        + " doc</a>."),
        servers = {@Server(url = "/api/v1")})
public interface OasSpec {
}
