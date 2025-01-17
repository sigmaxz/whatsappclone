/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;



/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Messenger

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery


public List<List<String>> executeQueryResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      

      List<List<String>> result = new ArrayList<List<String>>();
      // iterates through the result set and output them to standard out.
      while (rs.next()){
		List<String> record = new ArrayList<String>();
	         for (int i=1; i<=numCol; ++i)
				record.add(rs.getString(i));	
         result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

/*============================================================*/
/*                      MAIN FUNCTION                         *
 *============================================================*/ 

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Messenger.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if
      
      Greeting();
      Messenger esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Messenger (dbname, dbport, user, "");


         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println();
            String mainTitle = "MAIN MENU";
            System.out.println(mainTitle);
            printDashes(mainTitle.length());
            System.out.println();
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            System.out.println();
            String authorisedUser = null;
			aUser au = null;
			int pUpdate = 0;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql);
						if(authorisedUser != null) 
						{
							au = new aUser(esql, authorisedUser);
						}
						break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end main switch

            if (authorisedUser != null) {
            //user menu
                boolean usermenu = true;

                String userTitle = au.login + "'s Menu";
				String userS = "";
				int sint = 0;
	            while(usermenu) {
    				if(au.status != null)
					{
						sint = 1;	
					}
					if(pUpdate == 1)
					{
						au = new aUser(esql, authorisedUser);
						pUpdate = 0;
					} 
	                System.out.println(userTitle);
					if(sint == 1)
					{
						System.out.println(au.status);
					}
                    printDashes(userTitle.length());
                    System.out.println();
                    System.out.println("1. View Notifications");
                    System.out.println("2. View Chats");
                    System.out.println("3. New Message");
                    System.out.println("4. Settings"); 
                    System.out.println("9. Log out");
                    
                    switch(readChoice())
                    {
                        case 1: //viewing notifications
                            /*
                             *   List all notifications of authorized user
                             *   public static void ReadNotifications(Messenger esql, aUser au); (already declared below)
                             */
                            //CHANGE :) delete tab, add \n
                            String subTitle1 = "\nNotifications List\n";
                            System.out.print(subTitle1);
                            printDashes(subTitle1.length());
                            System.out.println();
							ReadNotifications(esql, au);
                            break;
                        case 2: //viewing chats
                             /*  
                             *  List all chats of authorized user
                             */

                            //chat list
                            int chat_pos = 0;
                            boolean viewing_chat_list = true;
                            String subTitle2 = "\tChat List\n\t";
							List<List<String>> chats;
                            while(viewing_chat_list)
                            {
                                System.out.print(subTitle2);
                                printDashes(subTitle2.length());
                                //BEFORE OUTPUTTING OPTIONS, PRINT LIST OF CHATS IN CHRONOLOGICAL ORDER BASED ON UPDATE DATE
								chats = printChats(esql, au);
                                System.out.println("\n\t1. Select a Chat");
                                System.out.println("\t2. New Chat");
                                System.out.print("\t9. Go back to main menu\n\n\t");

                                switch(readChoice())
                                {
                                    case 1: //choose a chat -- list messaging options
										if(chats == null || chats.size() == 0)
										{
											System.out.println("\tYou have no chats\n");
											break;	
										}
                                        
										System.out.print("\n\n\tWhat number is the chat you want?\n\t");
										int cnum = readChoice();
										while(cnum <= 0 || cnum > chats.size())
										{
											System.out.println("\tSorry thats not an option");
											cnum = readChoice();
										}
										cnum = cnum -1; 
                                        int cDepth = 0;
                                        boolean viewing_chat = true;
										String subSubTitle1 = "\n\t\tChat Title";
										System.out.print(subSubTitle1 + "\n\t\t");
                                        printDashes(subSubTitle1.length());
                                        while(viewing_chat)
                                        {
											//set delete interval
											String interval = "DELETE FROM MESSAGE WHERE msg_id NOT IN (select msg_id from NOTIFICATION where MESSAGE.msg_id = NOTIFICATION.msg_id) and destr_timestamp <= now() ";
											esql.executeUpdate(interval);


                                            //TODO: BEFORE OUTPUTTING OPTIONS, PRINT MESSAGES IN CHRONOLOGICAL ORDER BASED ON CREATION DATE
											List<List<String>> messages = cMessage(esql, au, cDepth, chats.get(cnum));
                                            System.out.println("\n\t\t1. Load Earlier Messages");
                                            System.out.println("\t\t2. Load Later Messages");
                                            System.out.println("\t\t3. New Message"); //send notification
                                            System.out.println("\t\t4. Delete Message"); //send notification
                                            System.out.println("\t\t5. Edit Message"); //send notification
                                            System.out.println("\t\t6. Add member to chat");
                                            System.out.println("\t\t7. Delete member from chat");
                                            System.out.println("\t\t8. Delete this chat");
                                            System.out.println("\t\t9. Back to chat list");
                                            //TODO: EACH MESSAGE SHOULD LOOK LIKE THIS
                                            /*-------------------------------------------------------------------
                                             *  Author:                         Creation Date:
                                             *  Text:
                                             *  sdkjfhdsfjs --THIS IS THE BODY OF THE TEXT-- ESLKGJESLKFJS
                                             *-------------------------------------------------------------------
                                             * USE THE printDashes(int numDashes) function to print the dashes
                                             */

                                            switch(readChoice())  
                                            {
                                                                                                                                                  
                                                case 1: //load earlier messages
                                                    //TODO: PRINT 10 EARLIER MESSAGES
                                                    //      IF CANNOT LOAD EARLIER MESSAGES, RETURN ERROR VALUE
                                                    //      THIS WILL NOT AFFECT OTHER USERS' CHAT LIST
													cDepth = loadE(esql, au, cDepth,chats.get(cnum));	
                                                    break;
                                                case 2: //load later messages
                                                    //TODO: PRINT 10 LATER MESSAGES
                                                    //      IF CANNOT LOAD LATER MESSSAGES, RETURN ERROR VALUE
                                                    //      THIS WILL NOT AFFECT OTHER USERS' CHAT LIST
													cDepth = loadL(esql, au, cDepth, chats.get(cnum));
													break;
                                                case 3: //create a new message
                                                    //TODO: INITIALIZE A NEW MESSAGE WITH THE AUTHOR, CREATION DATE, AND ITS TEXT
                                                    //IF MEDIA ATTACHMENT OR URL IS ATTACHED TO MESSAGE, THEN DISPLAY THAT ATTACHEMENT/ URL TOO
                                                    //UPDATE AFFECTS ALL OTHER USERS' CHATS IN THEIR CHAT LIST
                                                    System.out.println("\t\tCreated a new Message\n\n");
                                                    
                                                    ChatNewMessage(esql, au, chats.get(cnum).get(0));

                                                    break;
                                                case 4: //Delete your own message
                                                    //TODO: AUTHORIZED USER CAN ONLY DELETE THEIR OWN MESSAGES
                                                    //      ERROR IF ATTEMPT TO DELETE OTHER USERS' MESSAGES OR NON-EXISTANT MESSAGE
                                                    //      UPDATE AFFECTS ALL OTHER USERS' CHATS IN THEIR CHAT LIST
													System.out.print("\n\n\tWhat number is the message you want?\n\t");
													int mnum = readChoice();
													while(mnum <= 0 || mnum > messages.size())
													{
														System.out.println("\tSorry thats not an option");
														mnum = readChoice();
													}
													mnum = mnum -1;
													DeleteMessage(esql, au, messages.get(mnum));
													//TODO pass in 
                                                    break;
                                                case 5: //edit messages
                                                    //TODO: AUTHORIZED USER CAN ONLY EDIT THEIR OWN MESSAGES
                                                    //      ERROR IF ATTEMPT TO EDIT OTHER USERS' MESSAGES OR NON-EXISTANT MESSAGE
                                                    //      UPDATE AFFECTS ALL OTHER USERS' CHATS IN THEIR CHAT LIST
                                                    //      *OPTIONAL ?*
                                                    //      THIS MESSAGE WILL LOOK MORE SPECIAL. IT WILL PRINT THE ORIGINAL MESSAGE
                                                    //      AND INDENT THE NEWLY EDITED MESSAGE TO SEE THE NEW CHANGE. 
                                                   // System.out.print("\t\tEdited your own message\n\n");

													System.out.print("\n\n\tWhat number is the message you want?\n\t");
													mnum = readChoice();
													while(mnum <= 0 || mnum > messages.size())
													{
														System.out.println("\tSorry thats not an option");
														mnum = readChoice();
													}
													mnum = mnum -1;
                                                    EditMessage(esql, au, messages.get(mnum));

                                                    break;
                                                case 6: //add member/s to a chat
                                                    //TODO: PRINT LIST OF CHATS AND CHOOSE WHICH CHAT TO ADD MEMBER/MEMBERS TO CHAT
                                                    //      UPDATES ALL USERS' CHATS
                                                    //      ERROR IF USER INPUTS NON-EXISTANT USER OR BLOCKED USER OR AUTHORIZED USER IS BLOCKED BY OTHER USERS
													addMC(esql, au, chats.get(cnum));
                                                    break;
                                                case 7: //delete member/s to a chat
                                                    //TODO: PRINT LIST OF CHATS AND CHOOSE WHICH CHAT TO ADD MEMBER/MEMBERS FROM CHAT
                                                    //      UPDATEs ALL USERS' CHATS
                                                    //      ERROR IF USER INPUTS NON-EXISTANT USER
													deleteMC(esql, au, chats.get(cnum));
                                                    break;
                                                case 8: //delete a chat
                                                    String subsubTitle3 = "\t\tDelete a chat";
                                                    System.out.print(subsubTitle3 + "\n\t\t");
                                                    printDashes(subsubTitle3.length());
                                                    System.out.println();
                                                    //: PRINT LIST OF CHATS AND CHOOSE WHICH CHAT TO DELETE
                                                    //      LIST OF CHATS WILL BE DISPLAYED WITH USER OPTIONS (USE A SWITCH STATEMENT)
													cDelete(esql, au,chats.get(cnum)); 
													
                                                case 9: //Go back to main menu
                                                    viewing_chat = false;
                                                    break;
                                                default:
                                                    System.out.println("Unrecognized Choice!");
                                                    break;
                                            }//end viewing_chat switch
                                        }//end viewing_chat while
                                        break;
                                    case 2: //create a new chat
                                        String subsubTitle2 = "\t\tCreate a new chat";
                                        System.out.print(subsubTitle2 + "\n\t\t");
                                        printDashes(subsubTitle2.length());
                                        System.out.println("\n");
										cChat(esql,au);
                                        //TODO: INITIALIZE A NEW CHAT WITH AUTHORIZED USER AS THE INITIAL SENDER
                                        //      ASK THE AUTHORIZED USER TO INPUT MEMBERS OF THIS USER LIST
                                        //      *ERROR* IF USER INPUTS NON-EXISTANT USERS, BLOCKED USER OR AUTHORIZED USER IS BLOCKED BY OTHER USERS
                                        break;
                                    case 9: //Go back to main menu
                                        viewing_chat_list = false;
                                        break;
                                    default:
                                        System.out.println("Unrecognised choice!");
                                        break;
                        
                                } //end viewing_chat_list switch
                            }// end viewing_chat_list while
                            break;
                        case 3: //new message
                            /*
                                TODO:  INITIALIZE A NEW MESSAGE
                                       ASK INPUT FOR MEMBERS
                                       ERROR IF INPUT CONTAINS NON-EXISTANT USERS, BLOCKED USERS, OR AUTHORIZED USER IS BLOCKED
                            */
                            String subTitle3 = "\tNew Message";
                            System.out.println(subTitle3);
                            System.out.print("\t");
                            printDashes(subTitle3.length());
                            System.out.println("\n");
							NewMessage(esql, au);

                            break;
                        case 4: //settings
                            String subTitle4 = "\tSettings";
                            System.out.println(subTitle4 + "\n\t");
							System.out.print("\t");
                            printDashes(subTitle4.length());
                            
                            boolean settings = true;
                            while(settings)
                            {
								System.out.println("\n\t1. Edit Status");
								System.out.println("\t2. manage contacts");
								System.out.println("\t3. delete account");
								System.out.println("\t9. back to main menu");
		
                                switch(readChoice())
                                {
                                    case 1: //edit status
										au.login = eStatus(esql, au);
										pUpdate = 1;
                                        break;
                                    case 2: //manage contacts
											//print menu to console
											boolean manageContacts = true;
											String subTitlex = "\t\tManaging Contacts";
											while(manageContacts)
											{
												System.out.println(subTitlex);
												System.out.print("\t\t");
												printDashes(subTitlex.length());
												System.out.println();
												System.out.println("\t\t1. Add to contact list");
												System.out.println("\t\t2. Browse contact list");
												System.out.println("\t\t3. Delete from contact list");
												System.out.println("\t\t4. Add to block list");
												System.out.println("\t\t5. Delete from block list");
												System.out.println("\t\t6. Browse block list");
												System.out.println("\t\t9. Return to Main Menu");
												//determine user's actions                
												switch(readChoice())
												{
													case 1: 
														AddToContact(esql, au);
														break;
													case 2:
														ListContacts(esql, au);
														break;
													case 3:
														DeleteFromContact(esql, au);
														break;
													case 4:
														AddToBlock(esql, au);
														break;
													case 5:
														DeleteFromBlock(esql, au);
														break;
													case 6:
														ListBlocks(esql, au);
														break;
													case 9:
														manageContacts = false;
														break;
													default:
														System.out.println("Unrecognized choice!");
														break;
													} //end manageContacts switch
												} //end manageContacts while
											break;
                                    case 3: //delete profile
										if(dProfile(esql, au) == 1)
										{
											settings = false;
											usermenu = false;
										}
                                        break;

                                    case 9: //log out
                                        settings = false;
                                        break;
                                    default:
                                        System.out.println("\t\tUnrecognized choice!");
                                }//end settings switch
                            } //end settings while
                            break;
                        case 9:
                            usermenu = false;
                            break;
                        default: //invalid options
                            System.out.println("Unrecognized choice!");
                            break;

                    }//end usermenu switch
                }//end usermenu while
            }//end if authorised user != NULL
         }//end main while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

           

