package com.chick.operation.aop;

import com.chick.operation.dto.ResultDTO;
import com.chick.operation.http.ResourceServlet;
import com.chick.operation.repository.BaseRepository;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Slf4j
@Component
public class HandlerAspect {

  @Autowired
  private BaseRepository baseRepository;

  @Pointcut("execution(public void com.chick.operation.service.*.*(..))")
  public void handlerPointcut() {
    //RestfulService中的增删改方法
    // Method is empty as this is just a Pointcut, the implementations are in the advices.
  }

  @Pointcut("execution(public boolean com.chick.operation.service.ConfigService.*(..))")
  public void configPointcut() {

  }

  @Pointcut("execution(public * com.chick.operation.controller.*.*(..))")
  public void allController() {

  }

//  @Pointcut("execution(public * com.chick.operation.controller.RestfulController.)")
//  public void addApiPointcut(){
//
//  }

  @After("handlerPointcut()")
  public void handlerRefresh(JoinPoint joinPoint) throws Throwable {
    //前台通过restful接口修改api和sql的配置表，当配置发生改变时需要刷新内存
    final Object[] args = joinPoint.getArgs();
    final String apiName = args.length > 0 ? String.valueOf(args[0]) : "";
    if ("sql".equals(apiName) || "api".equals(apiName)) {
      baseRepository.refreshConfigFromDB();
    }
  }

  @After("configPointcut()")
  public void refreshConfig(JoinPoint joinPoint) throws Throwable {
    baseRepository.refreshConfigFromDB();
  }

  @Around("allController()")
  public Object checkSession(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes()).getRequest();
    final HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder
        .getRequestAttributes()).getResponse();
    final HttpSession session = request.getSession();
    final Object user = session.getAttribute(ResourceServlet.SESSION_USER_KEY);

    if (user == null) {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    return ResultDTO.builder().isSuccess(Boolean.FALSE).code("401").message("session失效").build();
    } else {
      return proceedingJoinPoint.proceed();
    }
  }
}
