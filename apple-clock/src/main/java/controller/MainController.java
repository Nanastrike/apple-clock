package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Setter;
import model.WorkLogs;
import model.WorkType;
import util.BaseController;
import service.WorkLogsService;
import service.WorkTypeService;
import util.LocalizationManager;
import util.I18nKey;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


/**
 * 控制器类，处理界面元素交互逻辑。
 */

public class MainController extends BaseController implements Initializable {

    // -------------- 绑定 FXML --------------
    @FXML private ImageView appleImage;
    @FXML private Label timerLabel;
    @FXML @I18nKey("button.start")
    private Button startButton;
    @FXML @I18nKey("button.pause")
    private Button pauseButton;
    @FXML @I18nKey("button.stop")
    private Button stopButton;
    @FXML @I18nKey("main.currentType")
    private Label eventLabel;
    @FXML private ComboBox<String> workTypeComboBox;
    @FXML private AnchorPane timePickerPanel;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML
    private Button settingButton;
    @FXML
    private Button staticsButton;
    @FXML @I18nKey("main.chooseTimer")
    private Label chooseTimerLabel;
    @FXML @I18nKey("button.confirm")
    private Button confirmButton;
    @FXML @I18nKey("button.cancel")
    private Button cancelButton;
    @FXML private BorderPane rootPane;

    // -------------- 内部变量 --------------
    private Timeline timeline;
    private int remainingSeconds = 30 * 60;
    private boolean isPaused = false;

    // 提供给外部设置 Service
    @Setter
    private WorkTypeService workTypeService;
    @Setter
    private WorkLogsService workLogsService;
    private WorkLogs currentLog; // 当前正在进行的计时


    /**
     * 初始化方法，在界面加载后自动调用。
     */

