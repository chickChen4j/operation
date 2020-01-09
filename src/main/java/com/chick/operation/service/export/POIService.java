package com.chick.operation.service.export;


import com.alibaba.fastjson.JSONObject;
import com.chick.operation.dto.ResultDTO;
import com.chick.operation.mapping.entity.ExportTask;
import com.chick.operation.mapping.enums.ExportStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
public class POIService {


  @Autowired
  ExportTaskService exportTaskService;

  //下载文件的临时目录
  @Value("${operation.download.tempPath:temp}")
  private String downloadTempPath;

  @Autowired
  DownloadCenterService downloadCenterService;

  public static SXSSFWorkbook initExcel(Long totalCount, Integer sheetRowCount,
      List<String> titles) {
    SXSSFWorkbook wb = new SXSSFWorkbook(500);
    Integer sheetCount = Math.toIntExact(
        (totalCount % sheetRowCount == 0) ? (totalCount / sheetRowCount)
            : (totalCount / sheetRowCount + 1));
    for (int i = 0; i < sheetCount; i++) {
      SXSSFSheet sheet = wb.createSheet("sheet" + (i + 1));
      SXSSFRow headRow = sheet.createRow(0);
      for (int j = 0; j < titles.size(); j++) {
        SXSSFCell headRowCell = headRow.createCell(j);
        headRowCell.setCellValue(titles.get(j));
      }
    }
    return wb;
  }

  /**
   * @param totalCount 总行数
   * @param maxWriteRowCount 分页请求的行数
   * @param sheetRowCount 每个sheet页最大行数
   * @param pages 总页数
   * @param fileName 文件名
   * @param titles 列标题
   */
  public ResultDTO uploadExcel(Long totalCount, Integer maxWriteRowCount, Integer sheetRowCount,
      Long pages,
      String fileName, List<String> titles,
      WriteExcelDelegated writeExcelDelegated) throws  Exception {
    ExportTask exportTask = exportTaskService.findByTaskId(fileName);
    //文件大小(byte)
    long fileSize;

    //进度
    Double progress = 0D;

    //每页数据写入最大需要的sheet数
    Integer pageCountNeedMaxSheetCount = 1;
    //sheet下标
    int sheetIndex = 0;
    //最后一次写入的行数
    Integer lastRowNumber = 0;
    int startRow = 0;
    int endRow = 0;
    // 初始化EXCEL
    SXSSFWorkbook wb = POIService.initExcel(totalCount, sheetRowCount, titles);
    for (int page = 0; page < pages; page++) {
      Integer writeRowCount =
          (page + 1) * maxWriteRowCount < totalCount.intValue() ? maxWriteRowCount
              : totalCount.intValue() - page * maxWriteRowCount;
      //示例：每页100条数据，一个sheet150条，最多需要100/150+1，最多需要1个sheet页容纳
      pageCountNeedMaxSheetCount = Math.toIntExact(writeRowCount / sheetRowCount + 1);
      for (Integer j = 0; j <= pageCountNeedMaxSheetCount; j++) {
        //假如当前页还有剩余，则先在当前页写入
        //当前页剩余的行数
        int currentSheetRemainRowNumber = sheetRowCount * (sheetIndex + 1) - endRow;
        //如果当前页没有剩余行可写入了，sheetIndex需要+1
        if (currentSheetRemainRowNumber <= 0) {
          sheetIndex++;
          currentSheetRemainRowNumber = sheetRowCount * (sheetIndex + 1) - endRow;
        }
        startRow = endRow + 1;
        endRow =
            startRow + currentSheetRemainRowNumber - 1 <= page * maxWriteRowCount + writeRowCount ?
                startRow + currentSheetRemainRowNumber - 1
                : page * maxWriteRowCount + writeRowCount;
        SXSSFSheet eachSheet =  wb.getSheetAt(sheetIndex);
        writeExcelDelegated
            .writeExcelData(sheetIndex, eachSheet, startRow, endRow, page, writeRowCount);
        //如果写完以后还有剩余，说明一个sheet页足够写入一页数据了，所以直接跳出循环
        if (currentSheetRemainRowNumber > maxWriteRowCount) {
          break;
        }
      }

      //todo 生成进度
      BigDecimal bg = new BigDecimal((float) (page + 1) / pages).setScale(4, RoundingMode.UP);
      progress = bg.multiply(new BigDecimal(100)).doubleValue();

      if (exportTask != null) {
        //说明不是最后一页
        if (page != pages) {
          exportTask.setExportStatusEnum(ExportStatusEnum.exporting);
          //这地方只写入进度！文件大小，导出结束的状态，url等，在最后只写入一遍就够了
          exportTask.setProgress(progress);
          exportTaskService.save(exportTask);
        }
      }
    }

    File file = new File(downloadTempPath);
    if (!file.exists()) {
      file.mkdirs();
    }
    File tmpfile = new File(downloadTempPath + fileName + ".xlsx");

    FileOutputStream outputStream = new FileOutputStream(tmpfile);
    wb.write(outputStream);
    wb.dispose();
    outputStream.close();

    //注意！！！取文件大小必须等本地文件生成了才能去得到，否则取到的永远都是0
    fileSize = tmpfile.length();

    //todo 上传文件
    JSONObject jsonObject_upload = downloadCenterService.uploadFile(tmpfile);
    if (jsonObject_upload != null) {
      //byte转kb
      exportTask.setFileSize(fileSize / 1024);
      exportTask.setDownloadUrl(jsonObject_upload.getString("fileDownloadUri"));
      return ResultDTO.builder().data(exportTask).build();
    }

    return ResultDTO.builder().build();
  }

}

