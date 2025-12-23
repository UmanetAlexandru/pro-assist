package md.hashcode.proassist.phone.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "phone_record")
public class PhoneRecordEntity {

    @Id
    @Column(name = "phone_key", nullable = false, length = 64)
    private String phoneKey;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "currency")
    private String currency;

    @Column(name = "address")
    private String address;

    @Column(name = "services_json")
    private String servicesJson;

    @Column(name = "comment")
    private String comment;

    @Column(name = "visited")
    private Integer visited; // 0/1/null

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "finished")
    private String finished;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    @Column(name = "updated_at", nullable = false)
    private String updatedAt;
}
