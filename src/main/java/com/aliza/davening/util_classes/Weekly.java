package com.aliza.davening.util_classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Weekly {
	public String parashaName;
	public long categoryId;
	public String cName;
	public String message = null;
}
