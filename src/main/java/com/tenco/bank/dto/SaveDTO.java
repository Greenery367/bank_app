package com.tenco.bank.dto;

import com.tenco.bank.repository.model.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SaveDTO {

	private String number;
	private String password;
	private Long balance;
	
	public Account toAccount(Integer principalId) {
		return Account.builder()
				.balance(this.balance)
				.number(this.number)
				.password(this.password)
				.userId(principalId)
				.build();
	}
	
}
