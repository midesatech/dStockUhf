package app.config;

import domain.usecase.CategoriaUseCase;
import infrastructure.fx.controller.LoginController;
import infrastructure.fx.controller.catalog.CategoriasController;
import infrastructure.fx.controller.catalog.EmpleadosController;
import infrastructure.fx.controller.system.ChangePasswordController;
import infrastructure.fx.controller.system.RolesController;
import infrastructure.fx.controller.system.UserController;
import javafx.util.Callback;

public class ControllerFactory implements Callback<Class<?>, Object> {

    @Override
    public Object call(Class<?> type) {
        if (type == CategoriasController.class) {
            CategoriaUseCase categoriaUC = AppBootstrap.categoriaUseCase();
            return new CategoriasController(categoriaUC);
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

        if (type == EmpleadosController.class) {
            return new EmpleadosController(AppBootstrap.empleadoUseCase());
        }



        // otros controladores que necesiten dependencias
        throw new IllegalArgumentException("No controller mapping for " + type);
    }
}
