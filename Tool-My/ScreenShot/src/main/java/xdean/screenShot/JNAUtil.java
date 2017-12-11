package xdean.screenShot;

import static xdean.jex.extra.annotation.marker.PlatformDependents.WINDOWS;
import static xdean.jex.util.function.Predicates.not;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

import javafx.geometry.Rectangle2D;
import xdean.jex.extra.annotation.marker.PlatformDependents.PlatformDependent;

public class JNAUtil {
  @PlatformDependent(platform = WINDOWS)
  public static List<Rectangle2D> getWindowBounds() {
    final List<HWND> order = new ArrayList<>();
    HWND top = User32.INSTANCE.GetWindow(User32.INSTANCE.GetForegroundWindow(), new DWORD(User32.GW_HWNDFIRST));
    while (top != null) {
      order.add(top);
      top = User32.INSTANCE.GetWindow(top, new DWORD(User32.GW_HWNDNEXT));
    }
    final Rectangle2D[] bounds = new Rectangle2D[order.size()];
    User32.INSTANCE.EnumWindows((hWnd, lParam) -> {
      if (User32.INSTANCE.IsWindowVisible(hWnd)) {
        RECT r = new RECT();
        User32.INSTANCE.GetWindowRect(hWnd, r);
        if (r.left > -32000) {
          int index = order.indexOf(hWnd);
          if (index != -1) {
            bounds[index] = new Rectangle2D(r.left, r.top, r.right - r.left, r.bottom - r.top);
          }
        }
      }
      return true;
    }, null);
    return Arrays.stream(bounds).filter(not(null)).collect(Collectors.toList());
  }
}
