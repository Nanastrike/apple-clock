package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Setter;
import model.Misc;
import repository.MiscRepositoryImpl;
import service.MiscService;
import service.WorkTypeService;
import util.I18nKey;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import util.BaseController;
import util.LocalizationManager;

public class SettingsController extends BaseController {


    @FXML
    private TextField usernameField;
    @FXML @I18nKey("settings.username")
    private Label usernameLabel;
    @FXML
    private ComboBox<String> themeComboBox;

    @FXML
    private ComboBox<Locale> languageComboBox;
    @FXML @I18nKey("settings.save")
    private Button saveButton;
    @FXML @I18nKey("settings.title")
    private Label titleLabel;
    @FXML @I18nKey("settings.theme")
    private Label themeLabel;
    @FXML @I18nKey("settings.manageWorkType")
    private Label manageWorkTypeLabel;
    @FXML @I18nKey("settings.language")
    private Label languageLabel;
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
    @Override
    protected void onInitialize(URL location, ResourceBundle resources) {        // 1) 绑定标题文本到资源文件，语言切换时会自动更新

        // 主题下拉
        List<String> themeKeys = List.of("theme.red", "theme.green");
        themeComboBox.getItems().setAll(themeKeys);
        themeComboBox.setConverter(new StringConverter<String>() {
                                       @Override
                                       public String toString(String key) {
                                           return LocalizationManager.getBundle().getString(key);
                                       }
                                       @Override
                                       public String fromString(String string) {
                                           // 选中回写时，按显示值反查 key
                                           return themeKeys.stream()
                                                   .filter(k -> LocalizationManager.getBundle().getString(k).equals(string))
                                                   .findFirst().orElse(themeKeys.get(0));
                                       }});

        // 语言下拉
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
            String savedKey = misc.getThemeStyle() == 1 ? "theme.green" : "theme.red";
            themeComboBox.getSelectionModel().select(savedKey);
            languageComboBox.getSelectionModel()
                    .select(misc.getLanguage() == 0
                            ? Locale.ENGLISH
                            : Locale.SIMPLIFIED_CHINESE);
        } else {
            // 没有就用默认
            String sysName = LocalizationManager.getBundle().getString("settings.usernameDefault");
            usernameField.setText(sysName);
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
        showAlert(LocalizationManager.getBundle().getString("settings.saved"));

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
