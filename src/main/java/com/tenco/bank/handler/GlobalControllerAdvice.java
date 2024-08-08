package com.tenco.bank.handler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.handler.exception.UnAuthorizedException;

@ControllerAdvice  // IoC 대상 (싱글톤 패턴) --> HTML 렌더링 예외에 많이 사용
public class GlobalControllerAdvice {
	
	/**
	 * (개발시에 많이 활용) 
	 * 모든 예외 클래스를 알 수 없기 때문에 로깅으로 확인할 수 있도록 설정  
	 * 로깅처리 - 동기적 방식(System.out.println), @slf4j (비동기 처리 됨) 
	 */
	@ExceptionHandler(Exception.class)
	public void exception(Exception e) {
		System.out.println("----------------------");
		System.out.println(e.getClass().getName());
		System.out.println(e.getMessage());
		System.out.println("----------------------");
	}
	
	/**
	 * Data로 예외를 내려주는 방법 
	 * @ResponseBody 활용 
	 * 브라우저에서 자바스크립트 코드로 동작 하게 됨
	 */
	
	// 예외를 내릴 때 데이터를 내리고 싶다면 1. @RestControllerAdvice 를 사용하면 된다.
	// 단. @ControllerAdvice 사용하고 있다면 @ResponseBody 를 붙여서 사용하면 된다. 
	@ResponseBody
	@ExceptionHandler(DataDeliveryException.class)
	public String dataDeleveryExcption(DataDeliveryException e) {
		StringBuffer sb = new StringBuffer();
		sb.append(" <script>");
		sb.append(" alert('"+ e.getMessage()  +"');");
		sb.append(" window.history.back();");
		sb.append(" </script>");
		return sb.toString(); 
	}
	
	@ResponseBody
	@ExceptionHandler(UnAuthorizedException.class)
	public String unAuthorizedException(UnAuthorizedException e) {
		StringBuffer sb = new StringBuffer();
		sb.append(" <script>");
		sb.append(" alert('"+ e.getMessage()  +"');");
		sb.append(" location.href='/user/sign-in'");
		sb.append(" </script>");
		return sb.toString(); 
	}
	
	/*
	 *  에러 페이지로 이동 처리 
	 *  JSP로 이동시 데이터를 담아서 보내는 방법 
	 *  ModelAndView, Model 사용 가능 
	 *  throw new RedirectException('페이지 없는데???', 404);
	 */
	@ExceptionHandler(RedirectException.class)
	public ModelAndView redirectException(RedirectException e) {
		ModelAndView modelAndView = new ModelAndView("errorPage");
		modelAndView.addObject("statusCode", e.getStatus().value());
		modelAndView.addObject("message", e.getMessage());
		return  modelAndView; // 페이지 반환 + 데이터 내려줌 
	}
	
}
