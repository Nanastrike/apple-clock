package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Setter;
import model.Misc;
import repository.MiscRepositoryImpl;
import repository.WorkTypeRepositoryImpl;
import service.MiscService;
import service.WorkTypeService;

import java.io.IOException;
import java.util.Map;

public class SettingsController {

    @FXML
    private Text titleLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private ComboBox<String> themeComboBox;

    @FXML
    private ComboBox<String> languageComboBox;

    @FXML
    private Button saveButton;


    @FXML
    private Button manageWorkTypeButton;
    @Setter
    private WorkTypeService workTypeService;
    //    private WorkTypeService workTypeService = new WorkTypeService(new WorkTypeRepositoryImpl());
    private final MiscService miscService = new MiscService(new MiscRepositoryImpl());

    private static final Map<String, Integer> LANGUAGE_MAP = Map.of(
            "English", 0,
            "简体中文", 1
    );

    private static final Map<String, Integer> THEME_MAP = Map.of(
            "红苹果", 0,
            "绿苹果", 1
    );


    // 初始化方法（FXML加载后自动调用）
    @FXML
    public void initialize() {
        // 初始化下拉框内容
        themeComboBox.getItems().addAll("红苹果", "绿苹果");
        languageComboBox.getItems().addAll("English", "简体中文");

        // 查询数据库有没有已有设置
        Misc misc = miscService.getMisc(); // 你要写一个 getMisc() 方法

        if (misc != null) {
            // 读到设置，回显
            usernameField.setText(misc.getUsername());
            themeComboBox.getSelectionModel().select(misc.getThemeStyle() == 0 ? "红苹果" : "绿苹果");
            languageComboBox.getSelectionModel().select(misc.getLanguage() == 0 ? "English" : "简体中文");
        } else {
            // 没有就用默认
            themeComboBox.getSelectionModel().selectFirst();
            languageComboBox.getSelectionModel().selectFirst();
        }
    }

    // 点击保存按钮时
    @FXML
    private void handleSaveSettings() {
        String username = usernameField.getText();
        String selectedTheme = themeComboBox.getSelectionModel().getSelectedItem();
        String selectedLanguage = languageComboBox.getSelectionModel().getSelectedItem();

        Misc misc = new Misc();
        misc.setUsername(username);
        misc.setThemeStyle(THEME_MAP.getOrDefault(selectedTheme, 0));    // 用 Map
        misc.setLanguage(LANGUAGE_MAP.getOrDefault(selectedLanguage, 0)); // 用 Map

        miscService.saveOrUpdateMisc(misc);
        showAlert("保存成功！");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleManageWorkTypes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/WorkTypeManageView.fxml"));
            Parent managePage = loader.load();

            EventManagementController controller = loader.getController();
            controller.setWorkTypeService(this.workTypeService);

            Stage stage = (Stage) manageWorkTypeButton.getScene().getWindow();
            Scene scene = new Scene(managePage, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
