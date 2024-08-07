package com.tenco.bank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.AccountService;

import jakarta.servlet.http.HttpSession;

@Controller // IoC 대상(싱글톤으로 관리)
@RequestMapping("/account")
public class AccountController {
	
	
	// 계좌 생성 화면 요청 DI 처리
	private final HttpSession session;
	private final AccountService accountService;
	
	public AccountController(HttpSession session,AccountService accountService) {
		this.session=session;
		this.accountService=accountService;
		if(session==null) {
			throw new UnAuthorizedException("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED);
		}
	}
	
	/**
	 * 계좌 생성 페이지 요청
	 * 주소 설계 : http://localhost:8080/account/save
	 * @return save.jsp
	 */
	@GetMapping("/save")
	public String savePage() {
		// 1. 인증 검사가 필요(account 전체 필요함)
		User principal = (User)session.getAttribute("principal");
		if(principal==null) {
			throw new UnAuthorizedException("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED);
		}
		
		return "account/save";
	}
	
	
	/**
	 * 계좌 생성 기능 요청
	 * 주소 설계 : http://localhost:8080/account/save
	 * @param dto
	 * @return
	 */
	@PostMapping("/save")
	public String saveProc(SaveDTO dto) {
		// 1. form 데이터 추출(파싱 전략)
		// 2. 인증 검사
		// 3. 유효성 검사
		// 4. 서비스 호출
		User principal = (User) session.getAttribute("principal");
		if(principal==null) {
			throw new UnAuthorizedException("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED);
		}
		
		if(dto.getNumber()==null||dto.getNumber().isEmpty()) {
			throw new DataDeliveryException("계좌 번호를 입력하세요.", HttpStatus.BAD_REQUEST);
		}
		if(dto.getPassword()==null||dto.getPassword().isEmpty()) {
			throw new DataDeliveryException("계좌 비밀 번호를 입력하세요.", HttpStatus.BAD_REQUEST);
		}
		if(dto.getBalance()==null||dto.getBalance()<=0) {
			throw new DataDeliveryException("계좌 잔액을 입력하세요.", HttpStatus.BAD_REQUEST);
		}
		
		accountService.createAccount(dto,principal.getId());
		
		return "redirect:/index";
	}
	
	
	/**
	 * 계좌 생성 기능 요청
	 * 주소 설계: http://localhost:8080/account/save
	 */
	// 1. saveDTO를 만든다.
	// 2. saveDTO를 통해 Account를 반환하는 메서드를 만든다.
	// 3. AccountService를 만들어, 오류/상속 처리를 한다.
	// 4. 해당 메서드를 saveAccount()에서 호출
	
}
