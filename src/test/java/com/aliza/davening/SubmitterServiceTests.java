package com.aliza.davening;

import javax.mail.MessagingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.aliza.davening.entities.Category;
import com.aliza.davening.entities.CategoryType;
import com.aliza.davening.entities.Davenfor;
import com.aliza.davening.entities.Submitter;
import com.aliza.davening.exceptions.EmailException;
import com.aliza.davening.exceptions.EmptyInformationException;
import com.aliza.davening.exceptions.ObjectNotFoundException;
import com.aliza.davening.repositories.AdminRepository;
import com.aliza.davening.repositories.CategoryRepository;
import com.aliza.davening.repositories.DavenforRepository;
import com.aliza.davening.repositories.SubmitterRepository;
import com.aliza.davening.services.EmailSender;
import com.aliza.davening.services.SubmitterService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SubmitterServiceTests{

    @Autowired
    private SubmitterService submitterService;

    @MockBean
    private DavenforRepository davenforRep;
    
    @MockBean
    private CategoryRepository categoryRep;
    
    @MockBean
    private SubmitterRepository submitterRep;
    
    @MockBean
    private AdminRepository adminRep;
    
    @MockBean
    private EmailSender emailSender;
    
    static String submitterEmail = "sub.email@gmail.com";

    @Test
    public void addDavenfor() {
        // Mock repository response
        Mockito.when(submitterRep.findByEmail(submitterEmail))
               .thenReturn(new Submitter(submitterEmail));
        try {
			Mockito.when(emailSender.sendEmail(
					Mockito.anyString(), //subject
					Mockito.anyString(), //text
					Mockito.anyString(), //to, 
					Mockito.any(), 		 //bcc, 
					Mockito.any(), 		 //attachment, 
					Mockito.anyString()))  //attachmentName
				.thenReturn(true);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EmailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Category catShidduchim = new Category(CategoryType.SHIDDUCHIM, false, 40, 2);
        Category catBanim = new Category(CategoryType.BANIM, false, 50, 3);
        Category catRefua = new Category(CategoryType.REFUA, true, 180, 1);
        
        Mockito.when(categoryRep.findByCname(CategoryType.SHIDDUCHIM)).thenReturn(catShidduchim);
        Mockito.when(categoryRep.findByCname(CategoryType.BANIM)).thenReturn(catBanim);
        Mockito.when(categoryRep.findByCname(CategoryType.REFUA)).thenReturn(catRefua);
        
        Davenfor df1 = new Davenfor();
        df1.setSubmitterEmail(submitterEmail);
        df1.setCategory(catShidduchim);
        df1.setNameHebrew("ראובן בן לאה");
        df1.setNameEnglish("Reuven ben Leah");
        
        Davenfor df2 = new Davenfor();
        df2.setSubmitterEmail(submitterEmail);
        df2.setCategory(catBanim);
        df2.setNameHebrew("חיים בן יפה");
        df2.setNameEnglish("Chaim ben Yaffa");
        df2.setNameHebrewSpouse("רבקה בת אסתר");
        df2.setNameEnglishSpouse("Rivka bat Esther");
        
        Davenfor df3 = new Davenfor();
        df3.setSubmitterEmail(submitterEmail);
        df3.setCategory(catRefua);
        df3.setNameHebrew("משה בן מרים");
        df3.setNameEnglish("Moshe ben Miriam");

        try {
            Davenfor readyDf1 = submitterService.addDavenfor(df1, submitterEmail);
            Davenfor readyDf2 = submitterService.addDavenfor(df2, submitterEmail);
            Davenfor readyDf3 = submitterService.addDavenfor(df3, submitterEmail);
            System.out.println(readyDf1);
            System.out.println(readyDf2);
            System.out.println(readyDf3);

		} catch (EmptyInformationException | ObjectNotFoundException | EmailException | MessagingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
}
