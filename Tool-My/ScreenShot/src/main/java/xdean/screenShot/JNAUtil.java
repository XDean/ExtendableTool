package xdean.screenShot;

import static xdean.jex.util.function.Predicates.not;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.win32.StdCallLibrary;

import javafx.geometry.Rectangle2D;

public class JNAUtil {
  public static void main(String[] args) {
    getWindowBounds().forEach(System.out::println);
  }

  public static List<Rectangle2D> getWindowBounds() {
    List<Integer> order = new ArrayList<>();
    int top = User32.INSTANCE.GetTopWindow(0);
    while (top != 0) {
      order.add(top);
      top = User32.INSTANCE.GetWindow(top, User32.GW_HWNDNEXT);
    }
    Rectangle2D[] bounds = new Rectangle2D[order.size()];
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
    }, 0);
    return Arrays.stream(bounds).filter(not(null)).collect(Collectors.toList());
  }

  public static interface WndEnumProc extends StdCallLibrary.StdCallCallback {
    boolean callback(int hWnd, int lParam);
  }

  public static interface User32 extends StdCallLibrary {
    User32 INSTANCE = Native.loadLibrary("user32", User32.class);
    int GW_HWNDLAST = 1;
    int GW_HWNDNEXT = 2;
    int GW_HWNDPREV = 3;
    int GW_OWNER = 4;

    boolean EnumWindows(WndEnumProc wndenumproc, int lParam);

    boolean IsWindowVisible(int hWnd);

    int GetWindowRect(int hWnd, RECT r);

    void GetWindowTextA(int hWnd, byte[] buffer, int buflen);

    int GetTopWindow(int hWnd);

    int GetWindow(int hWnd, int flag);

  }
}
