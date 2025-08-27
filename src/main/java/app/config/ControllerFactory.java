package app.config;

import domain.usecase.CategoriaUseCase;
import infrastructure.fx.controller.LoginController;
import infrastructure.fx.controller.catalog.*;
import infrastructure.fx.controller.dashboard.DashboardController;
import infrastructure.fx.controller.stock.ScanController;
import infrastructure.fx.controller.stock.UHFTagController;
import infrastructure.fx.controller.system.ChangePasswordController;
import infrastructure.fx.controller.system.RolesController;
import infrastructure.fx.controller.system.UserController;
import javafx.util.Callback;

public class ControllerFactory implements Callback<Class<?>, Object> {

    @Override
    public Object call(Class<?> type) {
        if (type == CategoryController.class) {
            CategoriaUseCase categoriaUC = AppBootstrap.categoriaUseCase();
            return new CategoryController(categoriaUC);
        }

        if (type == LoginController.class) {
           return new LoginController();
        }

        if (type == RolesController.class) {
            return new RolesController(AppBootstrap.roleUseCase(), AppBootstrap.permissionUseCase());
        }

        if (type == UserController.class) {
            return new UserController(AppBootstrap.users(), AppBootstrap.roleUseCase());
        }

        if (type == ChangePasswordController.class) {
            return new ChangePasswordController();
        }

        if (type == EmployeeController.class) {
            return new EmployeeController(AppBootstrap.employeeUseCase());
        }

        if (type == UbicacionesController.class) {
            return new UbicacionesController(AppBootstrap.ubicacionUseCase());
        }

        if (type == ReaderController.class) {
            return new ReaderController(AppBootstrap.lectorUHFUseCase(), AppBootstrap.ubicacionUseCase());
        }

        if (type == EquipmentController.class) {
            return new EquipmentController(AppBootstrap.equipmentUseCase(), AppBootstrap.categoriaUseCase(), AppBootstrap.ubicacionUseCase());
        }

        if (type == UHFTagController.class) {
            return new UHFTagController(AppBootstrap.tagUhfUsecase(),
                    AppBootstrap.employeeUseCase(),
                    AppBootstrap.equipmentUseCase(),
                    AppBootstrap.readTagUseCase());
        }

        if (type == ScanController.class) {
            return new ScanController(
                    AppBootstrap.scanUseCase(),
                    AppBootstrap.tagUhfUsecase(),
                    AppBootstrap.employeeUseCase(),
                    AppBootstrap.equipmentUseCase(),
                    AppBootstrap.ubicacionUseCase()
            );
        }

        if (type == DashboardController.class) {
                     // El DashboardController arma internamente su servicio (mock o JDBC según env vars)
            return new DashboardController();
        }

        // otros controladores que necesiten dependencias
        throw new IllegalArgumentException("No controller mapping for " + type);
    }
}
