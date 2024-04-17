import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.UUID;

import javax.swing.*;

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * This is a flight manager to support: (1) add a flight (2) delete a flight (by
 * flight_no) (3) print flight information (by flight_no) (4) select a flight
 * (by source, dest, stop_no = 0) (5) select a flight (by source, dest, stop_no
 * = 1) (6) book a flight (7) cancel a booking
 */

public class FlightManager {

	Scanner in = null;
	Connection conn = null;
	// Database Host
	final String databaseHost = "orasrv1.comp.hkbu.edu.hk";
	// Database Port
	final int databasePort = 1521;
	// Database name
	final String database = "pdborcl.orasrv1.comp.hkbu.edu.hk";
	final String proxyHost = "faith.comp.hkbu.edu.hk";
	final int proxyPort = 22;
	final String forwardHost = "localhost";
	int forwardPort;
	Session proxySession = null;
	boolean noException = true;

	// JDBC connecting host
	String jdbcHost;
	// JDBC connecting port
	int jdbcPort;

    String[] options = {
            "add a flight", 
            "print flight information (by flight_no)", 
            "delete a flight (by flight_no)",
            "select a flight (by source, dest, stop_no = 0)", 
            "select a flight (by source, dest, stop_no = 1)",
            "search flights",
            "book a flight",
            "cancel a booking",
            "exit" 
    };
	
