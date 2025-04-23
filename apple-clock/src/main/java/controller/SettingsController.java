package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Setter;
import model.Misc;
import repository.MiscRepositoryImpl;
import service.MiscService;
import service.WorkTypeService;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import util.LocalizationManager;

public class SettingsController {


    @FXML
    private TextField usernameField;

    @FXML
    private ComboBox<String> themeComboBox;

    @FXML
    private ComboBox<Locale> languageComboBox;

    @FXML
    private Button saveButton;
    @FXML
    private Label titleLabel;


    @FXML
    private Button manageWorkTypeButton;
    @Setter
    private WorkTypeService workTypeService;
    //    private WorkTypeService workTypeService = new WorkTypeService(new WorkTypeRepositoryImpl());
    private final MiscService miscService = new MiscService(new MiscRepositoryImpl());

    private static final Map<String, Integer> THEME_MAP = Map.of(
            "红苹果", 0,
            "绿苹果", 1
    );


    // 初始化方法（FXML加载后自动调用）
    @FXML
    public void initialize() {
        // 1) 绑定标题文本到资源文件，语言切换时会自动更新
        titleLabel.textProperty().bind(
                LocalizationManager.bindString("settings.title")
        );

        // 2) 初始化“主题”下拉（保持原样）
        themeComboBox.getItems().addAll("红苹果", "绿苹果");

        // 3) 初始化“语言”下拉——用 Locale 类型
        languageComboBox.getItems().setAll(Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE);
        // 使用 StringConverter 显示成“English”/“简体中文”
        languageComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Locale loc) {
                return loc.equals(Locale.SIMPLIFIED_CHINESE) ? "简体中文" : "English";
            }
            @Override
            public Locale fromString(String string) {
                return "简体中文".equals(string)
                        ? Locale.SIMPLIFIED_CHINESE
                        : Locale.ENGLISH;
            }
        });
        // 初始选中当前 LocalizationManager 里的 Locale
        languageComboBox.setValue(LocalizationManager.getLocale());
        // 监听用户切换语言 → 调用 LocalizationManager.setLocale(...)
        languageComboBox.valueProperty().addListener((obs, oldLoc, newLoc) -> {
            if (newLoc != null && !newLoc.equals(oldLoc)) {
                LocalizationManager.setLocale(newLoc);
            }
        });

        // 查询数据库有没有已有设置
        Misc misc = miscService.getMisc();

        if (misc != null) {
            // 读到设置，回显
            usernameField.setText(misc.getUsername());
            themeComboBox.getSelectionModel().select(misc.getThemeStyle() == 0 ? "红苹果" : "绿苹果");
            languageComboBox.getSelectionModel()
                    .select(misc.getLanguage() == 0
                            ? Locale.ENGLISH
                            : Locale.SIMPLIFIED_CHINESE);
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
        Locale selectedLang = languageComboBox.getSelectionModel().getSelectedItem();

        Misc misc = new Misc();
        misc.setUsername(username);
        misc.setThemeStyle(THEME_MAP.getOrDefault(selectedTheme, 0));    // 用 Map
        misc.setLanguage(Locale.SIMPLIFIED_CHINESE.equals(selectedLang) ? 1 : 0);

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/WorkTypeManageView.fxml"),
                    LocalizationManager.getBundle() );
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
