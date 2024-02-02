package org.transitclock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.transitclock.config.ConfigFileReader;

@Slf4j
public class ApplicationInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigFileReader.processConfig();
        logger.info("{}", applicationContext);
    }
}
