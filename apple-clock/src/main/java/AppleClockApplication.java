import controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import repository.WorkLogsRepository;
import repository.WorkLogsRepositoryImpl;
import repository.WorkTypeRepository;
import repository.WorkTypeRepositoryImpl;
import service.WorkLogsService;
import service.WorkTypeService;
import util.LocalizationManager;

/**
 * 主程序入口类，继承自 JavaFX 的 Application。
 * 负责加载 FXML 界面文件，并展示主窗口。
 */
public class AppleClockApplication extends Application {

    private MainController controller;
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 创建 Repository
        WorkTypeRepository workTypeRepository = new WorkTypeRepositoryImpl();
        WorkLogsRepository workLogsRepository = new WorkLogsRepositoryImpl();

        // 创建 Service，并传入 Repository
        WorkTypeService workTypeService = new WorkTypeService(workTypeRepository);
        WorkLogsService workLogsService = new WorkLogsService(workLogsRepository, workTypeRepository);

        // 加载 FXML 布局
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"),
                LocalizationManager.getBundle());
        loader.setControllerFactory(type -> {
            if (type == MainController.class) {
                MainController c = new MainController();
                c.setWorkTypeService(workTypeService);
                c.setWorkLogsService(workLogsService);
                return c;
            }
            try { return type.getDeclaredConstructor().newInstance(); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        Scene scene = new Scene(loader.load());
        MainController controller = loader.getController();

        // 配置主舞台
        primaryStage.setTitle(LocalizationManager.getBundle().getString("app.title"));
        primaryStage.setWidth(400);
        primaryStage.setHeight(600);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(ev -> controller.handleStop());
        controller.initData();
    }

    @Override
    public void stop() throws Exception {
        // 当整个应用关闭时，再次确保保存
        if (controller != null) {
            controller.handleStop();
        }
        super.stop();
        System.out.println("stopped and saved to file");
    }

    public static void main(String[] args) {
        launch(args); // 启动 JavaFX 应用
    }
}