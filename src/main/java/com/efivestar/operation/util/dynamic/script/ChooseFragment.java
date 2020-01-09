package com.efivestar.operation.util.dynamic.script;

import com.efivestar.operation.util.dynamic.Context;
import java.util.List;

public class ChooseFragment implements SqlFragment {

  private SqlFragment defaultSqlFragment;
  private List<SqlFragment> ifSqlFragments;

  public ChooseFragment(List<SqlFragment> ifSqlFragments,
      SqlFragment defaultSqlFragment) {
    this.ifSqlFragments = ifSqlFragments;
    this.defaultSqlFragment = defaultSqlFragment;
  }

  public boolean apply(Context context) {
    for (SqlFragment sqlNode : ifSqlFragments) {
      if (sqlNode.apply(context)) {
        return true;
      }
    }
    if (defaultSqlFragment != null) {
      defaultSqlFragment.apply(context);
      return true;
    }
    return false;
  }

}