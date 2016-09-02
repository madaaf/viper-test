package fr.viper.app.login;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.nicolasmouchel.executordecorator.ExecutorDecorator;

import fr.viper.app.ApplicationModule;
import fr.viper.app.Attachable;
import fr.viper.app.login.controller.LoginController;
import fr.viper.app.login.controller.LoginControllerDecorator;
import fr.viper.app.login.controller.LoginControllerImpl;
import fr.viper.app.login.presentation.LoginPresenterImpl;
import fr.viper.app.login.presentation.LoginView;
import fr.viper.app.login.presentation.LoginViewDecorator;
import fr.viper.core.login.LoginInteractor;
import fr.viper.core.login.LoginPresenter;
import fr.viper.core.login.LoginRepository;
import fr.viper.repositories.login.FakeLoginRepository;
import fr.viper.repositories.login.SimulateDelayLoginRepository;

public class LoginModule {
    private final ApplicationModule applicationModule;
    private final LoginView view;

    public LoginModule(ApplicationModule applicationModule) {
        this.applicationModule = applicationModule;
        this.view = getView();
    }

    public LoginViewDecorator getViewDecorator() {
        return (LoginViewDecorator) view;
    }

    @ExecutorDecorator()
    public LoginController getController() {
        final LoginInteractor interactor = new LoginInteractor(getRepository(), getPresenter());
        final LoginControllerImpl controller = new LoginControllerImpl(interactor);
        return new LoginControllerDecorator(applicationModule.getAsyncExecutor(),controller);
    }

    @ExecutorDecorator(mutable = true)
    private LoginView getView() {
        return new LoginViewDecorator(applicationModule.getUiThreadExecutor());
    }

    private LoginRepository getRepository() {
        final ObjectMapper objectMapper = applicationModule.getMapperModule().getObjectMapper();
        final FakeLoginRepository repository = new FakeLoginRepository(objectMapper);
        return new SimulateDelayLoginRepository(repository);
    }

    private LoginPresenter getPresenter(){
        return new LoginPresenterImpl(view, applicationModule.getContext());
    }
}
