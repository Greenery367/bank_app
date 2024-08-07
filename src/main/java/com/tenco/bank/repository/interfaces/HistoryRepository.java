package com.tenco.bank.repository.interfaces;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tenco.bank.repository.model.History;

//HistoryRepository, history.xml 파일을 매칭 시킨다. 
@Mapper 
public interface HistoryRepository {

	public int insert(History history);
	public int updateById(History history);
	public int deleteById(Integer id);
	
	// 거래내역 조회 
	public History findById(Integer id);
	public List<History> findAll();
	
	//코드 추가 예정 - 모델을 반드시  1:1 엔터티에 매핑을 시킬 필요는 없다. 
	// 조인 쿼리, 서브쿼리 
	
}



