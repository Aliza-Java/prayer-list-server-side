use daveningdb;
CREATE TABLE IF NOT EXISTS category (
    id INT PRIMARY KEY,
    english VARCHAR(255) NOT NULL,
    hebrew VARCHAR(255) NOT NULL, 
    isCurrent boolean NOT NULL,
    updateRate INT NOT NULL,
    catOrder INT);

INSERT INTO category (id, cat_order,is_current,update_rate,english,hebrew) VALUES (1,9,0,50,'banim','זרע של קיימא');
INSERT INTO category (id, cat_order,is_current,update_rate,english,hebrew) VALUES (2,777,0,180,'refua','רפואה');
INSERT INTO category (id, cat_order,is_current,update_rate,english,hebrew) VALUES (3,7777,0,180,'soldiers','שמירת חיילים');
INSERT INTO category (id, cat_order,is_current,update_rate,english,hebrew) VALUES (4,77,0,180,'yeshuah','ישועה');
INSERT INTO category (id, cat_order,is_current,update_rate,english,hebrew) VALUES (5,7,1,40,'shidduchim','שידוכים');