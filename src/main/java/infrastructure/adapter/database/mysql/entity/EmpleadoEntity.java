
package infrastructure.adapter.database.mysql.entity;

import domain.model.TipoDocumento;
import domain.model.TipoSangre;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "empleados")
public class EmpleadoEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 64)
    private String codigo;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 5)
    private TipoDocumento docType;

    @Column(name = "doc_number", nullable = false, length = 30, unique = true)
    private String docNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_type", nullable = false, length = 6)
    private TipoSangre bloodType;

    @Column(length = 120)
    private String email;

    @Column(length = 25)
    private String phone;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public TipoDocumento getDocType() { return docType; }
    public void setDocType(TipoDocumento docType) { this.docType = docType; }
    public String getDocNumber() { return docNumber; }
    public void setDocNumber(String docNumber) { this.docNumber = docNumber; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public TipoSangre getBloodType() { return bloodType; }
    public void setBloodType(TipoSangre bloodType) { this.bloodType = bloodType; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
