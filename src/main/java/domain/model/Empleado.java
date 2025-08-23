
package domain.model;

import java.time.LocalDate;

public class Empleado {
    private Long id;
    private String codigo;
    private String epc;              // TAG UHF (opcional)
    private String fullName;            // requerido
    private String lastName;            // requerido
    private TipoDocumento docType; // requerido
    private String docNumber;     // requerido (puede tener letras)
    private LocalDate birthDate;  // requerido
    private TipoSangre bloodType;   // requerido
    private String email;               // opcional
    private String phone;            // opcional

    public Empleado() {}

    public Empleado(Long id, String epc, String fullName, String lastName) {
        this.id = id; this.epc = epc; this.fullName = fullName; this.lastName = lastName;
    }

    // Constructor completo (útil para tests)
    public Empleado(Long id, String epc, String fullName, String lastName,
                    TipoDocumento docType, String docNumber,
                    LocalDate birthDate, TipoSangre bloodType,
                    String email, String phone) {
        this.id = id;
        this.epc = epc;
        this.fullName = fullName;
        this.lastName = lastName;
        this.docType = docType;
        this.docNumber = docNumber;
        this.birthDate = birthDate;
        this.bloodType = bloodType;
        this.email = email;
        this.phone = phone;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEpc() { return epc; }
    public void setEpc(String epc) { this.epc = epc; }
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return fullName.concat(" ").concat(lastName);
    }
}

