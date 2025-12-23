package md.hashcode.proassist.phone.dto;

import java.util.List;

public record PhoneRecordResponse(
        String phone,
        PhoneInfo info,
        String createdAt,
        String updatedAt,
        List<PhotoRef> photos
) {
    public record PhotoRef(String fileName, String url) {}
}
