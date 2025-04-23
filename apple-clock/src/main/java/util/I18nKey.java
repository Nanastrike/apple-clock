package util;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface I18nKey {
    String value();  // ResourceBundle 中的 key
}