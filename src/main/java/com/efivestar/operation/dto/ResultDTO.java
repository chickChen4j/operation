package com.efivestar.operation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultDTO<T> {

  @Builder.Default
  private Boolean isSuccess = true;
  private String message;
  @Builder.Default
  private String code = "success";
  private T data;
}
