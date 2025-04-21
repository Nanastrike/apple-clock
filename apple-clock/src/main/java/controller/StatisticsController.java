package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.WorkLogs;
import model.WorkType;
import service.WorkLogsService;
import service.WorkTypeService;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsController {

    @FXML
    private AnchorPane calendarPopup;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label dateRangeLabel;

    @FXML
    private Button selectWorkTypeButton;
    @FXML
    private VBox workTypeSelectionPanel;
    @FXML
    private ListView<String> workTypeListView;

    // 保存用户选中的事件类型
    private final List<String> selectedWorkTypes = new ArrayList<>();

    @FXML
    private void initialize() {
        /*

         */
    }



    // 点击"选择时间范围"按钮，弹出日历面板
    @FXML
    private void onDateRangeButtonClicked() {
        calendarPopup.setVisible(true);
    }

    // 快捷按钮 - 今天
    @FXML
    private void onTodayClicked() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today);
        endDatePicker.setValue(today);
    }

    // 快捷按钮 - 昨天
    @FXML
    private void onYesterdayClicked() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        startDatePicker.setValue(yesterday);
        endDatePicker.setValue(yesterday);
    }

    // 快捷按钮 - 过去7天
    @FXML
    private void onLast7DaysClicked() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.minusDays(6));
        endDatePicker.setValue(today);
    }

    // 快捷按钮 - 过去30天
    @FXML
    private void onLast30DaysClicked() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.minusDays(29));
        endDatePicker.setValue(today);
    }

    // 点击"确定"，应用选择的时间
    @FXML
    private void onConfirmDateRange() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start != null && end != null && !start.isAfter(end)) {
            dateRangeLabel.setText(start.toString() + " 到 " + end.toString());
            calendarPopup.setVisible(false);
        } else {
            showAlert("请选择正确的时间范围！");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 打开选择事件面板
     */
    @FXML
    private void openWorkTypeSelection() {
        // 创建一个对话框
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("选择要展示的事件");

        // 添加按钮
        ButtonType okButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // 创建多选框
        ListView<String> listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setItems(FXCollections.observableArrayList(workTypeNames)); // 这里是事件名列表
        listView.getSelectionModel().selectAll(); // 默认全部选中

        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new ArrayList<>(listView.getSelectionModel().getSelectedItems());
            }
            return null;
        });

        // 显示并等待
        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            selectedWorkTypes.clear();
            selectedWorkTypes.addAll(selected);
            updateChart();
        });
    }
    /**
     * 点击确认按钮
     */
    @FXML
    private void onConfirmWorkType() {
        selectedWorkTypes.clear();
        selectedWorkTypes.addAll(workTypeListView.getSelectionModel().getSelectedItems());

        // 这里可以加其他逻辑，比如根据选中的事件刷新饼图或列表
        System.out.println("用户选择了这些事件：" + selectedWorkTypes);

        workTypeSelectionPanel.setVisible(false);
        // 重新刷新饼图
        refreshPieChart();
    }

    /**
     * 点击取消按钮
     */
    @FXML
    private void onCancelWorkType() {
        workTypeSelectionPanel.setVisible(false);
    }

    @FXML
    private void handleToggleView(){
        /*点击后，整体UI完全切换为另一种样式
        类似于log的造型
        按 日期分组（比如 2025/04/20、2025/04/19）
每个日期下有若干条事件
每条事件显示：
时间（比如 13:00）
事件名称（比如 学习）
持续时长（比如 2h15m）
过去7天内的所有事件
按时间排序（最新的在上面）
set(LogsView).visible = true;
         */
    }

    @FXML
    private void refreshPieChart(){
        /*根据选择的时间+事件类型读取数据库
        生成一个pie图
        鼠标悬浮在pie的分块上的时候，出现提示事件的名字和时间
        pie图的颜色划分读取颜色map，需要先预设15种颜色
        并且在饼图下方展示饼图中的历史记录，包括事件类型，累计时间
        * */
    }







}