package com.aliza.davening.exceptions;


import java.util.Arrays;
import java.util.List;

//Creating an exception object
public class ApiError {

		
		private String code;
		private List<String> messages;
		
		public ApiError(String code, String... messages) {
			this.code = code;
			this.messages = Arrays.asList(messages);
		}

		public String getCode() {
			return code;
		}
		
		public List<String> getMessages() {
			return messages;
		}

		@Override
		public String toString() {
			return "apiError [code=" + code + ", messages=" + messages + "]";
		}	

}
