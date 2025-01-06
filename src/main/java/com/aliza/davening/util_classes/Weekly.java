package com.aliza.davening.util_classes;

import com.aliza.davening.entities.Category;

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
    public Category category;
    public String message = null;
}
