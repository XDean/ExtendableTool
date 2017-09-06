package xdean.tool.sys.other;

import io.reactivex.Flowable;

import org.junit.Test;

public class TestCustomCommandTool {
  @Test
  public void testSplitCommand() {
    assertSplit("1 2 3", "1", "2", "3");
    assertSplit("1 \"2 3\"", "1", "\"2 3\"");
    assertSplit("1 \\\\", "1", "\\\\");
    assertSplit("1 \"2 \\\"3\"", "1", "\"2 \\\"3\"");
    assertSplit("\\ \\\" \"\\ \\ \"", "\\", "\\\"", "\"\\ \\ \"");
  }

  private void assertSplit(String command, String... answer) {
    Flowable.fromArray(CustomCommandTool.split(command))
        .test()
        .assertValues(answer);
    System.out.println();
  }
}
