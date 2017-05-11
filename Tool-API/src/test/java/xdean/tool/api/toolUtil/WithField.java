package xdean.tool.api.toolUtil;

import xdean.tool.api.IToolGetter;
import xdean.tool.api.Tool;

@Tool
public class WithField {

  @Tool
  public static final IToolGetter LEGAL = () -> new EmptyToolItem();

  @Tool
  public static final Object ILLEGAL1 = null;

  @Tool
  public IToolGetter ILLEGAL2 = () -> new EmptyToolItem();

  @Tool
  public static final IToolGetter ILLEGAL3 = null;

  public static final IToolGetter ILLEGAL4 = () -> new EmptyToolItem();
}
