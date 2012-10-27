
CREATE ALIAS IF NOT EXISTS FTL_INIT FOR "org.h2.fulltext.FullTextLucene.init";
CALL FTL_INIT();


CREATE TABLE PEX(
	ID IDENTITY PRIMARY KEY, 
	PEX_ID INT NOT NULL UNIQUE, 
	PEX_CASE_REF_NO NVARCHAR(255), 
	PEX_CAN_PUBLISH_CREDITOR_DATA BOOLEAN, 
	PEX_ENTITLEMENT_TEXT NVARCHAR(255), 
	PEX_ENTITLEMENT_DOCUMENT_REF_NO NVARCHAR(255), 
	PEX_ICUR_ID INT, 
	PEX_AMOUNT_TOTAL DECIMAL(20, 2), 
	PEX_AMOUNT_OPEN DECIMAL(20, 2), 
	PEX_DATE_DUE DATE, 
	PEX_DATE_MODIFIED DATETIME,

	DEBTOR_FIRST_NAME NVARCHAR(255), 
	DEBTOR_NAME NVARCHAR(255), 
	DEBTOR_NIP NVARCHAR(255), 
	DEBTOR_PESEL NVARCHAR(255), 
	DEBTOR_REGON NVARCHAR(255), 
	DEBTOR_STREET NVARCHAR(255), 
	DEBTOR_HOUSE_NUMBER NVARCHAR(255), 
	DEBTOR_FLAT_NUMBER NVARCHAR(255), 
	DEBTOR_ZIP NVARCHAR(255), 
	DEBTOR_IC_ID INT,

	CREDITOR_FIRST_NAME NVARCHAR(255), 
	CREDITOR_NAME NVARCHAR(255), 
	CREDITOR_NIP NVARCHAR(255), 
	CREDITOR_PESEL NVARCHAR(255), 
	CREDITOR_REGON NVARCHAR(255), 
	CREDITOR_STREET NVARCHAR(255), 
	CREDITOR_HOUSE_NUMBER NVARCHAR(255), 
	CREDITOR_FLAT_NUMBER NVARCHAR(255), 
	CREDITOR_ZIP NVARCHAR(255), 
	CREDITOR_IC_ID INT
);

CALL FTL_CREATE_INDEX('PUBLIC', 'PEX', 'DEBTOR_FIRST_NAME,DEBTOR_NAME,DEBTOR_STREET');


INSERT INTO PEX (PEX_ID, DEBTOR_FIRST_NAME) VALUES(1, 'Hello World');




--INSERT INTO TEST VALUES(1, 'Hello World');
--INSERT INTO TEST VALUES(2, 'Hello World1');
--INSERT INTO TEST VALUES(3, 'Dupa');


--CREATE TABLE TEST_TEMP(ID INT PRIMARY KEY, NAME VARCHAR);
--CALL FTL_CREATE_INDEX('PUBLIC', 'TEST_TEMP', NULL);


--DROP TABLE TEST;
--ALTER TABLE TEST_TEMP RENAME TO TEST;
--CALL FTL_REINDEX();

--FTL_CREATE_INDEX(schemaNameString, tableNameString, columnListString)
--FTL_SEARCH(queryString, limitInt, offsetInt): result set
--FTL_REINDEX()
--FTL_DROP_ALL()
--