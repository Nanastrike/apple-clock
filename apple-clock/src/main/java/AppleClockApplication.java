import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 主程序入口类，继承自 JavaFX 的 Application。
 * 负责加载 FXML 界面文件，并展示主窗口。
 */
public class AppleClockApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 加载 FXML 布局
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        Scene scene = new Scene(loader.load());

        // 配置主舞台
        primaryStage.setTitle("Apple Clock");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // 启动 JavaFX 应用
    }
}