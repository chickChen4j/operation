package com.chick.operation.util.dynamic.script;

import com.chick.operation.util.dynamic.Context;
import java.util.List;

public class MixedSqlFragment implements SqlFragment {

  private List<SqlFragment> contents ;

  public MixedSqlFragment(List<SqlFragment> contents){
    this.contents  = contents ;
  }

  public boolean apply(Context context) {

    for(SqlFragment sf : contents){
      sf.apply(context);
    }

    return true;
  }





}