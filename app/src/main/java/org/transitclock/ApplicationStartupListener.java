package org.transitclock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.transitclock.config.data.DbSetupConfig;
import org.transitclock.domain.ApiKeyManager;
import org.transitclock.domain.webstructs.WebAgency;

import java.util.TimeZone;

@Slf4j
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {
    private final ApiKeyManager apiKeyManager;
    private final ApplicationProperties properties;

    public ApplicationStartupListener(ApiKeyManager apiKeyManager, ApplicationProperties properties) {
        this.apiKeyManager = apiKeyManager;
        this.properties = properties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        TimeZone aDefault = TimeZone.getDefault();
        logger.warn("Application started using Timezone [{}, offset={}, daylight={}]", aDefault.getID(), aDefault.getRawOffset(), aDefault.useDaylightTime());

        try {
            apiKeyManager
                .generateApiKey(
                    "Sean Og Crudden",
                    "http://www.transitclock.org",
                    "og.crudden@gmail.com",
                    "123456",
                    "foo");
        } catch (IllegalArgumentException ignored) {

        }

        String agencyId = properties.getCore().getAgencyId();
        WebAgency webAgency = new WebAgency(agencyId,
            "127.0.0.1",
            true,
            DbSetupConfig.getDbName(),
            DbSetupConfig.getDbType(),
            DbSetupConfig.getDbHost(),
            DbSetupConfig.getDbUserName(),
            DbSetupConfig.getDbPassword());

        try {
            // Store the WebAgency
            webAgency.store(agencyId);
        } catch (IllegalArgumentException ignored) {

        }
    }

}
