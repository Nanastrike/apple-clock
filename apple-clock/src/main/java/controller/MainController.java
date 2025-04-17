package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;


/**
 * 控制器类，处理界面元素交互逻辑。
 */
public class MainController {

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

    private Timeline timeline; // JavaFX自带定时器
    private int remainingSeconds = 30 * 60; // 默认30分钟（单位：秒）
    private boolean isPaused = false;

    /**
     * 初始化方法，在界面加载后自动调用。
     */
    @FXML
    public void initialize() {
        // 加载红苹果图片作为默认表盘
        appleImage.setImage(new Image(getClass().getResourceAsStream("/images/apple_red.png")));
        updateTimerLabel();
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
}
