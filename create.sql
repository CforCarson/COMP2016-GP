CREATE TABLE FLIGHTS (
    FLIGHT_NO VARCHAR(5) NOT NULL,
    DEPART_TIME DATE,
    ARRIVE_TIME DATE,
    FARE INT,
    SOURCE VARCHAR(20),
    DEST VARCHAR(20),
    SEAT_LIMIT INT, -- Add seat limit column
    PRIMARY KEY(FLIGHT_NO)
);

CREATE TABLE CUSTOMERS (
    CUSTOMER_ID VARCHAR(10) NOT NULL,
    NAME VARCHAR(50),
    NATIONALITY VARCHAR(50),
    PASSPORT_NUMBER VARCHAR(20),
    PRIMARY KEY(CUSTOMER_ID)
);

CREATE TABLE BOOKINGS (
    BOOKING_ID VARCHAR(15) NOT NULL,
    CUSTOMER_ID VARCHAR(10),
    FLIGHT_NO VARCHAR(5),
    FARE INT,
    FOREIGN KEY(CUSTOMER_ID) REFERENCES CUSTOMERS(CUSTOMER_ID),
    FOREIGN KEY(FLIGHT_NO) REFERENCES FLIGHTS(FLIGHT_NO)
);

COMMIT;

-- Trigger to update available seats on flight booking
CREATE OR REPLACE TRIGGER UPDATE_SEAT_ON_BOOKING
AFTER INSERT ON BOOKINGS
FOR EACH ROW
BEGIN
    UPDATE FLIGHTS
    SET SEAT_LIMIT = SEAT_LIMIT - 1
    WHERE FLIGHT_NO = :NEW.FLIGHT_NO;
END;
/

-- Trigger to update available seats on flight cancellation
CREATE OR REPLACE TRIGGER UPDATE_SEAT_ON_CANCELLATION
AFTER DELETE ON BOOKINGS
FOR EACH ROW
BEGIN
    UPDATE FLIGHTS
    SET SEAT_LIMIT = SEAT_LIMIT + 1
    WHERE FLIGHT_NO = :OLD.FLIGHT_NO;
END;
/

COMMIT;

-- Insert data into FLIGHTS table
INSERT INTO FLIGHTS VALUES('CX100', to_date('15/03/2015 12:00:00', 'dd/mm/yyyy hh24:mi:ss'), to_date('15/03/2015 16:00:00', 'dd/mm/yyyy hh24:mi:ss'), 2000, 'HK', 'Tokyo', 3);
INSERT INTO FLIGHTS VALUES('CX101', to_date('15/03/2015 18:30:00', 'dd/mm/yyyy hh24:mi:ss'), to_date('15/03/2015 23:30:00', 'dd/mm/yyyy hh24:mi:ss'), 4000, 'Tokyo', 'New York', 3);
INSERT INTO FLIGHTS VALUES('CX102', to_date('15/03/2015 10:00:00', 'dd/mm/yyyy hh24:mi:ss'), to_date('15/03/2015 13:00:00', 'dd/mm/yyyy hh24:mi:ss'), 2000, 'HK', 'Beijing', 1);
INSERT INTO FLIGHTS VALUES('CX103', to_date('15/03/2015 15:00:00', 'dd/mm/yyyy hh24:mi:ss'), to_date('15/03/2015 18:00:00', 'dd/mm/yyyy hh24:mi:ss'), 1500, 'Beijing', 'Tokyo', 3);
INSERT INTO FLIGHTS VALUES('CX104', to_date('15/03/2015 10:00:00', 'dd/mm/yyyy hh24:mi:ss'), to_date('15/03/2015 14:00:00', 'dd/mm/yyyy hh24:mi:ss'), 1500, 'New York', 'Beijing', 3);
INSERT INTO FLIGHTS VALUES('CX105', to_date('15/03/2015 04:00:00', 'dd/mm/yyyy hh24:mi:ss'), to_date('15/03/2015 09:00:00', 'dd/mm/yyyy hh24:mi:ss'), 1000, 'HK', 'New York', 2);
INSERT INTO FLIGHTS VALUES('CX106', to_date('15/03/2015 23:40:00', 'dd/mm/yyyy hh24:mi:ss'), to_date('16/03/2015 03:00:00', 'dd/mm/yyyy hh24:mi:ss'), 5000, 'New York', 'LA', 3);
INSERT INTO FLIGHTS VALUES('CX107', to_date('15/03/2015 08:00:00', 'dd/mm/yyyy hh24:mi:ss'), to_date('15/03/2015 11:00:00', 'dd/mm/yyyy hh24:mi:ss'), 1500, 'Beijing', 'Tokyo', 3);
-- INSERT INTO FLIGHTS VALUES('CX109', to_date('15/03/2015 13:00:00', 'dd/mm/yyyy hh24:mi:ss'), to_date('15/03/2015 19:00:00', 'dd/mm/yyyy hh24:mi:ss'), 2000, 'HK', 'Tokyo', 3);

-- Insert data into CUSTOMERS table
INSERT INTO CUSTOMERS VALUES('C001', 'John Doe', 'USA', 'US123456');
INSERT INTO CUSTOMERS VALUES('C002', 'Alice Smith', 'UK', 'UK789012');
INSERT INTO CUSTOMERS VALUES('C003', 'Mohammed Ahmed', 'UAE', 'UAE345678');
INSERT INTO CUSTOMERS VALUES('C01', 'Alice', 'CHN', 'P1234567');
INSERT INTO CUSTOMERS VALUES('C02', 'Bob', 'UK', 'P1111111');
INSERT INTO CUSTOMERS VALUES('C03', 'Cole', 'US', 'P7654321');

COMMIT;

SET AUTOCOMMIT ON