/*============================================================*/
/*              START OF FUNCTION DEFINITIONS                 *
 *============================================================*/ 
   public static void printDashes(int size)
   {
        for(int i = 0; i < size; ++i)
        {
            System.out.print("-");
        }
   }//end printDashes

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /* functionCall: readChoice()
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /* functionCall: CreateUser(Messenger esql)
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

         //Creating empty contact\block lists for a user
         esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
         int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
             esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
         int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");
             
         String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list) VALUES ('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);
             esql.executeUpdate(query);
             System.out.println ("User successfully created!");

      } catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser
   
   /* functionCall: String LogIn(Messenger esql)
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         
     //check if login information is in the database
         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
	        return login;
         else
            System.out.println("Error: " + login + " does not exist or incorrect login/password!");
            return null;
         } catch(Exception e){
             System.err.println (e.getMessage ());
             return null;
         }
   }//end LogIn

   /* functionCall: AddToContact(Messenger esql, aUser aUser au)
    * Description: Authorised user can add an existing user to their contact list.
    * @return: void
    **/
   public static void AddToContact(Messenger esql, aUser au){

        try{
          // get new contact login
             System.out.print("\tEnter user login to add: ");
             String logintoadd = in.readLine();

          // check if new contact is not yourself
            if(logintoadd.equals(au.login))
            {
                System.out.println("\tYou cannot add yourself as a contact\n");
                return;
            }
        //check if new contact exists in contact list
            String query1 = String.format("Select USR.login  From USR Where login = '%s'" , logintoadd);
            int numR = esql.executeQuery(query1);
            if( numR == 0)
            {
                System.out.println("\tUser does not exist\n");
                return;
            }
        /*check if there is a relation (if new contact is already in contact list) */
            String query = String.format("Select * from USER_LIST_CONTAINS where  list_member = '%s' AND list_id = '%s'" ,logintoadd,  au.contact_list);
            int contact_int = esql.executeQuery(query);
            //  check for empty list
            if( contact_int != 0)
            {
                System.out.println("\tyou are already pals\n");
                return;
            }
            else  /* Check if the new contact is in the block list. If so, confirm deletion. */
            {
                String query3 = String.format("select * from USER_LIST_CONTAINS where list_member ='%s' and list_id = '%s' ",logintoadd, au.block_list );
                int numR2 = esql.executeQuery(query3);
                if( numR2 == 1)
                {
                    System.out.println("\t" + logintoadd + " is on your block list");
                    System.out.println("\tThey will be deleted from block list if you do add");
                    System.out.println("\tare you sure? (y/n)");
                    String ans = in.readLine();
                    while(!ans.equals("n") && !ans.equals("y"))
                    {
                        System.out.println("\tError: invalid answer. (y/n) ? ");
                        ans = in.readLine();
                    }
                    //if no, don't delete from block list and return.	
                    if( ans.equals("n"))
                    {
                        return;
                    }
                    else{   //delete from block list
                        String update = String.format("Delete from USER_LIST_CONTAINS where list_member = '%s' and list_id = '%s'",logintoadd, au.block_list);
                        esql.executeUpdate(update);
                        System.out.println("\t" + logintoadd + " is now deleted from block list");
                    }
                }

                //Add new contact to contact list
                String query2 = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES( '%s', '%s')", au.contact_list, logintoadd);
                esql.executeUpdate(query2);
                
                System.out.println("\tYou are now friends with " + logintoadd + "\n");
                
            }
        } catch( Exception e){
            System.err.println (e.getMessage() );
            return;
        }
   }//end AddToContact

   /* functionCall: ListContacts(Messenger esql, aUser au)
    * Allow the User to see their list of contacts
    * @return void
    **/

   public static void ListContacts(Messenger esql, aUser au ){

        try{
        //select all contacts that is in both USER and USER_LIST_CONTAINS
            String query = String.format("select u.list_member , USR.status  from USER_LIST_CONTAINS u, USR where u.list_id = '%s' and USR.login = u.list_member  " , au.contact_list);
            List< List<String>> contact_members = esql.executeQueryResult(query);
            if( contact_members.size()  <= 0)
            {
                System.out.println("\ncontact list is empty\n");
                return;
            }
        //print all user logins followed by their status message.
        //numDashes based on length of the contact's title size.
            String contact_title = au.login + "'s Contact List";
            System.out.println(contact_title);
            printDashes(contact_title.length());
            System.out.println();
            //these are for making sure that I do not keep recalling function calls.
            int contact_size = contact_members.size();
            int dashCalls = contact_size - 1;
            int contact_title_size = contact_title.length();

            for( int i = 0 ; i < contact_size; i++)
            {
                System.out.println(contact_members.get(i).get(0));
                if(contact_members.get(i).get(1) != null)
                {
                    String status = "Status: " + contact_members.get(i).get(1);

                    System.out.println(status);
                }
                if(i != dashCalls) //makes sure not to print the last dashed line
                {
                    printDashes(contact_title_size);
                    System.out.println();
                }
            }
            printDashes(contact_title_size);
            System.out.println();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
   }//end ListContacts

   public static void NewMessage(Messenger esql, aUser au){
        try{
			System.out.println("Enter The names of whom you wish to message(enter empty when done)");
			ArrayList<String> reciv = new ArrayList<String>();
			reciv.add(au.login);
			int rtotal = 0;
			String r = in.readLine();
			while(!r.equals(""))
			{
				//check if usr exists
				String ue = String.format("select * from usr where login = '%s'", r);
				int uc = esql.executeQuery(ue);
				if(uc == 0)
				{
					System.out.println("User does not exist");
				}
				else
				{
					// check if blocked
					String cb = String.format("select * from USER_LIST_CONTAINS where list_member ='%s' and list_id = '%s' ", r, au.block_list);
					int cc = esql.executeQuery(cb);
					if(cc == 1)
					{
						System.out.println("He is blocked");
					}
					else
					{
						reciv.add(r);
						rtotal = rtotal +1;
					}
					
				}
				r = in.readLine();
			}
			if(rtotal == 0)
			{
				System.out.println("Nobody to message back to menu");
				return;	
			}

			//query for an existing chat
			String ec = String.format("select chat_id from (select * from CHAT_LIST where member = '%s') as c group by c.chat_id having count(*) = '%s' ",au.login, rtotal );
			List<List<String>> possible = esql.executeQueryResult(ec);
			// check if any of the possbile have all the pl of the message as recipients
			String hit = "";
			for(int i = 0; i < possible.size() && hit.equals(""); i++)
			{
				int count = 1;
				for(int j = 0; j <reciv.size() && count == 1; j++)
				{
					//query to see they exist in chat_list
					String ecl = String.format("select * from CHAT_LIST where member = '%s' and chat_id = '%s'" , reciv.get(j), possible.get(i).get(0));
					int ecn = esql.executeQuery(ecl);
					if(ecn == 0)
					{
						count = 0;
					}
				}
				if(count == 1)
				{
					hit = possible.get(i).get(0);
				}
			}
			// if no hit is found / no existing chat
			if(hit.equals(""))
			{
				String type = "private";
				if(reciv.size() > 2)
				{
					type = "group";
				}
				// didn't find chat make new chat
				System.out.println("Making new chat ");
				String eu = String.format("insert into CHAT(chat_type, init_sender) Values('%s', '%s')", type, au.login); 
        		esql.executeUpdate(eu);
         		int chat_id = esql.getCurrSeqVal("chat_chat_id_seq");
				System.out.println("chat made");

	
				// add all
				for(int x = 0; x < reciv.size(); x++)
				{
					String adda = String.format("insert into CHAT_LIST(chat_id,member) values('%s', '%s')", chat_id, reciv.get(x));
					esql.executeUpdate(adda);
					System.out.println("new chat_list made");
				}
				hit = String.valueOf(chat_id);
			}
			// here on hit is chat_id
			
				//get message
			System.out.println("Type your message");
			String msg = in.readLine();
			while(msg.equals(""))
			{
				System.out.println("must have a msg");
				msg = in.readLine();
			}

			//assume have msg now
			//add media attchment
			ArrayList<String> arr = new ArrayList<String>();
			System.out.println("Do you want media attchments? (y/n)");
			String ans3 = in.readLine();
			while(!ans3.equals("y") && !ans3.equals("n"))
			{
				System.out.println("This is (y/n) only");
				ans3 = in.readLine();
			}
			while(ans3.equals("y"))
			{
				//add them
				System.out.println("attatchment type? :");
				String atype = in.readLine();
				while(atype.equals(""))
				{
					System.out.println(" empty is not valid");
					atype = in.readLine();
				}
				System.out.println("URL? :");
				String url = in.readLine();
				while(url.equals(""))
				{
					System.out.println(" empty is not valid");
					url = in.readLine();
				}
				//insert into arr
				arr.add(atype);
				arr.add(url);
				
				System.out.println("Do you want more media attchments? (y/n)");
				ans3 = in.readLine();
				while(!ans3.equals("y") && !ans3.equals("n"))
				{
					System.out.println("This is (y/n) only");
					ans3 = in.readLine();
				}
		
			}

			// get if they want destr_timestamp
			System.out.println("Do you want destruction time? (y/n)");
			String ans2 = in.readLine();
			while(!ans2.equals("y") && !ans2.equals("n"))
			{
				System.out.println("This is (y/n) only");
				ans2 = in.readLine();
			}
			Timestamp tsd = null;

			if(ans2.equals("y"))
			{
				System.out.println("How many milliseconds? (1hr:3,600,000) (min:60,000) (seconds:1000) :");
				long mil = readChoice();
				if( mil < 100)
				{
					System.out.println("minimum is 100 milliseconds");
					mil = readChoice();
				}
				tsd = new Timestamp(mil);
			}
			//got destr if needed
			Date date = new Date();
			Timestamp ts = new Timestamp(date.getTime());

			//make message
			System.out.println("Making Message ");
			if(tsd == null)
			{
				// TODO Temp fix need to come back 
				tsd = new Timestamp((long) 111111111); 
			}
				tsd = new Timestamp(ts.getTime() + tsd.getTime());

			String mm = String.format("insert into MESSAGE( msg_text, msg_timestamp, destr_timestamp,sender_login,chat_id) Values( '%s', '%s', '%s', '%s', '%s')", msg, ts, tsd, au.login, hit); 
       		esql.executeUpdate(mm);
			int m_id = esql.getCurrSeqVal("message_msg_id_seq");


			System.out.println("Message is made");

			//insert into media attachment table 
			for(int y = 0; y < arr.size(); y = y+ 2)
			{
				String aUpdate = String.format("insert into MEDIA_ATTACHMENT(media_type, URL, msg_id) values('%s', '%s', '%s');",arr.get(y), arr.get(y+1), m_id);
				esql.executeUpdate(aUpdate);
			}
			if(arr.size() >= 2)
			{
				System.out.println("Attachments are now added");
			}

			System.out.println("here");
			//notify
			// add all

			reciv.remove(au.login);
			for(int x = 0; x < reciv.size(); x++)
			{
				String adda = String.format("insert into NOTIFICATION(usr_login, msg_id) values('%s', '%s')",  reciv.get(x), m_id);
				esql.executeUpdate(adda);
				System.out.println("new chat_list made");
			}


			return;

			
			
	    }catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
   }//end NewMessage

public static void ChatNewMessage(Messenger esql, aUser au, String chat_id)
{
    try{
        //Find chat_id in list of chats with all the members to create a message
        String query_chat_members = String.format("SELECT member FROM CHAT_LIST WHERE chat_id = '%s'", chat_id);
        List<List<String>> members = esql.executeQueryResult(query_chat_members);
   
        // here on hit is chat_id
       
            //get message
        System.out.println("Type your message");
        String msg = in.readLine();
        while(msg.equals(""))
        {
            System.out.println("must have a msg");
            msg = in.readLine();
        }

        //assume have msg now
        //add media attchment
        ArrayList<String> arr = new ArrayList<String>();
        System.out.println("Do you want media attchments? (y/n)");
        String ans3 = in.readLine();
        while(!ans3.equals("y") && !ans3.equals("n"))
        {
            System.out.println("This is (y/n) only");
            ans3 = in.readLine();
        }
        while(ans3.equals("y"))
        {
            //add them
            System.out.println("attatchment type? :");
            String atype = in.readLine();
            while(atype.equals(""))
            {
                System.out.println(" empty is not valid");
                atype = in.readLine();
            }
            System.out.println("URL? :");
            String url = in.readLine();
            while(url.equals(""))
            {
                System.out.println(" empty is not valid");
                url = in.readLine();
            }
            //insert into arr
            arr.add(atype);
            arr.add(url);
           
            System.out.println("Do you want more media attchments? (y/n)");
            ans3 = in.readLine();
            while(!ans3.equals("y") && !ans3.equals("n"))
            {
                System.out.println("This is (y/n) only");
                ans3 = in.readLine();
            }
   
        }

        // get if they want destr_timestamp
        System.out.println("Do you want destruction time? (y/n)");
        String ans2 = in.readLine();
        while(!ans2.equals("y") && !ans2.equals("n"))
        {
            System.out.println("This is (y/n) only");
            ans2 = in.readLine();
        }
        Timestamp tsd = null;

        if(ans2.equals("y"))
        {
            System.out.println("How many milliseconds? (1hr:3,600,000) (min:60,000) (seconds:1000) :");
            long mil = readChoice();
            if( mil < 100)
            {
                System.out.println("minimum is 100 milliseconds");
                mil = readChoice();
            }
            tsd = new Timestamp(mil);
        }
        //got destr if needed
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());

        //make message
        System.out.println("Making Message ");
        if(tsd == null)
        {
            // TODO Temp fix need to come back
            tsd = new Timestamp((long) 111111111);
        }
            tsd = new Timestamp(ts.getTime() + tsd.getTime());

        String mm = String.format("insert into Message( msg_text, msg_timestamp, destr_timestamp,sender_login,chat_id) Values( '%s', '%s', '%s', '%s', %s)", msg, ts, tsd, au.login, chat_id);
           esql.executeUpdate(mm);
        int m_id = esql.getCurrSeqVal("message_msg_id_seq");


        System.out.println("Message is made");

        //insert into media attachment table
        for(int y = 0; y < arr.size(); y = y+ 2)
        {
            String aUpdate = String.format("insert into MEDIA_ATTACHMENT(media_type, URL, msg_id) values('%s', '%s', '%s');",arr.get(y), arr.get(y+1), m_id);
            esql.executeUpdate(aUpdate);
        }
        if(arr.size() >= 2)
        {
            System.out.println("Attachments are now added");
        }
       
        //notify all
        for(int x = 0; x < members.get(0).size(); x++)
        {
            String adda = String.format("INSERT INTO NOTIFICATION(usr_login, msg_id) values('%s', '%s')",  members.get(0).get(x), m_id);
            esql.executeUpdate(adda);
        }
            return;
       
    }catch (Exception e)
    {
        System.err.println(e.getMessage());
    }
}

