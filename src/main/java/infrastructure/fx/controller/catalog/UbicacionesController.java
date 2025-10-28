
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
    private TableColumn<Ubicacion, String> colPadre;
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<Ubicacion> cmbPadre;
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
            colPadre.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                    c.getValue().getParentName() == null ? "(Principal)" : c.getValue().getParentName()
            ));

            tbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, u) -> {
                if (u != null) {
                    txtNombre.setText(u.getNombre());
                    // Selecciona el padre si existe
                    if (u.getParentId() == null) cmbPadre.getSelectionModel().clearSelection();
                    else cmbPadre.getItems().stream().filter(p -> u.getParentId().equals(p.getId())).findFirst()
                            .ifPresent(p -> cmbPadre.getSelectionModel().select(p));
                }
            });
        }
        // Combo de padres (solo principales)
        cmbPadre.setButtonCell(new ListCell<>() { @Override protected void updateItem(Ubicacion it, boolean e){
            super.updateItem(it,e); setText(e||it==null? "": it.getNombre()); }});
        cmbPadre.setCellFactory(cb -> new ListCell<>() { @Override protected void updateItem(Ubicacion it, boolean e){
            super.updateItem(it,e); setText(e||it==null? "": it.getNombre()); }});

        refresh();
    }

    @FXML
    public void refresh() {
        data.clear();
        if (useCase != null) {
            data.addAll(useCase.listar());
            cmbPadre.getItems().setAll(useCase.principales());
        }
    }

    @FXML
    public void nuevo() {
        txtNombre.clear();
        cmbPadre.getSelectionModel().clearSelection();
        tbl.getSelectionModel().clearSelection();
    }

    @FXML
    public void guardar() {
        try {
            if (useCase == null) throw new IllegalStateException("Use JPA mode");
            var parent = cmbPadre.getSelectionModel().getSelectedItem();
            useCase.crear(txtNombre.getText(), parent == null ? null : parent.getId());
            refresh();
            new Alert(Alert.AlertType.INFORMATION, "Guardado").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void eliminar() {
        var sel = tbl.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (new Alert(Alert.AlertType.CONFIRMATION,"Eliminar ubicación seleccionada?", ButtonType.OK, ButtonType.CANCEL)
                .showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            useCase.eliminar(sel.getId());
            refresh();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }
}
