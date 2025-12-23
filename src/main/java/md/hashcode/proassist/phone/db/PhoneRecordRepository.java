package md.hashcode.proassist.phone.db;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneRecordRepository extends JpaRepository<@NonNull PhoneRecordEntity, @NonNull String> {
}
