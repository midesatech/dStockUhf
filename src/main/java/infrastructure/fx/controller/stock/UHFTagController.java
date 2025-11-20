package infrastructure.fx.controller.stock;

import domain.model.People;
import domain.model.Product;
import domain.model.UHFTag;
import domain.model.tag.ESerialMode;
import domain.model.tag.ErrorCode;
import domain.model.tag.ReadWriteResult;
import domain.model.tag.RxDto;
import domain.usecase.PeopleUseCase;
import domain.usecase.ProductUseCase;
import domain.usecase.TagUHFUseCase;
import domain.usecase.tag.ReadTagUseCase;
import infrastructure.fx.controller.MainController;
import infrastructure.logging.LogMessages;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class UHFTagController {

    @FXML private TextField txtEpc;
    @FXML private ComboBox<UHFTag.Tipo> cmbTipo;
    @FXML private CheckBox chkActivo;
    @FXML private TableView<UHFTag> tabla;
    @FXML private TableColumn<UHFTag, Long> colId;
    @FXML private TableColumn<UHFTag, String> colEpc;
    @FXML private TableColumn<UHFTag, UHFTag.Tipo> colTipo;
    @FXML private TableColumn<UHFTag, Boolean> colActivo;
    @FXML private ComboBox<Object> cmbAsignacion;
    @FXML private TextField txtFiltroEpc;
    @FXML private TableColumn<UHFTag, String> colAsignado;

    private final ObservableList<UHFTag> data = FXCollections.observableArrayList();
    private final TagUHFUseCase useCase;
    private final PeopleUseCase peopleUseCase;
    private final ProductUseCase productUseCase;
    private final ReadTagUseCase readTagUseCase;

    private UHFTag seleccionado;

    private boolean isBackGroundActivate = false;
    private static final String ACTIVATE = "Activar    ";
    private static final String DEACTIVATE = "Activado ";

    private static final Logger logger = LogManager.getLogger(UHFTagController.class);

    public UHFTagController(TagUHFUseCase useCase,
                            PeopleUseCase peopleUseCase,
                            ProductUseCase productUseCase,
                            ReadTagUseCase readTagUseCase) {
        this.useCase = useCase;
        this.peopleUseCase = peopleUseCase;
        this.productUseCase = productUseCase;
        this.readTagUseCase = readTagUseCase;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        colEpc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEpc()));
        colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTipo()));
        colActivo.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isActivo()));
        colAsignado.setCellValueFactory(c -> {
            UHFTag tag = c.getValue();
            String epc = tag.getEpc();
            String display = "";
            try {
                if (tag.getTipo() == UHFTag.Tipo.PEOPLE) {
                    display = peopleUseCase.findByEpc(epc)
                            .map(emp -> emp.getFullName() + " " + emp.getLastName())
                            .orElse("");
                } else if (tag.getTipo() == UHFTag.Tipo.PRODUCT) {
                    display = productUseCase.findByEpc(epc)
                            .map(eq -> {
                                String sku = (eq.getSku() == null ? "" : eq.getSku());
                                String nombre = (eq.getNombre() == null ? "" : eq.getNombre());
                                String sep = (!sku.isBlank() && !nombre.isBlank()) ? " - " : "";
                                return sku + sep + nombre;
                            })
                            .orElse("");
                }
            } catch (Exception ex) {
                // swallow and show empty to keep the table robust
                display = "";
            }
            return new javafx.beans.property.SimpleStringProperty(display);
        });


        cmbTipo.setItems(FXCollections.observableArrayList(
                Arrays.stream(UHFTag.Tipo.values()).filter(UHFTag.Tipo::isEnabled).toList()));
        tabla.setItems(data);

        // 2. Define the Cell Factory for the dropdown list items
        cmbTipo.setCellFactory(lv -> new ListCell<UHFTag.Tipo>() {
            @Override
            protected void updateItem(UHFTag.Tipo item, boolean empty) {
                super.updateItem(item, empty);
                // If the item is null (empty cell), display nothing
                setText(empty || item == null ? null : item.getLabel());
            }
        });

        // 3. Define the Button Cell for the currently selected item
        cmbTipo.setButtonCell(new ListCell<UHFTag.Tipo>() {
            @Override
            protected void updateItem(UHFTag.Tipo item, boolean empty) {
                super.updateItem(item, empty);
                // If the item is null (no selection), display nothing
                setText(empty || item == null ? null : item.getLabel());
            }
        });

        cmbTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Limpiar siempre la asignación visual
            cmbAsignacion.getItems().clear();
            cmbAsignacion.setValue(null);

            /*
            if (newVal == null) {
                return;
            } else {
                abrirDialogoSegunTipo(newVal);
            }

            if (newVal == UHFTag.Tipo.PEOPLE) {
                People seleccionado = mostrarDialogoBusquedaPeople();
                if (seleccionado != null) {
                    cmbAsignacion.getItems().add(seleccionado);
                    cmbAsignacion.setValue(seleccionado);
                }
            } else if (newVal == UHFTag.Tipo.PRODUCT) {
                Product seleccionado = mostrarDialogoBusquedaProduct();
                if (seleccionado != null) {
                    cmbAsignacion.getItems().add(seleccionado);
                    cmbAsignacion.setValue(seleccionado);
                }
            }
             */
        });

        cmbTipo.setCellFactory(cb -> {
            ListCell<UHFTag.Tipo> cell = new ListCell<>() {
                @Override
                protected void updateItem(UHFTag.Tipo item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.name());
                }
            };

            cell.setOnMousePressed(e -> {
                if (!cell.isEmpty() && cell.getItem() != null) {
                    // Siempre que el usuario haga click en un ítem (aunque sea el mismo),
                    // abrimos el diálogo para ese tipo:
                    abrirDialogoSegunTipo(cell.getItem());
                }
            });

            return cell;
        });

        // Para que el botón del combo muestre el texto del tipo
        cmbTipo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(UHFTag.Tipo item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name());
            }
        });


        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                seleccionado = newSel;
                txtEpc.setText(newSel.getEpc());
                cmbTipo.setValue(newSel.getTipo());
                chkActivo.setSelected(newSel.isActivo());
                // 🔹 Seleccionar en cmbAsignacion según el tipo
                if (newSel.getTipo() == UHFTag.Tipo.PEOPLE) {
                    cmbAsignacion.setItems(FXCollections.observableArrayList(peopleUseCase.listar()));
                    peopleUseCase.findByEpc(newSel.getEpc()).ifPresent(emp -> cmbAsignacion.setValue(emp));
                } else if (newSel.getTipo() == UHFTag.Tipo.PRODUCT) {
                    cmbAsignacion.setItems(FXCollections.observableArrayList(productUseCase.listar()));
                    productUseCase.findByEpc(newSel.getEpc()).ifPresent(eq -> cmbAsignacion.setValue(eq));
                } else {
                    cmbAsignacion.getItems().clear();
                }

            }
        });

        refresh();
        setReadTagUseCaseProperties();
    }

    private void setReadTagUseCaseProperties() {
        this.readTagUseCase.messageProperty()
                .addListener((observable, oldMessage, newMessage) -> {
                    new Alert(Alert.AlertType.INFORMATION, newMessage).show();
        });

        this.readTagUseCase.setOnSucceeded(event -> {
            ReadWriteResult result = readTagUseCase.getValue(); // <-- result from createTask()
            System.out.println("Tag read result: " + result);

            if (Objects.nonNull(result)) {
                switch (result.getErrorCode()) {
                    case OK:
                        this.txtEpc.setText(result.getData().getHexEpc());
                        break;
                }
                if (result.isShowMessage()) {
                    new Alert(Alert.AlertType.INFORMATION, result.getMessage()).show();
                }
            }
            deactivateBackgroundTagDetection();

        });

        // Optional: attach error handler
        this.readTagUseCase.setOnFailed(event -> {
            Throwable ex = readTagUseCase.getException();
            ex.printStackTrace();
        });
    }

    @FXML
    public void nuevo() {
        seleccionado = null;
        txtEpc.clear();
        cmbTipo.getSelectionModel().clearSelection();
        cmbAsignacion.getSelectionModel().clearSelection();
        cmbAsignacion.getItems().clear();
        cmbAsignacion.setValue(null);
        chkActivo.setSelected(true);
    }

    @FXML
    public void guardar() {
        String epc = txtEpc.getText();
        UHFTag.Tipo tipo = cmbTipo.getValue();
        Object asignacion = cmbAsignacion.getValue();
        boolean activo = chkActivo.isSelected();

        if (epc == null || epc.isBlank() || tipo == null || asignacion == null) {
            new Alert(Alert.AlertType.WARNING, "Debe ingresar EPC, seleccionar Tipo y asignación.").show();
            return;
        }

        // Validar si ya existe EPC
        if (useCase.findByEpc(epc).isPresent() && seleccionado == null) {
            new Alert(Alert.AlertType.ERROR, "El EPC ya está asignado a otro registro.").show();
            return;
        }

        UHFTag tag = (seleccionado == null)
                ? new UHFTag(null, epc, tipo, activo)
                : new UHFTag(seleccionado.getId(), epc, tipo, activo);

        // Persistir el Tag
        UHFTag saved = (seleccionado == null) ? useCase.save(tag) : useCase.update(tag);

        // Asignación según tipo
        if (tipo == UHFTag.Tipo.PEOPLE) {
            peopleUseCase.asignarEpc(((People) asignacion).getId(), epc);
        } else {
            productUseCase.asignarEpc(((Product) asignacion).getId(), epc);
        }

        refresh();
        nuevo();
    }

    @FXML
    public void eliminar() {
        if (seleccionado != null) {
            useCase.deleteById(seleccionado.getId());
            refresh();
            nuevo();
        }
    }

    @FXML
    public void refresh() {
        data.setAll(useCase.findAll());
    }

    @FXML
    public void buscarEpc() {
        String filtro = txtFiltroEpc.getText();
        if (filtro != null && !filtro.isBlank()) {
            Optional<UHFTag> encontrado = useCase.findByEpc(filtro);
            if (encontrado.isPresent()) {
                tabla.getSelectionModel().select(encontrado.get());
                tabla.scrollTo(encontrado.get());
            } else {
                new Alert(Alert.AlertType.INFORMATION, "No se encontró TAG con EPC: " + filtro).show();
            }
        } else {
            refresh();
        }
    }

    @FXML
    public void detectarTag() {
        handleTagDetection();
    }

    public void handleTagDetection() {
            if (!isBackGroundActivate) {
                setSaveAction(DEACTIVATE, false);
                activateBackgroundTagDetection();
            } else {
                setSaveAction(ACTIVATE, true);
                deactivateBackgroundTagDetection();
            }
     }

    private void setSaveAction(String message, boolean activate) {
        MainController.getInstance().setTagDetectionStatus(message);
        isBackGroundActivate = !activate;
    }

    private void deactivateBackgroundTagDetection() {
        Worker.State currentState = readTagUseCase.getState();
        if (currentState == Worker.State.RUNNING) {
            readTagUseCase.cancel();
        }
        setSaveAction(ACTIVATE, true);
        txtEpc.getScene().setCursor(Cursor.DEFAULT);
    }

    private void activateBackgroundTagDetection() {
        //poneMensaje("Detección activada: Esperando TAG");
        txtEpc.getScene().setCursor(Cursor.WAIT);
        startTagDetection(1L);
    }

    public void startTagDetection(Long order) {
        if (readTagUseCase.getSerialMode() == ESerialMode.NO) {
            setSaveAction(ACTIVATE, true);
            deactivateBackgroundTagDetection();
            new Alert(Alert.AlertType.ERROR, "Error: Lector no seleccionado").show();
            return;
        }
        findTagTask(order);
    }

    private void findTagTask(Long numOrder) {
        Worker.State currentState = readTagUseCase.getState();
        if (currentState == Worker.State.READY) {
            logger.info(LogMessages.fromComponent("UHFTagController", LogMessages.EPC_READ_STARTED));
            readTagUseCase.start();
        } else if (currentState == Worker.State.RUNNING) {
            logger.info(LogMessages.fromComponent("UHFTagController", LogMessages.TASK_ALREADY_RUNNING));
        } else {
            logger.info(LogMessages.fromComponent("UHFTagController", LogMessages.TASK_RESTARTING));
            readTagUseCase.restart();
        }
    }

    private void processResult(ReadWriteResult result) {
        if (Objects.nonNull(result) && result.isShowMessage()) {
            new Alert(Alert.AlertType.ERROR, result.getMessage()).show();
        }
        setSaveAction(ACTIVATE, true);
        if (result.getErrorCode().equals(ErrorCode.OK)) {
            //this.txtIdOrdenFabricacion.setText("");
            //startNotification();
        }
    }

    private boolean isValidRx(RxDto rxDto) {
        return Objects.nonNull(rxDto) && rxDto.getIsValid();
    }

    /**
     * Diálogo de búsqueda para asignar un TAG a una persona.
     * Filtros horizontales: nombre, apellido, documento, correo.
     * Resultados en tabla inferior con colores pastel.
     */
    private People mostrarDialogoBusquedaPeople() {
        Dialog<People> dialog = new Dialog<>();
        dialog.setTitle("Buscar persona para asignar TAG");

        DialogPane pane = dialog.getDialogPane();
        ButtonType btnSeleccionar = new ButtonType("Seleccionar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().setAll(btnSeleccionar, btnCancelar);

        // --- Filtros horizontales ---
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");

        TextField txtApellido = new TextField();
        txtApellido.setPromptText("Apellido");

        TextField txtDocumento = new TextField();
        txtDocumento.setPromptText("Nro. documento");

        TextField txtCorreo = new TextField();
        txtCorreo.setPromptText("Correo");

        Button btnBuscar = new Button("Buscar");

        HBox filtros = new HBox(8, txtNombre, txtApellido, txtDocumento, txtCorreo, btnBuscar);
        filtros.setPadding(new Insets(8));
        filtros.setStyle("-fx-background-color: #f4f4ff;"); // pastel suave
        filtros.setFillHeight(true);

        // --- Tabla de resultados ---
        TableView<People> tabla = new TableView<>();
        tabla.setPrefHeight(260);

        TableColumn<People, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setPrefWidth(150);
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFullName() == null ? "" : c.getValue().getFullName()));

        TableColumn<People, String> colApellido = new TableColumn<>("Apellido");
        colApellido.setPrefWidth(150);
        colApellido.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getLastName() == null ? "" : c.getValue().getLastName()));

        TableColumn<People, String> colDoc = new TableColumn<>("Documento");
        colDoc.setPrefWidth(130);
        colDoc.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getDocNumber() == null ? "" : c.getValue().getDocNumber()));

        TableColumn<People, String> colCorreo = new TableColumn<>("Correo");
        colCorreo.setPrefWidth(180);
        colCorreo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getEmail() == null ? "" : c.getValue().getEmail()));

        tabla.getColumns().setAll(colNombre, colApellido, colDoc, colCorreo);

        VBox contenido = new VBox(10, filtros, tabla);
        contenido.setPadding(new Insets(10));
        contenido.setStyle("-fx-background-color: #fdf2ff;"); // fondo pastel

        pane.setContent(contenido);

        // --- Acción de búsqueda ---
        Runnable ejecutarBusqueda = () -> {
            String nombre = txtNombre.getText();
            String apellido = txtApellido.getText();
            String documento = txtDocumento.getText();
            String correo = txtCorreo.getText();

            java.util.List<People> resultados = peopleUseCase.buscarBasico(
                    nombre, apellido, documento, correo
            );
            tabla.setItems(FXCollections.observableArrayList(resultados));
        };

        btnBuscar.setOnAction(e -> ejecutarBusqueda.run());
        txtNombre.setOnAction(e -> ejecutarBusqueda.run());
        txtApellido.setOnAction(e -> ejecutarBusqueda.run());
        txtDocumento.setOnAction(e -> ejecutarBusqueda.run());
        txtCorreo.setOnAction(e -> ejecutarBusqueda.run());

        // Cargar datos iniciales (por ejemplo, todos según filtros vacíos)
        ejecutarBusqueda.run();

        dialog.setResultConverter(btn -> {
            if (btn == btnSeleccionar) {
                return tabla.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    /**
     * Diálogo de búsqueda para asignar un TAG a un producto.
     * Filtros horizontales: SKU y nombre.
     */
    private Product mostrarDialogoBusquedaProduct() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Buscar producto para asignar TAG");

        DialogPane pane = dialog.getDialogPane();
        ButtonType btnSeleccionar = new ButtonType("Seleccionar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        pane.getButtonTypes().setAll(btnSeleccionar, btnCancelar);

        // --- Filtros ---
        TextField txtSku = new TextField();
        txtSku.setPromptText("SKU");

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");

        Button btnBuscar = new Button("Buscar");

        HBox filtros = new HBox(8, txtSku, txtNombre, btnBuscar);
        filtros.setPadding(new Insets(8));
        filtros.setStyle("-fx-background-color: #e0f7fa;"); // pastel azul
        filtros.setFillHeight(true);

        // --- Tabla de resultados ---
        TableView<Product> tabla = new TableView<>();
        tabla.setPrefHeight(260);

        TableColumn<Product, String> colSku = new TableColumn<>("SKU");
        colSku.setPrefWidth(140);
        colSku.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getSku() == null ? "" : c.getValue().getSku()));

        TableColumn<Product, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setPrefWidth(220);
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getNombre() == null ? "" : c.getValue().getNombre()));

        tabla.getColumns().setAll(colSku, colNombre);

        VBox contenido = new VBox(10, filtros, tabla);
        contenido.setPadding(new Insets(10));
        contenido.setStyle("-fx-background-color: #e3f2fd;"); // fondo pastel

        pane.setContent(contenido);

        // --- Acción de búsqueda ---
        Runnable ejecutarBusqueda = () -> {
            String sku = txtSku.getText();
            String nombre = txtNombre.getText();

            java.util.List<Product> resultados = productUseCase.buscar(sku, nombre);
            tabla.setItems(FXCollections.observableArrayList(resultados));
        };

        btnBuscar.setOnAction(e -> ejecutarBusqueda.run());
        txtSku.setOnAction(e -> ejecutarBusqueda.run());
        txtNombre.setOnAction(e -> ejecutarBusqueda.run());

        // Cargar al inicio (p. ej. todos)
        ejecutarBusqueda.run();

        dialog.setResultConverter(btn -> {
            if (btn == btnSeleccionar) {
                return tabla.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private void abrirDialogoSegunTipo(Object tipo) {

        // Limpiar asignación
        cmbAsignacion.getItems().clear();
        cmbAsignacion.setValue(null);

        if (tipo == UHFTag.Tipo.PEOPLE) {
            People seleccionado = mostrarDialogoBusquedaPeople();
            if (seleccionado != null) {
                cmbAsignacion.getItems().add(seleccionado);
                cmbAsignacion.setValue(seleccionado);
            }
        }
        else if (tipo == UHFTag.Tipo.PRODUCT) {
            Product seleccionado = mostrarDialogoBusquedaProduct();
            if (seleccionado != null) {
                cmbAsignacion.getItems().add(seleccionado);
                cmbAsignacion.setValue(seleccionado);
            }
        }
    }

}