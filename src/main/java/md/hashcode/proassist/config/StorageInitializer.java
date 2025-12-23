package md.hashcode.proassist.config;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
public class StorageInitializer implements ApplicationRunner {

    private final StorageProperties props;

    public StorageInitializer(StorageProperties props) {
        this.props = props;
    }

    @Override
    public void run(@Nonnull ApplicationArguments args) throws Exception {
        Path base = Path.of(props.basePath()).toAbsolutePath().normalize();

        Files.createDirectories(base);
        Files.createDirectories(base.resolve("phones")); // new root for phone folders

        log.info("Storage initialized. basePath={}", base);
    }
}
