package com.chick.operation.util.dynamic.script;

import com.chick.operation.util.dynamic.Context;
import com.chick.operation.util.dynamic.token.GenericTokenParser;
import com.chick.operation.util.dynamic.token.TokenHandler;

public class TextFragment implements SqlFragment {

  private String sql;

  public TextFragment(String sql) {
    this.sql = sql;
  }

  public boolean apply(final Context context) {

    GenericTokenParser parser2 = new GenericTokenParser("${", "}",
        new TokenHandler() {

          public String handleToken(String content) {

            Object value = OgnlCache.getValue(content,
                context.getBinding());

            return value == null ? "" : value.toString();
          }
        });

    context.appendSql(parser2.parse(sql));
    return true;
  }

}

