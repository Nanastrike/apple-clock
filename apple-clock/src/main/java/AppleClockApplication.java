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

/**
 * 主程序入口类，继承自 JavaFX 的 Application。
 * 负责加载 FXML 界面文件，并展示主窗口。
 */
public class AppleClockApplication extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        // 创建 Repository
        WorkTypeRepository workTypeRepository = new WorkTypeRepositoryImpl();
        WorkLogsRepository workLogsRepository = new WorkLogsRepositoryImpl();

        // 创建 Service，并传入 Repository
        WorkTypeService workTypeService = new WorkTypeService(workTypeRepository);
        WorkLogsService workLogsService = new WorkLogsService(workLogsRepository, workTypeRepository);

        // 加载 FXML 布局
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        Scene scene = new Scene(loader.load());

        // 给 Controller 注入 Service
        MainController controller = loader.getController();
        controller.setWorkTypeService(workTypeService);
        controller.setWorkLogsService(workLogsService);
        controller.initData();

        // 配置主舞台
        primaryStage.setTitle("Apple Clock");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args); // 启动 JavaFX 应用
    }
}