    @Override
    public void onInitialize(URL location, ResourceBundle resources) {
        // --- original setupUI logic, without any setText(...) calls ---
        setupUI();
        updateTimerLabel();
        // 这时给苹果图绑定点击逻辑
        appleImage.setOnMouseClicked(evt -> onAppleClick());
        // 让 Spinner 可编辑，用户既能点按钮也能手动输
        minuteSpinner.setEditable(true);
        minuteSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 240, 30)
        );
        initData(); // load work types
        Platform.runLater(() -> rootPane.requestFocus()); //不设置默认focus
    }

    private void setupUI() {
        try {
            InputStream appleStream = MainController.class.getResourceAsStream("/images/apple_red.png");
            if (appleStream != null) {
                appleImage.setImage(new Image(appleStream));
            }

            InputStream settingsStream = MainController.class.getResourceAsStream("/images/settings.png");
            if (settingsStream != null) {
                ImageView settingsIcon = new ImageView(new Image(settingsStream));
                settingsIcon.setFitWidth(24);
                settingsIcon.setFitHeight(24);
                settingButton.setGraphic(settingsIcon);
            }

            InputStream staticsStream = MainController.class.getResourceAsStream("/images/clocker.png");
            if (staticsStream != null) {
                ImageView staticsIcon = new ImageView(new Image(staticsStream));
                staticsIcon.setFitWidth(24);
                staticsIcon.setFitHeight(24);
                staticsButton.setGraphic(staticsIcon);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 初始化倒计时显示
        updateTimerLabel();
        minuteSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 240, 30)); // 1~240 分钟，默认 30
    }


    // -------------- 外部手动调用，加载后端数据 --------------
    public void initData() {
        if (workTypeService == null) {
            System.out.println("WorkTypeService未注入，无法初始化！");
            return;
        }

        workTypeService.initializeDefaultWorkTypes();

        List<WorkType> types = workTypeService.getAllTypes();
        workTypeComboBox.getItems().clear();
        for (WorkType wt : types) {
            workTypeComboBox.getItems().add(wt.getName());
        }
        if (!workTypeComboBox.getItems().isEmpty()) {
            workTypeComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * 点击开始按钮时触发。
     */

    @FXML
    public void handleStart() {

        // ⓐ 已在计时但「暂停」状态 → 继续播放
        if (timeline != null && isPaused) {
            timeline.play();
            isPaused = false;
            startButton.setDisable(true);
            pauseButton.setDisable(false);
            return;
        }

        // ⓑ 第一次开始或上一次 Stop 后
        if (timeline == null) {
            // 读取下拉框事件类型
            String selectedTypeName = workTypeComboBox.getSelectionModel().getSelectedItem();
            if (selectedTypeName == null) {
                System.out.println("请选择一个事件类型！");
                return;
            }

            // 写数据库：新建 WorkLogs
            WorkType selectedType = workTypeService.findByName(selectedTypeName);
            if (selectedType == null) {
                System.out.println("找不到对应的事件类型！");
                return;
            }
            currentLog = workLogsService.startLog(selectedType.getId());

            // 启动倒计时
            startTimer();
            // ★ 启动后：禁用苹果图和下拉框
            appleImage.setDisable(true);
            workTypeComboBox.setDisable(true);

            // UI 按钮状态
            startButton.setDisable(true);
            pauseButton.setDisable(false);
            stopButton.setDisable(false);
        }
    }



    /**
     * 点击暂停按钮时触发。
     */
    @FXML
    public void handlePause() {
        if (timeline != null) {
            timeline.pause();
            isPaused = true;
        }
        startButton.setDisable(false);
    }

    /**
     * 点击停止按钮时触发。
     */
    @FXML
    public void handleStop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }

        if (currentLog != null) {
            workLogsService.stopLog(currentLog.getId());
            currentLog = null;
        }
        remainingSeconds = 30 * 60;
        isPaused = false;
        updateTimerLabel();

        // ★ 停止后：恢复苹果图和下拉框
        appleImage.setDisable(false);
        workTypeComboBox.setDisable(false);

        startButton.setDisable(false);
        pauseButton.setDisable(true);
        stopButton.setDisable(true);
    }

    /**
     * 启动倒计时。
     */
    private void startTimer() {
        timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerLabel();

            if (remainingSeconds <= 0) {
                // 自动结束并写库
                handleStop();          // ← ★ 直接复用
                System.out.println("计时结束并已保存记录！");
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        isPaused = false;
    }


    /**
     * 更新时间显示文字。
     */
    private void updateTimerLabel() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    @FXML
    private void onSettingsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/MainView.fxml")
            ); // Note: MainView does not need bundle here
            FXMLLoader settingsLoader = new FXMLLoader(
                    getClass().getResource("/view/SettingView.fxml"),
                    LocalizationManager.getBundle()      // ★ pass bundle
            );
            Parent settingsPage = settingsLoader.load();
            SettingsController ctrl = settingsLoader.getController();
            ctrl.setWorkTypeService(workTypeService);
            Stage settingsStage = new Stage();
            settingsStage.setTitle(LocalizationManager.getBundle().getString("settings.title")); // ★ i18n title
            settingsStage.setScene(new Scene(settingsPage));
            settingsStage.setResizable(false);
            settingsStage.setOnHidden(e -> initData());
            settingsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("点击了设置按钮");
    }

    @FXML
    private void onStatisticsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StatisticsView.fxml"),LocalizationManager.getBundle());
            Parent statisticsPage = loader.load();

            Stage stage = new Stage();
            stage.setTitle("统计");
            stage.setScene(new Scene(statisticsPage));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("点击了统计按钮");
    }

    //点击苹果图片后出现弹窗
    @FXML
    private void onAppleClick() {
        // ★ 如果倒计时还在（未调用 handleStop），直接返回，不弹面板
        if (timeline != null) return;
        timePickerPanel.setVisible(true); // 显示选择时间面板
    }

    //选择即将开始的倒计时
    @FXML
    private void handleConfirmTime() {
        int minutes = minuteSpinner.getValue();
        remainingSeconds = minutes * 60; // 更新倒计时
        updateTimerLabel();
        timePickerPanel.setVisible(false); // 隐藏选择面板
    }

    @FXML
    private void handleCancelTime(){
        timePickerPanel.setVisible(false); // 隐藏选择面板
    }

    /** 重新从数据库加载事件类型列表，并保持当前选中项 */
    private void refreshWorkTypeComboBox() {
        List<WorkType> types = workTypeService.getAllTypes();

        String current = workTypeComboBox.getSelectionModel().getSelectedItem();
        workTypeComboBox.getItems().setAll(
                types.stream().map(WorkType::getName).toList()
        );

        // 如果原来的选项还在，保持选中；否则选第一个
        if (current != null && workTypeComboBox.getItems().contains(current)) {
            workTypeComboBox.getSelectionModel().select(current);
        } else if (!workTypeComboBox.getItems().isEmpty()) {
            workTypeComboBox.getSelectionModel().selectFirst();
        }
    }
}
