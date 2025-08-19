package infrastructure.fx.component;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Popup;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DatePicker con selector directo de año y mes.
 * - Hereda de DatePicker (puedes seguir tipeándolo como DatePicker en tu controller).
 * - Reemplaza show() para abrir un popup propio con combo de año/mes y grilla de días.
 * - Rango de años configurable (minYear/maxYear), por defecto 1900..año actual.
 */
public class YearPickerDate extends DatePicker {

    private final Popup popup = new Popup();
    private YearMonth displayed;
    private ComboBox<Month> cmbMonth;
    private ComboBox<Integer> cmbYear;
    private GridPane dayGrid;

    private final IntegerProperty minYear = new SimpleIntegerProperty(this, "minYear", 1900);
    private final IntegerProperty maxYear = new SimpleIntegerProperty(this, "maxYear", LocalDate.now().getYear());

    public YearPickerDate() {
        super();
        // skin estándar para conservar editor y botón
        setSkin(new DatePickerSkin(this));

        // valor inicial y mes mostrado
        LocalDate base = getValue() != null ? getValue() : LocalDate.now();
        displayed = YearMonth.of(base.getYear(), base.getMonth());

        // cerrar con ESC desde el editor
        getEditor().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                hidePopup();
            }
        });

        popup.setAutoHide(true);
        popup.setAutoFix(true);
    }

    // ==== API de propiedades para FXML ====
    public int getMinYear() { return minYear.get(); }
    public void setMinYear(int y) { this.minYear.set(y); }
    public IntegerProperty minYearProperty() { return minYear; }

    public int getMaxYear() { return maxYear.get(); }
    public void setMaxYear(int y) { this.maxYear.set(y); }
    public IntegerProperty maxYearProperty() { return maxYear; }

    @Override
    protected Skin<?> createDefaultSkin() {
        // mantenemos DatePickerSkin para el editor + botón
        return new DatePickerSkin(this);
    }

    @Override
    public void show() {
        if (isDisabled() || popup.isShowing()) return;

        if (getValue() != null) {
            displayed = YearMonth.from(getValue());
        } else if (displayed == null) {
            displayed = YearMonth.now();
        }

        Pane content = buildPopupContent();
        popup.getScene().setRoot(content);
        positionAndShow();
    }

    @Override
    public void hide() {
        hidePopup();
    }

    private void hidePopup() {
        if (popup.isShowing()) popup.hide();
    }

    private void positionAndShow() {
        Bounds b = localToScreen(getBoundsInLocal());
        double x = b.getMinX();
        double y = b.getMaxY();
        popup.show(this, x, y);
    }

    private Pane buildPopupContent() {
        // header: mes + año + botones navegar
        HBox header = new HBox(8);
        header.setPadding(new Insets(4));
        header.setAlignment(Pos.CENTER_LEFT);

        Button btnPrev = new Button("<");
        Button btnNext = new Button(">");

        Locale locale = Locale.getDefault();
        cmbMonth = new ComboBox<>();
        cmbMonth.getItems().setAll(Month.values());
        cmbMonth.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Month item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName(TextStyle.FULL, locale));
            }
        });
        cmbMonth.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Month item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName(TextStyle.FULL, locale));
            }
        });

        cmbYear = new ComboBox<>();
        List<Integer> years = new ArrayList<>();
        int from = getMinYear();
        int to = getMaxYear();
        if (from > to) { int tmp = from; from = to; to = tmp; }
        for (int y = to; y >= from; y--) years.add(y); // descendente para llegar rápido a años antiguos
        cmbYear.getItems().setAll(years);

        cmbMonth.getSelectionModel().select(displayed.getMonth());
        cmbYear.getSelectionModel().select((Integer) displayed.getYear());

        btnPrev.setOnAction(e -> {
            displayed = displayed.minusMonths(1);
            syncHeader();
            rebuildCalendar();
        });

        btnNext.setOnAction(e -> {
            displayed = displayed.plusMonths(1);
            syncHeader();
            rebuildCalendar();
        });

        cmbMonth.setOnAction(e -> {
            Month m = cmbMonth.getValue();
            if (m != null) {
                displayed = YearMonth.of(displayed.getYear(), m);
                rebuildCalendar();
            }
        });

        cmbYear.setOnAction(e -> {
            Integer y = cmbYear.getValue();
            if (y != null) {
                int yy = Math.max(getMinYear(), Math.min(getMaxYear(), y));
                displayed = YearMonth.of(yy, displayed.getMonth());
                rebuildCalendar();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnToday = new Button("Hoy");
        btnToday.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            setValue(today);
            displayed = YearMonth.from(today);
            hidePopup();
        });

        Button btnClear = new Button("Clear");
        btnClear.setOnAction(e -> {
            setValue(null);
            hidePopup();
        });

        header.getChildren().addAll(btnPrev, cmbMonth, cmbYear, btnNext, spacer, btnClear);

        dayGrid = new GridPane();
        dayGrid.setHgap(4);
        dayGrid.setVgap(4);
        dayGrid.setPadding(new Insets(3));

        // llenamos el calendario
        rebuildCalendar();

        VBox root = new VBox(header, dayGrid);
        root.getStyleClass().add("year-selection-date-picker");
        root.setPrefWidth(355);

        // 👇 Añadimos un fondo y borde para que no sea transparente
        root.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #ccc; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 2, 2);"
        );

        return root;
    }

    private void syncHeader() {
        if (cmbYear != null && cmbMonth != null) {
            if (!cmbYear.getItems().contains(displayed.getYear())) {
                int clamped = Math.max(getMinYear(), Math.min(getMaxYear(), displayed.getYear()));
                displayed = YearMonth.of(clamped, displayed.getMonth());
            }
            cmbYear.getSelectionModel().select((Integer) displayed.getYear());
            cmbMonth.getSelectionModel().select(displayed.getMonth());
        }
    }

    private void rebuildCalendar() {
        dayGrid.getChildren().clear();

        Locale locale = Locale.getDefault();
        DayOfWeek firstDay = WeekFields.of(locale).getFirstDayOfWeek();
        DayOfWeek dow = firstDay;

        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(dow.getDisplayName(TextStyle.SHORT_STANDALONE, locale));
            lbl.setStyle("-fx-font-weight: bold;");
            StackPane cell = new StackPane(lbl);
            cell.setPrefSize(44, 24);
            dayGrid.add(cell, i, 0);
            dow = dow.plus(1);
        }

        LocalDate firstOfMonth = displayed.atDay(1);
        int length = displayed.lengthOfMonth();

        int firstCol = (firstOfMonth.getDayOfWeek().getValue() - firstDay.getValue() + 7) % 7;
        int row = 1;
        int col = firstCol;

        LocalDate minDate = LocalDate.of(getMinYear(), 1, 1);
        LocalDate maxDate = LocalDate.of(getMaxYear(), 12, 31);

        for (int day = 1; day <= length; day++) {
            LocalDate date = displayed.atDay(day);

            Button b = new Button(String.valueOf(day));
            b.setMaxWidth(Double.MAX_VALUE);
            b.setPrefSize(44, 28);

            boolean outOfRange = date.isBefore(minDate) || date.isAfter(maxDate);
            b.setDisable(outOfRange);

            LocalDate val = getValue();
            if (val != null && val.equals(date)) {
                b.setStyle("-fx-background-color: -fx-accent; -fx-text-fill: white;");
            } else if (date.equals(LocalDate.now())) {
                b.setStyle("-fx-border-color: -fx-accent;");
            }

            b.setOnAction(e -> {
                if (!b.isDisable()) {
                    setValue(date);
                    hidePopup();
                }
            });

            GridPane.setHgrow(b, Priority.ALWAYS);
            dayGrid.add(b, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        syncHeader();
    }
}