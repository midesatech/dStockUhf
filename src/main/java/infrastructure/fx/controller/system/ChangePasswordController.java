
package infrastructure.fx.controller.system;
import app.config.AppBootstrap; import domain.usecase.ChangePasswordUseCase; import javafx.fxml.FXML; import javafx.scene.control.Alert; import javafx.scene.control.PasswordField;
public class ChangePasswordController {
    @FXML private PasswordField txtCurrent; @FXML private PasswordField txtNext; @FXML private PasswordField txtConfirm;
    private final ChangePasswordUseCase useCase = new ChangePasswordUseCase(AppBootstrap.users(), AppBootstrap.encoder());
    private final Long currentUserId = 1L;
    @FXML public void change(){ String c = txtCurrent.getText(); String n = txtNext.getText(); String r = txtConfirm.getText(); if(n==null || n.isBlank() || !n.equals(r)){ alert("La nueva contraseña y su confirmación no coinciden"); return; } try{ useCase.change(currentUserId, c, n); alert("Contraseña actualizada"); txtCurrent.clear(); txtNext.clear(); txtConfirm.clear(); }catch(Exception ex){ alert(ex.getMessage()); } }
    private void alert(String m){ new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
}
