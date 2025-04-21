package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.WorkLogs;  // 假设你有WorkLogs模型
import model.WorkType;  // 假设你有WorkType模型
import service.WorkLogsService;
import service.WorkTypeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsController {

    @FXML
    private ComboBox<String> quickDateComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private VBox eventFilterBox;

    @FXML
    private StackPane contentStackPane;

    @FXML
    private VBox pieView;

    @FXML
    private VBox logView;

    @FXML
    private PieChart pieChart;

    @FXML
    private VBox logList;

    @FXML
    private Button toggleButton;

    private WorkLogsService workLogsService;
    private WorkTypeService workTypeService;

    private Map<Long, String> workTypeMap = new HashMap<>();
    private Map<Long, String> colorMap = new HashMap<>();

    private boolean showingPieChart = true;

    public void setServices(WorkLogsService workLogsService, WorkTypeService workTypeService) {
        this.workLogsService = workLogsService;
        this.workTypeService = workTypeService;
        init();
    }

    private void init() {
        // 初始化日期快捷选项
        quickDateComboBox.setItems(FXCollections.observableArrayList("Today", "Yesterday", "Last 7 Days", "Last 30 Days"));
        quickDateComboBox.getSelectionModel().selectFirst();
        quickDateComboBox.setOnAction(e -> applyQuickDateSelection());

        // 初始化日期选择器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        startDatePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
            }
        });
        endDatePicker.setConverter(startDatePicker.getConverter());

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());

        // 初始化颜色Map
        for (long i = 1; i <= 15; i++) {
            colorMap.put(i, randomColor());
        }

        // 初始化事件过滤器
        List<WorkType> allTypes = workTypeService.getAllTypes();
        for (WorkType type : allTypes) {
            CheckBox cb = new CheckBox(type.getName());
            cb.setUserData(type.getId());
            cb.setSelected(true);
            eventFilterBox.getChildren().add(cb);
            workTypeMap.put(type.getId(), type.getName());
        }

        toggleButton.setOnAction(e -> toggleView());

        refreshData();
    }

    private void applyQuickDateSelection() {
        String selected = quickDateComboBox.getValue();
        LocalDate today = LocalDate.now();

        switch (selected) {
            case "Today" -> {
                startDatePicker.setValue(today);
                endDatePicker.setValue(today);
            }
            case "Yesterday" -> {
                startDatePicker.setValue(today.minusDays(1));
                endDatePicker.setValue(today.minusDays(1));
            }
            case "Last 7 Days" -> {
                startDatePicker.setValue(today.minusDays(6));
                endDatePicker.setValue(today);
            }
            case "Last 30 Days" -> {
                startDatePicker.setValue(today.minusDays(29));
                endDatePicker.setValue(today);
            }
        }
        refreshData();
    }

    private void toggleView() {
        showingPieChart = !showingPieChart;
        pieView.setVisible(showingPieChart);
        logView.setVisible(!showingPieChart);
    }

    private void refreshData() {
        // 简化版，先用模拟数据
        List<WorkLogs> logs = workLogsService.getLogsBetween(startDatePicker.getValue(), endDatePicker.getValue());

        Set<Long> selectedTypes = eventFilterBox.getChildren().stream()
                .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                .map(node -> (Long) node.getUserData())
                .collect(Collectors.toSet());

        Map<Long, Integer> durationMap = new HashMap<>();

        for (WorkLogs log : logs) {
            if (selectedTypes.contains(log.getWorkTypeId())) {
                durationMap.put(log.getWorkTypeId(), durationMap.getOrDefault(log.getWorkTypeId(), 0) + log.getDuration());
            }
        }

        updatePieChart(durationMap);
        updateLogView(logs);
    }

    private void updatePieChart(Map<Long, Integer> data) {
        pieChart.getData().clear();
        for (Map.Entry<Long, Integer> entry : data.entrySet()) {
            PieChart.Data slice = new PieChart.Data(workTypeMap.getOrDefault(entry.getKey(), "未知"), entry.getValue());
            pieChart.getData().add(slice);

            slice.getNode().setStyle("-fx-pie-color: " + colorMap.getOrDefault(entry.getKey(), "#cccccc") + ";");
            Tooltip tooltip = new Tooltip(workTypeMap.getOrDefault(entry.getKey(), "") + "\n" + formatMinutes(entry.getValue()));
            Tooltip.install(slice.getNode(), tooltip);
        }
    }

    private void updateLogView(List<WorkLogs> logs) {
        logList.getChildren().clear();

        logs.sort(Comparator.comparing(WorkLogs::getBegin).reversed());

        LocalDate lastDate = null;
        for (WorkLogs log : logs) {
            LocalDate logDate = log.getBegin().toLocalDate();
            if (!logDate.equals(lastDate)) {
                Label dateLabel = new Label(logDate.toString());
                dateLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
                logList.getChildren().add(dateLabel);
                lastDate = logDate;
            }

            Label timeLabel = new Label(log.getBegin().toLocalTime() + " - " + workTypeMap.getOrDefault(log.getWorkTypeId(), "未知") + " (" + formatMinutes(log.getDuration()) + ")");
            logList.getChildren().add(timeLabel);
        }
    }

    private String formatMinutes(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0) {
            return h + "h " + m + "m";
        } else {
            return m + "m";
        }
    }

    private String randomColor() {
        Random random = new Random();
        return String.format("#%06x", random.nextInt(0xFFFFFF));
    }

    @FXML
    private void onDatePickerChanged() {
        refreshData();
    }

    @FXML
    private void onEventFilterChanged() {
        refreshData();
    }
}