package md.hashcode.proassist.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "proassist.storage")
public record StorageProperties(
        @NotBlank String basePath
) {}

