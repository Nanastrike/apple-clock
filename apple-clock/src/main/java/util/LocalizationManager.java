package util;

import javafx.beans.binding.StringBinding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationManager {
    // 当前语言（Locale）属性
    private static final ObjectProperty<Locale> localeProperty =
            new SimpleObjectProperty<>(Locale.ENGLISH);

    /** 切换语言，会触发所有依赖了 localeProperty 的 Binding 重计算 */
    public static void setLocale(Locale locale) {
        localeProperty.set(locale);
    }
    public static Locale getLocale() {
        return localeProperty.get();
    }
    public static ObjectProperty<Locale> localeProperty() {
        return localeProperty;
    }

    /** 根据当前 locale 获取 ResourceBundle */
    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("i18n.messages", getLocale());
    }

    /**
     * 为某个 key 创建一个 StringBinding，
     * 当 localeProperty 变化时，会自动从新的 Bundle 拿到新文本。
     */
    public static StringBinding bindString(String key) {
        return Bindings.createStringBinding(
                () -> getBundle().getString(key),
                localeProperty
        );
    }
}