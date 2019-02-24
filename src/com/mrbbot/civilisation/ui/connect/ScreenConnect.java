package com.mrbbot.civilisation.ui.connect;

import com.mrbbot.civilisation.logic.map.MapSize;
import com.mrbbot.civilisation.net.CivilisationServer;
import com.mrbbot.civilisation.ui.Screen;
import com.mrbbot.civilisation.ui.UIHelpers;
import com.mrbbot.generic.net.ClientOnly;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@ClientOnly
public class ScreenConnect extends Screen {
  private enum Choice {
    JOIN("Join a Game"),
    HOST("Create and Host a New Game"),
    LOAD("Load and Host a Saved Game");

    private String description;

    Choice(String description) {
      this.description = description;
    }
  }

  private class GameSave {
    private String filePath;
    private String gameName;

    private GameSave(String filePath, String gameName) {
      this.filePath = filePath;
      this.gameName = gameName;
    }
  }

  private final ClientCreator clientCreator;
  private final ServerCreator serverCreator;
  private GameSave[] saves;

  private Choice choice = Choice.JOIN;
  private MapSize selectedMapSize = MapSize.STANDARD;
  private ObservableList<String> nameList;
  private ComboBox<String> nameBox;
  private RadioButton[] sizeRadioButtons;
  private TextField hostField, portField, idField;
  private Button joinButton;

  private GridPane pane;
  private ProgressIndicator progressIndicator;

