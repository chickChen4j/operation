package com.chick.operation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Sortable {

  public static final String SORT_NAME = "sortName";

  public static final String SORT_ORDER = "sortOrder";

  private String sortName;

  @Builder.Default
  private String sortOrder = "desc";
}
