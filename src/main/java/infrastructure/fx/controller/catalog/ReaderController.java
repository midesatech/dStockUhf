package infrastructure.fx.controller.catalog;

import domain.exception.DuplicateFieldException;
import domain.model.UHFReader;
import domain.model.Ubicacion;
import domain.usecase.UHFReaderUseCase;
import domain.usecase.LocationUseCase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ReaderController {

    @FXML
    private TableView<UHFReader> tbl;
    @FXML
    private TableColumn<UHFReader, Long> colId;
    @FXML
    private TableColumn<UHFReader, String> colCodigo;
    @FXML
    private TableColumn<UHFReader, String> colDescripcion;
    @FXML
    private TableColumn<UHFReader, String> colUbicacion;

    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private ComboBox<Ubicacion> cmbUbicacion;
    @FXML private TextField filtroCodigo;
    @FXML private ComboBox<Ubicacion> filtroUbicacion;


    private final ObservableList<UHFReader> data = FXCollections.observableArrayList();
    private final ObservableList<Ubicacion> ubicaciones = FXCollections.observableArrayList();

    private final UHFReaderUseCase useCase;
    private final LocationUseCase locationUseCase;

    public ReaderController(UHFReaderUseCase useCase, LocationUseCase locationUseCase) {
        this.useCase = useCase;
        this.locationUseCase = locationUseCase;
    }

    @FXML
    public void initialize() {
        if (tbl != null) {
            tbl.setItems(data);

            colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
            colCodigo.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCodigo()));
            colDescripcion.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDescripcion()));
            colUbicacion.setCellValueFactory(c ->
                    new ReadOnlyStringWrapper(c.getValue().getUbicacion() != null ? c.getValue().getUbicacion().getNombre() : "")
            );

            tbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    txtCodigo.setText(newSel.getCodigo());
                    txtDescripcion.setText(newSel.getDescripcion());
                    cmbUbicacion.setValue(newSel.getUbicacion());
                }
            });
        }

        if (cmbUbicacion != null && locationUseCase != null) {
            ubicaciones.setAll(locationUseCase.listar());
            cmbUbicacion.setItems(ubicaciones);
            filtroUbicacion.setItems(ubicaciones);
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
        txtCodigo.clear();
        txtDescripcion.clear();
        cmbUbicacion.getSelectionModel().clearSelection();
        tbl.getSelectionModel().clearSelection();
    }

    @FXML
    public void guardar() {
        try {
            if (useCase == null) throw new IllegalStateException("Use JPA mode");

            Ubicacion ubicacion = cmbUbicacion.getValue();
            if (ubicacion == null) throw new IllegalArgumentException("Seleccione una ubicación");

            UHFReader seleccionado = tbl.getSelectionModel().getSelectedItem();

            if (seleccionado == null) {
                // --- Nuevo ---
                useCase.crear(txtCodigo.getText(), txtDescripcion.getText(), ubicacion);
                show("Éxito", "Lector creado correctamente", Alert.AlertType.INFORMATION);
            } else {
                // --- Edición ---
                useCase.actualizar(
                        seleccionado.getId(),
                        txtCodigo.getText(),
                        txtDescripcion.getText(),
                        ubicacion
                );
                show("Éxito", "Lector actualizado correctamente", Alert.AlertType.INFORMATION);
            }

            refresh();
            nuevo(); // limpia campos después de guardar

        } catch (DuplicateFieldException ex) {
            show("Error", ex.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void eliminar() {
        UHFReader s = tbl.getSelectionModel().getSelectedItem();
        if (s == null) {
            new Alert(Alert.AlertType.INFORMATION, "Seleccione un lector").showAndWait();
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

    private void show(String m) {
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait();
    }

    private void show(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void buscar() {
        String codigo = filtroCodigo.getText() != null ? filtroCodigo.getText().trim() : "";
        Ubicacion ubic = filtroUbicacion.getValue();

        data.clear();
        if (useCase != null) {
            data.addAll(useCase.listar().stream()
                    .filter(l -> (codigo.isEmpty() || l.getCodigo().toLowerCase().contains(codigo.toLowerCase())))
                    .filter(l -> (ubic == null || (l.getUbicacion() != null && l.getUbicacion().getId().equals(ubic.getId()))))
                    .toList()
            );
        }
    }

    @FXML
    public void limpiarFiltros() {
        filtroCodigo.clear();
        filtroUbicacion.getSelectionModel().clearSelection();
        refresh();
    }
}
