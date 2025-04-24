package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import model.WorkLogs;
import org.controlsfx.control.CheckListView;
import repository.WorkLogsRepositoryImpl;
import repository.WorkTypeRepositoryImpl;
import service.WorkLogsService;
import service.WorkTypeService;
import util.I18nKey;
import util.LocalizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsController {

    /* ==== FXML 注入 ==== */
    @FXML
    @I18nKey("stats.title")
    private Label statsTitle;
    @FXML
    @I18nKey("stats.selectDateRange")
    private Button selectDateRangeButton;
    @FXML
    @I18nKey("stats.filterEvents")
    private Button openFilterButton;
    @FXML
    private VBox dateRangePanel;
    @FXML
    private DatePicker startDatePicker, endDatePicker;

    @FXML
    private PieChart pieChart;
    @FXML
    private ScrollPane recordsScrollPane;
    @FXML
    private VBox recordsContainer;
    @FXML
    private VBox logListContainer;
    @FXML
    @I18nKey("stats.noData")
    private Label noDataLabel;
    @FXML
    private Button toggleViewButton;

    @FXML private HBox chartPane;
    @FXML private ScrollPane logPane;
    private boolean showingLogs = false;


    /* ==== 后端服务 ==== */
    private final WorkTypeService workTypeService =
            new WorkTypeService(new WorkTypeRepositoryImpl());
    private final WorkLogsService workLogsService =
            new WorkLogsService(new WorkLogsRepositoryImpl(),
                    new WorkTypeRepositoryImpl());

    /* ==== 运行时状态 ==== */
    private List<String> workTypeNames;
    private final List<String> selectedWorkTypes = new ArrayList<>();

    /** 15 色调色板，和 CSS 里的 default-colorN 对应 */
    private static final String[] PALETTE = {
            "#4E79A7", "#F28E2B", "#E15759", "#76B7B2",
            "#59A14F", "#EDC948", "#B07AA1", "#FF9DA7",
            "#9C755F", "#BAB0AC", "#6A4C93", "#D33F49",
            "#FF8C42", "#A1C181", "#50514F"
    };

    @FXML
    private void initialize() {
        // 1) 准备事件类型列表
        workTypeNames = workTypeService.getAllWorkTypeNames();
        selectedWorkTypes.addAll(workTypeNames);

        // 2) 默认「过去 7 天」
        setRange(-6);

        // 3) 首次刷新
        onConfirmDateRange();
    }

    /* —— 日期快捷按钮 —— */
    @FXML private void onDateRangeButtonClicked() {
        dateRangePanel.setVisible(!dateRangePanel.isVisible());
        selectDateRangeButton.setText(
                LocalizationManager.getBundle().getString("stats.selectDateRange"));
    }
    @FXML private void onTodayClicked()     { setRange(0);onConfirmDateRange(); }
    @FXML private void onYesterdayClicked() { setRange(-1);onConfirmDateRange(); }
    @FXML private void onLast7DaysClicked() { setRange(-6);onConfirmDateRange(); }
    @FXML private void onLast30DaysClicked(){ setRange(-29);onConfirmDateRange(); }

    private void setRange(int offsetDays) {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(offsetDays<0 ? today.plusDays(offsetDays) : today);
        endDatePicker.setValue(today);
        dateRangePanel.setVisible(false);
    }

    /** 点击「确定」后既刷新文字也重绘图表 */
    @FXML private void onConfirmDateRange() {
        LocalDate s = startDatePicker.getValue(), e = endDatePicker.getValue();
        if (s==null || e==null || s.isAfter(e)) {
            new Alert(Alert.AlertType.WARNING,
                    LocalizationManager.getBundle().getString("stats.invalidRange"),
                    ButtonType.OK).showAndWait();
            return;
        }
        selectDateRangeButton.setText(s + " － " + e);
        refreshPieChart();
    }

    /* —— 事件类型筛选 —— */
    @FXML private void openWorkTypeSelection() {
        Dialog<List<String>> dlg = new Dialog<>();
        dlg.setTitle(LocalizationManager.getBundle().getString("stats.filterEvents"));
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        CheckListView<String> clv =
                new CheckListView<>(FXCollections.observableArrayList(workTypeNames));
        selectedWorkTypes.forEach(clv.getCheckModel()::check);
        dlg.getDialogPane().setContent(clv);

        dlg.setResultConverter(btn ->
                btn==ButtonType.OK
                        ? new ArrayList<>(clv.getCheckModel().getCheckedItems())
                        : null
        );
        dlg.showAndWait().ifPresent(sel -> {
            selectedWorkTypes.clear();
            if (sel.isEmpty()) selectedWorkTypes.addAll(workTypeNames);
            else             selectedWorkTypes.addAll(sel);
            refreshPieChart();
        });
    }

    /* —— 切换「饼图/日志」视图 —— */
    @FXML
    private void handleToggleView() {
        showingLogs = !showingLogs;
        chartPane.setVisible(!showingLogs);
        logPane.setVisible(showingLogs);

        if (showingLogs) {
            refreshLogView();   // 切到流水时重建列表
            toggleViewButton.setText(
                    LocalizationManager.getBundle().getString("stats.showChart"));
        } else {
            toggleViewButton.setText(
                    LocalizationManager.getBundle().getString("stats.toggleView"));
        }
    }
    private void refreshLogView() {

        logListContainer.getChildren().clear();
        // 1) 查数据，end 加一天
        List<WorkLogs> raw = workLogsService.findByDateRangeAndWorkNames(
                startDatePicker.getValue(),
                endDatePicker.getValue().plusDays(1),
                selectedWorkTypes
        );

        // 2) 排序
        raw.sort((a,b) -> b.getBegin().compareTo(a.getBegin()));
        // 3) 过滤 & 补全 end==null
        DateTimeFormatter dmf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter tmf = DateTimeFormatter.ofPattern("HH:mm");
        for (WorkLogs wl : raw) {
            LocalDateTime actualEnd = wl.getEnd() != null
                    ? wl.getEnd()
                    : wl.getBegin().plusMinutes(wl.getDuration());

            String range = wl.getBegin().format(dmf)
                    + " ～ " + actualEnd.format(tmf);
            String line  = range
                    + "   " + wl.getWorkType().getName()
                    + "   " + format(wl.getDuration());

            Label row = new Label(line);
            row.setStyle("-fx-font-size:14px; -fx-text-fill:#333;");
            logListContainer.getChildren().add(row);
        }

        if (raw.isEmpty()) {
            Label empty = new Label(
                    LocalizationManager.getBundle().getString("stats.noData")
            );
            empty.setStyle("-fx-text-fill:gray; -fx-font-size:16px;");
            logListContainer.getChildren().add(empty);
        }
    }

    /* —— 核心：重绘饼图和右侧列表 —— */
    private void refreshPieChart() {
        List<WorkLogs> logs = workLogsService.findByDateRangeAndWorkNames(
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                selectedWorkTypes
        );

        boolean empty = logs.isEmpty();
        pieChart.setVisible(!empty);
        recordsScrollPane.setVisible(!empty);
        logPane.setVisible(false);
        noDataLabel.setVisible(empty);
        if (empty) return;

        // 汇总
        Map<String,Integer> total = logs.stream().collect(
                Collectors.groupingBy(
                        l->l.getWorkType().getName(),
                        LinkedHashMap::new,
                        Collectors.reducing(0, WorkLogs::getDuration, Integer::sum)
                )
        );

        // 饼图数据
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        total.forEach((n,v)-> data.add(new PieChart.Data(n, v)));
        pieChart.setData(data);
        pieChart.setLegendVisible(false);

        // Tooltip
        Platform.runLater(() -> {
            for (PieChart.Data d : data) {
                Tooltip.install(d.getNode(),
                        new Tooltip(d.getName() + " : " + format(vToMin(d))));
            }
        });

        // 右侧列表
        recordsContainer.getChildren().clear();
        total.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String name = entry.getKey();
                    int minutes = entry.getValue();
                    // 找到它在 pieChart 中的 default-colorX
                    int colorIndex = data.stream()
                            .filter(d->d.getName().equals(name))
                            .map(d-> d.getNode().getStyleClass().stream()
                                    .filter(c->c.startsWith("default-color"))
                                    .findFirst()
                                    .map(c->Integer.parseInt(c.substring(13)))
                                    .orElse(0))
                            .findFirst().orElse(0);
                    Color fill = Color.web(PALETTE[colorIndex]);

                    Circle dot = new Circle(6, fill);
                    Label text = new Label(name + " — " + format(minutes));
                    HBox row = new HBox(8, dot, text);
                    row.setAlignment(Pos.CENTER_LEFT);
                    recordsContainer.getChildren().add(row);
                });
    }

    private static String format(int m) { return (m/60>0 ? m/60+"h":"") + (m%60)+"m"; }
    private static int    vToMin(PieChart.Data d) { return (int)d.getPieValue(); }
}
