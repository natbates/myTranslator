package org.example.myTranslator;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Translator extends Application {

    private ComboBox<Language> sourceLangComboBox;
    private ComboBox<Language> targetLangComboBox;
    private TextArea inputTextArea;
    private TextArea outputTextArea;

    private static final Map<String, String> languageMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {

        String iconFilePath = "src/css/icon.jpg";
        Image icon = loadImage(iconFilePath);

        if (icon.isError()) {
            System.err.println("Failed to load icon!");
            return;
        }
        primaryStage.getIcons().add(icon);

        sourceLangComboBox = new ComboBox<>();
        targetLangComboBox = new ComboBox<>();
        inputTextArea = new TextArea();
        outputTextArea = new TextArea();
        Button translateButton = new Button("Translate");
        translateButton.getStyleClass().add("button-translate");
        Button swapButton = new Button("Swap");
        swapButton.getStyleClass().add("button-swap");
        Button clearButton = new Button("Clear");
        clearButton.getStyleClass().add("button-clear");
        Button translateFileButton = new Button("Translate File");
        translateFileButton.getStyleClass().add("button-translate-file");


        setupLanguageMap();

        sourceLangComboBox.getItems().addAll(Language.values());
        targetLangComboBox.getItems().addAll(Language.values());
        sourceLangComboBox.setValue(Language.ENGLISH);
        targetLangComboBox.setValue(Language.FRENCH);

        inputTextArea.setPrefHeight(100);
        outputTextArea.setPrefHeight(100);
        inputTextArea.setWrapText(true);
        outputTextArea.setWrapText(true);

        // Set prompt text for inputTextArea
        inputTextArea.setPromptText("Type " + sourceLangComboBox.getValue().toString() + " here");
        outputTextArea.setPromptText(targetLangComboBox.getValue().toString() + " goes here");

        outputTextArea.setEditable(false);
        translateButton.setOnAction(event -> translateText());
        swapButton.setOnAction(event -> swapLanguages());
        clearButton.setOnAction(event -> clearTextFields());
        translateFileButton.setOnAction(event -> translateFile(primaryStage));

        // Label for 'to'
        Label toLabel = new Label("to");
        toLabel.getStyleClass().add("to-label");

        HBox langBox = new HBox(10);
        langBox.getChildren().addAll(sourceLangComboBox, toLabel, targetLangComboBox);

        VBox textFieldsBox = new VBox(10);
        textFieldsBox.getChildren().addAll(inputTextArea, outputTextArea);

        HBox buttonsBox = new HBox(10);
        buttonsBox.getChildren().addAll(translateButton, swapButton, clearButton, translateFileButton);
        buttonsBox.setAlignment(Pos.BOTTOM_RIGHT); // Align buttons to the bottom right

        // Adjust root layout
        VBox root = new VBox(10);
        root.getChildren().addAll(langBox, textFieldsBox, buttonsBox);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root"); // Add root style class

        // Set minimum width and height for the scene
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setMinWidth(380);
        primaryStage.setMinHeight(300);

        setCssFile(scene);

        // Add listeners to adjust text fields size
        inputTextArea.prefHeightProperty().bind(scene.heightProperty().divide(2));
        outputTextArea.prefHeightProperty().bind(scene.heightProperty().divide(2));

        VBox.setVgrow(textFieldsBox, Priority.ALWAYS); // Make text fields take all available space

        // Add listeners to update prompt text when language selection changes
        sourceLangComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Language>() {
            @Override
            public void changed(ObservableValue<? extends Language> observable, Language oldValue, Language newValue) {
                inputTextArea.setPromptText("Type " + newValue.toString() + " here");
            }
        });

        targetLangComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Language>() {
            @Override
            public void changed(ObservableValue<? extends Language> observable, Language oldValue, Language newValue) {
                outputTextArea.setPromptText(newValue.toString() + " goes here");
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Language Translator");
        primaryStage.show();
    }

    private Image loadImage(String filePath) {
        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            return new Image(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setCssFile(Scene scene) {
        File cssFile = new File("src/css/styles.css");
        if (cssFile.exists()) {
            String cssUrl = cssFile.toURI().toString();
            scene.getStylesheets().add(cssUrl);
        } else {
            System.err.println("CSS file not found");
        }
    }

    private void translateText() {
        try {
            String inputText = inputTextArea.getText();
            String sourceLang = sourceLangComboBox.getValue().getCode();
            String targetLang = targetLangComboBox.getValue().getCode();
            String translatedText = translateText(inputText, sourceLang, targetLang);
            outputTextArea.setText(translatedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void translateFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Text File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            try {
                StringBuilder fileContent = new StringBuilder();
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
                reader.close();

                // Set the content of the selected file to the input text area
                inputTextArea.setText(fileContent.toString());

                // Translate the text read from the file
                String translatedText = translateText(fileContent.toString(),
                        sourceLangComboBox.getValue().getCode(),
                        targetLangComboBox.getValue().getCode());

                // Display translated text in outputTextArea
                outputTextArea.setText(translatedText);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle file reading errors
            } catch (Exception e) {
                e.printStackTrace();
                // Handle translation errors
            }
        }
    }

    private void swapLanguages() {
        // Swap selected languages
        Language tempLang = sourceLangComboBox.getValue();
        sourceLangComboBox.setValue(targetLangComboBox.getValue());
        targetLangComboBox.setValue(tempLang);

        // Swap text contents
        String tempText = inputTextArea.getText();
        inputTextArea.setText(outputTextArea.getText());
        outputTextArea.setText(tempText);

        // Update prompt text of inputTextArea based on the new selected language
        inputTextArea.setPromptText("Type " + sourceLangComboBox.getValue().toString() + " here");
        outputTextArea.setPromptText(targetLangComboBox.getValue().toString() + " goes here");
    }

    private void clearTextFields() {
        inputTextArea.clear();
        outputTextArea.clear();
    }

    private void setupLanguageMap() {
        languageMap.put(Language.ENGLISH.getCode(), "English");
        languageMap.put(Language.FRENCH.getCode(), "French");
        languageMap.put(Language.SPANISH.getCode(), "Spanish");
        languageMap.put(Language.MANDARIN_CHINESE.getCode(), "Mandarin Chinese");
        languageMap.put(Language.HINDI.getCode(), "Hindi");
        languageMap.put(Language.ARABIC.getCode(), "Arabic");
        languageMap.put(Language.BENGALI.getCode(), "Bengali");
        languageMap.put(Language.PORTUGUESE.getCode(), "Portuguese");
        languageMap.put(Language.RUSSIAN.getCode(), "Russian");
        languageMap.put(Language.JAPANESE.getCode(), "Japanese");
        languageMap.put(Language.PUNJABI.getCode(), "Punjabi");
        languageMap.put(Language.GERMAN.getCode(), "German");
        languageMap.put(Language.TELUGU.getCode(), "Telugu");
        // Add more languages as needed
    }

    private String translateText(String textToTranslate, String sourceLang, String targetLang) {
        try {
            String url = "http://mymemory.translated.net/api/get?q=" +
                    URLEncoder.encode(textToTranslate, StandardCharsets.UTF_8) +
                    "&langpair=" + sourceLang + "|" + targetLang;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject responseData = jsonResponse.getJSONObject("responseData");
            String translatedText = responseData.getString("translatedText");

            System.out.println("Translated text: " + translatedText);

            return translatedText;
        } catch (Exception e) {
            System.out.println("Failed to translate. Error: " + e.getMessage());
            return "Failed to Translate!";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private enum Language {
        ENGLISH("en"),
        FRENCH("fr"),
        SPANISH("es"),
        MANDARIN_CHINESE("zh"),
        HINDI("hi"),
        ARABIC("ar"),
        BENGALI("bn"),
        PORTUGUESE("pt"),
        RUSSIAN("ru"),
        JAPANESE("ja"),
        PUNJABI("pa"),
        GERMAN("de"),
        TELUGU("te");
        // Add more languages as needed

        private final String code;

        Language(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return languageMap.getOrDefault(code, "");
        }
    }
}
