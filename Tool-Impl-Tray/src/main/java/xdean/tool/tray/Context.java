package xdean.tool.tray;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

@UtilityClass
@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
public class Context {
  String ICON_PATH = xdean.tool.api.Context.HOME_PATH + "/icon.png";
}
