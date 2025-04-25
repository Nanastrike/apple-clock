package controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
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
    private ContextMenu dateContextMenu;
    @FXML
    @I18nKey("stats.filterEvents")
    private Button openFilterButton;
    @FXML
    private DatePicker startDatePicker, endDatePicker;

    @FXML
    private PieChart pieChart;
    @FXML
    private ScrollPane recordsScrollPane;
    @FXML
    private VBox recordsContainer;
    @FXML
    @I18nKey("stats.noData")
    private Label noDataLabel;
    @FXML
    private Button toggleViewButton;
    @FXML
    private HBox chartPane;
    @FXML
    private VBox logPaneContainer;
    @FXML
    private DialogPane filterDialog;
    @FXML
    private CheckListView<String> filterClv;
    @FXML private TreeTableView<LogEntry> logTable;
    @FXML private TreeTableColumn<LogEntry, String> dateColumn;
    @FXML private TreeTableColumn<LogEntry, String> timeColumn;
    @FXML private TreeTableColumn<LogEntry, String> typeColumn;
    @FXML private TreeTableColumn<LogEntry, String> durationColumn;
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

    /**
     * 15 色调色板，和 CSS 里的 default-colorN 对应
     */
    private static final String[] PALETTE = {
            "#4E79A7", "#F28E2B", "#E15759", "#76B7B2",
            "#59A14F", "#EDC948", "#B07AA1", "#FF9DA7",
            "#9C755F", "#BAB0AC", "#6A4C93", "#D33F49",
            "#FF8C42", "#A1C181", "#50514F"
    };

    @FXML
    private void initialize() {
        // 设置列的值工厂
        dateColumn.setCellValueFactory( p ->
                new SimpleStringProperty(p.getValue().getValue().getDateHeader())
        );
        timeColumn.setCellValueFactory( p ->
                new SimpleStringProperty(p.getValue().getValue().getTimeRange())
        );
        typeColumn.setCellValueFactory( p ->
                new SimpleStringProperty(p.getValue().getValue().getWorkType())
        );
        durationColumn.setCellValueFactory( p ->
                new SimpleStringProperty(p.getValue().getValue().getDurationText())
        );
        // 1) 准备事件类型列表
        workTypeNames = workTypeService.getAllWorkTypeNames();
        selectedWorkTypes.addAll(workTypeNames);

        // 2) 默认「过去 7 天」
        setRange(-6);

        // 3) 首次刷新
        onConfirmDateRange();
    }

    /* —— 弹出/隐藏日期悬浮面板 —— */
    @FXML
    private void onDateRangeButtonClicked() {
        if (dateContextMenu.isShowing()) {
            dateContextMenu.hide();
        } else {
            dateContextMenu.show(
                    selectDateRangeButton,
                    Side.BOTTOM,  // 从按钮下方弹出
                    0, 0
            );
        }
    }

    /* —— 快捷按钮 —— */
    @FXML
    private void onTodayClicked() {
        setRange(0);
        onConfirmDateRange();
    }

    @FXML
    private void onYesterdayClicked() {
        setRange(-1);
        onConfirmDateRange();
    }

    @FXML
    private void onLast7DaysClicked() {
        setRange(-6);
        onConfirmDateRange();
    }

    @FXML
    private void onLast30DaysClicked() {
        setRange(-29);
        onConfirmDateRange();
    }

    private void setRange(int offsetDays) {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(offsetDays < 0 ? today.plusDays(offsetDays) : today);
        endDatePicker.setValue(today);
    }

    /**
     * 点击确定后刷新并隐藏悬浮面板
     */
    @FXML
    private void onConfirmDateRange() {
        LocalDate s = startDatePicker.getValue(), e = endDatePicker.getValue();
        if (s == null || e == null || s.isAfter(e)) {
            new Alert(Alert.AlertType.WARNING,
                    LocalizationManager.getBundle().getString("stats.invalidRange"),
                    ButtonType.OK).showAndWait();
            return;
        }
        selectDateRangeButton.setText(s + " － " + e);
        dateContextMenu.hide();           // 隐藏悬浮面板
        if (showingLogs) {
            refreshLogView();
        } else {
            refreshPieChart();
        }
    }

    /* —— 事件类型筛选 —— */
    @FXML
    private void openWorkTypeSelection() {
        // —— 1) 原有：创建对话框 & 标题 ——
        Dialog<List<String>> dlg = new Dialog<>();
        dlg.setTitle(LocalizationManager.getBundle().getString("stats.filterEvents"));

        // —— 2) 原有：添加按钮 ——
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // —— 3) 原有：生成 CheckListView 并打钩 ——
        CheckListView<String> clv = new CheckListView<>(
                FXCollections.observableArrayList(workTypeNames)
        );
        selectedWorkTypes.forEach(clv.getCheckModel()::check);

        // —— 4) **新增 UI**：复用 CSS 做“卡片”样式 ——
        DialogPane pane = dlg.getDialogPane();
        pane.getStylesheets().add(
                getClass().getResource("/css/statistics.css").toExternalForm()
        );
        pane.getStyleClass().add("my-list-pane");
        // —— 5) **新增 UI**：为了给内容加 padding，我们把 clv 包在一个 VBox 里 ——
        VBox wrapper = new VBox(clv);
        wrapper.setSpacing(12);
        wrapper.setPadding(new Insets(20));  // 四边 20 的统一内边距
        pane.setContent(wrapper);

        // —— 6) 原有：结果转换 & 显示 ——
        dlg.setResultConverter(btn ->
                btn == ButtonType.OK
                        ? new ArrayList<>(clv.getCheckModel().getCheckedItems())
                        : null
        );
        dlg.showAndWait().ifPresent(sel -> {
            selectedWorkTypes.clear();
            if (sel.isEmpty()) selectedWorkTypes.addAll(workTypeNames);
            else selectedWorkTypes.addAll(sel);

            if (showingLogs) refreshLogView();
            else refreshPieChart();
        });
    }


    /* —— 切换「饼图/日志」视图 —— */
    @FXML
    private void handleToggleView() {
        showingLogs = !showingLogs;
        chartPane.setVisible(!showingLogs);
        logPaneContainer.setVisible(showingLogs);  // MODIFIED: 切换到新容器
        toggleViewButton.setText(showingLogs
                ? LocalizationManager.getBundle().getString("stats.showChart")
                : LocalizationManager.getBundle().getString("stats.toggleView"));

        if (showingLogs) {
            refreshLogView();
        } else {
            refreshPieChart();
        }
    }

    /**
     * 构建流水视图
     */
    private void refreshLogView() {
        // 隐藏饼图 & 列表，显示日志表
        pieChart.setVisible(false);
        recordsScrollPane.setVisible(false);
        logTable.setVisible(true);
        noDataLabel.setVisible(false);

        // 1) 拉出数据并按日期分组
        List<WorkLogs> raw = workLogsService.findByDateRangeAndWorkNames(
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                selectedWorkTypes
        );
        Map<LocalDate, List<WorkLogs>> grouped = raw.stream()
                .collect(Collectors.groupingBy(
                        wl -> wl.getBegin().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 2) 构造树状节点
        TreeItem<LogEntry> root = new TreeItem<>(new LogEntry());
        root.setExpanded(true);

        DateTimeFormatter tmf = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dTitle = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault());

        grouped.forEach((date, list) -> {
            String header = date.format(dTitle);
            TreeItem<LogEntry> dateNode = new TreeItem<>(LogEntry.dateHeader(header));
            dateNode.setExpanded(true);

            for (WorkLogs wl : list) {
                LocalDateTime end = wl.getEnd()!=null
                        ? wl.getEnd()
                        : wl.getBegin().plusMinutes(wl.getDuration());
                String range = wl.getBegin().format(tmf) + " – " + end.format(tmf);
                String dur   = format(wl.getDuration());
                TreeItem<LogEntry> row = new TreeItem<>(LogEntry.logRow(range, wl.getWorkType().getName(), dur));
                dateNode.getChildren().add(row);
            }
            root.getChildren().add(dateNode);
        });

        // 3) 应用到 Table
        logTable.setRoot(root);
        logTable.refresh();
    }

    public static class LogEntry {
        private final String dateHeader, timeRange, workType, durationText;
        // 构造通用行
        private LogEntry(String dateHeader, String timeRange, String workType, String durationText) {
            this.dateHeader = dateHeader;
            this.timeRange = timeRange;
            this.workType = workType;
            this.durationText = durationText;
        }
        // 用于根节点
        public LogEntry() { this("", "", "", ""); }
        // 一级节点工厂
        public static LogEntry dateHeader(String dateTitle) {
            return new LogEntry(dateTitle, "", "", "");
        }
        // 二级日志行工厂
        public static LogEntry logRow(String timeRange, String workType, String duration) {
            return new LogEntry("", timeRange, workType, duration);
        }
        public String getDateHeader()  { return dateHeader; }
        public String getTimeRange()   { return timeRange; }
        public String getWorkType()    { return workType; }
        public String getDurationText(){ return durationText; }
    }

    /**
     * 构建饼图和列表
     */
    private void refreshPieChart() {
        List<WorkLogs> logs = workLogsService.findByDateRangeAndWorkNames(
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                selectedWorkTypes
        );
        boolean empty = logs.isEmpty();
        pieChart.setVisible(!empty);
        recordsScrollPane.setVisible(!empty);
        logPaneContainer.setVisible(false);
        noDataLabel.setVisible(empty);
        if (empty) return;

        Map<String, Integer> total = logs.stream().collect(
                Collectors.groupingBy(
                        l -> l.getWorkType().getName(),
                        LinkedHashMap::new,
                        Collectors.reducing(0, WorkLogs::getDuration, Integer::sum)
                )
        );

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        total.forEach((n, v) -> data.add(new PieChart.Data(n, v)));
        pieChart.setData(data);
        pieChart.setLegendVisible(false);

        Platform.runLater(() -> {
            for (PieChart.Data d : data) {
                Tooltip.install(d.getNode(),
                        new Tooltip(d.getName() + " : " + format((int) d.getPieValue())));
            }
        });

        recordsContainer.getChildren().clear();
        total.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    int idx = data.stream()
                            .filter(d -> d.getName().equals(entry.getKey()))
                            .map(d -> d.getNode().getStyleClass().stream()
                                    .filter(c -> c.startsWith("default-color"))
                                    .findFirst()
                                    .map(c -> Integer.parseInt(c.substring(13)))
                                    .orElse(0))
                            .findFirst().orElse(0);
                    Color fill = Color.web(PALETTE[idx]);
                    Circle dot = new Circle(6, fill);
                    Label text = new Label(entry.getKey() + " — " + format(entry.getValue()));
                    HBox row = new HBox(8, dot, text);
                    row.setAlignment(Pos.CENTER_LEFT);
                    recordsContainer.getChildren().add(row);
                });
    }

    private static String format(int m) {
        return (m / 60 > 0 ? m / 60 + "h" : "") + (m % 60) + "m";
    }
}