	boolean getYESorNO(String message) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(message));
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		JDialog dialog = pane.createDialog(null, "Question");
		dialog.setVisible(true);
		boolean result = JOptionPane.YES_OPTION == (int) pane.getValue();
		dialog.dispose();
		return result;
	}

	/**
	 * Get username & password. Do not change this function.
	 */
	String[] getUsernamePassword(String title) {
		JPanel panel = new JPanel();
		final TextField usernameField = new TextField();
		final JPasswordField passwordField = new JPasswordField();
		panel.setLayout(new GridLayout(2, 2));
		panel.add(new JLabel("Username"));
		panel.add(usernameField);
		panel.add(new JLabel("Password"));
		panel.add(passwordField);
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectInitialValue() {
				usernameField.requestFocusInWindow();
			}
		};
		JDialog dialog = pane.createDialog(null, title);
		dialog.setVisible(true);
		dialog.dispose();
		return new String[] { usernameField.getText(), new String(passwordField.getPassword()) };
	}

	/**
	 * Login the proxy. Do not change this function.
	 */
	public boolean loginProxy() {
		if (getYESorNO("Using ssh tunnel or not?")) { // if using ssh tunnel
			String[] namePwd = getUsernamePassword("Login cs lab computer");
			String sshUser = namePwd[0];
			String sshPwd = namePwd[1];
			try {
				proxySession = new JSch().getSession(sshUser, proxyHost, proxyPort);
				proxySession.setPassword(sshPwd);
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				proxySession.setConfig(config);
				proxySession.connect();
				proxySession.setPortForwardingL(forwardHost, 0, databaseHost, databasePort);
				forwardPort = Integer.parseInt(proxySession.getPortForwardingL()[0].split(":")[0]);
			} catch (JSchException e) {
				e.printStackTrace();
				return false;
			}
			jdbcHost = forwardHost;
			jdbcPort = forwardPort;
		} else {
			jdbcHost = databaseHost;
			jdbcPort = databasePort;
		}
		return true;
	}

	/**
	 * Login the oracle system. Change this function under instruction.
	 * 
	 * @return boolean
	 */
	public boolean loginDB() {
		String username = "f0252250";
		String password = "f0252250";
		
		/* Do not change the code below */
		if(username.equalsIgnoreCase("e1234567") || password.equalsIgnoreCase("e1234567")) {
			String[] namePwd = getUsernamePassword("Login sqlplus");
			username = namePwd[0];
			password = namePwd[1];
		}
		String URL = "jdbc:oracle:thin:@" + jdbcHost + ":" + jdbcPort + "/" + database;

		try {
			System.out.println("Logging " + URL + " ...");
			conn = DriverManager.getConnection(URL, username, password);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**                                                                                    
	 * Show the options. If you want to add one more option, put into the
	 * options array above.
	 */
	public void showOptions() {
		System.out.println("Please choose following option:");
		for (int i = 0; i < options.length; ++i) {
			System.out.println("(" + (i + 1) + ") " + options[i]);
		}
	}

	/**
	 * Run the manager
	 */
    public void run() {
        while (noException) {
            showOptions();
            String line = in.nextLine();
            if (line.equalsIgnoreCase("exit"))
                return;
            int choice = -1;
            try {
                choice = Integer.parseInt(line);
            } catch (Exception e) {
                System.out.println("This option is not available");
                continue;
            }
            if (!(choice >= 1 && choice <= options.length)) {
                System.out.println("This option is not available");
                continue;
            }
            switch (options[choice - 1]) {
                case "add a flight":
                    addFlight();
                    break;
                case "delete a flight (by flight_no)":
                    deleteFlight();
                    break;
                case "print flight information (by flight_no)":
                    printFlightByNo();
                    break;
                case "select a flight (by source, dest, stop_no = 0)":
                    selectFlightsInZeroStop();
                    break;
                case "select a flight (by source, dest, stop_no = 1)":
                    selectFlightsInOneStop();
                    break;
                case "search flights":
                    searchFlights();
                    break;
                case "book a flight":
                    bookFlight();
                    break;
                case "cancel a booking":
                    cancelBooking();
                    break;
                case "exit":
                    return;
            }
        }
    }

	/**
	 * Print out the information of a flight given a flight_no
	 */
	private void printFlightInfo(String flight_no) {
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT * FROM FLIGHTS WHERE Flight_no = '" + flight_no + "'";
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return;
			String[] heads = { "Flight_no", "Depart_Time", "Arrive_Time", "Fare", "Source", "Dest" };
			for (int i = 0; i < 6; ++i) { // flight table 6 attributes
				try {
					System.out.println(heads[i] + " : " + rs.getString(i + 1)); // attribute id starts with 1
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}
	}

	/**
	 * List all flights in the database.
	 */
	private void listAllFlights() {
		System.out.println("All flights in the database now:");
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT Flight_no FROM FLIGHTS";
			ResultSet rs = stm.executeQuery(sql);

			int resultCount = 0;
			while (rs.next()) {
				System.out.println(rs.getString(1));
				++resultCount;
			}
			System.out.println("Total " + resultCount + " flight(s).");
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
		}
	}

	/**
	 * Select out a flight according to the flight_no.
	 */
	private void printFlightByNo() {
        System.out.println("----- ----- ----- ------ ------");
		listAllFlights();
		System.out.println("Please input the flight_no to print info:");
		String line = in.nextLine();
		line = line.trim();
		if (line.equalsIgnoreCase("exit"))
			return;

		printFlightInfo(line);
        System.out.println("----- ----- ----- ------ ------");
	}

	/**
	 * Given source and dest, select all the flights can arrive the dest
	 * directly. For example, given HK, Tokyo, you may find HK -> Tokyo Your job
	 * to fill in this function.
	 */
	private void selectFlightsInZeroStop() {
        System.out.println("----- ----- ----- ------ ------");
		System.out.println("Please input source, dest:");

		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;

		String[] values = line.split(",");
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();

		try {
			/**
			 * Create the statement and sql
			 */
			Statement stm = conn.createStatement();

			String sql = "SELECT * FROM FLIGHTS WHERE Source = '" + values[0] + "' AND Dest = '" + values[1] + "'";

			/**
			 * Formulate your own SQL query:
			 *
			 * sql = "...";
			 *
			 */
			System.out.println(sql);

			ResultSet rs = stm.executeQuery(sql);

			int resultCount = 0; // a counter to count the number of result
									// records
			while (rs.next()) { // this is the result record iterator, see the
								// tutorial for details

	            printFlightInfo(rs.getString("Flight_no")); // Print flight information
	            resultCount++;

			}
			System.out.println("Total " + resultCount + " choice(s).");
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
		}
        System.out.println("----- ----- ----- ------ ------");
	}

	/**
	 * Given source and dest, select all the flights can arrive the dest in one
	 * stop. For example, given HK, Tokyo, you may find HK -> Beijing, Beijing
	 * -> Tokyo Your job to fill in this function.
	 */
	private void selectFlightsInOneStop() {
        System.out.println("----- ----- ----- ------ ------");
		System.out.println("Please input source, dest:");

		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;

		String[] values = line.split(",");
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();

	    try {
	        Statement stm = conn.createStatement();

	        String sql = "SELECT * FROM FLIGHTS f1, FLIGHTS f2 \n" +
	                     "WHERE f1.Source = '" + values[0] + "' AND " +
	                     "f1.Dest = f2.Source AND " +
	                     "f2.Dest = '" + values[1] + "'";
	        System.out.println(sql);
	        
	        ResultSet rs = stm.executeQuery(sql);

	        int resultCount = 0; // Counter for the number of result records
	        while (rs.next()) {
	            printFlightInfo(rs.getString("Flight_no")); // Print flight information
	            resultCount++;
	        }
	        System.out.println("Total " + resultCount + " choice(s).");
	        rs.close();
	        stm.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	        noException = false;
	    }
	    
        System.out.println("----- ----- ----- ------ ------");
	}

	/**
	 * Insert data into database
	 */
	private void addFlight() {
		/**
		 * A sample input is: CX109, 2015/03/15/13:00:00, 2015/03/15/19:00:00, 2000, 3, HK, Tokyo
		 * 					  CX111, 2015/03/15/13:00:00, 2015/03/15/19:00:00, 2000, 3, Beijing, Tokyo
		 */
        System.out.println("----- ----- ----- ------ ------");
		System.out.println("Please input the flight_no, depart_time, arrive_time, fare, limit, source, dest:");
		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;
		String[] values = line.split(",");

		if (values.length < 7) {
			System.out.println("The value number is expected to be 7");
			return;
		}
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();

		try {
			Statement stm = conn.createStatement();
			String sql = "INSERT INTO FLIGHTS VALUES(" + "'" + values[0] + "', " + // this
																					// is
																					// flight
																					// no
			"to_date('" + values[1] + "', 'yyyy/mm/dd/hh24:mi:ss'), " + // this
																		// is
																		// depart_time
			"to_date('" + values[2] + "', 'yyyy/mm/dd/hh24:mi:ss'), " + // this
																		// is
																		// arrive_time
			values[3] + ", " + // this is fare
					"'" + values[5] + "', " + // this is source
					"'" + values[6] + "', " + // this is dest
					"'" + values[4] + "'" + // this is amount
					")";
			
//			 System.out.println(sql);
//			 if(true) return;			
			 stm.executeUpdate(sql);
			 stm.close();
			System.out.println("succeed to add flight ");
			printFlightInfo(values[0]);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to add a flight " + line);
			noException = false;
		}
        System.out.println("----- ----- ----- ------ ------");
	}

	/**
	 * Please fill in this function to delete a flight.
	 */
	private void deleteFlight() {
        System.out.println("----- ----- ----- ------ ------");
		listAllFlights();
		System.out.println("Please input the flight_no to delete:");
		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;
		line = line.trim();

		try {
			Statement stm = conn.createStatement();

			String sql = "DELETE FROM FLIGHTS WHERE Flight_no = '" + line + "'";

			stm.executeUpdate(sql);

			stm.close();

			System.out.println("succeed to delete flight " + line);

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to delete flight " + line);
			noException = false;
		}
        System.out.println("----- ----- ----- ------ ------");
	}

	private void searchFlights() {
        System.out.println("----- ----- ----- ------ ------");
	    System.out.println("Please input source, destination, maximum number of connections, and maximum travel hours:");

	    String line = in.nextLine();

	    if (line.equalsIgnoreCase("exit"))
	        return;

	    String[] values = line.split(",");
	    if (values.length != 4) {
	        System.out.println("Invalid input format.");
	        return;
	    }

	    String source = values[0].trim();
	    String destination = values[1].trim();
	    int maxConnections;
	    int maxTravelHours;

	    try {
	        maxConnections = Integer.parseInt(values[2].trim());
	        maxTravelHours = Integer.parseInt(values[3].trim());
	    } catch (NumberFormatException e) {
	        System.out.println("Invalid input for maximum number of connections or maximum travel hours.");
	        return;
	    }

	    searchFlights(source, destination, maxConnections, maxTravelHours);
        System.out.println("----- ----- ----- ------ ------");
	}
	
	/**
	 * SELECT
	 * FROM FLIGHTS f1
	 * JOIN FLIGHTS f2 ON f1.DEST = f2.SOURCE
	 * JOIN FLIGHTS f3 ON f2.DEST = f3.SOURCE
	 * WHERE f1.SOURCE = 'HK' AND f3.DEST = 'LA'
	 * AND f3.ARRIVE_TIME - f1.DEPART_TIME <= 20;
	 **/
	private void searchFlights(String source, String destination, int maxConnections, int maxTravelHours) {
	    	
		try {
	        Statement stm = conn.createStatement();
	        String sql = "SELECT * FROM FLIGHTS f1";

	        // Construct JOIN operations for connections if maxConnections is greater than 0
	        for (int i = 2; i <= maxConnections + 1; i++) {
	            sql += " JOIN FLIGHTS f" + i + " ON f" + (i - 1) + ".DEST = f" + i + ".SOURCE";
	        }

	        // Construct WHERE clause with source and destination
	        sql += " WHERE f1.SOURCE = '" + source + "' AND f" + (maxConnections + 1) + ".DEST = '" + destination + "'";

	        // Add conditions for travel hours if maxTravelHours is specified
	        if (maxTravelHours > 0) {
	            sql += " AND f" + (maxConnections + 1) + ".ARRIVE_TIME - f1.DEPART_TIME <= " + maxTravelHours;
	        }

	        // Execute the SQL query
	        ResultSet rs = stm.executeQuery(sql);

	        int resultCount = 0;
	        while (rs.next()) {
	            // Process and display flight connection information
	            resultCount++;
	            System.out.println("CHOICE " + resultCount + ":");
	            for (int i = 1; i <= maxConnections + 1; i++) {
	                System.out.println("Flight " + i + ": " + rs.getString("FLIGHT_NO"));
	            }
	        }
	        
	        System.out.println("TOTAL " + resultCount + " CHOICE(S)");
	        
	        if (resultCount == 0) {
	            System.out.println("No valid flight connections found.");
	        }

	        rs.close();
	        stm.close();
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        noException = false;
	    }
	}

	
	
    /**
     * Book a flight for a customer
     */
	public void bookFlight() {
        System.out.println("----- ----- ----- ------ ------");
	    System.out.println("Please input customer ID and flight numbers (separated by commas):");

	    String line = in.nextLine();

	    if (line.equalsIgnoreCase("exit"))
	        return;

	    String[] values = line.split(",");
	    if (values.length < 2) {
	        System.out.println("Invalid input format.");
	        return;
	    }

	    String customerId = values[0].trim();
	    String[] flightNos = new String[values.length - 1];
	    for (int i = 1; i < values.length; i++) {
	        flightNos[i - 1] = values[i].trim();
	    }

	    bookFlight(customerId, flightNos);
        System.out.println("----- ----- ----- ------ ------");
	}
	
	private void bookFlight(String customerId, String[] flightNos) {
	    try {
	        conn.setAutoCommit(false); // Start transaction
	        Statement stm = conn.createStatement();

	        // Fetch the highest booking ID and increment it
	        String idQuery = "SELECT MAX(BOOKING_ID) AS max_id FROM BOOKINGS WHERE BOOKING_ID LIKE 'B%'";
	        ResultSet idResult = stm.executeQuery(idQuery);
	        String nextBookingId = "B01"; // Default starting ID
	        if (idResult.next()) {
	            String maxId = idResult.getString("max_id");
	            if (maxId != null) {
	                int idNum = Integer.parseInt(maxId.substring(1)) + 1;
	                nextBookingId = "B" + String.format("%02d", idNum);
	            }
	        }

	        int totalFare = 0;
	        int flightCount = flightNos.length;

	        for (String flightNo : flightNos) {
	            // Check seat availability and get fare
	            String query = "SELECT FARE, SEAT_LIMIT FROM FLIGHTS WHERE FLIGHT_NO = '" + flightNo + "'";
	            ResultSet result = stm.executeQuery(query);
	            if (result.next() && result.getInt("SEAT_LIMIT") > 0) {
	                totalFare += result.getInt("FARE");
	            } else {
	                System.out.println("No available seats or invalid flight number: " + flightNo);
	                conn.rollback(); // Rollback transaction
	                return;
	            }
	        }

	        // Apply discount based on the number of flights
	        if (flightCount == 2) {
	            totalFare *= 0.9; // 90% of the total fare
	        } else if (flightCount == 3) {
	            totalFare *= 0.75; // 75% of the total fare
	        }

	        // Book all flights
	        for (String flightNo : flightNos) {
	            String bookingQuery = "INSERT INTO BOOKINGS VALUES ('" + nextBookingId + "', '" + customerId + "', '" + flightNo + "', " + totalFare + ")";

	            stm.executeUpdate(bookingQuery);
	        }

	        conn.commit(); // Commit transaction
	        System.out.println("Booking successful. Booking ID: " + nextBookingId);
	    } catch (SQLException e) {
	        try {
	            conn.rollback(); // Roll back transaction on exception
	        } catch (SQLException rollbackException) {
	            rollbackException.printStackTrace();
	        }
	        e.printStackTrace();
	    } finally {
	        try {
	            conn.setAutoCommit(true); // Reset auto-commit mode
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}


    /**
     * Cancel a booking
     */

	public void cancelBooking() {
        System.out.println("----- ----- ----- ------ ------");
	    System.out.println("Please input booking ID:");

	    Scanner in = new Scanner(System.in);
	    String bookingId = in.nextLine();

	    if (bookingId.equalsIgnoreCase("exit"))
	        return;

	    cancelBooking(bookingId.trim());
        System.out.println("----- ----- ----- ------ ------");
	}

	private void cancelBooking(String bookingId) {
	    try {
	        conn.setAutoCommit(false); // Start transaction
	        Statement stm = conn.createStatement();

	        // Check if the booking exists and retrieve flight numbers for the booking
	        String checkBookingQuery = "SELECT FLIGHT_NO FROM BOOKINGS WHERE BOOKING_ID = '" + bookingId + "'";
	        ResultSet flightNosResult = stm.executeQuery(checkBookingQuery);

	        if (!flightNosResult.next()) {
	            System.out.println("Booking ID " + bookingId + " does not exist or is already cancelled.");
	            conn.rollback(); // Rollback to avoid partial commit
	            return;
	        }

	        // At this point, we know the booking exists
	        // Delete booking
	        String deleteBookingQuery = "DELETE FROM BOOKINGS WHERE BOOKING_ID = '" + bookingId + "'";
	        int rowsAffected = stm.executeUpdate(deleteBookingQuery);

	        if (rowsAffected > 0) {
	            System.out.println("Booking cancellation successful. Booking ID: " + bookingId);
	            conn.commit(); // Commit transaction only if deletion is successful
	        } else {
	            System.out.println("Failed to cancel booking. Booking ID: " + bookingId);
	            conn.rollback(); // Rollback in case of any unexpected issue
	        }
	    } catch (SQLException e) {
	        try {
	            conn.rollback(); // Rollback transaction on exception
	            System.out.println("Error during cancellation. Transaction rolled back.");
	        } catch (SQLException rollbackException) {
	            rollbackException.printStackTrace();
	        }
	        e.printStackTrace();
	    } finally {
	        try {
	            conn.setAutoCommit(true); // Reset auto-commit mode
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	/**
	 * Close the manager. Do not change this function.
	 */
	public void close() {
		System.out.println("Thanks for using this manager! Bye...");
		try {
			if (conn != null)
				conn.close();
			if (proxySession != null) {
				proxySession.disconnect();
			}
			in.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Constructor of flight manager Do not change this function.
	 */
	public FlightManager() {
		System.out.println("Welcome to use this manager!");
		in = new Scanner(System.in);
	}
	
	/**
	 * Main function
	 */
	public static void main(String[] args) {
		FlightManager manager = new FlightManager();
		if (!manager.loginProxy()) {
			System.out.println("Login proxy failed, please re-examine your username and password!");
			return;
		}
		if (!manager.loginDB()) {
			System.out.println("Login database failed, please re-examine your username and password!");
			return;
		}
		System.out.println("Login succeed!");
		try {
			manager.run();
		} finally {
			manager.close();
		}
	}
}
