package fr.bouml;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Java doesn't define a simple Dialog just to ask to a question :-((
 * this one is defined here
 */
class ConfirmBox {
  private boolean choice;
  private final Stage stage;

  public ConfirmBox(String msg) {
    stage = new Stage();
    stage.setTitle("Html generator");
    stage.initModality(Modality.APPLICATION_MODAL);
    
    Label label = new Label(msg);
    
    Button yesButton = new Button("Yes");
    yesButton.setOnAction(e -> {
      choice = true;
      stage.close();
    });
    
    Button noButton = new Button("No");
    noButton.setOnAction(e -> {
      choice = false;
      stage.close();
    });
    
    HBox buttonBox = new HBox(10, yesButton, noButton);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setPadding(new Insets(10));
    
    VBox vbox = new VBox(20, label, buttonBox);
    vbox.setAlignment(Pos.CENTER);
    vbox.setPadding(new Insets(20));
    
    Scene scene = new Scene(vbox);
    stage.setScene(scene);
    
    // Show and wait on JavaFX thread or platform runLater
    if (Platform.isFxApplicationThread()) {
      stage.showAndWait();
    } else {
      CompletableFuture<Void> future = new CompletableFuture<>();
      Platform.runLater(() -> {
        stage.showAndWait();
        future.complete(null);
      });
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public boolean ok() {
    return choice;
  }
}