public static void EditMessage(Messenger esql, aUser au, List<String> message)

{
    try{
        String m_id = message.get(0);
        System.out.println("MESSAGE ID: " + m_id);
        //query for the author of message and the au
        String query = String.format("SELECT sender_login FROM MESSAGE WHERE msg_id = '%s' AND sender_login = '%s'", m_id, au.login);   
        int rows = esql.executeQuery(query);
        if(rows == 0)
        {
            System.out.println("Error: Message doesn't exist or does not belong to the authorized user!");
            return;
        }
        else
        {
                //edit the text field of a message
                System.out.println("Text: " );
                String input = in.readLine();
                String update = String.format("UPDATE MESSAGE SET msg_text = '%s' WHERE msg_id = '%s' AND sender_login = '%s'", input, m_id, au.login);
                esql.executeUpdate(update);
               
                //check if original message has any attachments
                
                String query1 = String.format("SELECT * FROM MEDIA_ATTACHMENT WHERE msg_id = '%s'", m_id);
                List<List<String>> media_ids = esql.executeQueryResult(query1);
                System.out.println("MEDIA_IDS: " + media_ids.size());
                int rows1 = media_ids.size();
                String ans;
                if(rows1 == 0)
                {
                    System.out.print("Do you want to add attachments? (y/n): ");
                    ans = in.readLine();
                    while(!ans.equals("y") && !ans.equals("n"))
                    {
                        System.out.print("Error: Invalid response. (y/n): ");
                        ans = in.readLine();
                    }
                    if(ans.equals("y"))
                    {
                        boolean exit = false;
                        while(!exit)
                        {
                            System.out.print("Enter the media type: ");
                            String get_type = in.readLine();
                            System.out.print("\nEnter the URL: ");
                            String get_URL = in.readLine();
                            String update1 = String.format("INSERT INTO MEDIA_ATTACHMENT(media_type, URL, msg_id) VALUES('%s', '%s', '%s');", get_type, get_URL, m_id);
                            esql.executeUpdate(update1);
                           
                            System.out.print("Do you want to continue to add more attachments? (y/n): ");
                            ans = in.readLine();
                            while(!ans.equals("y") && !ans.equals("n"))
                            {
                                System.out.print("Error: Invalid response. (y/n): ");
                                ans = in.readLine();
                            }
                            if(ans.equals("n"))
                            {
                                exit = true;
                            }
                        }
                    }
                    System.out.println("\t\tYou have edited a messaged!\n");
                }
                else
                {
                    // If there are already attchments you must make them choose and use the media_id to update
                    System.out.println("There are existing attachments to the message.");
                    System.out.println("Do you want to modify them? (y/n)");
                    ans = in.readLine();
                    while(!ans.equals("y") && !ans.equals("n"))
                    {
                        System.out.print("Error: Invalid response. (y/n): ");
                        ans = in.readLine();
                    }
                    System.out.println("Which media attachments do you want to modify?");
                    for(int i = 0; i < rows1; ++i)
                    {  
                        int temp = i + 1;
                        System.out.print(temp + ") ");
                        System.out.println("\t\t" + media_ids.get(i).get(0));
                    }
                   
                    ArrayList<String> media_to_modify = new ArrayList<String>();
                    ArrayList<List<String>> modify_media = new ArrayList<List<String>>();
                    System.out.println("Choose all media_ids that you want to modify (press '0' when finished)");
                    boolean exit = false;
                    int choice = -1;
                    while(!exit)
                    {
                        choice = readChoice();
                        if(choice == 0)
                        {
                            exit = true;
                        }
                        else{
                            System.out.print("Enter a media type: ");
                            String type = in.readLine();
                            System.out.print("\nEnter a URL: ");
                            String URL = in.readLine();
                            String update2 = String.format("UPDATE MEDIA_ATTACHMENT SET media_type = '%s', URL = '%s', msg_id = '%s' WHERE media_id = '%s'", type, URL, media_ids.get(choice -1).get(3), media_ids.get(choice - 1).get(0));
                            esql.executeUpdate(update2);
                        }
                    }
                }
        }
    } catch(Exception e){
        System.err.println(e.getMessage());
    }
}

 public static int dProfile(Messenger esql, aUser au){
        try{
			//check if there is linked info
			String link = String.format("select * from CHAT where init_sender ='%s'", au.login);
			int count = esql.executeQuery(link);
			if(count != 0)
			{
				System.out.println("You are curretly initail sender of chats you cannot delete");
				return 0;
			}
			String mlink = String.format("select * from Message where sender_login = '%s'", au.login);
			count = esql.executeQuery(mlink);
			if(count != 0)
			{
				System.out.println("You still have published content ie:messages cannot delete");
				return 0;
			}
			System.out.println("Enter password :");
			String pass = in.readLine();
			while(pass.equals(""))
			{
				System.out.println("need a password");
				pass = in.readLine();
			}
			String temp = au.password;
			String p1 = temp.substring(0, temp.indexOf(' '));
			if(!pass.equals(p1))
			{
				System.out.println("Wrong password cannot delete");
				return 0;
			}
			System.out.println("Goodbye please try us again");
			String update = String.format("delete from USR where login = '%s'", au.login);
			esql.executeUpdate(update);
			
	    }catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
		return 1;
   }//end temp


 public static String eStatus(Messenger esql, aUser au){
        String msg = "";
		try{
			//acquire status
			System.out.println("Type your status: ");
			msg = in.readLine();
			while(msg.equals(""))
			{
				System.out.println("must have a status");
				msg = in.readLine();
			}

			// insert
			String update = String.format("update USR set status = '%s' where login = '%s'", msg, au.login);
			esql.executeUpdate(update);
			System.out.println("status changed");

			
	    }catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
		return msg;
	
   }//end temp

 public static void cChat(Messenger esql, aUser au){
        try{
			System.out.println("Enter The names of whom you wish to message(enter empty when done)");
			ArrayList<String> reciv = new ArrayList<String>();
			reciv.add(au.login);
			int rtotal = 0;
			String r = in.readLine();
			while(!r.equals(""))
			{
				//check if usr exists
				String ue = String.format("select * from usr where login = '%s'", r);
				int uc = esql.executeQuery(ue);
				if(uc == 0)
				{
					System.out.println("User does not exist");
				}
				else
				{
					// check if blocked
					String cb = String.format("select * from USER_LIST_CONTAINS where list_member ='%s' and list_id = '%s' ", r, au.block_list);
					int cc = esql.executeQuery(cb);
					if(cc == 1)
					{
						System.out.println("He is blocked");
					}
					else
					{
						reciv.add(r);
						rtotal = rtotal +1;
					}
					
				}
				r = in.readLine();
			}
			if(rtotal == 0)
			{
				System.out.println("Nobody to message back to menu");
				return;	
			}
			//query for an existing chat
			String ec = String.format("select chat_id from (select * from CHAT_LIST where member = '%s') as c group by c.chat_id having count(*) = '%s' ",au.login, rtotal );
			List<List<String>> possible = esql.executeQueryResult(ec);
			// check if any of the possbile have all the pl of the message as recipients
			String hit = "";
			for(int i = 0; i < possible.size() && hit.equals(""); i++)
			{
				int count = 1;
				for(int j = 0; j <reciv.size() && count == 1; j++)
				{
					//query to see they exist in chat_list
					String ecl = String.format("select * from CHAT_LIST where member = '%s' and chat_id = '%s'" , reciv.get(j), possible.get(i).get(0));
					int ecn = esql.executeQuery(ecl);
					if(ecn == 0)
					{
						count = 0;
					}
				}
				if(count == 1)
				{
					hit = possible.get(i).get(0);
				}
			}
			// if no hit is found / no existing chat
			if(hit.equals(""))
			{
				String type = "private";
				if(reciv.size() > 2)
				{
					type = "group";
				}
				// didn't find chat make new chat
				System.out.println("Making new chat ");
				String eu = String.format("insert into CHAT(chat_type, init_sender) Values('%s', '%s')", type, au.login); 
        		esql.executeUpdate(eu);
         		int chat_id = esql.getCurrSeqVal("chat_chat_id_seq");
				System.out.println("chat made");

	
				// add all
				for(int x = 0; x < reciv.size(); x++)
				{
					String adda = String.format("insert into CHAT_LIST(chat_id,member) values('%s', '%s')", chat_id, reciv.get(x));
					esql.executeUpdate(adda);
				}
				hit = String.format("%s",chat_id);
			}
			else{
					System.out.println("Chat with these members already exist");
					return;
			}
			//get message
			System.out.println("Type your message");
			String msg = in.readLine();
			while(msg.equals(""))
			{
				System.out.println("must have a msg");
				msg = in.readLine();
			}

			//assume have msg now

			//add media attchment
			ArrayList<String> arr = new ArrayList<String>();
			System.out.println("Do you want media attchments? (y/n)");
			String ans3 = in.readLine();
			while(!ans3.equals("y") && !ans3.equals("n"))
			{
				System.out.println("This is (y/n) only");
				ans3 = in.readLine();
			}
			while(ans3.equals("y"))
			{
				//add them
				System.out.println("attatchment type? :");
				String atype = in.readLine();
				while(atype.equals(""))
				{
					System.out.println(" empty is not valid");
					atype = in.readLine();
				}
				System.out.println("URL? :");
				String url = in.readLine();
				while(url.equals(""))
				{
					System.out.println(" empty is not valid");
					url = in.readLine();
				}
				//insert into arr
				arr.add(atype);
				arr.add(url);
				
				System.out.println("Do you want more media attchments? (y/n)");
				ans3 = in.readLine();
				while(!ans3.equals("y") && !ans3.equals("n"))
				{
					System.out.println("This is (y/n) only");
					ans3 = in.readLine();
				}
		
			}

			// get if they want destr_timestamp
			System.out.println("Do you want destruction time? (y/n)");
			String ans2 = in.readLine();
			while(!ans2.equals("y") && !ans2.equals("n"))
			{
				System.out.println("This is (y/n) only");
				ans2 = in.readLine();
			}
			Timestamp tsd = null;

			if(ans2.equals('y'))
			{
				System.out.println("How many milliseconds? (1hr:3,600,000) (min:60,000) (seconds:1000) :");
				long mil = readChoice();
				if( mil < 100)
				{
					System.out.println("minimum is 100 milliseconds");
					mil = readChoice();
				}
				tsd = new Timestamp(mil);
			}
			//got destr if needed
			Date date = new Date();
			Timestamp ts = new Timestamp(date.getTime());

			//make message
			System.out.println("Making Message ");
			if(tsd == null)
			{
				// TODO Temp fix need to come back 
				tsd = new Timestamp((long) 11111111); 
			}
				tsd = new Timestamp(ts.getTime() + tsd.getTime());
			String mm = String.format("insert into Message( msg_text, msg_timestamp, destr_timestamp,sender_login,chat_id) Values( '%s', '%s', '%s', '%s', %s);", msg, ts, tsd, au.login, hit);
			System.out.println(mm); 
       		esql.executeUpdate(mm);
			int m_id = esql.getCurrSeqVal("message_msg_id_seq");


			System.out.println("Message is made");

			//insert into media attachment table 
			for(int y = 0; y < arr.size(); y = y+ 2)
			{
				String aUpdate = String.format("insert into MEDIA_ATTACHMENT(media_type, URL, msg_id) values('%s', '%s', '%s');",arr.get(y), arr.get(y+1), m_id);
				esql.executeUpdate(aUpdate);
			}
			if(arr.size() >= 2)
			{
				System.out.println("Attachments are now added");
			}
			
			//notify
			// add all
				for(int x = 0; x < reciv.size(); x++)
				{
					String adda = String.format("insert into NOTIFICATION(usr_login, msg_id) values('%s', '%s')", m_id, reciv.get(x));
					esql.executeUpdate(adda);
				}


			return;


			



			
	    }catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
   }//end temp



