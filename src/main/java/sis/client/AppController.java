package sis.client;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.DataStores;
import sis.client.banner.BannerController;
import sis.client.dnd.DndController;
import sis.client.map.Map;

/**
 * FXML Controller class
 *
 * @author Siddhesh Rane
 */
public class AppController implements Initializable {

    @FXML
    Pane banner;
    @FXML
    BannerController bannerController;
    @FXML
    BorderPane borderPane;

    @FXML
    TabPane tabPane;
    @FXML
    Tab homeTab;
    @FXML
    Tab mapTab;
    @FXML
    Tab fileTab;

    Pane dndPane;
    @FXML
    DndController dndController;

    @FXML
    StackPane homePane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mapTab.setContent(map);
        dndController.setFileNodeFactory(this::createDisplayNode);

        generateDiagnostics();
    }

    private void generateDiagnostics() {
        
    }

    public AppController() {
        map = new Map();
    }
    private final Map map;

    @FXML
    void onDragEntered(DragEvent de) {
        if (de.getDragboard().hasFiles() && de.getSource() != tabPane) {
            tabPane.getSelectionModel().select(fileTab);
        }
    }

    @FXML
    void onDragExited(DragEvent de) {

    }

    public void openMetadataTab(File file) {
        Tab tab = new Tab(file.getName());
        tab.setClosable(true);
        tab.setContent(new MetadataView(file));
        tabPane.getTabs().add(tab);
    }

    /**
     * This function generates a graphical {@code Node} to represent
     * {@code file} in the drag and drop pane.
     *
     * @param file the file that needs to be represented
     * @return a new node to represent file
     */
    private Node createDisplayNode(File file) {
        final Label icon = AwesomeDude.createIconLabel(AwesomeIcon.FILE, "45");
        icon.getStyleClass().add("icon");
        Text filename = new Text(file.getName());
        filename.getStyleClass().add("filename");
        String mime = "N/A";
        try {
            mime = DataStores.probeContentType(file);
            if (mime == null) {
                mime = Files.probeContentType(file.toPath()) + '*';
            }
        } catch (DataStoreException ex) {
            mime = "N/A";
        } catch (IOException ex) {
            Logger.getLogger(DndController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Text type = new Text(mime);
        type.getStyleClass().add("mime");
        VBox vBox = new VBox(icon, filename, type);
        vBox.getStyleClass().add("file-vbox");
        final MenuItem openMeta = new MenuItem("Open metadata");
        openMeta.setOnAction(ae -> {
            openMetadataTab(file);
        });
        ContextMenu cm = new ContextMenu(openMeta);

        vBox.setOnContextMenuRequested(cme -> {
            cm.show(vBox, cme.getScreenX(), cme.getScreenY());
        });
        return vBox;
    }

    
//    private void createCRSTable() {
//        FXCRSTable table = new FXCRSTable();
//        Tab tab = new Tab("CRS");
//        tab.setContent(table);
//        tabPane.getTabs().add(tab);
//    }
}
