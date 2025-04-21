package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.WorkLogs;
import model.WorkType;
import repository.WorkLogsRepositoryImpl;
import repository.WorkTypeRepository;
import repository.WorkTypeRepositoryImpl;
import service.WorkLogsService;
import service.WorkTypeService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计页面控制器
 */
public class StatisticsController {

    /* ---------- FXML 节点 ---------- */
    @FXML private Button selectDateRangeButton;
    @FXML private VBox   dateRangePanel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private PieChart pieChart;
    @FXML private VBox recordsContainer;
    @FXML private ScrollPane recordsScrollPane;

    @FXML private ScrollPane logScrollPane;
    @FXML private VBox logListContainer;

    @FXML private Button toggleViewButton;

    /* ---------- 业务数据 ---------- */
    /** 所有事件名列表（由服务层提供） */
    private List<String> workTypeNames;

    /** 用户已选事件类型，默认全部 */
    private final List<String> selectedWorkTypes = new ArrayList<>();
    WorkTypeService workTypeService = new WorkTypeService(new WorkTypeRepositoryImpl());
    WorkLogsService workLogsService = new WorkLogsService(new WorkLogsRepositoryImpl(),new WorkTypeRepositoryImpl());

    /** 15 个预设颜色循环使用 */
    private final String[] pieColors = {
            "#4E79A7","#F28E2B","#E15759","#76B7B2","#59A14F",
            "#EDC948","#B07AA1","#FF9DA7","#9C755F","#BAB0AC",
            "#6A4C93","#D33F49","#FF8C42","#A1C181","#50514F"
    };

    /* ---------- 初始化 ---------- */
    @FXML
    private void initialize() {
        // 拿到所有事件名
        workTypeNames = workTypeService.getAllWorkTypeNames();
        selectedWorkTypes.addAll(workTypeNames);
        refreshPieChart();

        // 默认显示过去 7 天
        onLast7DaysClicked();
        updateDateButtonText();
        refreshPieChart();
    }

    /* ---------- 日期面板 ---------- */
    @FXML private void onDateRangeButtonClicked() { dateRangePanel.setVisible(true); }

    @FXML private void onTodayClicked() {
        LocalDate d = LocalDate.now();
        startDatePicker.setValue(d);
        endDatePicker.setValue(d);
    }
    @FXML private void onYesterdayClicked() {
        LocalDate d = LocalDate.now().minusDays(1);
        startDatePicker.setValue(d);
        endDatePicker.setValue(d);
    }
    @FXML private void onLast7DaysClicked() {
        LocalDate d = LocalDate.now();
        startDatePicker.setValue(d.minusDays(6));
        endDatePicker.setValue(d);
    }
    @FXML private void onLast30DaysClicked() {
        LocalDate d = LocalDate.now();
        startDatePicker.setValue(d.minusDays(29));
        endDatePicker.setValue(d);
    }

    @FXML private void onConfirmDateRange() {
        LocalDate s = startDatePicker.getValue();
        LocalDate e = endDatePicker.getValue();
        if (s == null || e == null || s.isAfter(e)) {
            showAlert("请选择正确的时间范围！");
            return;
        }
        updateDateButtonText();
        dateRangePanel.setVisible(false);
        refreshPieChart();
    }

    private void updateDateButtonText() {
        selectDateRangeButton.setText(
                startDatePicker.getValue() + " 到 " + endDatePicker.getValue()
        );
    }

    /* ---------- 事件筛选 ---------- */
    @FXML private void openWorkTypeSelection() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("选择要展示的事件");
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        ListView<String> listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setItems(FXCollections.observableArrayList(workTypeNames));
        // 保留之前的选择
        selectedWorkTypes.forEach(t -> listView.getSelectionModel().select(t));

        dialog.getDialogPane().setContent(listView);
        dialog.setResultConverter(btn -> btn == ButtonType.OK
                ? new ArrayList<>(listView.getSelectionModel().getSelectedItems())
                : null);

