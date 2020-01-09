package com.chick.operation.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagedResultDTO<T> {
   private List<T> content;
   private Long totalPages;
   private Boolean last;
   private Long totalElements;
   private Boolean first;
   private Integer size;
   private Integer number;
}
