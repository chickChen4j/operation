package com.efivestar.operation.service.export;

import org.apache.poi.xssf.streaming.SXSSFSheet;

public interface WriteExcelDelegated {

  default void counterChanged(int delta) {
  }

  default void initExcelData(SXSSFSheet eachSheet,
      Integer startRowCount,
      Integer endRowCount,
      Integer currentPage, Integer pageSize) throws Exception {

  }

  void writeExcelData(Integer sheetIndex,
      SXSSFSheet eachSheet,
      Integer startRowCount,
      Integer endRowCount,
      Integer currentPage, Integer pageSize) throws Exception;
}
