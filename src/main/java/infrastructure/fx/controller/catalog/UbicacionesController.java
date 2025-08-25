
package infrastructure.fx.controller.catalog;

import domain.model.Ubicacion;
import domain.usecase.LocationUseCase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UbicacionesController {
    @FXML
    private TableView<Ubicacion> tbl;
    @FXML
    private TableColumn<Ubicacion, Long> colId;
    @FXML
    private TableColumn<Ubicacion, String> colNombre;
    @FXML
    private TextField txtNombre;
    private final ObservableList<Ubicacion> data = FXCollections.observableArrayList();
    private final LocationUseCase useCase;

    public UbicacionesController(LocationUseCase useCase) {
        this.useCase = useCase;
    }

    @FXML
    public void initialize() {
        if (tbl != null) {
            tbl.setItems(data);

            colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
            colNombre.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNombre()));

            tbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) txtNombre.setText(newSel.getNombre());
            });
        }
        refresh();
    }

    @FXML
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
