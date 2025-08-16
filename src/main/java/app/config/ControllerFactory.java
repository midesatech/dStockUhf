package app.config;

import domain.usecase.CategoriaUseCase;
import infrastructure.fx.controller.LoginController;
import infrastructure.fx.controller.catalog.CategoriasController;
import infrastructure.fx.controller.system.RolesController;
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

        // otros controladores que necesiten dependencias
        throw new IllegalArgumentException("No controller mapping for " + type);
    }
}
