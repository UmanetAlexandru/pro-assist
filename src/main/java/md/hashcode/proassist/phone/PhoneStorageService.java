package md.hashcode.proassist.phone;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import md.hashcode.proassist.config.StorageProperties;
import md.hashcode.proassist.phone.db.PhoneRecordEntity;
import md.hashcode.proassist.phone.db.PhoneRecordRepository;
import md.hashcode.proassist.phone.dto.PhoneInfo;
import md.hashcode.proassist.phone.dto.PhoneRecordResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PhoneStorageService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private static final TypeReference<PhoneInfo.Services> SERVICES_TYPE = new TypeReference<>() {
    };

    private final Path phonesRoot;
    private final ObjectMapper objectMapper;
    private final PhoneRecordRepository repo;

    public PhoneStorageService(StorageProperties props, ObjectMapper objectMapper, PhoneRecordRepository repo) {
        this.phonesRoot = Path.of(props.basePath()).toAbsolutePath().normalize().resolve("phones");
        this.objectMapper = objectMapper;
        this.repo = repo;
    }

    public PhoneRecordResponse get(String phoneKey) {
        Path photosDir = phonesRoot.resolve(phoneKey).resolve("photos");

        var entityOpt = repo.findById(phoneKey);

        PhoneInfo info = entityOpt.map(this::toInfo).orElse(null);
        String createdAt = entityOpt.map(PhoneRecordEntity::getCreatedAt).orElse(null);
        String updatedAt = entityOpt.map(PhoneRecordEntity::getUpdatedAt).orElse(null);

        List<PhoneRecordResponse.PhotoRef> photos = listPhotos(phoneKey, photosDir);
        return new PhoneRecordResponse(phoneKey, info, createdAt, updatedAt, photos);
    }

    public PhoneRecordResponse upsert(String phoneKey, PhoneInfo info, List<MultipartFile> photos) {
        // 1) Photos on disk (independent of DB)
        try {
            Files.createDirectories(phonesRoot.resolve(phoneKey).resolve("photos"));
            if (photos != null) {
                storePhotos(phoneKey, photos);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store photos for " + phoneKey, e);
        }

        // 2) Metadata in SQLite
        if (info != null) {
            String now = LocalDateTime.now().toString();

            PhoneRecordEntity entity = repo.findById(phoneKey).orElseGet(() -> {
                PhoneRecordEntity e = new PhoneRecordEntity();
                e.setPhoneKey(phoneKey);
                e.setCreatedAt(now); // set only once for new records
                return e;
            });

            // Core fields
            entity.setDescription(info.description());
            entity.setPrice(info.price());
            entity.setCurrency(info.currency() == null ? null : info.currency().name());
            entity.setAddress(info.address());

            // Services (JSON)
            entity.setServicesJson(writeServicesJson(info.services()));

            // Flags and rating
            entity.setComment(info.comment());
            entity.setVisited(info.visited() == null ? null : (info.visited() ? 1 : 0));
            entity.setRating(info.rating());
            entity.setFinished(info.finished() == null ? null : info.finished().name());

            // Source URL (stored only, not shown in UI)
            entity.setSourceUrl(info.sourceUrl());

            // Timestamps
            entity.setUpdatedAt(now);

            repo.save(entity);
        }

        // 3) Return the current view (DB + photos)
        return get(phoneKey);
    }


    public Path resolvePhoto(String phoneKey, String fileName) {
        Path photosDir = phonesRoot.resolve(phoneKey).resolve("photos");
        Path file = photosDir.resolve(fileName).normalize();
        if (!file.startsWith(photosDir)) {
            throw new IllegalArgumentException("Invalid file name");
        }
        return file;
    }

    // -------- mapping helpers --------

    private PhoneInfo toInfo(PhoneRecordEntity e) {
        return new PhoneInfo(
                e.getDescription(),
                e.getPrice(),
                parseCurrency(e.getCurrency()),
                e.getAddress(),
                readServicesJson(e.getServicesJson()),
                e.getComment(),
                parseVisited(e.getVisited()),
                e.getRating(),
                parseFinished(e.getFinished()),
                e.getSourceUrl()
        );
    }

    private PhoneInfo.Currency parseCurrency(String v) {
        if (!StringUtils.hasText(v)) return null;
        return PhoneInfo.Currency.valueOf(v);
    }

    private PhoneInfo.Finished parseFinished(String v) {
        if (!StringUtils.hasText(v)) return null;
        return PhoneInfo.Finished.valueOf(v);
    }

    private Boolean parseVisited(Integer v) {
        if (v == null) return null;
        return v != 0;
    }

    private String writeServicesJson(PhoneInfo.Services services) {
        try {
            return services == null ? null : objectMapper.writeValueAsString(services);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize services", ex);
        }
    }

    private PhoneInfo.Services readServicesJson(String json) {
        try {
            if (!StringUtils.hasText(json)) return null;
            return objectMapper.readValue(json, SERVICES_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse services", ex);
        }
    }

    // -------- photo helpers --------
    private void storePhotos(String phoneKey, List<MultipartFile> photos) throws Exception {
        Path photosDir = phonesRoot.resolve(phoneKey).resolve("photos");

        int i = 0;
        for (MultipartFile file : photos) {
            if (file == null || file.isEmpty()) continue;

            String ct = file.getContentType();
            if (ct == null || !ct.startsWith("image/")) {
                throw new IllegalArgumentException("Only image uploads are allowed. contentType=" + ct);
            }

            String ext = guessExtension(file);
            String filename = TS.format(LocalDateTime.now()) + "-" + (++i) + ext;

            Path target = photosDir.resolve(filename).normalize();
            if (!target.startsWith(photosDir)) {
                throw new IllegalArgumentException("Invalid file name");
            }

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private List<PhoneRecordResponse.PhotoRef> listPhotos(String phoneKey, Path photosDir) {
        if (!Files.exists(photosDir) || !Files.isDirectory(photosDir)) return List.of();

        try (var stream = Files.list(photosDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .map(fn -> new PhoneRecordResponse.PhotoRef(fn, "/api/phones/" + phoneKey + "/photos/" + fn))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list photos for " + phoneKey, e);
        }
    }

    private String guessExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (StringUtils.hasText(original) && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf('.'));
            if (ext.length() <= 6) return ext;
        }

        String ct = file.getContentType();
        if (MediaType.IMAGE_JPEG_VALUE.equals(ct)) return ".jpg";
        if (MediaType.IMAGE_PNG_VALUE.equals(ct)) return ".png";
        if (MediaType.IMAGE_GIF_VALUE.equals(ct)) return ".gif";
        if (ct != null && ct.startsWith("image/")) return ".img";

        return ".bin";
    }
}
