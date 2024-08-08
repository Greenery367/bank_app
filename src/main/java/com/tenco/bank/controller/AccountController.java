package com.tenco.bank.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.AccountService;
import com.tenco.bank.utils.Define;

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
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
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
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
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
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		
		if(dto.getNumber()==null||dto.getNumber().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		if(dto.getPassword()==null||dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		if(dto.getBalance()==null||dto.getBalance()<=0) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		
		accountService.createAccount(dto,principal.getId());
		
		return "redirect:/index";
	}
	
	/**
	 * 계좌 목록 화면 요청
	 * 주소 설계: http://localhost:8080/account/list ...
	 * @return list.jsp
	 */
	@GetMapping({"/list","/"})
	public String listPage(Model model) {
		
		// 1. 인증 검사
		User principal=(User)session.getAttribute("principal");
		if(principal==null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		
		// 2. 유효성 검사
		// 3. 서비스 호출
		List<Account> accountList=accountService.readAccountListByUserId(principal.getId());
		if(accountList.isEmpty()) {
			model.addAttribute("accountList",null);
		} else {
			model.addAttribute("accountList",accountList);
		}
		
		// JSP 데이터를 넣어주는 방법
		return "account/list";
	}
	
	
	/**
	 * 출금 페이지 요청
	 * @return
	 */
	@GetMapping("/withdrawal")
	public String withDrawalPage() {
		// 1. 인증 검사
		User principal=(User)session.getAttribute("principal");
		if(principal==null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		
		return "account/withdrawal";
	}
	
	/**
	 * 계좌 출금 요청
	 * @param dto
	 * @return
	 */
	@PostMapping("/withdrawal")
	public String withDrawalProc(WithdrawalDTO dto) {
		// 1. 인증 검사
		User principal=(User)session.getAttribute("principal");
		if(principal==null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		
		
		// 유효성 검사 (자바 코드를 개발) --> 스프링부트 @Valid 라이브러리가 존재한다.
		if(dto.getAmount()==null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if(dto.getAmount().longValue()<=0) {
			throw new DataDeliveryException(Define.W_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
		}
		if(dto.getWAccountNumber()==null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		if(dto.getWAccountPassword()==null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		
		accountService.updateAccountWithdrawal(dto, principal.getId());
		
		return "redirect:/account/list";
	}
	
	/**
	 * 입금 페이지 요청
	 */
	@GetMapping("/deposit")
	public String depositPage() {
		// 1. 인증 검사
		User principal=(User)session.getAttribute("principal");
		if(principal==null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
				
		return "account/deposit";
	}
	
	/**
	 * 
	 */
	
	/**
	 * 입금 요청 처리
	 */
	@PostMapping("/deposit")
	public String depositProc(DepositDTO dto) {
		// 1. 인증 검사
		User principal=(User)session.getAttribute("principal");
		if(principal==null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		
		// 입금시 검사해야 할 것
		// 1. 계좌가 존재하는지
		// 2. 돈이 존재하는지
		
		if (dto.getAmount() == null) {
            throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
        }
		if(dto.getAmount()<=0) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		if(dto.getDAccountNumber()==null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		
		accountService.updateAccountDeposit(dto, principal.getId());
		
		return "redirect:/account/list";
	}
	
	
	/**
	 * 이체 페이지 요청
	 */
	@GetMapping("/transfer")
	public String transferPage() {
		// 1. 인증 검사
		User principal=(User)session.getAttribute("principal");
		if(principal==null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
				
		return "account/transfer";
	}
	
	/**
	 * 
	 */
	
	/**
	 * 이체 요청 처리
	 */
	@PostMapping("/transfer")
	public String transferProc(TransferDTO dto) {
		// 1. 인증 검사
		User principal=(User)session.getAttribute("principal");
		if(principal==null) {
			throw new UnAuthorizedException(Define.NOT_AN_AUTHENTICATED_USER, HttpStatus.UNAUTHORIZED);
		}
		
		// 입금시 검사해야 할 것
		// 1. 계좌가 존재하는지
		// 2. 돈이 존재하는지
		System.out.println(dto);
		
		// 입금-출금 계좌 중복 여부 확인 확인
		if(dto.getDAccountNumber().equals(dto.getWAccountNumber())) {
			throw new DataDeliveryException(Define.INVALID_INPUT, HttpStatus.BAD_REQUEST);
		}
		// 입금 금액 널 확인
		if (dto.getAmount() == null) {
            throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
        }
		// 입금 금액 - 확인
		if(dto.getAmount()<=0) {
			throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
		}
		// 출금 계좌 널 확인
		if(dto.getDAccountNumber()==null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		// 입금 계좌 널 확인
		if(dto.getWAccountNumber()==null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
		}
		// 출금 계좌 비밀번호 널 확인
		if(dto.getPassword()==null) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		
		accountService.updateAccountTransfer(dto, principal.getId());
		
		return "redirect:/account/list";
	}
	
	
}
