import controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import repository.WorkTypeRepository;
import repository.WorkTypeRepositoryImpl;
import service.WorkTypeService;

/**
 * 主程序入口类，继承自 JavaFX 的 Application。
 * 负责加载 FXML 界面文件，并展示主窗口。
 */
@SpringBootApplication
public class AppleClockApplication extends Application {

    private ConfigurableApplicationContext ctx;

    @Override
    public void init() {
        // 启动 Spring Boot
        ctx = SpringApplication.run(AppleClockApplication.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 加载 FXML 布局
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        Scene scene = new Scene(loader.load());

        MainController controller = loader.getController();
        WorkTypeRepository workTypeRepository = new WorkTypeRepositoryImpl();
        WorkTypeService workTypeService = new WorkTypeService(workTypeRepository);
        controller.setWorkTypeService(workTypeService);

        // 配置主舞台
        primaryStage.setTitle("Apple Clock");
        primaryStage.setWidth(800);    // 宽度 800px
        primaryStage.setHeight(600);   // 高度 600px
        primaryStage.setMinWidth(600);//最小尺寸
        primaryStage.setMinHeight(400);//最小尺寸

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        ctx.close();
    }

    public static void main(String[] args) {
        launch(args); // 启动 JavaFX 应用
    }
}