package xdean.tool.api;

import static org.junit.Assert.assertEquals;
import static xdean.jex.util.lang.AssertUtil.*;
import static xdean.tool.api.ToolUtil.*;

import org.junit.Test;

import xdean.tool.api.impl.AbstractToolItem;

public class ToolUtilTest {

  @Test
  public void testGetPath() {
    assertEquals("/", getToolPath(A.class));
    assertEquals("/a", getToolPath(B.class));
    assertEquals("/a/b", getToolPath(C.class));
  }

  @Test
  public void testGetTool() {
    assertEmpty(getTool(NoAnno.class));
    assertEmpty(getTool(NotTool.class));
    assertEmpty(getTool(NoDefaultConstruct.class));
    assertPresent(getTool(Normal.class));
  }
  
  @Test
  public void testToolPath(){
    ITool a = getWrappedTool(C.class).get();
    ITool b = a.getChildren().get(0);
    ITool c = b.getChildren().get(0);
    assertInstanceOf(c, C.class);
  }
}

class EmptyToolItem extends AbstractToolItem {
  @Override
  public void onClick() {
  }
}

class NoAnno extends EmptyToolItem {
}

@Tool
class NotTool {
}

@Tool
class NoDefaultConstruct extends EmptyToolItem {
  public NoDefaultConstruct(String need) {
    super();
  }
}

@Tool
class Normal extends EmptyToolItem {
}

@Tool
class A extends EmptyToolItem {
}

@Tool(path = "/a")
class B extends EmptyToolItem {
}

@Tool(path = "/b", parent = B.class)
class C extends EmptyToolItem {
}