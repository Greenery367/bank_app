package com.tenco.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TransferDTO {
	
	private Long amount; // 출금 금액
	private String wAccountNumber; // 출금 계좌
	private String dAccountNumber; // 이체 계좌
	private String password; // 출금 계좌 비밀 번호

}
