# COMP2016-GP
0. Using filezilla, upload the script file to home directory on faith.comp.hkbu.edu.hk

1. Launch SQL*Plus and log in

    @ ./drop.sql;

2. select table_name from user_tables;

3. @ ./create.sql;

4. 
select * from Customers; 
select * from Flights;

SELECT TRIGGER_NAME
FROM USER_TRIGGERS
WHERE TABLE_NAME = 'BOOKINGS';

5. Test functionalities.

a. Flight management in Java program:
i. Show all the information of flights, or select a flight information by flight id:
 CX102
 Flight_no : CX102
	Depart_Time : 2015-03-01 10:00:00.0 
	Arrive_Time : 2015-03-01 13:00:00.0 
	Fare : 2000 
	Seat Limit: 1 
	Source : HK 
	Dest : Beijing
ii. Add a flight by inputting information:
 CX108, 2015/03/15/08:00:00, 2015/03/15/20:00:00, 9000, 3, HK, LA 
 Succeed to add flight CX108
iii. Delete a flight by inputting
 CX108
 Succeed to delete the flight CX108

b. Flight search in Java program:
i. Search by inputting
 HK, Beijing, 2, 20
 Total 2 choice(s):
(1) CX102, fare: 2000
(2) CX105->CX104, fare: 2250
 HK, Tokyo, 2, 5
 Total 1 choice(s):		
(1) CX100, fare: 2000
 HK, Tokyo, 1, 3
 Total 0 choice(s):

c. Flight booking in Java program:
i. Book a flight by inputting customer id and flight no
 C01, CX105, CX104
 Succeed to book a flight for C01, flight id is B1(Use trigger to decrease the seat limit) 
 C01, CX102, CX103
 Succeed to book a flight for C01, flight id is B2 (Use trigger to decrease the seat limit) 
 C02, CX102, CX103

d. Flight cancelling in Java program 
   (for each successful booking, it has a booking id, such as “B1” for the first booking)
i. Cancel a flight by inputting the customer id and booking id
 C01, B1
 Booking B1for customer C01 is cancelled (Use trigger to increase the seat limit) 
 C01, B1
 Booking B1 customer C01 fails to cancel (Use trigger or Java program to check B1 is already cancelled) 
