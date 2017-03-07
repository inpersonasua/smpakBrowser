package smpakBrowser;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    private Stage primaryStage;
    private AnchorPane rootLayout;
    private RootLayoutController controller;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("smpakBrowser");
		initRootLayout();
	}
	
	private void initRootLayout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("RootLayout.fxml"));
            fxmlLoader.setResources(ResourceBundle.getBundle("messages", Locale.getDefault()));
            rootLayout = (AnchorPane) fxmlLoader.load();
            Scene scene = new Scene(rootLayout);
            
            primaryStage.setScene(scene);
            
            controller = fxmlLoader.getController();
            controller.setMainApp(this);
            
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static void main(String[] args) {
		launch(args);
	}

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}