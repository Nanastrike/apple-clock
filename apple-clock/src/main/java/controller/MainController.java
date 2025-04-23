package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;
import model.WorkLogs;
import model.WorkType;

import service.WorkLogsService;
import service.WorkTypeService;
import util.LocalizationManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


/**
 * 控制器类，处理界面元素交互逻辑。
 */

public class MainController implements Initializable {

    // -------------- 绑定 FXML --------------
    @FXML private ImageView appleImage;
    @FXML private Label timerLabel;
    @FXML private Button startButton, pauseButton, stopButton;
    @FXML private ComboBox<String> workTypeComboBox;
    @FXML private AnchorPane timePickerPanel;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private Button settingButton, staticsButton;

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
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
    }

    private void setupUI() {
        try {
            InputStream appleStream = MainController.class.getResourceAsStream("/images/apple_red.png");
            if (appleStream != null) {
                appleImage.setImage(new Image(appleStream));
            } else {
                System.out.println("警告：apple_red.png 未找到！");
            }

            InputStream settingsStream = MainController.class.getResourceAsStream("/images/settings.png");
            if (settingsStream != null) {
                ImageView settingsIcon = new ImageView(new Image(settingsStream));
                settingsIcon.setFitWidth(24);
                settingsIcon.setFitHeight(24);
                settingButton.setGraphic(settingsIcon);
            } else {
                System.out.println("警告：settings.png 未找到！");
            }

            InputStream staticsStream = MainController.class.getResourceAsStream("/images/clocker.png");
            if (staticsStream != null) {
                ImageView staticsIcon = new ImageView(new Image(staticsStream));
                staticsIcon.setFitWidth(24);
                staticsIcon.setFitHeight(24);
                staticsButton.setGraphic(staticsIcon);
            } else {
                System.out.println("警告：statics.png 未找到！");
            }

        } catch (Exception e) {
            System.out.println("加载图片时出错：" + e.getMessage());
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
        remainingSeconds = 30 * 60;
        isPaused = false;
        updateTimerLabel();

        //调用后端停止记录
        if (currentLog != null) {
            workLogsService.stopLog(currentLog.getId());
            currentLog = null;
        }

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SettingView.fxml"), LocalizationManager.getBundle());
            Scene settingsScene = new Scene(loader.load());

            /* ★ 把同一个 Service 注入 Setting 控制器 */
            SettingsController ctrl = loader.getController();
            ctrl.setWorkTypeService(workTypeService);

            // 新建一个新的窗口（Stage）
            Stage settingsStage = new Stage();
            settingsStage.setTitle("设置");
            settingsStage.setScene(settingsScene);
            settingsStage.setResizable(false); // 可选：如果想让设置页固定大小

            /* ★ 监听窗口关闭，刷新下拉框 */
            settingsStage.setOnHidden(e -> refreshWorkTypeComboBox());
            settingsStage.show(); // 打开！

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("点击了设置按钮");
    }

    @FXML
    private void onStatisticsClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StatisticsView.fxml"));
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