  public ScreenConnect(ClientCreator clientCreator, ServerCreator serverCreator) {
    this.clientCreator = clientCreator;
    this.serverCreator = serverCreator;
    try {
      String savesDirectoryPath = System.getProperty("user.dir") + File.separator + "saves";
      File savesDirectory = new File(savesDirectoryPath);
      if (!savesDirectory.exists()) {
        boolean made = savesDirectory.mkdir();
        if (!made) throw new IOException("unable to create saves directory");
      }
      saves = Files.list(Paths.get(savesDirectoryPath))
        .map(Path::toString)
        .filter(path -> path.endsWith(".yml"))
        .map(path -> {
          String name = "Unknown";
          try (FileReader reader = new FileReader(path)) {
            //noinspection unchecked
            Map<String, Object> map = CivilisationServer.YAML.loadAs(reader, Map.class);
            name = (String) map.get("name");
          } catch (IOException e) {
            e.printStackTrace();
          }
          return new GameSave(path, name);
        })
        .toArray(GameSave[]::new);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void setLoading(boolean loading) {
    pane.setOpacity(loading ? 0 : 1);
    progressIndicator.setVisible(loading);
  }

  private void setSizeRadioButtonsDisable(boolean disable) {
    for (RadioButton sizeRadioButton : sizeRadioButtons) {
      sizeRadioButton.setDisable(disable);
    }
  }

  private void checkJoinButtonEnabled() {
    boolean enabled = false;
    switch (choice) {
      case JOIN:
        enabled = !hostField.getText().isEmpty() && !portField.getText().isEmpty() && !idField.getText().isEmpty();
        break;
      case HOST:
        enabled = !nameBox.getEditor().getText().isEmpty() && !portField.getText().isEmpty() && !idField.getText().isEmpty();
        break;
      case LOAD:
        enabled = nameBox.getValue() != null && !nameBox.getValue().isEmpty() && !portField.getText().isEmpty() && !idField.getText().isEmpty();
        break;
    }
    joinButton.setDisable(!enabled);
  }

  private void resetForChoice(Choice choice) {
    this.choice = choice;
    switch (choice) {
      case JOIN:
        nameList.clear();
        nameBox.setDisable(true);
        nameBox.setEditable(false);
        setSizeRadioButtonsDisable(true);
        hostField.setDisable(false);
        joinButton.setText("Join");
        break;
      case HOST:
        nameList.clear();
        nameBox.setDisable(false);
        nameBox.setEditable(true);
        setSizeRadioButtonsDisable(false);
        hostField.setDisable(true);
        joinButton.setText("Host and Join");
        break;
      case LOAD:
        nameList.clear();
        nameList.addAll(Arrays.stream(saves).map(save -> save.gameName).collect(Collectors.toList()));
        nameBox.setDisable(false);
        nameBox.setEditable(false);
        if (nameList.size() > 0) nameBox.setValue(nameList.get(0));
        setSizeRadioButtonsDisable(true);
        hostField.setDisable(true);
        joinButton.setText("Host and Join");
        break;
    }
    checkJoinButtonEnabled();
  }

  private void launch() {
    setLoading(true);
    Thread bootstrapThread = new Thread(() -> {
      try {
        int port = Integer.parseInt(portField.getText());
        String id = idField.getText();

        switch (choice) {
          case JOIN:
            String host = hostField.getText();
            clientCreator.createClient(host, port, id);
            break;
          case HOST:
            String newGameName = nameBox.getEditor().getText();
            String newGameFileName = "saves" + File.separator + newGameName.toLowerCase().replaceAll(" ", "_") + ".yml";
            serverCreator.createServer(newGameFileName, newGameName, selectedMapSize, port);
            clientCreator.createClient("127.0.0.1", port, id);
            break;
          case LOAD:
            String loadGameName = nameBox.getValue();
            GameSave loadGameSave = null;
            for (GameSave gameSave : saves) {
              if (gameSave.gameName.equals(loadGameName)) {
                loadGameSave = gameSave;
                break;
              }
            }
            assert loadGameSave != null;
            //gameName = null signals load
            serverCreator.createServer(loadGameSave.filePath, null, MapSize.STANDARD, port);
            clientCreator.createClient("127.0.0.1", port, id);
            break;
        }

      } catch (IOException e) {
        Platform.runLater(() -> {
          setLoading(false);
          UIHelpers.showDialog(e.getMessage(), true);
        });
        e.printStackTrace();
      }
    });
    bootstrapThread.setName("Bootstrap");
    bootstrapThread.start();
  }

  @SuppressWarnings("Duplicates")
  @Override
  public Scene makeScene(Stage stage, int width, int height) {
    ChangeListener<String> changeListener = (observable, oldValue, newValue) -> checkJoinButtonEnabled();

    pane = new GridPane();
    pane.setHgap(10);
    pane.setVgap(10);

    ToggleGroup choiceToggleGroup = new ToggleGroup();
    Choice[] choices = Choice.values();
    for (int i = 0; i < choices.length; i++) {
      final Choice choice = choices[i];
      RadioButton choiceRadioButton = new RadioButton(choice.description);
      choiceRadioButton.setToggleGroup(choiceToggleGroup);
      choiceRadioButton.setOnAction(e -> resetForChoice(choice));
      pane.add(choiceRadioButton, 0, i, 4, 1);
      if (choice == Choice.JOIN) choiceRadioButton.setSelected(true);
    }

    Label nameLabel = new Label("Name");
    Label hostLabel = new Label("Host");
    Label portLabel = new Label("Port");
    Label idLabel = new Label("ID");
    nameLabel.setPrefWidth(80);
    hostLabel.setPrefWidth(80);
    portLabel.setPrefWidth(80);
    idLabel.setPrefWidth(80);

    nameList = FXCollections.observableArrayList();
    nameBox = new ComboBox<>(nameList);
    nameBox.setPrefWidth(300);
    nameBox.setEditable(true);
    nameBox.valueProperty().addListener(changeListener);
    nameBox.getEditor().textProperty().addListener(changeListener);

    HBox sizeBox = new HBox(10);
    ToggleGroup sizeToggleGroup = new ToggleGroup();
    MapSize[] mapSizes = MapSize.values();
    sizeRadioButtons = new RadioButton[mapSizes.length];
    for (int i = 0; i < mapSizes.length; i++) {
      final MapSize mapSize = mapSizes[i];
      RadioButton sizeRadioButton = new RadioButton(mapSize.name);
      sizeRadioButton.setToggleGroup(sizeToggleGroup);
      sizeRadioButton.setOnAction(e -> selectedMapSize = mapSize);
      if (mapSize == MapSize.STANDARD) sizeRadioButton.setSelected(true);
      sizeRadioButtons[i] = sizeRadioButton;
      sizeBox.getChildren().add(sizeRadioButton);
    }

    hostField = new TextField("127.0.0.1");
    portField = new TextField("1234");
    idField = new TextField();

    portField.setTextFormatter(new TextFormatter<>(change -> isDigits(change.getText()) ? change : null));

    hostField.textProperty().addListener(changeListener);
    portField.textProperty().addListener(changeListener);
    idField.textProperty().addListener(changeListener);

    joinButton = new Button("Join");
    joinButton.setPrefWidth(300);
    joinButton.setOnAction(e -> this.launch());

    pane.add(nameLabel, 0, choices.length + 1, 1, 1);
    pane.add(nameBox, 1, choices.length + 1, 3, 1);

    pane.add(sizeBox, 0, choices.length + 2, 4, 1);

    pane.add(hostLabel, 0, choices.length + 4);
    pane.add(hostField, 1, choices.length + 4);
    pane.add(portLabel, 2, choices.length + 4);
    pane.add(portField, 3, choices.length + 4);

    pane.add(idLabel, 0, choices.length + 5);
    pane.add(idField, 1, choices.length + 5, 3, 1);

    pane.add(joinButton, 0, choices.length + 7, 4, 1);

    resetForChoice(Choice.JOIN);

    progressIndicator = new ProgressIndicator();
    progressIndicator.setMaxSize(100, 100);
    setLoading(false);

    StackPane layers = new StackPane(pane, progressIndicator);
    layers.setAlignment(Pos.CENTER);

    StackPane root = new StackPane(makeTitledPane("Game", layers));
    root.setAlignment(Pos.CENTER);
    return new Scene(root, width, height);
  }

  private boolean isDigits(String text) {
    for (char c : text.toCharArray()) {
      int code = (int) c;
      if (code < 48 || code > 57) return false;
    }
    return true;
  }

  private TitledPane makeTitledPane(String title, Node child) {
    TitledPane titledPane = new TitledPane(title, child);
    titledPane.setMaxSize(300, 0);
    titledPane.setCollapsible(false);
    return titledPane;
  }
}
