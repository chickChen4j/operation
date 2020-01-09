//package com.chick.operation.controller.exceptionhandler;
//
//import com.chick.operation.dto.ResultDTO;
//import com.chick.operation.exception.OperationException;
//import java.sql.SQLException;
//import javax.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//@ControllerAdvice
//@Slf4j
//public class ExceptionTranslator {
//
//
//  @ExceptionHandler({OperationException.class})
//  @ResponseBody
//  public ResponseEntity handleException(HttpServletRequest request, OperationException ex) {
//    log.info(ex.getMessage(), ex);
//    return new ResponseEntity<>(ResultDTO.builder()
//        .isSuccess(Boolean.FALSE)
//        .message(ex.getMessage())
//        .code(ex.getCode())
//        .build(), HttpStatus.OK);
//  }
//
//  @ExceptionHandler({IllegalArgumentException.class})
//  @ResponseBody
//  public ResponseEntity handleException(HttpServletRequest request, IllegalArgumentException ex) {
//    log.info(ex.getMessage(), ex);
//    return new ResponseEntity<>(ResultDTO.builder()
//        .isSuccess(Boolean.FALSE)
//        .message(ex.getMessage())
//        .code("IllegalArgumentException")
//        .build(), HttpStatus.OK);
//  }
//
//  @ExceptionHandler({SQLException.class})
//  @ResponseBody
//  public ResponseEntity handleException(HttpServletRequest request, SQLException ex) {
//    log.info(ex.getMessage(), ex);
//    return new ResponseEntity<>(ResultDTO.builder()
//        .isSuccess(Boolean.FALSE)
//        .message(ex.getMessage())
//        .code("SQLException")
//        .build(), HttpStatus.OK);
//  }
//
//
//  @ExceptionHandler({MethodArgumentNotValidException.class})
//  @ResponseBody
//  public ResponseEntity handleException(HttpServletRequest request,
//      MethodArgumentNotValidException ex) {
//    log.info(ex.getMessage(), ex);
//    return new ResponseEntity<>(ResultDTO.builder()
//        .isSuccess(Boolean.FALSE)
//        .message(ex.getMessage())
//        .code("MethodArgumentNotValidException")
//        .build(), HttpStatus.OK);
//  }
//
//  @ExceptionHandler({Exception.class})
//  @ResponseBody
//  public ResponseEntity handleException(HttpServletRequest request, Exception ex) {
//    log.error(ex.getMessage(), ex);
//    return new ResponseEntity<>(ResultDTO.builder()
//        .isSuccess(Boolean.FALSE)
//        .message(ex.getMessage())
//        .code("Exception")
//        .build(), HttpStatus.INTERNAL_SERVER_ERROR);
//  }
//
//
//}