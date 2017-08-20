package xdean.tool.sys;

import java.util.Optional;

import javax.swing.JOptionPane;

public class Util {

  public static void showMessage(String message) {
    JOptionPane.showMessageDialog(null, message);
  }

  public static Optional<String> showInput(String message) {
    return Optional.ofNullable(JOptionPane.showInputDialog(message));
  }

  public static Optional<Integer> showInputInteger(String message) {
    Optional<String> input = showInput(message);
    if (input.isPresent()) {
      try {
        return Optional.of(Integer.valueOf(input.get()));
      } catch (NumberFormatException e) {
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }
}