        dialog.showAndWait().ifPresent(sel -> {
            selectedWorkTypes.clear();
            selectedWorkTypes.addAll(sel.isEmpty() ? workTypeNames : sel);
            refreshPieChart();
        });
    }

    /* ---------- 视图切换 ---------- */
    @FXML private void handleToggleView() {
        boolean showLogs = !logScrollPane.isVisible();
        logScrollPane.setVisible(showLogs);
        recordsScrollPane.setVisible(!showLogs);
        pieChart.setVisible(!showLogs);
    }

    /* ---------- 饼图 & 历史记录刷新 ---------- */
    // 1. 获取用户选择的时间范围
    // 2. 获取用户选择的事件类型
    // 3. 从数据库查询对应的 WorkLogs
    // 4. 生成数据集
    // 5. 设置到 PieChart 控件上
    private void refreshPieChart() {
        // 查询选定日期范围 + 事件类型的日志
        LocalDate s = startDatePicker.getValue();
        LocalDate e = endDatePicker.getValue();

        if (s == null || e == null) {
            onLast7DaysClicked();
        }

        if (s.isAfter(e)) {
            showAlert("开始日期不能晚于结束日期！");
            return;
        }
        // ★ 把名称转换成实体
        List<WorkType> typeEntities =
                workTypeService.findByNames(selectedWorkTypes);

        // 如果一个都没选，直接清空图表
        if (typeEntities.isEmpty()) {
            pieChart.getData().clear();
            recordsContainer.getChildren().clear();
            logListContainer.getChildren().clear();
            return;
        }
        List<WorkLogs> logs = workLogsService   // ← 你实例化的 service
                .findByDateRangeAndWorkNames(s, e, selectedWorkTypes);

        /* --- 汇总每种类型的总时长 --- */
        Map<String, Integer> totalMap = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getWorkType().getName(),         // 分组键：事件名称
                        Collectors.reducing(
                                0,                                  // 初始值
                                WorkLogs::getDuration,              // 取出每条记录的 duration
                                Integer::sum                        // 累加
                        )));

        /* --- 更新饼图 --- */
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        int colorIdx = 0;
        pieChart.getData().clear();
        for (Map.Entry<String, Integer> entry : totalMap.entrySet()) {
            String type    = entry.getKey();
            int minutes    = entry.getValue();          // 直接拿到分钟
            PieChart.Data data = new PieChart.Data(type, minutes);
            pieData.add(data);


            // 颜色
            String color = pieColors[colorIdx++ % pieColors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");

            // 悬浮提示
            Tooltip.install(data.getNode(),
                    new Tooltip(type + " : " + formatDuration(minutes)));
        }
        pieChart.setData(pieData);

        /* --- 更新饼图下方累计记录列表 --- */
        recordsContainer.getChildren().clear();
        // totalMap 是 Map<String, Integer>
        totalMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(en -> {
                    String type  = en.getKey();
                    int minutes  = en.getValue();
                    Label lbl = new Label(type + " — " + formatDuration(minutes));
                    recordsContainer.getChildren().add(lbl);
                });


        /* --- 如果当前在日志视图，也顺便刷新日志列表 --- */
        if (logScrollPane.isVisible()) {
            refreshLogList(logs);
        }
    }

    private void refreshLogList(List<WorkLogs> logs) {
        // logs 已经按默认顺序；我们想按日期—时间倒序
        logs.sort(Comparator.comparing(WorkLogs::getBegin).reversed());

        logListContainer.getChildren().clear();
        LocalDate currentDate = null;

        for (WorkLogs wl : logs) {
            if (!Objects.equals(currentDate, wl.getBegin().toLocalDate())) {
                currentDate = wl.getBegin().toLocalDate();
                Label dateLabel = new Label(currentDate.toString());
                dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                logListContainer.getChildren().add(dateLabel);
            }
            String item = String.format(
                    "  %s | %s | %s",
                    wl.getBegin().toLocalTime().withSecond(0).withNano(0),
                    wl.getWorkType().getName(),
                    formatDuration(wl.getDuration())
            );
            logListContainer.getChildren().add(new Label(item));
        }
    }

    /* ---------- 工具函数 ---------- */
    private static String formatDuration(int minutes) {
        long h = minutes / 60;
        long m = minutes % 60;
        return (h > 0 ? h + "h" : "") + (m > 0 ? m + "m" : (h > 0 ? "" : "0m"));
    }

    private static void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
