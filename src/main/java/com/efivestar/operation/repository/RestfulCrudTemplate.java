package com.efivestar.operation.repository;

import com.efivestar.operation.dto.Pageable;
import com.efivestar.operation.dto.PagedResultDTO;
import com.efivestar.operation.mapping.InParameter;
import java.util.List;

/**
 * restful风格所对应的dao层接口
 */
public interface RestfulCrudTemplate<T> {

  int insert(T t);

  int delete(Long id);

  int update(T t);

  T selectOne(Long id);

  List<T> selectAll(T t);

  PagedResultDTO<T> selectByPage(T t, Pageable pageable);

  int insert(InParameter[] inParameters);

  int delete(InParameter[] inParameters);

  int update(InParameter[] inParameters);

  T selectOne(InParameter[] inParameters);

  List<T> selectAll(InParameter[] inParameters);

  PagedResultDTO<T> selectByPage(InParameter[] inParameters);

}
