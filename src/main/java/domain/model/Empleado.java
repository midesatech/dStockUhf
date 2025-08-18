
package domain.model;

import java.time.LocalDate;

public class Empleado {
    private Long id;
    private String codigo;              // TAG UHF (opcional)
    private String fullName;            // requerido
    private String lastName;            // requerido
    private TipoDocumento tipoDocumento; // requerido
    private String numeroDocumento;     // requerido (puede tener letras)
    private LocalDate fechaNacimiento;  // requerido
    private TipoSangre tipoSanguineo;   // requerido
    private String email;               // opcional
    private String telefono;            // opcional

    public Empleado() {}

    public Empleado(Long id, String codigo, String fullName, String lastName) {
        this.id = id; this.codigo = codigo; this.fullName = fullName; this.lastName = lastName;
    }

    // Constructor completo (útil para tests)
    public Empleado(Long id, String codigo, String fullName, String lastName,
                    TipoDocumento tipoDocumento, String numeroDocumento,
                    LocalDate fechaNacimiento, TipoSangre tipoSanguineo,
                    String email, String telefono) {
        this.id = id;
        this.codigo = codigo;
        this.fullName = fullName;
        this.lastName = lastName;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.tipoSanguineo = tipoSanguineo;
        this.email = email;
        this.telefono = telefono;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public TipoSangre getTipoSanguineo() { return tipoSanguineo; }
    public void setTipoSanguineo(TipoSangre tipoSanguineo) { this.tipoSanguineo = tipoSanguineo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}

