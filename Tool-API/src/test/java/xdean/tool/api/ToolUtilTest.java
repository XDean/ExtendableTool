package xdean.tool.api;

import static org.junit.Assert.assertEquals;
import static xdean.jex.util.lang.AssertUtil.assertInstanceOf;
import static xdean.tool.api.ToolUtil.*;

import org.junit.Test;

import xdean.tool.api.toolUtil.A;
import xdean.tool.api.toolUtil.B;
import xdean.tool.api.toolUtil.C;
import xdean.tool.api.toolUtil.NoAnno;
import xdean.tool.api.toolUtil.NoDefaultConstruct;
import xdean.tool.api.toolUtil.NotTool;
import xdean.tool.api.toolUtil.WithClass;
import xdean.tool.api.toolUtil.WithField;
import xdean.tool.api.toolUtil.WithMethod;

public class ToolUtilTest {

  @Test
  public void testGetPath() {
    assertEquals("/", getToolPath(A.class));
    assertEquals("/a", getToolPath(B.class));
    assertEquals("/a/b", getToolPath(C.class));
  }

  @Test
  public void testGetTool() {
    loadTool(NoAnno.class).test().assertValueCount(0);
    loadTool(NotTool.class).test().assertValueCount(0);
    loadTool(NoDefaultConstruct.class).test().assertValueCount(0);
    loadTool(WithClass.class).test().assertValueCount(1);
    loadTool(WithField.class).test().assertValueCount(1);
    loadTool(WithMethod.class).test().assertValueCount(1);
  }

  @Test
  public void testToolPath() {
    ITool a = getWrappedTool(C.class).toSingle().toBlocking().value();
    ITool b = a.getChildren().get(0);
    ITool c = b.getChildren().get(0);
    assertInstanceOf(c, C.class);
  }
}