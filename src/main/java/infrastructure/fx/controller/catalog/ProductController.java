
package infrastructure.fx.controller.catalog;

import app.config.AppBootstrap;
import domain.model.Category;
import domain.model.Product;
import domain.model.Ubicacion;
import domain.usecase.CategoriaUseCase;
import domain.usecase.LocationUseCase;
import domain.usecase.ProductUseCase;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ProductController {
    @FXML
    private TableView<Product> tbl;
    @FXML
    private TextField txtSku;
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<Category> cmbCategoria;
    @FXML
    private ComboBox<Ubicacion> cmbUbicacion;
    @FXML
    private TableColumn<Product, Long> colId;
    @FXML
    private TableColumn<Product, String> colSku;
    @FXML
    private TableColumn<Product, String> colNombre;
    @FXML
    private TableColumn<Product, String> colCategoria;
    @FXML
    private TableColumn<Product, String> colUbicacion;
    @FXML
    private TextField filtroSku;
    @FXML
    private TextField filtroNombre;
    @FXML
    private ComboBox<Category> filtroCategoria;

    private final ObservableList<Product> data = FXCollections.observableArrayList();
    private final ObservableList<Category> cats = FXCollections.observableArrayList();
    private final ObservableList<Ubicacion> ubic = FXCollections.observableArrayList();
    private final ProductUseCase productUseCase;
    private final CategoriaUseCase useCat;
    private final LocationUseCase useUb;

    public ProductController(ProductUseCase productUseCase, CategoriaUseCase useCat, LocationUseCase useUb) {
        this.productUseCase = productUseCase;
        this.useCat = useCat;
        this.useUb = useUb;
    }

    @FXML
    public void initialize() {
        if (!AppBootstrap.isJpaMode()) {
            return;
        }

        if (tbl != null) {
            tbl.setItems(data);

            colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
            colSku.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getSku()));
            colNombre.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNombre()));
            colCategoria.setCellValueFactory(c ->
                    new ReadOnlyStringWrapper(c.getValue().getCategoria() != null ? c.getValue().getCategoria().getNombre() : "")
            );
            colUbicacion.setCellValueFactory(c ->
                    new ReadOnlyStringWrapper(c.getValue().getUbicacion() != null ? c.getValue().getUbicacion().getNombre() : "")
            );

            // opcional: selección -> llenar formulario
            tbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    txtSku.setText(newSel.getSku());
                    txtNombre.setText(newSel.getNombre());
                    cmbCategoria.setValue(newSel.getCategoria());
                    cmbUbicacion.setValue(newSel.getUbicacion());
                }
            });
        }

        cmbCategoria.setItems(cats);
        cmbUbicacion.setItems(ubic);
        filtroCategoria.setItems(cats);
        refreshAll();
    }

    @FXML
    public void refresh() {
        refreshAll();
    }

    private void refreshAll() {
        data.clear();
        cats.clear();
        ubic.clear();
        data.addAll(productUseCase.listar());
        cats.addAll(useCat.listar());
        ubic.addAll(useUb.listar());
    }

    @FXML
    public void nuevo() {
        txtSku.clear();
        txtNombre.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbUbicacion.getSelectionModel().clearSelection();
        tbl.getSelectionModel().clearSelection();
    }

    @FXML
    public void guardar() {
        try {
            if (productUseCase == null) throw new IllegalStateException("Use JPA mode");

            // 1. Obtener el elemento seleccionado de la tabla
            Product seleccionado = tbl.getSelectionModel().getSelectedItem();

            if (seleccionado == null) {
                // --- Lógica para CREAR un nuevo Equipment ---
                Product nuevo = new Product();
                nuevo.setSku(txtSku.getText());
                nuevo.setNombre(txtNombre.getText());
                nuevo.setCategoria(cmbCategoria.getValue());
                nuevo.setUbicacion(cmbUbicacion.getValue());
                productUseCase.crear(nuevo);
                new Alert(Alert.AlertType.INFORMATION, "Equipo creado correctamente").showAndWait();
            } else {
                // --- Lógica para ACTUALIZAR el Equipment seleccionado ---
                seleccionado.setSku(txtSku.getText());
                seleccionado.setNombre(txtNombre.getText());
                seleccionado.setCategoria(cmbCategoria.getValue());
                seleccionado.setUbicacion(cmbUbicacion.getValue());
                productUseCase.actualizar(seleccionado); // Asumo que tienes un método actualizar(Equipment)
                new Alert(Alert.AlertType.INFORMATION, "Equipo actualizado correctamente").showAndWait();
            }

            // 2. Refrescar la tabla y limpiar los campos después de guardar
            refreshAll();
            nuevo(); // Llama al método 'nuevo()' para limpiar los campos

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void eliminar() {
        Product sel = tbl.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.INFORMATION, "Seleccione").showAndWait();
            return;
        }
        try {
            productUseCase.eliminar(sel.getId());
            refreshAll();
            new Alert(Alert.AlertType.INFORMATION, "Eliminado").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    @FXML
    public void buscar() {
        String sku = filtroSku.getText();
        String nombre = filtroNombre.getText();
        Category cat = filtroCategoria.getValue();

        data.clear();
        data.addAll(productUseCase.buscar(sku, nombre, cat));
    }

    @FXML
    public void limpiarFiltros() {
        filtroSku.clear();
        filtroNombre.clear();
        filtroCategoria.getSelectionModel().clearSelection();
        refreshAll();
    }
}
