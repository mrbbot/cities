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

/**
 * Screen for letting a user join a game, create a new game, or load an
 * existing game. The first screen the user lands on when the game starts.
 */
@ClientOnly
public class ScreenConnect extends Screen {
  /**
   * Enum representing the choices the user has on this screen. These are
   * translated to radio buttons to allow the user to select each one. When
   * clicked they en/disable various UI components that are(n't) needed.
   */
  private enum Choice {
    JOIN("Join a Game"),
    HOST("Create and Host a New Game"),
    LOAD("Load and Host a Saved Game");

    /**
     * Description of this choice. To be displayed on the radio button for this
     * choice.
     */
    private String description;

    Choice(String description) {
      this.description = description;
    }
  }

  /**
   * Class representing a game save file to be shown in the save list
   */
  private class GameSave {
    /**
     * Path to the game save file, should end with .yml.
     */
    private String filePath;
    /**
     * Name of the game, loaded from the file
     */
    private String gameName;

    private GameSave(String filePath, String gameName) {
      this.filePath = filePath;
      this.gameName = gameName;
    }
  }

  /**
   * Function to be called when the user requests a connection to a server
   */
  private final ClientCreator clientCreator;
  /**
   * Function to be called when the user requests a server be created
   */
  private final ServerCreator serverCreator;
  /**
   * Array containing all the game saves in the "saves" directory
   */
  private GameSave[] saves;

  /**
   * User's selected choice. Determines what UI elements to enable and how to
   * handle the Join/Host button click.
   */
  private Choice choice = Choice.JOIN;
  /**
   * User's selected map size. Used when creating a new game.
   */
  private MapSize selectedMapSize = MapSize.STANDARD;
  /**
   * List containing names of game saves. Observable so changes made to it can
   * be reflected in the combo box for names.
   */
  private ObservableList<String> nameList;
  /**
   * Combo box for name that lists all the values in nameList.
   */
  private ComboBox<String> nameBox;
  /**
   * Array containing all radio buttons for controlling map size. Stored so
   * they can be en/disabled when the user's choice changes.
   */
  private RadioButton[] sizeRadioButtons;
  /**
   * Text field for the host name of the server to connect too.
   */
  private TextField hostField;
  /**
   * Text field for the port number of the server to connect too. Should only
   * accept numeric values.
   */
  private TextField portField;
  /**
   * Text field for the user's player ID when joining a server. Controls unit/
   * cities owners, panel border colours, etc.
   */
  private TextField idField;
  /**
   * Button that joins/hosts a game depending on the user's choice. Should
   * only be enabled if all the required UI components have sensible data.
   */
  private Button joinButton;

  /**
   * Pane containing UI elements for choices, host, sizes, id, port, and other
   * connection details. Should be hidden when the user clicks the join button.
   */
  private GridPane pane;
  /**
   * Loading indicator shown when the user clicks the join button to indicate
   * that something is happening.
   */
  private ProgressIndicator progressIndicator;