public static void DeleteMessage(Messenger esql, aUser au, List<String> message)
    {
        try{
            String m_id = message.get(0);
            //query for the author of message and the authorised user
            String query = String.format("SELECT sender_login FROM MESSAGE WHERE msg_id = '%s' and sender_login = '%s'", m_id, au.login);
            int rows = esql.executeQuery(query);
            if(rows == 0)
            {
                System.out.println("Error: Message doesn't exist or does not belong to authorized user!");
                return;
            }
            else
            {
                String update = String.format("DELETE FROM MESSAGE WHERE msg_id = '%s' AND sender_login = '%s'",m_id, au.login);
                esql.executeUpdate(update);
                System.out.println("\t\tYou have deleted a message!\n");
                //return;
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }

    }

/*
public static int NewMessageChat(Messenger esql, aUser au)
{
    try{

        System.out.println("Enter the names of whom you wish to message(enter empty when done)\n");
        ArrayList<String> reciv = new ArrayList<String>();
        reciv.add(au.login);
        int rtotal = 0;
        String r = in.readLine();
        while(!r.equals(""))
        {
            //check if usr exists
            String ue = String.format("SELECT * FROM USR WHERE login = '%s'", r);
            int uc = esql.executeQuery(ue);
            if(uc == 0)
            {
                System.out.println("user does not exist");
            }
            else
            {
                //check if user is blocked
                String cb = String.format("SELECT * FROM USER_LIST_CONTAINS WHERE list_member ='%s' AND list_id = '%s'", r, au.block_list);
                int cc = esql.executeQuery(cb);
                if(cc == 1)
                {
                    System.out.println("He is blocked");
                }
                else
                {
                    reciv.add(r);
                    rtotal = rtotal + 1;
                }
            }
        }
        
    //return -1;
        
    } catch(Exception e) {
        System.err.println(e.getMessage());
    }
}
*/
   public static int loadL(Messenger esql, aUser au, int depth, List<String> chat){
        try{
			int offset = (depth+1) * 10;
			String tenM = String.format("select * from MESSAGE where chat_id = '%s' order by msg_timestamp DESC Limit 10 offset '%s' ", chat.get(0), offset);
			int snum = esql.executeQuery(tenM);
			if( snum > 0)
			{
				System.out.println("Going to next 10");
				return depth +1;
			}
	    }catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
		System.out.println("There are no more messages");
		return depth;
   }//end loadL


   public static int loadE(Messenger esql, aUser au, int depth, List<String> chat){
        try{
			if( depth <= 0)
			{
				System.out.println("There is no earlier messages");
				return depth;
			}
            else{
			    System.out.println("Going to previous 10");
    			return depth -1; //originall depth +1
            }
			
	    }catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
		return depth;
   }//end loadE


   public static void deleteMC(Messenger esql, aUser au, List<String> chat){
        try{
			//check if they are initial 
			String check_i = String.format("select * from CHAT where chat_id = '%s' and init_sender = '%s'", chat.get(0), au.login);
    		int cnum = esql.executeQuery(check_i);
  			if(cnum ==0)
    		{
        		System.out.println("You are not the initial chat sender");
        		return;
    		}
    		else{
        		System.out.println("Please give login of user to be deleted from chat ");
        		String loginToDelete = in.readLine();
        		// query if user exists
				String exists = String.format("select * from CHAT_LIST where member = '%s' and chat_id = '%s' ", loginToDelete, chat.get(0));
        		int lnum = esql.executeQuery(exists);
        		if(lnum == 0)
        		{
            		System.out.println("User does not exist in chat");
            		return;
        		}
				String already = String.format("select * from CHAT_LIST where chat_id = '%s' and member = '%s'", chat.get(0), loginToDelete);
				int al = esql.executeQuery(already);
				if( al == 0)
				{
					System.out.println("They are not in this Chat");
					return;
				}

				String update = String.format("delete from CHAT_LIST where chat_id = '%s' and member = '%s' ", chat.get(0), loginToDelete);
        		esql.executeUpdate(update);
        		System.out.println("Deleted " + loginToDelete + " Successfully"); 
    		}
    		return;

	    }catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
   }//end deleteMC



   public static void addMC(Messenger esql, aUser au, List<String> chat){
        try{
			//check if they are initial 
			String check_i = String.format("select * from CHAT where chat_id = '%s' and init_sender = '%s'",chat.get(0), au.login);
    		int cnum = esql.executeQuery(check_i);
    		if(cnum ==0)
    		{
        		System.out.println("You are not the initial chat sender");
        		return;
    		}
    		else{
        		System.out.println("Please give login of user to be added: ");
        		String loginToAdd = in.readLine();
        		// query if user exists
				String exists = String.format("select * from USR where login = '%s'",loginToAdd);
        		int lnum = esql.executeQuery(exists);
        		if(lnum == 0)
        		{
            		System.out.println("User does not exist");
            		return;
        		}
				String already = String.format("select * from CHAT_LIST where chat_id = '%s' and member = '%s'", chat.get(0), loginToAdd);
				int al = esql.executeQuery(already);
				if( al == 1)
				{
					System.out.println("Already in chat");
					return;
				}
				String update = String.format("insert into CHAT_LIST values('%s', '%s')", chat.get(0), loginToAdd);
        		esql.executeUpdate(update);
        		System.out.println("Added " + loginToAdd + " Successfully"); 
    		}
    		return;

	    }catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
   }//end addMC

   public static void cDelete(Messenger esql, aUser au, List<String> chat){
        try{
				// check if they are initial
				String check_i = String.format("select * from CHAT where chat_id = '%s' and init_sender = '%s'",chat.get(0),au.login);
				int cnum = esql.executeQuery(check_i);
				if(cnum == 0)
				{
					System.out.println("You are not the initial chat sender");
    				return;
				}
				else{
					System.out.println("\tAll related messages will be deleted");
                    System.out.println("\tare you sure? (y/n): ");
                    String ans = in.readLine();
                    while(!ans.equals("n") && !ans.equals("y") )
                    {
                        System.out.println("\tError: invalid answer (y/n): ");
                        ans = in.readLine();
                    }
                    //if no, then don't delete from contact list and return.
                    if( ans.equals("n"))
                    {
                        return;
                    }
	
						String update = String.format("delete from CHAT where chat_id = '%s' ", chat.get(0));
    					esql.executeUpdate(update);
   						System.out.println("Delete Successful");
				}
				return;

	    }catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
   }//end NewMessage


   public static List<List<String>> cMessage(Messenger esql, aUser au, int depth, List<String> chat_id){
        List<List<String>> m = null;
		try{
			//query for 10 chats in depth range 
			int offset = (depth*10);
			String tenM = String.format("select * from MESSAGE where chat_id = '%s' order by msg_timestamp DESC Limit 10 offset '%s' ", chat_id.get(0), offset);
			m = esql.executeQueryResult(tenM);
			if( m == null || m.size() == 0)
			{
				System.out.println("\t\t\tEmpty Chat");
				return m;
			}
			System.out.println();
			for(int i = 0; i < m.size(); i++)
			{
				int temp = i+1;
				int go = 1;
				//CHANGE :) removed tabbing
				System.out.println(temp + ")");
				System.out.println("Author: " + m.get(i).get(4));
				System.out.println("Creation Date: " + m.get(i).get(2));
				System.out.println("Text: " + m.get(i).get(1));
				String att_look = String.format("select media_type, URL from MEDIA_ATTACHMENT where msg_id = '%s' ", m.get(i).get(0));
				List<List<String>> aQ = esql.executeQueryResult(att_look);
				if(aQ.size() == 0 || aQ == null)
				{
					go = 0;
				}
				for(int j = 0; j < aQ.size() && go == 1; j++)
				{
					System.out.print("Media type: ");
					System.out.println(aQ.get(j).get(0));
					System.out.print("URL :");
					System.out.println(aQ.get(j).get(1));
				}
					
			}
			return m;
	    }catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
		return m;
   }//end cMessage

   public static List<List<String>> printChats(Messenger esql, aUser au){
		List<List<String>> temp = null;
        try{
			// Get all the chats user has membership of and find most current 
				String c_time = String.format("select * from (select m.chat_id as chat_id, Max(m.msg_timestamp) as msg_timestamp from MESSAGE m,(select chat_id from CHAT_LIST, USER_LIST_CONTAINS ulc where member = '%s' and not member = ulc.list_member) as c where c.chat_id = m.chat_id group by m.chat_id ) as id_t order by id_t.msg_timestamp DESC", au.login);  
 
	    	temp = esql.executeQueryResult(c_time);
			System.out.println("");
			//print out in 
			for(int j = 0; j < temp.size(); j++)
			{
				System.out.println("\t"+(j+1) + ": chat_id: " + temp.get(j).get(0));
				// query for all recipients
				String all_r = String.format("select member from CHAT_LIST where chat_id = '%s'", temp.get(j).get(0));
				List<List<String>> q_r = esql.executeQueryResult(all_r);
				System.out.print("\tRecipients: \n");
				//print all the recipients
				for(int x = 0; x < q_r.size(); x++)
				{
					System.out.println("\t\t" + q_r.get(x).get(0) + " ");
				}
				System.out.print("\n");
				System.out.println("\tLast update: " + temp.get(j).get(1));
				System.out.print("\n");
				
			}
			return temp;
		}catch (Exception e)
    	{
	    	System.err.println(e.getMessage());
    	}
		return temp;
   }//end printChats

   public static void ReadNotifications(Messenger esql, aUser au){
     	try{
			String get_msg_id = String.format("select msg_id from NOTIFICATION where usr_login = '%s'", au.login);
			List< List<String>> n_message_id = esql.executeQueryResult(get_msg_id);
			if(n_message_id == null || n_message_id.size() == 0)
			{
				System.out.println("You have no new notifications\n");
				return;
			}
            //System.out.println(n_message_id.get(0));
			
			//currently prints out all notifications in one go 
			for(int i = 0; i < n_message_id.size(); i++)
			{
				String get_msg_i = String.format("select msg_text, sender_login from MESSAGE where msg_id = '%s'", n_message_id.get(i).get(0));
				List< List<String>> msg_i = esql.executeQueryResult(get_msg_i);
				
				// check if blocked 
				String cb = String.format("select * from USER_LIST_CONTAINS where list_id = '%s' and list_member ='%s'", au.block_list, msg_i.get(0).get(1));
				int cn = esql.executeQuery(cb);
				if(cn == 0)
				{
				System.out.println(msg_i.get(0).get(0));
				//assuming it is now considered read delete from Notifications
				String msg_d = String.format("delete from NOTIFICATION where usr_login = '%s' and msg_id = '%s'", au.login, n_message_id.get(i).get(0));
				esql.executeUpdate(msg_d);
				}
			}
			

    	}catch (Exception e)
	    {
		    System.err.println(e.getMessage());
    	}
   }//end ReadNotifications

   public static void DeleteFromContact(Messenger esql, aUser au){
    	try{
	        System.out.print("\tEnter user login to delete: ");
        	String logintodelete = in.readLine();
            //check if the contact to delete is on your contact list
            String query = String.format("select * from USER_LIST_CONTAINS where list_member ='%s' and list_id = '%s' ",logintodelete, au.contact_list );
            int numR = esql.executeQuery(query);
            if( numR == 0)
            {
                System.out.println("\t" + logintodelete + " is not on your contact list" + "\n");
                return;
            }
            else{
                String update = String.format("Delete from USER_LIST_CONTAINS where list_member = '%s' and list_id = '%s'",logintodelete, au.contact_list);
                esql.executeUpdate(update);
                System.out.println("\t" + logintodelete + " is now deleted from contacts" + "\n");
                return;
            }
		
        }catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
   }//end DeleteFromContact

   public static void AddToBlock(Messenger esql, aUser au){
     	try{
        // get contact
            System.out.print("\tEnter user login to block: ");
            String logintoblock = in.readLine();
        
            if(logintoblock.equals(au.login))
            {
                System.out.println("\tYou cannot add yourself as a blocked contact\n");
                return;
            }

        //check if contact exists in block list
            String query1 = String.format("Select USR.login  From USR Where login = '%s'" , logintoblock);
            int numR = esql.executeQuery(query1);
            if( numR == 0)
            {
                System.out.println("\t" + logintoblock + " does not exist\n");
                return;
            }
        //check if there is a relation
            String query = String.format("Select * from USER_LIST_CONTAINS where  list_member = '%s' AND list_id = '%s'" ,logintoblock,  au.block_list);
            int block_int = esql.executeQuery(query);
            //  check if contact exists in the block list
            if( block_int != 0)
            {
                System.out.println("\t" + logintoblock + " is already blocked\n");
                return;
            }
            else //check if contact is in the contact list. 
            {
                String query3 = String.format("select * from USER_LIST_CONTAINS where list_member ='%s' and list_id = '%s' ",logintoblock, au.contact_list );
                int numR2 = esql.executeQuery(query3);
                if( numR2 == 1) //If in contact, confirm deletion from contact
                {
                    System.out.println("\n\t" + logintoblock + " is on your contact list\n");
                    System.out.println("\tThey will be deleted from contact list if you do block");
                    System.out.println("\tare you sure? (y/n): ");
                    String ans = in.readLine();
                    while(!ans.equals("n") && !ans.equals("y") )
                    {
                        System.out.println("\tError: invalid answer (y/n): ");
                        ans = in.readLine();
                    }
                    //if no, then don't delete from contact list and return.
                    if( ans.equals("n"))
                    {
                        return;
                    }
                    else{ //delete contact from contact list
                    String update = String.format("Delete from USER_LIST_CONTAINS where list_member = '%s' and list_id = '%s'",logintoblock, au.contact_list);
                    esql.executeUpdate(update);
                    System.out.println("\t" + logintoblock + " is now deleted from contacts\n");
                    }
                }

                //insert contact into the blocked list
                String query2 = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES( '%s', '%s')", au.block_list, logintoblock);
                esql.executeUpdate(query2);
                System.out.println("\t" + logintoblock + " is now blocked\n");
            }
        }catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
   }//end AddToBlock

    public static void DeleteFromBlock(Messenger esql, aUser au){
        try{
	        System.out.print("\tEnter user login to unblock: ");
        	String logintounblock = in.readLine();

            //check if contact to unblock exists in the block list. If not, return, else delete from the block list
            String query = String.format("select * from USER_LIST_CONTAINS where list_member ='%s' and list_id = '%s' ",logintounblock, au.block_list );
            int numR = esql.executeQuery(query);
            if( numR == 0)
            {
                System.out.println("\t" + logintounblock + " is not on your block list\n");
                return;
                
            }
            else{
                String update = String.format("Delete from USER_LIST_CONTAINS where list_member = '%s' and list_id = '%s'",logintounblock, au.block_list);
                esql.executeUpdate(update);
                System.out.println("\t" + logintounblock + " is now deleted from blocked\n");
                return;
            }
        }catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
 
   }//end DeleteFromBlock

    public static void ListBlocks(Messenger esql, aUser au ){
        try{
            String query = String.format("select u.list_member   from USER_LIST_CONTAINS u, USR where u.list_id = '%s' and USR.login = u.list_member  " , au.block_list);
            List< List<String>> block_members = esql.executeQueryResult(query);
            if( block_members.size()  <= 0)
            {
                System.out.println("\n\tblock list is empty\n ");
                return;
            }
            String block_title = "\t" + au.login + "'s block list";
            int block_size = block_members.size();
            int dashCalls = block_size - 1;
            int numDashes = block_title.length();
            printDashes(numDashes);
            System.out.println();
            for( int i = 0 ; i < block_size ; i++)
            {
                System.out.println(block_members.get(i).get(0));
                if(i != dashCalls)
                {
                    printDashes(numDashes);
                    System.out.println();
                }
                
            }
            printDashes(numDashes);
            System.out.println();
        } catch (Exception e)
        {
            System.err.println(e.getMessage());
        }

       }//end ListBlocks

/*================================================================*
 *                  ERROR CHECKING TEST                           *
 *================================================================*/

    public static void errorResult(int depth, int error)
    {
        switch(error)
        {
            case -1:
                break;
            case -2:
                break;
            case -3:
                break;
            case -4:
                break;
            case -5:
                break;
            case -6:
                break;
            case -7:
                break;
            case -8:
                break;
            case -9:
                break;
            default:
                for(int i = 0; i < depth; ++i)
                {
                    System.out.print("\t");
                }
		        System.out.println("Unknown Error!\n");
				break;
        }

    }

}//end Messenger
