package com.efivestar.operation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Pageable {

  public static final String SIZE = "size";

  public static final String NUMBER = "number";

  @Builder.Default
  private Integer size = 10;

  @Builder.Default
  private Integer number = 0;
}
