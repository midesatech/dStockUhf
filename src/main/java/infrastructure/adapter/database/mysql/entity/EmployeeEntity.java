
package infrastructure.adapter.database.mysql.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "empleados",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_empleados_doc_number", columnNames = "doc_number")
        },
        indexes = {
                @Index(name = "idx_empleados_last_name", columnList = "last_name"),
                @Index(name = "idx_empleados_doc_type",  columnList = "doc_type")
        }
)
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 20)
    private domain.model.TipoDocumento docType;

    @Column(name = "doc_number", nullable = false, length = 30, unique = true)
    private String docNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", nullable = false, length = 4)
    private domain.model.TipoSangre bloodType;

    @Column(length = 120)
    private String email;

    @Column(length = 25)
    private String phone;

    @OneToOne(fetch = FetchType.LAZY,
            optional = true,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH })
    @JoinColumn(
            name = "tag_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_empleados_tag") // DDL for ON DELETE below in SQL
    )
    private UHFTagEntity tag;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public domain.model.TipoDocumento getDocType() { return docType; }
    public void setDocType(domain.model.TipoDocumento docType) { this.docType = docType; }
    public String getDocNumber() { return docNumber; }
    public void setDocNumber(String docNumber) { this.docNumber = docNumber; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public domain.model.TipoSangre getBloodType() { return bloodType; }
    public void setBloodType(domain.model.TipoSangre bloodType) { this.bloodType = bloodType; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public UHFTagEntity getTag() { return tag; }
    public void setTag(UHFTagEntity tag) { this.tag = tag; }
}
