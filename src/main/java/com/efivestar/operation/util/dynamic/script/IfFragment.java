package com.efivestar.operation.util.dynamic.script;

import com.efivestar.operation.util.dynamic.Context;

public class IfFragment implements SqlFragment {

  private String test;

  private SqlFragment contents;

  private ExpressionEvaluator expression ;

  public IfFragment(SqlFragment contents, String test) {

    this.expression = new ExpressionEvaluator();
    this.contents = contents;
    this.test = test;
  }

  public boolean apply(Context context) {
    if (expression.evaluateBoolean(test, context.getBinding())) {

      this.contents.apply(context);

      return true;
    }
    return false;
  }

}

