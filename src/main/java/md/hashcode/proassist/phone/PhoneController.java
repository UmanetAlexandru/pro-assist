package md.hashcode.proassist.phone;

import jakarta.validation.Valid;
import md.hashcode.proassist.phone.dto.PhoneInfo;
import md.hashcode.proassist.phone.dto.PhoneRecordResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/phones")
public class PhoneController {

    private final PhoneStorageService storage;

    public PhoneController(PhoneStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{phone}")
    public PhoneRecordResponse get(@PathVariable String phone) {
        String key = PhoneKey.normalize(phone);
        return storage.get(key);
    }

    /**
     * multipart/form-data:
     * - info: JSON string representing PhoneInfo
     * - photos: 0..N image files
     */
    @PostMapping(value = "/{phone}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PhoneRecordResponse upsert(
            @PathVariable String phone,
            @RequestPart(value = "info", required = false) @Valid PhoneInfo info,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) {
        String key = PhoneKey.normalize(phone);
        return storage.upsert(key, info, photos);
    }

    @GetMapping("/{phone}/photos/{fileName}")
    public ResponseEntity<@NonNull Resource> getPhoto(@PathVariable String phone, @PathVariable String fileName) throws Exception {
        String key = PhoneKey.normalize(phone);

        Path file = storage.resolvePhoto(key, fileName);

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(file);
        MediaType mt = (contentType != null) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mt)
                .body(new FileSystemResource(file));
    }
}
