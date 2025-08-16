
package infrastructure.fx.controller.catalog;

import app.config.AppBootstrap;
import domain.model.Ubicacion;
import domain.usecase.UbicacionUseCase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UbicacionesController {
    @FXML
    private TableView<Ubicacion> tbl;
    @FXML
    private TextField txtNombre;
    private final ObservableList<Ubicacion> data = FXCollections.observableArrayList();
    private UbicacionUseCase useCase;

    @FXML
    public void initialize() {
        useCase = AppBootstrap.isJpaMode() ? new UbicacionUseCase(new infrastructure.adapter.database.jpa.UbicacionRepositoryAdapter(infrastructure.persistence.JPAUtil.getEmf())) : null;
        if (tbl != null) tbl.setItems(data);
        refresh();
    }

    public void refresh() {
        data.clear();
        if (useCase != null) data.addAll(useCase.listar());
    }

    @FXML
    public void nuevo() {
        txtNombre.clear();
        tbl.getSelectionModel().clearSelection();
    }

    @FXML
    public void guardar() {
        try {
            if (useCase == null) throw new IllegalStateException("Use JPA mode");
            useCase.crear(txtNombre.getText());
            refresh();
            new Alert(Alert.AlertType.INFORMATION, "Guardado").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void eliminar() {
        Ubicacion s = tbl.getSelectionModel().getSelectedItem();
        if (s == null) {
            new Alert(Alert.AlertType.INFORMATION, "Seleccione").showAndWait();
            return;
        }
        try {
            useCase.eliminar(s.getId());
            refresh();
            new Alert(Alert.AlertType.INFORMATION, "Eliminado").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }
}
