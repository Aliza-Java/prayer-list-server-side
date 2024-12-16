package com.aliza.davening;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

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
import com.aliza.davening.services.SubmitterService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SubmitterTests{

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
    
    static String submitterEmail = "sub.email@gmail.com";

    @Test
    public void testGetData() {
        // Mock repository response
        Mockito.when(submitterRep.findByEmail(submitterEmail))
               .thenReturn(new Submitter(submitterEmail));

        Davenfor df1 = new Davenfor();
        df1.setSubmitterEmail(submitterEmail);
        df1.setCategory(categoryRep.findByCname(CategoryType.SHIDDUCHIM.toString()));
        df1.setNameHebrew("ראובן בן לאה");
        df1.setNameEnglish("Reuven ben Leah");
        //Davenfor df2 = new Davenfor(2, submitterEmail, CategoryType.REFUA, "חיים בן יפה","Chaim ben Yafa", null, null, false, null, null, null, null, null);
        //Davenfor df3 = new Davenfor(3, submitterEmail, CategoryType.BANIM, "יצחק בן אברהם", "Yitzchak ben Avraham", "רבקה בת בתואל", "Rivka Bat Betuel", true, null, null, null, null, null);
        

        try {
            submitterService.addDavenfor(df1, submitterEmail);
           //// submitterService.addDavenfor(df2, submitterEmail);
			//submitterService.addDavenfor(df3, submitterEmail);
		} catch (EmptyInformationException | ObjectNotFoundException | EmailException | MessagingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        // Call service method and assert results
        List<Davenfor> submittedDavenfors = new ArrayList<Davenfor>();
		try {
			submittedDavenfors = submitterService.getAllSubmitterDavenfors(submitterEmail);
		} catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Assert.assertEquals(1, submittedDavenfors.size());
    }
}
