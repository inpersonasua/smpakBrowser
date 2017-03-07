package smpakBrowser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class RootLayoutController {

    private MainApp mainApp;
    private SmpakParser smpakParser;
    private File smpakFile = null;
    private ObservableList<FileEntry> files = FXCollections.observableArrayList();

    @FXML
    private AnchorPane rootLayuot;

    @FXML
    private TableView<FileEntry> filesTable;

    @FXML
    private TableColumn<FileEntry, String> fileColumn;

    @FXML
    private TableColumn<FileEntry, Number> fileSizeColumn;

    @FXML
    private Label statusBar;

    @FXML
    private SplitMenuButton extractAll;

    @FXML
    private MenuItem extractSelected;

    @FXML
    private void initialize() {
        filesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        filesTable.disableProperty().bind(Bindings.size(files).lessThan(1));

        extractSelected.disableProperty()
                .bind(Bindings.size(filesTable.getSelectionModel().getSelectedItems()).greaterThan(0).not());

        extractAll.disableProperty().bind(Bindings.size(files).greaterThan(0).not());

        filesTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super FileEntry>) c -> {
            Platform.runLater(() -> {
                statusBar.setText(Messages.getString("RootLayoutController.selected") + c.getList().size()); //$NON-NLS-1$
            });
        });

    }

    @FXML
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(
                Messages.getString("RootLayoutController.supermemoFile"), //$NON-NLS-1$
                Arrays.asList("*.smpak", "*.smdif", "*.smdif2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        fileChooser.getExtensionFilters().add(extensionFilter);
        fileChooser.setInitialFileName("course.smpak"); //$NON-NLS-1$

        smpakFile = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (smpakFile != null) {
            parse();
        }
    }

    private void parse() {
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                smpakParser = new SmpakParser(smpakFile.toPath());
                if (smpakParser.isSmpakFile()) {
                    smpakParser.parse();

                    files.clear();
                    files.addAll(smpakParser.getCachedEntries().values());
                }

                filesTable.setItems(files);

                fileColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().getFileName()));
                fileSizeColumn.setCellValueFactory(value -> new SimpleIntegerProperty(value.getValue().getFileSize()));

                return null;
            }
        };

        Thread convThread = new Thread(task);
        convThread.setDaemon(true);
        convThread.start();
    }

    @FXML
    private void extractAllFiles() {
        extract(files);
    }

    @FXML
    private void extractSelectedFiles() {
        extract(filesTable.getSelectionModel().getSelectedItems());
    }

    private void extract(ObservableList<FileEntry> fileEntries) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(Messages.getString("RootLayoutController.chooseDirectory")); //$NON-NLS-1$
        File outputDir = directoryChooser.showDialog(mainApp.getPrimaryStage());

        fileEntries.stream().forEach(fileEntry -> {
            try {
                Path pathToFile = Paths.get(outputDir.getAbsolutePath() + File.separator + fileEntry.getFileName());
                Files.createDirectories(pathToFile.getParent());
                Files.write(pathToFile, smpakParser.getFile(fileEntry.getFileName()), StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            smpakParser.getFile(fileEntry.getFileName());
        });
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
