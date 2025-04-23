package controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import model.WorkLogs;
import org.controlsfx.control.CheckListView;
import repository.WorkLogsRepositoryImpl;
import repository.WorkTypeRepositoryImpl;
import service.WorkLogsService;
import service.WorkTypeService;
import util.BaseController;
import util.I18nKey;
import util.LocalizationManager;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsController {

    /* ---------- FXML ---------- */
    @FXML @I18nKey("stats.selectDateRange") private Button  selectDateRangeButton;
    @FXML @I18nKey("stats.filterEvents")
    private Button openFilterButton;
    @FXML private VBox    dateRangePanel;
    @FXML private DatePicker startDatePicker, endDatePicker;

    @FXML @I18nKey("stats.pieChart") private PieChart pieChart;
    @FXML private VBox     recordsContainer;
    @FXML private ScrollPane recordsScrollPane,
            logScrollPane;
    @FXML private VBox logListContainer;
    @FXML @I18nKey("stats.noData") private Label noDataLabel;
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
    @FXML private void onDateRangeButtonClicked() {
        dateRangePanel.setVisible(!dateRangePanel.isVisible());
        selectDateRangeButton.setText(
                LocalizationManager.getBundle().getString("stats.selectDateRange"));
    }
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
            showAlert(
                    LocalizationManager.getBundle().getString("stats.invalidRange")
            );
            return;
        }
        selectDateRangeButton.setText(s + " 到 " + e);
        dateRangePanel.setVisible(false);

        refreshPieChart();
    }

    /* ========== 事件筛选对话框 ========== */
    @FXML private void openWorkTypeSelection() {

        Dialog<List<String>> dlg = new Dialog<>();
        dlg.setTitle(
                LocalizationManager.getBundle().getString("stats.filterEvents")
        );
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

    private static final String[] PALETTE = {
            "#4E79A7", "#F28E2B", "#E15759", "#76B7B2",
            "#59A14F", "#EDC948", "#B07AA1", "#FF9DA7",
            "#9C755F", "#BAB0AC", "#6A4C93", "#D33F49",
            "#FF8C42", "#A1C181", "#50514F"
    };
    private void refreshPieChart() {
        // 1) 查询
        List<WorkLogs> logs = workLogsService.findByDateRangeAndWorkNames(
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                selectedWorkTypes
        );

        // 2) 空数据处理
        boolean empty = logs.isEmpty();
        pieChart.setVisible(!empty);
        recordsScrollPane.setVisible(!empty);
        logScrollPane.setVisible(false);
        noDataLabel.setVisible(empty);
        if (empty) return;

        // 3) 汇总
        Map<String,Integer> total = logs.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getWorkType().getName(),
                        LinkedHashMap::new,
                        Collectors.reducing(0, WorkLogs::getDuration, Integer::sum)
                ));

        // 4) 生成饼图数据
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        total.forEach((name, minutes) -> data.add(new PieChart.Data(name, minutes)));
        pieChart.setData(data);

        // 5) 去掉内置 Legend
        pieChart.setLegendVisible(false);

        // 6) 注册 Tooltip （可选）
        Platform.runLater(() -> {
            for (PieChart.Data d : data) {
                Tooltip.install(d.getNode(),
                        new Tooltip(d.getName() + " : " + format(vToMin(d))));
            }
        });

        // 7) 在右侧列表生成带色块的行
        recordsContainer.getChildren().clear();
        List<PieChart.Data> pieData = pieChart.getData();
        total.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String name = entry.getKey();
                    int minutes = entry.getValue();

                    int colorIndex = 0;
                    for (PieChart.Data d : pieData) {
                        if (d.getName().equals(name)) {
                            // 找到这个 Data 对应的 Node
                            for (String cls : d.getNode().getStyleClass()) {
                                if (cls.startsWith("default-color")) {
                                    // cls 形如 "default-color3"
                                    colorIndex = Integer.parseInt(cls.substring("default-color".length()));
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    Color fill = Color.web(PALETTE[colorIndex]);

                    // 小色块 + 文本
                    Circle dot = new Circle(6, fill);
                    Label text = new Label(name + " — " + format(minutes));
                    text.setStyle("-fx-font-size:14px;");

                    HBox row = new HBox(8, dot, text);
                    row.setAlignment(Pos.CENTER_LEFT);
                    recordsContainer.getChildren().add(row);
                });
    }

    // 辅助方法
    private static String format(int min) {
        return (min/60>0 ? min/60 + "h" : "") + (min%60) + "m";
    }
    private static int vToMin(PieChart.Data d) {
        return (int)d.getPieValue();
    }
    private static void showAlert(String m) {
        new Alert(Alert.AlertType.WARNING, m, ButtonType.OK).showAndWait();
    }

}