  /**
   * Constructor for a new connection screen
   *
   * @param clientCreator callback function for creating a client
   * @param serverCreator callback function for creating a server
   */
  public ScreenConnect(
    ClientCreator clientCreator,
    ServerCreator serverCreator
  ) {
    this.clientCreator = clientCreator;
    this.serverCreator = serverCreator;

    // Load all available game saves
    try {
      // Get a reference to the saves directory
      // ("current working directory/saves")
      String savesDirectoryPath =
        System.getProperty("user.dir") + File.separator + "saves";
      File savesDirectory = new File(savesDirectoryPath);

      // Check if the folder exists, otherwise make it
      if (!savesDirectory.exists()) {
        boolean made = savesDirectory.mkdir();
        if (!made) throw new IOException("unable to create saves directory");
      }

      // Load the list of GameSave objects
      saves = Files.list(Paths.get(savesDirectoryPath))
        // Convert path objects to their absolute file path
        .map(Path::toString)
        // We only want files ending with .yml
        .filter(path -> path.endsWith(".yml"))
        .map(path -> {
          // Load the game save as we normally would to extract the name
          String name = "Unknown";
          try (FileReader reader = new FileReader(path)) {
            //noinspection unchecked
            Map<String, Object> map =
              CivilisationServer.YAML.loadAs(reader, Map.class);
            name = (String) map.get("name");
          } catch (IOException e) {
            e.printStackTrace();
          }
          // Return a new game save object with the required data
          return new GameSave(path, name);
        })
        // Convert the stream to an array
        .toArray(GameSave[]::new);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Set the loading state. Shows/hides the details pane/loading indicator.
   *
   * @param loading whether to show the loading indicator
   */
  private void setLoading(boolean loading) {
    // Hide the pane if we're loading. Do this with setOpacity not setVisible
    // so the pane is still used in layout calculations (so the wrapping title
    // pane doesn't change size).
    pane.setOpacity(loading ? 0 : 1);
    // We can just use setVisible for the loading indicator. It's smaller than
    // the pane.
    progressIndicator.setVisible(loading);
  }

  /**
   * Set all the size radio buttons disabled state
   *
   * @param disable whether all the buttons should be disabled
   */
  private void setSizeRadioButtonsDisable(boolean disable) {
    for (RadioButton sizeRadioButton : sizeRadioButtons) {
      sizeRadioButton.setDisable(disable);
    }
  }

  /**
   * Calculates whether the join button should be enabled from the state of the
   * other UI components. Which UI components to check depends on the user's
   * choice.
   */
  private void checkJoinButtonEnabled() {
    boolean enabled = false;
    switch (choice) {
      case JOIN:
        // If we're joining a game, we need a host name, port number, and
        // player ID
        enabled = !hostField.getText().isEmpty()
          && !portField.getText().isEmpty()
          && !idField.getText().isEmpty();
        break;
      case HOST:
        // If we're hosting a game, we need a new game name, port number, and
        // player ID. We also need a map size, but this will always be set.
        enabled = !nameBox.getEditor().getText().isEmpty()
          && !portField.getText().isEmpty()
          && !idField.getText().isEmpty();
        break;
      case LOAD:
        // If we're loading an existing game, we need an existing game name, a
        // port number, and player ID.
        enabled = nameBox.getValue() != null
          && !nameBox.getValue().isEmpty()
          && !portField.getText().isEmpty()
          && !idField.getText().isEmpty();
        break;
    }
    // Set the enabled state
    joinButton.setDisable(!enabled);
  }

  /**
   * En/disables the required UI components for the user's new choice
   * selection.
   *
   * @param choice new selected choice
   */
  private void resetForChoice(Choice choice) {
    // Store the choice selection
    this.choice = choice;
    switch (choice) {
      case JOIN:
        // If we're joining a game, we need a host name, port number, and
        // player ID
        nameList.clear();
        nameBox.setDisable(true);
        nameBox.setEditable(false);
        // Disable map size selection buttons
        setSizeRadioButtonsDisable(true);
        hostField.setDisable(false);
        joinButton.setText("Join");
        break;
      case HOST:
        // If we're hosting a game, we need a new game name, map size, port
        // number, and player ID.
        nameList.clear();
        nameBox.setDisable(false);
        nameBox.setEditable(true);
        // Enable map size selection buttons
        setSizeRadioButtonsDisable(false);
        hostField.setDisable(true);
        joinButton.setText("Host and Join");
        break;
      case LOAD:
        // If we're loading an existing game, we need an existing game name, a
        // port number, and player ID.
        nameList.clear();
        // Get existing game names
        nameList.addAll(
          Arrays.stream(saves)
            .map(save -> save.gameName)
            .collect(Collectors.toList())
        );
        nameBox.setDisable(false);
        nameBox.setEditable(false);
        if (nameList.size() > 0) nameBox.setValue(nameList.get(0));
        // Disable map size selection buttons
        setSizeRadioButtonsDisable(true);
        hostField.setDisable(true);
        joinButton.setText("Host and Join");
        break;
    }
    checkJoinButtonEnabled();
  }

  /**
   * Called on click of the join button.
   */
  private void launch() {
    // Show the loading indicator
    setLoading(true);
    // Start a new thread for launching the client/server. This should be
    // done in a separate thread so the UI thread isn't blocked. This would
    // cause the loading spinner animation not to work.
    Thread bootstrapThread = new Thread(() -> {
      try {
        // Get the port number and player ID as these are required for all
        // choices
        int port = Integer.parseInt(portField.getText());
        String id = idField.getText();

        switch (choice) {
          case JOIN:
            // If we're joining a game, get the host name and connect to it
            String host = hostField.getText();
            clientCreator.createClient(host, port, id);
            break;
          case HOST:
            // If we're hosting a game, get the new game name
            String newGameName = nameBox.getEditor().getText();
            // Make a file name for this game name. (lower case,
            // spaces -> underscores, + .yml)
            String newGameFileName = "saves" + File.separator + newGameName
              .toLowerCase()
              .replaceAll(" ", "_")
              + ".yml";
            // Create the server and then immediately connect to it as if it
            // was over the network. Even though we running these in the same
            // program instance and could exchange data more efficiently, this
            // reduces the need to write duplicate code for exchanging data
            // between the client and server.
            serverCreator.createServer(
              newGameFileName,
              newGameName,
              selectedMapSize,
              port
            );
            // Use the local loopback address as the host name (this computer)
            clientCreator.createClient("127.0.0.1", port, id);
            break;
          case LOAD:
            // Get the existing game name
            String loadGameName = nameBox.getValue();
            // Try and find the game save with that name, we should be able to
            // because these names come from the list of game saves
            GameSave loadGameSave = null;
            for (GameSave gameSave : saves) {
              if (gameSave.gameName.equals(loadGameName)) {
                loadGameSave = gameSave;
                break;
              }
            }
            // Check we did find a save
            assert loadGameSave != null;
            // Create the server and then immediately connect to it as if it
            // was over the network. See above comment for more details. We
            // pass null as the game name here to signify that we want to load
            // the game save. This makes the passed map size irrelevant.
            serverCreator.createServer(
              loadGameSave.filePath,
              null,
              MapSize.STANDARD,
              port
            );
            // Use the local loopback address as the host name (this computer)
            clientCreator.createClient("127.0.0.1", port, id);
            break;
        }

      } catch (IOException e) {
        // If there was an error, show a dialog on the UI thread stating such
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

  /**
   * Function to check whether text only contains digits
   *
   * @param text text to check against
   * @return whether the text only contains digits
   */
  private boolean isDigits(String text) {
    for (char c : text.toCharArray()) {
      int code = (int) c;
      // Check every ASCII code is between 0 and 9.
      if (code < 48 || code > 57) return false;
    }
    return true;
  }

  /**
   * Wraps a node with a titled border
   *
   * @param title title for the border
   * @param child node to wrap
   * @return titled pane containing the child
   */
  private TitledPane makeTitledPane(String title, Node child) {
    TitledPane titledPane = new TitledPane(title, child);
    // Specify a desired size for the child (it would fill the screen
    // otherwise)
    titledPane.setMaxSize(300, 0);
    // Titled panes are collapsible by default which is something we don't
    // really want
    titledPane.setCollapsible(false);
    return titledPane;
  }

  /**
   * Creates a scene representing this screen
   *
   * @param stage  stage the scene would be placed in
   * @param width  width of the screen
   * @param height height of the screen
   * @return scene representing this screen
   */
  @SuppressWarnings("Duplicates")
  @Override
  public Scene makeScene(Stage stage, int width, int height) {
    // Create a change listener that will be used in all UI components to
    // check whether the join button should be enabled when data changes.
    ChangeListener<String> changeListener =
      (observable, oldValue, newValue) -> checkJoinButtonEnabled();

    pane = new GridPane();
    pane.setHgap(10);
    pane.setVgap(10);

    // Create choice radio buttons, adding them to a toggle group so only one
    // choice can be selected at once.
    ToggleGroup choiceToggleGroup = new ToggleGroup();
    Choice[] choices = Choice.values();
    for (int i = 0; i < choices.length; i++) {
      final Choice choice = choices[i];
      RadioButton choiceRadioButton = new RadioButton(choice.description);
      choiceRadioButton.setToggleGroup(choiceToggleGroup);
      // Reset other UI components on button selection change
      choiceRadioButton.setOnAction(e -> resetForChoice(choice));
      // Add the button to the pane filling all available width
      pane.add(choiceRadioButton, 0, i, 4, 1);
      // Set a default selection
      if (choice == Choice.JOIN) choiceRadioButton.setSelected(true);
    }

    // Create labels
    Label nameLabel = new Label("Name");
    Label hostLabel = new Label("Host");
    Label portLabel = new Label("Port");
    Label idLabel = new Label("ID");
    nameLabel.setPrefWidth(80);
    hostLabel.setPrefWidth(80);
    portLabel.setPrefWidth(80);
    idLabel.setPrefWidth(80);

    // Create the game name box, this will act like a text field when creating
    // a new game, and a combo box when selecting a game to load
    nameList = FXCollections.observableArrayList();
    nameBox = new ComboBox<>(nameList);
    nameBox.setPrefWidth(300);
    nameBox.setEditable(true);
    nameBox.valueProperty().addListener(changeListener);
    // Watch for changes
    nameBox.getEditor().textProperty().addListener(changeListener);

    // Create the size radio buttons row
    HBox sizeBox = new HBox(10);
    ToggleGroup sizeToggleGroup = new ToggleGroup();
    MapSize[] mapSizes = MapSize.values();
    sizeRadioButtons = new RadioButton[mapSizes.length];
    for (int i = 0; i < mapSizes.length; i++) {
      final MapSize mapSize = mapSizes[i];
      RadioButton sizeRadioButton = new RadioButton(mapSize.name);
      sizeRadioButton.setToggleGroup(sizeToggleGroup);
      // Store the new selected state
      sizeRadioButton.setOnAction(e -> selectedMapSize = mapSize);
      // Set a default selection
      if (mapSize == MapSize.STANDARD) sizeRadioButton.setSelected(true);
      sizeRadioButtons[i] = sizeRadioButton;
      sizeBox.getChildren().add(sizeRadioButton);
    }

    // Create text fields
    hostField = new TextField("127.0.0.1");
    portField = new TextField("1234");
    idField = new TextField();

    // Make sure that the port field only allows digits to be inputted
    portField.setTextFormatter(
      // Returning null discards the change
      new TextFormatter<>(change -> isDigits(change.getText()) ? change : null)
    );

    // Register text change listeners
    hostField.textProperty().addListener(changeListener);
    portField.textProperty().addListener(changeListener);
    idField.textProperty().addListener(changeListener);

    // Create the join button that launches the game when clicked
    joinButton = new Button("Join");
    joinButton.setPrefWidth(300);
    joinButton.setOnAction(e -> this.launch());

    // Add UI components to the pane (argument order: node, column index, row
    // index, [column span, row span]) [spans are optional]
    pane.add(
      nameLabel,
      0, choices.length + 1,
      1, 1
    );
    pane.add(
      nameBox,
      1, choices.length + 1,
      3, 1
    );

    pane.add(
      sizeBox,
      0, choices.length + 2,
      4, 1
    );

    pane.add(hostLabel, 0, choices.length + 4);
    pane.add(hostField, 1, choices.length + 4);
    pane.add(portLabel, 2, choices.length + 4);
    pane.add(portField, 3, choices.length + 4);

    pane.add(idLabel, 0, choices.length + 5);
    pane.add(
      idField,
      1, choices.length + 5,
      3, 1
    );

    pane.add(
      joinButton,
      0, choices.length + 7,
      4, 1
    );

    // En/disable UI components for the default choice
    resetForChoice(Choice.JOIN);

    // Create the size loading indicator and hide it by default
    progressIndicator = new ProgressIndicator();
    progressIndicator.setMaxSize(100, 100);
    setLoading(false);

    // Create the layered layout
    StackPane layers = new StackPane(pane, progressIndicator);
    layers.setAlignment(Pos.CENTER);

    // Title the pane
    StackPane root = new StackPane(makeTitledPane("Game", layers));
    root.setAlignment(Pos.CENTER);

    // Create the scene
    return new Scene(root, width, height);
  }
}
