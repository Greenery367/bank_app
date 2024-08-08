package com.tenco.bank.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;

@Service
public class AccountService {
	
	private final AccountRepository accountRepository;
	private final HistoryRepository historyRepository;
	
	@Autowired
	public AccountService(AccountRepository accountRepository,HistoryRepository historyRepository) {
		this.accountRepository=accountRepository;
		this.historyRepository=historyRepository;
	}

	
	/**
	 * 계좌 생성 기능 
	 * @param dto
	 * @param id
	 */
	// 트랜잭션
	@Transactional
	public void createAccount(SaveDTO dto, Integer principalId) {
		int result=0;
		
		try {
			result=accountRepository.insert(dto.toAccount(principalId));
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.INVALID_INPUT, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new DataDeliveryException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}

		if(result==0) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}


	// 원자성을 위한 트랜잭션 처리
	// @Transactional
	public List<Account> readAccountListByUserId(Integer userId) {
		List<Account> accountListEntity=new ArrayList<>();
		
		try {
			accountListEntity=accountRepository.findByUserId(userId);
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.INVALID_INPUT, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}
		return accountListEntity;
		
	}

	// 한번에 모든 기능을 생각하는 건 힘들다.
	// 1. 계좌 존재 여부를 확인 -- select (Account)
	// 2. 본인 계좌 여부를 확인 -- select (1의 결과를 토대로 검증)(=객체 상태값에서 비교)
	// 3. 계좌 비밀번호 확인 -- 객체 상태값에서 일치 여부 확인
	// 4. 잔액 여부 확인 -- 객체 상태값에서 확인
	// 5. 출금 처리 -- Update 쿼리 전송
	// 6. hitory에 거래 내역 등록 -- insert(history_tb)
	// 7. Transaction 처리
	@Transactional
	public void updateAccountWithdrawal(WithdrawalDTO dto, Integer principalId) {
		// 1. 계좌 존재 여부를 확인
		Account accountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		if(accountEntity==null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		
		// 2.
		accountEntity.checkOwner(principalId);
		// 3.
		accountEntity.checkPassword(dto.getWAccountPassword());
		// 4. 
		accountEntity.checkBalance(dto.getAmount());
		
		// 5. 출금 처리 
		// accountEntity 객체의 잔액을 변경하고, 업데이트 처리해야 한다.
		accountEntity.withdraw(dto.getAmount());
		accountRepository.updateById(accountEntity);
		
		// 6. history에 거래 내역 등록
		History history=new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(accountEntity.getBalance());
		history.setDBalance(null);
		history.setWAccountId(accountEntity.getId());
		history.setDAccountId(null);
		
		int rowResultCount=historyRepository.insert(history);
		if(rowResultCount!=1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		System.out.println("history 확인 : "+history);
		
		// 7. Transaction 처리
	}

	// 입금 기능 만들기
	@Transactional
	public void updateAccountDeposit(DepositDTO dto, Integer principalId) {
		// 1. 계좌 존재 여부를 확인
		Account accountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
		if(accountEntity==null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		
		// 2. 본인 계좌 여부 확인
		accountEntity.checkOwner(principalId);
		
		// 2. 입금 처리
		accountEntity.deposit(dto.getAmount());
		accountRepository.updateById(accountEntity);
		History history=History.builder()
				.amount(dto.getAmount())
				.wBalance(null)
				.dBalance(accountEntity.getBalance())
				.wAccountId(null)
				.dAccountId(accountEntity.getId())
				.build();
		int rowResultcount= historyRepository.insert(history);

		System.out.println("입금 전 ;"+ accountRepository.findByUserId(principalId));
		
		
		System.out.println(history);
		if(rowResultcount!=1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// 이체 기능 만들기
	// 1. 출금 계좌 존재 여부 확인 -select (객체 리턴 받은 상태)
	// 2. 입금 계좌 존재 여부 확인 -select (객체 리턴 받은 상태)
	// 3. 출금 계좌 본인 소유자 확인 -객체 상태값과 세션 id 비교
	// 4. 출금 계좌 비밀번호 확인 -
	// 5. 출금 계좌 잔액 여부 확인 - 객체 상태값 확인, dto와 비교
	
	// 6. 입금 계좌 객체 상태값 변경 처리(거래 금액 증가처리)
	// 7. 입금 계좌 업데이트 처리
	// 8. 출금 계좌 객체 상태값 변경 처리(거래 금액 감소처리)
	// 9. 출금 계좌 업데이트 처리
	// 10. 거래 내역 등록 처리
	// 11. 트랜잭션 처리
	
	@Transactional
	public void updateAccountTransfer(TransferDTO dto, Integer principalId) {
		// 1. 출금 계좌 존재 여부를 확인
		Account accountEntity = accountRepository.findByNumber(dto.getWAccountNumber());
		if(accountEntity==null) {	
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		
		// 2. 입금 계좌 존재 여부 확인 -select (객체 리턴 받은 상태)
		Account depositAccount=accountRepository.findByNumber(dto.getDAccountNumber());
		if(depositAccount==null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		
		// 3. 출금 계좌 본인 소유자 확인 -객체 상태값과 세션 id 비교
		accountEntity.checkOwner(principalId);
		
		// 4. 출금 계좌 비밀번호 확인
		accountEntity.checkPassword(dto.getPassword());
		
		// 5. 출금 계좌 잔액 여부 확인 - 객체 상태값 확인, dto와 비교
		accountEntity.checkBalance(dto.getAmount());
		
		// 6. 입금 계좌 객체 상태값 변경 처리(거래 금액 증가처리)
		depositAccount.deposit(dto.getAmount());
		accountRepository.updateById(depositAccount);
		
		// 7. 입금 계좌 업데이트 처리
		accountEntity.withdraw(dto.getAmount());
		accountRepository.updateById(accountEntity);
		
		// 8. 거래 내역 등록 처리
		History history=History.builder()
				.amount(dto.getAmount())
				.wBalance(accountEntity.getBalance())
				.dBalance(depositAccount.getBalance())
				.wAccountId(accountEntity.getId())
				.dAccountId(depositAccount.getId())
				.build();
		
		int rowResultcount= historyRepository.insert(history);
		if(rowResultcount!=1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
