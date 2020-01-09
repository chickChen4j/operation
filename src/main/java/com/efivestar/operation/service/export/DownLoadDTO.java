package com.efivestar.operation.service.export;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownLoadDTO {

  private String fileDownloadUri;

  private String fileName;

  private String fileType;

  private Long size;

}
