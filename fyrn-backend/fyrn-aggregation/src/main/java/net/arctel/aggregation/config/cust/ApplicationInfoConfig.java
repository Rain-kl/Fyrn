package net.arctel.aggregation.config.cust;

import lombok.extern.slf4j.Slf4j;
import net.arctel.framework.dto.ApplicationInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@Slf4j
public class ApplicationInfoConfig {

    @Bean
    public ApplicationInfo applicationInfo() {
        UUID uuid = UUID.randomUUID();
        log.info("Application ID: {}", uuid);
        return new ApplicationInfo(uuid.toString());
    }
}
