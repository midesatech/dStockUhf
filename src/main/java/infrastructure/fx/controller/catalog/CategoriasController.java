
package infrastructure.fx.controller.catalog;

import domain.model.Categoria;
import domain.usecase.CategoriaUseCase;
import infrastructure.adapter.database.jpa.CategoriaRepositoryAdapter;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class CategoriasController {

    @FXML private TextField txtNombre;
    @FXML private TableView<Categoria> tbl;
    @FXML private TableColumn<Categoria, Number> colId;
    @FXML private TableColumn<Categoria, String> colNombre;

    private ObservableList<Categoria> categorias;
    private CategoriaUseCase useCase;

    private Categoria categoriaSeleccionada;

    @FXML
    public void initialize() {
        // 🔹 Configuración de JPA y UseCase
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("InventarioPU");
        CategoriaRepositoryAdapter repo = new CategoriaRepositoryAdapter(emf);
        useCase = new CategoriaUseCase(repo);

        // Configurar columnas
        colId.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getId()));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));

        categorias = FXCollections.observableArrayList();
        tbl.setItems(categorias);

        // Escuchar selección
        tbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                categoriaSeleccionada = newSel;
                txtNombre.setText(newSel.getNombre());
            }
        });

        // Cargar datos iniciales
        refrescar();
    }

    @FXML
    private void nuevo() {
        txtNombre.clear();
        categoriaSeleccionada = null;
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
            if (categoriaSeleccionada != null) {
                useCase.actualizar(categoriaSeleccionada.getId(), nombre);
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
        if (categoriaSeleccionada == null) {
            showAlert("Seleccione una categoría para eliminar");
            return;
        }

        try {
            useCase.eliminar(categoriaSeleccionada.getId());
            refrescar();
            nuevo();
        } catch (Exception e) {
            showAlert("Error al eliminar: " + e.getMessage());
        }
    }

    @FXML
    private void refrescar() {
        categorias.clear();
        categorias.addAll(useCase.listar());
    }

    private void showAlert(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alert.showAndWait();
    }
}