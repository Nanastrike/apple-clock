package controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.WorkLogs;
import model.WorkType;
import org.controlsfx.control.CheckListView;
import repository.WorkLogsRepositoryImpl;
import repository.WorkTypeRepositoryImpl;
import service.WorkLogsService;
import service.WorkTypeService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsController {

    /* ---------- FXML ---------- */
    @FXML private Button  selectDateRangeButton;
    @FXML private VBox    dateRangePanel;
    @FXML private DatePicker startDatePicker, endDatePicker;

    @FXML private PieChart pieChart;
    @FXML private VBox     recordsContainer;
    @FXML private ScrollPane recordsScrollPane,
            logScrollPane;
    @FXML private VBox logListContainer;
    @FXML private Label noDataLabel;
    @FXML private SplitPane splitPane;

    /* ---------- Service ---------- */
    private final WorkTypeService workTypeService =
            new WorkTypeService(new WorkTypeRepositoryImpl());
    private final WorkLogsService workLogsService =
            new WorkLogsService(new WorkLogsRepositoryImpl(),
                    new WorkTypeRepositoryImpl());

    /* ---------- 运行时数据 ---------- */
    private List<String>   workTypeNames;
    private final List<String> selectedWorkTypes = new ArrayList<>();

    /* ---------- 初始化 ---------- */
    @FXML
    private void initialize() {

        workTypeNames = workTypeService.getAllWorkTypeNames();
        selectedWorkTypes.addAll(workTypeNames);

        onLast7DaysClicked();     // 默认时间
        refreshPieChart();        // 首次刷新

        // —— 新增：等 Scene 加载完成后，监听宽高比，动态 setOrientation ——
        splitPane.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) return;
            ChangeListener<Number> sizeListener = (o, oldV, newV) -> {
                if (scene.getWidth() > scene.getHeight()) {
                    splitPane.setOrientation(Orientation.HORIZONTAL);
                } else {
                    splitPane.setOrientation(Orientation.VERTICAL);
                }
            };
            scene.widthProperty().addListener(sizeListener);
            scene.heightProperty().addListener(sizeListener);
            // 触发一次，初始化 orientation
            sizeListener.changed(null, null, null);
        });
    }

    /* ========== 日期快捷按钮 ========== */
    @FXML private void onDateRangeButtonClicked() {  dateRangePanel.setVisible(!dateRangePanel.isVisible()); }
    @FXML private void onTodayClicked()      { setRange(0); }
    @FXML private void onYesterdayClicked()  { setRange(-1); }
    @FXML private void onLast7DaysClicked()  { setRange(-6); }
    @FXML private void onLast30DaysClicked() { setRange(-29); }

    private void setRange(int offsetDays) {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(offsetDays < 0 ? today.plusDays(offsetDays) : today);
        endDatePicker.setValue(today);
        onConfirmDateRange();
    }

    /* ========== 日期面板“确定” ========== */
    @FXML private void onConfirmDateRange() {

        LocalDate s = startDatePicker.getValue();
        LocalDate e = endDatePicker.getValue();
        if (s == null || e == null || s.isAfter(e)) {
            showAlert("请选择正确的时间范围！");
            return;
        }
        selectDateRangeButton.setText(s + " 到 " + e);
        dateRangePanel.setVisible(false);

        refreshPieChart();
    }

    /* ========== 事件筛选对话框 ========== */
    @FXML private void openWorkTypeSelection() {

        Dialog<List<String>> dlg = new Dialog<>();
        dlg.setTitle("选择要展示的事件");
        dlg.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        CheckListView<String> clv =
                new CheckListView<>(FXCollections.observableArrayList(workTypeNames));
        selectedWorkTypes.forEach(clv.getCheckModel()::check);
        dlg.getDialogPane().setContent(clv);

        dlg.setResultConverter(btn ->
                btn == ButtonType.OK
                        ? new ArrayList<>(clv.getCheckModel().getCheckedItems())
                        : null);

        dlg.showAndWait().ifPresent(sel -> {
            // 1. 先清空旧的
            selectedWorkTypes.clear();
            // 2. 如果全都没勾（sel.isEmpty），就认为要全部类型；否则放入 sel
            if (sel.isEmpty()) {
                selectedWorkTypes.addAll(workTypeNames);
            } else {
                selectedWorkTypes.addAll(sel);
            }
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

    /* ========== 主刷新方法 ========== */
    // 1. 获取用户选择的时间范围
    // 2. 获取用户选择的事件类型
    // 3. 从数据库查询对应的 WorkLogs
    // 4. 生成数据集
    // 5. 设置到 PieChart 控件上
    private void refreshPieChart() {

        /* ---- 查询 ---- */
        List<WorkLogs> logs = workLogsService.findByDateRangeAndWorkNames(
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                selectedWorkTypes);

        /* ---- 空数据 ---- */
        boolean empty = logs.isEmpty();
        pieChart.setVisible(!empty);
        recordsScrollPane.setVisible(!empty);
        logScrollPane.setVisible(false);
        noDataLabel.setVisible(empty);
        if (empty) return;

        /* ---- 汇总 ---- */
        Map<String,Integer> total = logs.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getWorkType().getName(),
                        LinkedHashMap::new,
                        Collectors.reducing(0, WorkLogs::getDuration, Integer::sum)));

        /* ---- 饼图数据 ---- */
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        total.forEach((n,v)-> data.add(new PieChart.Data(n,v)));
        pieChart.getData().setAll(data);

        /* ---- Tooltip & Legend 提示 ---- */
        Platform.runLater(() -> {                          // UI 节点已生成
            for (int i = 0;i < data.size(); i++) {
                PieChart.Data d = data.get(i);
                Tooltip.install(d.getNode(),
                        new Tooltip(d.getName()+" : "+format(vToMin(d))));
            }
        });

        /* ---- 下方累计 ---- */
        recordsContainer.getChildren().clear();
        total.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .forEach(e -> recordsContainer.getChildren().add(
                        new Label(e.getKey()+" — "+format(e.getValue()))));
    }

    /* ---------- 辅助 ---------- */
    private static String format(int min){
        return (min/60>0?min/60+"h":"")+(min%60)+"m";
    }
    private static int vToMin(PieChart.Data d){ return (int)d.getPieValue(); }
    private static void showAlert(String m){ new Alert(Alert.AlertType.WARNING,m,ButtonType.OK).showAndWait(); }

    /* 日志视图刷新（若以后再用，可保留） */
    private void refreshLogList(List<WorkLogs> logs){ /* 原实现省略 */ }
}
