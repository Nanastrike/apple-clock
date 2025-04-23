// src/main/java/util/BaseController.java
package util;

import javafx.fxml.Initializable;
import javafx.scene.control.Labeled;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class BaseController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 先让子类做它自己的初始化
        onInitialize(location, resources);
        // 自动绑：所有 @I18nKey 注解的 Labeled 控件
        for (Field f : getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(I18nKey.class) && Labeled.class.isAssignableFrom(f.getType())) {
                I18nKey a = f.getAnnotation(I18nKey.class);
                try {
                    f.setAccessible(true);
                    Labeled ctl = (Labeled)f.get(this);
                    // bindString 来自你的 LocalizationManager
                    ctl.textProperty().bind(LocalizationManager.bindString(a.value()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected abstract void onInitialize(URL location, ResourceBundle resources);
}
