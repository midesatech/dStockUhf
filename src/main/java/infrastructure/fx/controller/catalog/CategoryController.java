
package infrastructure.fx.controller.catalog;

import domain.model.Category;
import domain.usecase.CategoriaUseCase;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class CategoryController {

    @FXML private TextField txtNombre;
    @FXML private TableView<Category> tbl;
    @FXML private TableColumn<Category, Number> colId;
    @FXML private TableColumn<Category, String> colNombre;

    private ObservableList<Category> categories;
    private final CategoriaUseCase useCase;

    private Category categorySeleccionada;

    public CategoryController(CategoriaUseCase useCase) {
        this.useCase = useCase;
    }

    @FXML
    public void initialize() {
        // Configurar columnas
        colId.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getId()));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));

        categories = FXCollections.observableArrayList();
        tbl.setItems(categories);

        // Escuchar selección
        tbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                categorySeleccionada = newSel;
                txtNombre.setText(newSel.getNombre());
            }
        });

        // Cargar datos iniciales
        refrescar();
    }

    @FXML
    private void nuevo() {
        txtNombre.clear();
        categorySeleccionada = null;
        tbl.getSelectionModel().clearSelection();
    }

    @FXML
    private void guardar() {
        String nombre = txtNombre.getText();
        if (nombre == null || nombre.isBlank()) {
            showAlert("El nombre no puede estar vacío");
            return;
        }

        try {
            if (categorySeleccionada != null) {
                useCase.actualizar(categorySeleccionada.getId(), nombre);
            } else {
                useCase.crear(nombre);
            }
            refrescar();
            nuevo();
        } catch (Exception e) {
            showAlert("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (categorySeleccionada == null) {
            showAlert("Seleccione una categoría para eliminar");
            return;
        }

        try {
            useCase.eliminar(categorySeleccionada.getId());
            refrescar();
            nuevo();
        } catch (Exception e) {
            showAlert("Error al eliminar: " + e.getMessage());
        }
    }

    @FXML
    private void refrescar() {
        categories.clear();
        categories.addAll(useCase.listar());
    }

    private void showAlert(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alert.showAndWait();
    }
}