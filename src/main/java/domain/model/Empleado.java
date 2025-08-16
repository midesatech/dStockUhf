
package domain.model;
public class Empleado {
    private Long id; private String codigo; private String fullName;
    public Empleado(){} public Empleado(Long id, String codigo, String fullName){ this.id=id; this.codigo=codigo; this.fullName=fullName; }
    public Long getId(){ return id; } public void setId(Long id){ this.id=id; }
    public String getCodigo(){ return codigo; } public void setCodigo(String codigo){ this.codigo=codigo; }
    public String getFullName(){ return fullName; } public void setFullName(String fullName){ this.fullName=fullName; }
}
