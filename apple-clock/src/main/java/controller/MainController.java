package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import model.WorkType;
import repository.WorkTypeRepository;
import service.WorkTypeService;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


/**
 * 控制器类，处理界面元素交互逻辑。
 */
public class MainController implements Initializable {

    @FXML
    private ImageView appleImage;

    @FXML
    private Label timerLabel;

    @FXML
    private Button startButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private AnchorPane timePickerPanel;

    @FXML
    private Spinner<Integer> minuteSpinner;

    @FXML
    private ComboBox<String> workTypeComboBox;

    private Timeline timeline; // JavaFX自带定时器
    private int remainingSeconds = 30 * 60; // 默认30分钟（单位：秒）
    private boolean isPaused = false;

    private WorkTypeService workTypeService;

    public void setWorkTypeService(WorkTypeService workTypeService) {
        this.workTypeService = workTypeService;
    }

    /**
     * 初始化方法，在界面加载后自动调用。
     */
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化默认事件
        workTypeService.initializeDefaultWorkTypes();
        // 加载红苹果图片作为默认表盘
        InputStream imageStream = getClass().getResourceAsStream("/images/apple_red.png");
        if (imageStream != null) {
            appleImage.setImage(new Image(imageStream));
        } else {
            System.out.println("苹果图片找不到！");
        }
        updateTimerLabel();
        // 加载事件到下拉框
        List<WorkType> workTypes = workTypeService.getAllTypes();
        for (WorkType wt : workTypes) {
            workTypeComboBox.getItems().add(wt.getName());
        }

        // 默认选中第一个（如果有的话）
        if (!workTypeComboBox.getItems().isEmpty()) {
            workTypeComboBox.getSelectionModel().select(0);
        }
    }

    /**
     * 点击开始按钮时触发。
     */
    @FXML
    public void handleStart() {
        if (timeline == null || isPaused) {
            startTimer();
        }
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        stopButton.setDisable(false);
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
        remainingSeconds = 30 * 60; // 重置倒计时
        isPaused = false;
        updateTimerLabel();

        startButton.setDisable(false);
        pauseButton.setDisable(true);
        stopButton.setDisable(true);
    }

    /**
     * 启动倒计时。
     */
    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerLabel();
            if (remainingSeconds <= 0) {
                timeline.stop();
                System.out.println("计时结束！");
                // 这里将来可以加播放音效、弹窗提示等
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
        // 点击设置按钮后的逻辑，比如弹一个提示框
        System.out.println("点击了设置按钮");
    }

    @FXML
    private void onStatisticsClick() {
        // 点击统计按钮后的逻辑
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

}
