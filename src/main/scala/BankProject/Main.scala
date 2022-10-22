package BankProject

import java.sql.{Connection, DriverManager, PreparedStatement, Blob}
import scala.collection.mutable

object Main{
  def main(args: Array[String]): Unit = {
    //Singleton object call
    BankProject.mainLoop()
  }
}


object BankProject{

  //An enumerator object with values representing every screen that the user can switch to.
  //Each value is assigned as a WhichScreen object, which overrides a "behaviour" method which is
  //declared later in the enumerator object. Each overridden behaviour method makes a call to another
  //appropriate method that'll perform all the actions relevant to the screen that the user is on.
  object en extends Enumeration {

    type en = Value

    val start: Value                = new WhichScreen { def behaviour: Any = { startScreen() } }
    val logIn: Value                = new WhichScreen { def behaviour: Any = { logInScreen() } }
    val accountRegister: Value      = new WhichScreen { def behaviour: Any = { accountRegistration() } }
    val home: Value                 = new WhichScreen { def behaviour: Any = { homeScreen() } }
    val cash: Value                 = new WhichScreen { def behaviour: Any = { cashHome() } }
    val withdrawCash: Value         = new WhichScreen { def behaviour: Any = { withdrawMoney() } }
    val depositCash: Value          = new WhichScreen { def behaviour: Any = { depositMoney() } }
    val checkBal: Value             = new WhichScreen { def behaviour: Any = { checkBalance() } }
    val transferCash: Value         = new WhichScreen { def behaviour: Any = { transferMoney() } }
    val loan: Value                 = new WhichScreen { def behaviour: Any = { loanHome() } }
    val personalLoan: Value         = new WhichScreen { def behaviour: Any = { personalLoanHandler() } }
    val mortgage: Value             = new WhichScreen { def behaviour: Any = { mortgageHandler() } }
    val account: Value              = new WhichScreen { def behaviour: Any = { accountManagementHome() } }
    val getAccountDetails: Value    = new WhichScreen { def behaviour: Any = { accountDetails() } }
    val makeCurrentAccount: Value   = new WhichScreen { def behaviour: Any = { makeCurrentAccountHandler() } }
    val makeSavingsAccount: Value   = new WhichScreen { def behaviour: Any = { makeSavingsAccountHandler() } }
    val switchAccount: Value        = new WhichScreen { def behaviour: Any = { switchAccountHome() } }
    val switchToSavings: Value      = new WhichScreen { def behaviour: Any = { switchToSavingsAccount() } }
    val switchToCurrent: Value      = new WhichScreen { def behaviour: Any = { switchToCurrentAccount() } }
    val admin: Value                = new WhichScreen { def behaviour: Any = { adminOptions() } }
    val searchForAccount: Value     = new WhichScreen { def behaviour: Any = { searchForAccountHandler() } }
    val removeAccount: Value        = new WhichScreen { def behaviour: Any = { removeAccountHandler() } }
    val logOut: Value               = new WhichScreen { def behaviour: Any = { logOutHandler() } }
    val exitProgram: Value          = new WhichScreen { def behaviour: Any = { exitProgramHandler() } }

    //As an abstract class, the behaviour method must be defined by any Value using it.
    //By extending Val(), it sets itself up to be accepted as a value assigned to a Value variable.
    protected abstract class WhichScreen extends Val() { def behaviour: Any }

    //Values would normally return errors that it cannot act as a WhichScreen object, so
    //implicit is used here to give additional context to each Value before errors are thrown.
    //In particular, the Value parameter is told to be an instance of WhichScreen, causing
    //a type cast that allows Values to behave as if they were WhichScreen objects. Since
    //WhichScreen objects extend Val(), all methods that Values normally have can still be called,
    //avoiding issues with them being unable to find anything they normally would.
    //This is used later in the mainLoop method, where the abstract behaviour method can be
    //called on the enumerator value variable to get the appropriate behaviour, instead of
    //creating a long match/case or if else statement to check the value and call the
    //appropriate method.
    implicit def valueToProgress(valu: Value) = valu.asInstanceOf[WhichScreen]
  }

  //This hashmap uses a String as a key, which corresponds to a user's username,
  //and a UserAccount as a value, corresponding to the user's account details.
  //Values initially given to this HashMap are given from the readFromFile method.
  val accountHashMap: mutable.HashMap[String, UserAccount] = readFromFile()

  //The state variable corresponds to the enumerator above, and is initially given
  //the start screen as a value
  var state: en.en = en.start

  //The programRunning boolean is used within the mainLoop method to check whether
  //the loop should keep on going, because the user is still making use of this program.
  //When the user decides to exit the program, it will be set to false to escape
  //the loop and reach the end of the program, terminating it.
  var programRunning: Boolean = true

  //The acc variable stores the UserAccount object for the user that has logged in.
  //It is used as an easier way to draw information from the user's account, instead
  //of needing to constantly reference the HashMap with a saved username variable.
  var acc: UserAccount = _

  //Connects to an active MySQL session.
  //url is used to determine the port number and initial database to connect to.
  //username is used to determine which user is accessing the database.
  //password is used alongside the username to allow them access.
  //IF YOU'RE RUNNING THIS PROGRAM ON ANOTHER DEVICE, YOU WILL MOST LIKELY HAVE TO
  //CHANGE THE PASSWORD TO WHATEVER PASSWORD YOU ARE USING.
  //connection is used to establish a connection with MySQL with these parameters given.
  val driver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://localhost:3306/mysql"
  val username = "root"
  val password = "CallumKnape"

  var connection: Connection = null

  try {
    //Since the HashMap should have been filled up on initialisation when it called readFromFile(),
    //it will have values ready to show at this point. For each key/value pairing, the key,
    //corresponding to a username, is printed.
    println("Users now in HashMap after reading from file: ")
    accountHashMap.foreach { case (k, v) => println(k) }

    //The driver variable is treated as a class and is called at this point to help set up the connection
    Class.forName(driver)
    //The connection var is given the url, username and password as details to connect to the database.
    connection = DriverManager.getConnection(url, username, password)

    //When this program first runs, it will create a new database called CallumBankProject.
    //If the database already exists, because this is the second or later time this program is running,
    //this command does nothing, thanks to the "if not exists" argument in the command.
    val startDatabase: PreparedStatement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS CallumBankProject;")
    startDatabase.execute()

    //Either the database already exists or one was just made. Either way, we move to that database to start using it.
    val switchToDatabase: PreparedStatement = connection.prepareStatement("USE CallumBankProject;")
    switchToDatabase.execute()

    //A table is created if it does not already exist inside the CallumBankProject database.
    //The table is called userAccountsCallum and can store two VarChars with a limit of 50 characters,
    //corresponding to a username and password.
    //The username is declared as the Primary Key, meaning that there cannot be any duplicates, helping to
    //identify each row separately from one another.
    val createTableForUsers: PreparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS userAccountsCallum(username VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, PRIMARY KEY(username));")
    createTableForUsers.execute()

    //To set up the table, a row is inserted, where the username is TheFirstAccount and the password
    //is password. If an error would happen because this row already exists and adding it again would
    //be a duplicate, it is ignored.
    val insertMySQL = """insert ignore into userAccountsCallum (username,password) values(?,?)"""
    val preparedStatement: PreparedStatement = connection.prepareStatement(insertMySQL)
    preparedStatement.setString(1, "TheFirstAccount")
    preparedStatement.setString(2, "password")
    preparedStatement.execute()

    //A statement is made to check each row on the table, returning just the value in their username.
    val checkTable = connection.createStatement()
    val results = checkTable.executeQuery("SELECT username FROM userAccountsCallum")
    println("\nUsers found in userAccountsCallum table: ")
    //For as long as the query has more lines to read through, we iterate through a loop that gets the
    //next string found in the username column and prints it to the console.
    while (results.next()) {
      val un = results.getString("username")
      println(un)
    }
  } catch {
        //If any error occurs, the database hasn't been set up properly, so some details should be fixed
        //before anything else runs. As such, programRunning is set to false so that the mainLoop immediately
        //exits and the program is terminated.
    case e: Exception => e.printStackTrace()
      println("It would probably be best to check what that error was before the rest of this program runs. Aborting!")
      programRunning = false
  }

  //readFromFile() is used to give accountHashMap its initial values based on what this method can find
  //from different files. It uses java libraries for file input and object input, so they are imported just for
  //this method.
  def readFromFile(): mutable.HashMap[String,UserAccount] ={
    import java.io._

    //A new HashMap is made but is left empty.
    val hashMapFromFile: mutable.HashMap[String, UserAccount] = mutable.HashMap.empty[String, UserAccount]

    try {
      //A FileInputStream is set up, looking for the CallumUserAccounts.txt file to read from.
      //The ObjectInputStream reads data from the file as if they were an object.
      val fis: FileInputStream = new FileInputStream(new File("CallumUserAccounts.txt"))
      val ois: ObjectInputStream = new ObjectInputStream(fis)

      //Each time this loop makes an iteration, it reads one object from the file, then attempts to
      //cast that object as a UserAccount, allowing the rest of this program to use it appropriately.
      //The getUsername, isAdmin and getPassword methods are called on the UserAccount object,
      //and the username is used to find a file with the same name, to get two lists,
      //one corresponding to the list of CurrentAccount objects that the UserAccount formerly held, and
      //the other corresponding to the list of SavingsAccounts. Since these files will always only have
      //these two lists, a nested loop isn't needed, nor a reason to catch an EOFException from them while
      //still continuing the UserAccount loop.
      //This loop will continue happening until an EOFException is thrown, for reaching the end of
      //the CallumUserAccounts.txt file and having no more objects to read.
      while (true) {
        val obj = ois.readObject()
        val ua: UserAccount = obj.asInstanceOf[UserAccount]

        val fisi: FileInputStream = new FileInputStream(new File(ua.getUsername) + ".txt")
        val oisi: ObjectInputStream = new ObjectInputStream(fisi)

        val l1 = oisi.readObject()
        val l2 = oisi.readObject()

        val list1 = l1.asInstanceOf[List[CurrentAccount]]
        val list2 = l2.asInstanceOf[List[SavingsAccount]]

        hashMapFromFile.put(ua.getUsername, new UserAccount(ua.isAdmin,ua.getUsername,ua.getPassword,list1,list2))
      }
      hashMapFromFile
    }catch{
          //That EOFException is caught here, and the HashMap created earlier, with entries added from what
          //was found in the files, is returned back to the accountHashMap that called this whole method.
      case e: EOFException => hashMapFromFile
      //The first time this program runs, there is not likely to be a file to read from at all, so this
      //error is thrown instead. It can be safely ignored and the accountHashMap will be initialised as
      //an empty HashMap
      case e: FileNotFoundException => println("No file was found to read UserAccount objects from. Perhaps this is your first time running this program? If so, don't worry about this."); hashMapFromFile
      case e: Throwable => e.printStackTrace(); hashMapFromFile
    }

  }

  //*******************************************************************************************************************

  //Called from Main.main, this method is the loop that keeps this program running after the user goes through
  //their options. programRunning is the last thing to be turned to false in exitProgram() and is the intended
  //way to leave this loop and this program safely.
  def mainLoop(): Unit ={
    print("Welcome to the Bank of Banks! ")
    while (programRunning) {
      //As mentioned with the enumerator earlier, the abstract method there means that we can simply call that
      //method on the variable instead of a long match/case or if else statement to determine what to do based
      //on the variable's value.
      state.behaviour
    }
  }

  //*******************************************************************************************************************
  //The startScreen is where this program begins, thanks to the initial value given to the "state" variable
  def startScreen(): Unit = {
    //We ask the user if they want to log in to an existing user account, make a new user account or exit program
    println("Please input a number corresponding to the option you'd like.")
    println("1) Log in")
    println("2) Create new user account")
    println("3) Exit program")
    print("Input: ")
    try {
      //We read the Int given by the user
      val i: Int = scala.io.StdIn.readInt()
      println("\n\n\n") //Three empty lines to space things out
      //We then compare the Int to find a match. The value of "state" is changed so that a different method is called
      //back in the mainLoop, allowing the user to navigate through menus to reach the option they are looking for.
      //If the user input an Int that is not 1, 2 or 3, the _ result is triggered, and instead of changing the value
      //of "state", a message is printed. Since "state" didn't change, the next iteration of the mainLoop brings the
      //user straight back here again, for a second try.
      i match {
        case 1 => state = en.logIn
        case 2 => state = en.accountRegister
        case 3 => state = en.exitProgram
        case _ => println("Your input did not match with any options. Please try again.")
      }
    } catch {
          //However, if the user input any value that cannot be recognised as an Int, because, for instance, it was
          //a String, or Double or any other type besides Int, this Exception is thrown instead.
          //A different message is printed to the user, but because the exception was caught here, the program is able
          //to continue running and, like the _ case earlier, it can loop back around to this method and await new
          //input from the user.
      case e: NumberFormatException => println("Your input did not match with any options. I don't think it was even a number! Please try again.")
    }
  }

  //*******************************************************************************************************************
  def logInScreen(): Unit = {
    //The log in screen, where we ask the user to input an existing username and password to access their user
    //account in this program.
    println("Log in. To return to the start screen, input 'back' as your username.")
    print("Username: ")
    val un: String = scala.io.StdIn.readLine()//We ask the user for their username
    if (un.toLowerCase.equals("back")) {
      //If the user inputs "Back" in any case, it is converted to lower case and compared here, triggering
      //this if statement. "state" is changed back to the start screen and return is used to exit this method
      //early, preventing the rest of the code in this method from executing. The mainLoop will then move on
      //to the next iteration, where "state" now makes it load the start screen instead of this screen.
      println("\n\n\n")
      state = en.start
      return //Exits this method early, preventing any code beyond this point from executing.
    }

    print("Password: ")
    val pw: String = scala.io.StdIn.readLine()//If back wasn't input, we ask the user for their password.

    println("\n\n\n")
    //We now compare the username to keys on the accountHashMap and look for a match
    if (accountHashMap.contains(un)) {
      //Since there was a user found, we get the password saved to that account and compare it to the input password
      if (accountHashMap(un).getPassword.equals(pw)) {
        acc = accountHashMap(un) //Save that account to a var so the rest of the program can more easily access it
        state = en.home //Then change the state to the logged in home screen
      } else {
        //If the password didn't match, inform the user and let mainLoop bring us back to the start of this method.
        println("User found but password did not match.")
      }
    } else {
      //If the username didn't match, inform the user about that.
      println("Username not found.")
    }
  }

  //*******************************************************************************************************************
  def accountRegistration(): Unit = {

    //In Account registration, we ask the user to input a new username for themselves, along with a password that they
    //must confirm a second time. Finally, and purely for the purposes of this project, the user is asked if the
    //account they are creating should be an admin account or not.
    println("Account creation. To return to the start screen, input 'back' as your username.")
    print("Username: ") //Ask the user for a username to use
    val un: String = scala.io.StdIn.readLine()

    //Identical to what happens in logInScreen(). Inputting "back" in any case, converted to
    //lower case and, if it matches, change "state" and return out of this method before anything
    //else in this method executes.
    if (un.toLowerCase.equals("back")) {
      state = en.start
      return
    }

    //Check that the username does NOT exist in the accountHashMap
    if (!accountHashMap.contains(un)) {

      print("Password: ") //Ask the user for a password to use
      val pw: String = scala.io.StdIn.readLine()
      print("Confirm password: ") //Ask the user to input the password again, just to be safe
      val pw2: String = scala.io.StdIn.readLine()

      if (pw.equals(pw2)) { //Compare the two passwords.
        //This admin question is only asked for the purposes of this project.
        //Admins will be able to search for, and delete accounts compared to non-admin accounts.
        //Inputting anything that isn't "true" is treated as false. Including "Maybe".
        var ad: Boolean = false
        try {
          print("\n(Input Boolean) Admin?: ")
          ad = scala.io.StdIn.readBoolean()
        }catch{
          case e: Throwable => //If the user input anything that would cause an error, we can ignore it
                               //and continue with the ad variable starting off as false, as it has been
                               //given as the initial value
        }

        //Make a new UserAccount object, with admin, username and password vars as arguments, and
        //put that as a value into the HashMap, with username also used as the key
        accountHashMap.put(un, new UserAccount(ad, un, pw))
        //Since the account has been created, we want to return the user back to the start screen.
        state = en.start
        println("\n\n\nYour new account has been created!")
      } else {
        println("\n\n\nYour passwords did not match. Please try again.")
      }
    } else {
      println("\n\n\nThat username is already in use")
    }
  }

  //*******************************************************************************************************************
  def homeScreen(): Unit = {

    //The user is asked to input an Int to decide how to navigate through the menus.
    //Depending of whether the user is an admin or not, different options are shown and numbered.
    println("Welcome, " + acc.getUsername + "\nPlease input a number corresponding to the category you'd like.")
    println("1) Cash\n2) Loan\n3) Account management\n4) Switch account")
    if (acc.isAdmin) {
      println("5) Admin options\n6) Log out")
    } else {
      println("5) Log out")
    }
    print("Input: ")
    try {
      val i: Int = scala.io.StdIn.readInt()
      println("\n\n\n")
      i match {
        case 1 => state = en.cash
        case 2 => state = en.loan
        case 3 => state = en.account
        case 4 => state = en.switchAccount
        case 5 =>
          if (acc.isAdmin) {
            state = en.admin
          } else {
            state = en.logOut
          }
        case 6 =>
          if (acc.isAdmin) {
            state = en.logOut
          } else {
            println("Your input did not match with any options. Please try again.")
          }
        case _ => println("Your input did not match with any options. Please try again.")
      }
    } catch {
      case e: NumberFormatException => println("Your input did not match with any options. Was that even a number? Please try again.")
    }
  }

  //*******************************************************************************************************************
  def cashHome(): Unit = {
    //The cash home screen.
    //Here, we want to know what the user wishes to do regarding money.
    println("Cash")
    println("Please input a number corresponding to the option you'd like.")
    println("1) Withdraw cash\n2) Deposit cash\n3) Check balance\n4) Transfer money\n5) Return")
    print("Input: ")
    try {
      val i: Int = scala.io.StdIn.readInt()
      println("\n\n\n")
      i match {
        case 1 => state = en.withdrawCash
        case 2 => state = en.depositCash
        case 3 => state = en.checkBal
        case 4 => state = en.transferCash
        case 5 => state = en.home
        case _ => println("Your input did not match with any options. Please try again.")
      }
    } catch {
      case e: NumberFormatException => println("Your input did not match with any options. I don't think it was even a number! Please try again.")
    }
  }

  //*******************************************************************************************************************
  def withdrawMoney(): Unit = {
    //Here, we ask the user how much money they'd like to withdraw from their active account.
    //The active account can be a Current Account or a Savings Account, and can be switched on
    //SwitchAccount screen, further down this file.
    println("Withdraw money. To return, input 0.")
    val i: Double = acc.getAccount.checkBalance()
    //The user's current amount of money from their active account is shown for convenience.
    //The "f" before the string means that it accepts formatting values. $i corresponds to the
    //variable "i", displaying it's value, and %9.2f means that the variable's value is formatted
    //to show up to 9 digits, followed by a decimal point, then up to 2 digits afterwards.
    //This keeps the displayed value to a financial appearance.
    print(f"You currently have £$i%9.2f.\nInput withdraw amount: £")
    try {
      val w: Double = scala.io.StdIn.readDouble()
      if (w == 0.0) {
        //If the user inputted 0, we want to return them to the cash home screen, before this
        //withdraw cash screen.
        state = en.cash
        return
      }
      //Formatting the user's input to a financial value means that it becomes a string, so we use
      //.toDouble to bring that formatted value back to a double again.
      val v: Double = f"$w%9.2f".toDouble
      //acc corresponds to the user's UserAccount object. getAccount returns the user's active account,
      //which is an Account object, an abstract class that can be extended to either a CurrentAccount or
      //a SavingsAccount, and on that Account object, we call the withdrawMoney method, passing
      //the user's formatted input double as an argument. The method returns a boolean; true if the
      //requested amount can be withdrawn without issue, false if the requested amount would cause problems,
      //such as withdrawing more money than what's in the account.
      if (acc.getAccount.withdrawMoney(v)) {
        println(f"You have withdrawn £$v%9.2f from your account.")
        state = en.cash
      } else {
        println("You don't have enough money in your account.")
      }
    } catch {
          //Unlike earlier methods, inputting a value that cannot be recognised as a double will send the user
          //back to the cash home screen.
          //To fit both the println and variable assignment on the same line, a semi-colon is used to separate
          //the two commands.
      case e: NumberFormatException => println("Not a valid number. Returning to General Cash screen."); state = en.cash
    }
  }

  //*******************************************************************************************************************
  def depositMoney(): Unit = {
    //Similarly to withdrawMoney, we inform the user of how much money is already in their account
    //and ask them to input how much more they'd like to deposit.
    println("Deposit money. To return, input 0.")
    val i: Double = acc.getAccount.checkBalance()
    print(f"You current have £$i%9.2f.\nInput deposit amount: £")
    try {
      val w: Double = scala.io.StdIn.readDouble()
      if (w == 0.0) {
        state = en.cash
        return
      }
      val v: Double = f"$w%9.2f".toDouble
      println("\n\n\n")
      //In the Account.scala file, the depositMoney method in the SavingsAccount case class imposes
      //a limit of 85,000 to how much money can be stored. If the user would exceed this amount,
      //the depositMoney method would return false. CurrentAccount does not have this limit.
      //In both types of Accounts, if the user's input was 0 or a negative number, it also returns
      //false, to avoid that loophole for reducing money.
      if (acc.getAccount.depositMoney(v)) {
        println(f"You have deposited £$v%9.2f into your account.")
      } else {
        println("Failed to deposit into account. Please consider making a new account.")
      }
      state = en.cash
    } catch {
      case e: NumberFormatException => println("Not a valid number. Returning to General Cash screen."); state = en.cash
    }
  }

  //*******************************************************************************************************************
  def checkBalance(): Unit = {
    //CheckBalance simply returns the amount of money in the user's active account, and its name.
    println("Account: " + acc.getAccount.getAccName())
    val i: Double = acc.getAccount.checkBalance()
    println(f"Amount: £$i%9.2f")

    println("\nPress Enter to continue")
    try {
      scala.io.StdIn.readChar() //Returns StringIndexOutOfBoundsException when you just press enter
    } catch {
      case e: Throwable => //It doesn't actually matter what error is thrown from the input.
      //We expected Enter to be pressed and we know it'll throw an error. If the user tried
      //anything else, what would we actually want to do besides let the user continue anyway?
      //There's no risk of wrong values being saved, or wrong behaviour being performed here.
      //Nothing needs to be output or really handled.
    }
    state = en.cash
  }

  //*******************************************************************************************************************
  def transferMoney(): Unit = {
    //This method involves transferring money from one user's active account to the most recently
    //used active account of another user.
    println("Transfer money. To return to the previous menu, input 'back' as the recipient.")
    print("Input username of recipient: ")
    val un: String = scala.io.StdIn.readLine()
    if (un.toLowerCase.equals("back")) {
      state = en.cash
      return
    }
    //Using the .contains method on a HashMap simply returns a boolean for whether the value given
    //can be found as a key in the HashMap
    if (accountHashMap.contains(un)) {
      print("Amount to transfer: £")
      val i: Double = scala.io.StdIn.readDouble()
      val v: Double = f"$i%9.2f".toDouble
      println("\n\n\n")

      //Since the withdrawMoney method already takes money away from the account before
      //returning true, we save it to a variable, along with the depositMoney's boolean return
      //in a separate variable. Both are compared using && to ensure both are true, and if so,
      //the transaction will already have been completed thanks to their methods.
      //If withdraw returns false, we do not want to attempt to give the recipient any money,
      //as the sender would not be able to deliver it anyway.
      //However, if the sender was able to send that amount but the recipient could not receive
      //it, perhaps because they're using a savings account and the deposit amount would exceed
      //£85,000, the else statement from (withdraw && deposit) checks this and refunds the money
      //back to the sender, undoing the amount that the withdrawMoney method had taken away.
      val withdraw: Boolean = acc.getAccount.withdrawMoney(v)
      var deposit: Boolean = false
      if(withdraw) {
        deposit = accountHashMap(un).getAccount.depositMoney(v)
      }

      if (withdraw && deposit) {
        println(f"Transaction completed. £$v%9.2f has been sent from your account to $un's account.")
        state = en.cash
      } else {
        if(withdraw && !deposit){
          acc.getAccount.depositMoney(v)
          println("Transaction failed. Recipient cannot accept so much money.")
        }else{
          println("Transaction failed. Your account does not have enough money to transfer.")
        }
      }
    } else {
      //Username not found
      println("No username found. Please try again.")
    }
  }


  //*******************************************************************************************************************
  //The loan home screen. Two types of loans are offered.
  //The personal loan allows the user to take between £100-£20,000. A different interest amount
  //is given based on the loan amount requested, and between that and 10% of the user's monthly
  //income, the user is judged whether they will be able to repay the loan + interest within
  //60 months/5 years.
  //The mortgage loan allows the user to take between £50,000-£800,000. A set interest amount,
  //5%, is used, but so is 75% of the user's income to judge whether they can repay. Unlike
  //personal loans, the user is allowed 480 months/40 years to repay this mortgage.
  def loanHome(): Unit = {
    println("Loans")
    println("Please input the number corresponding to the option you'd like.")
    println("1) Personal Loan\n2) Mortgage\n3) Return")
    print("Input: ")
    try {
      val i: Int = scala.io.StdIn.readInt()
      println("\n\n\n")
      i match {
        case 1 => state = en.personalLoan
        case 2 => state = en.mortgage
        case 3 => state = en.home
        case _ => println("Your input did not match with any option. Please try again.")
      }
    } catch {
      case e: NumberFormatException => println("Your input did not match with any options. I don't think it was even a number! Please try again.")
    }
  }

  //*******************************************************************************************************************
  def personalLoanHandler(): Unit = {
    //We ask the user for how much money they're like to take out as a loan. If they input a value
    //that is outside the given range, a custom exception is thrown to inform the user of this.
    //Because of the catch block at the end of this method, nothing else in the try block is performed
    //when the exception is thrown. An out of range loan will return the user back to the start of this
    //method, but any value not recognised as a Double will return the user to the previous loan screen.
    print("Personal loan.\nPlease input the loan amount you would like to take out, between £100 and £20,000, or input 0 to return.\n£")
    try {
      val l: Double = scala.io.StdIn.readDouble()
      if(l == 0.0){
        state = en.loan
        return
      }
      if (l > 20000.00 || l < 100) {
        throw new LoanOutOfRangeException("You are requesting a loan outside of the given range")
      }
      val lv: Double = f"$l%9.2f".toDouble

      print("Please input your monthly income after tax.\n£")
      val i: Double = scala.io.StdIn.readDouble()
      val li: Double = f"$i%9.2f".toDouble

      try {
        //totalMonthsToRepay is a recursive method that will return the amount of months needed to repay
        //the attempted loan, factoring in monthly interest and a percentage of the user's income per month
        //to repay the loan.
        //lv is the requested loan input and li is the user's income input.
        //personalLoanInterestAmt is a method that returns a Double, representing the interest amount that should be
        //charged monthly, based on the requested loan amount.
        //Since this is a Personal Loan, 10 is put down as the percentage amount of the user's income that can go
        //towards repaying the loan. 60 is given as the maximum amount of months allowed to repay the loan.
        //1 is given simply to start the recursive method off in keeping track of how many months have passed during
        //its calculations.
        //More details can be found in the loanRepayTime method.
        val totalMonthsToRepay: Int = loanRepayTime(lv, li, personalLoanInterestAmt(lv), 10, 60, 1)

        //The amount of months is divided by 12 and kept as an Int, effectively trimming any decimal figures, to
        //get the amount of years this converts to, and the modulus operator, %, gets the remainder of the months
        //after dividing it by 12. Together, they break down multiple months, such as 27 months, into 2 years and
        //3 months, making it easier for the user to know how long they can expect to take repaying this loan.
        val yrsToRepay: Int = totalMonthsToRepay / 12
        val mthsToRepay: Int = totalMonthsToRepay % 12
        //println("Years to repay: " + yrsToRepay)
        //println("Months to repay: " + mthsToRepay)
        //Ok, but now we need to make sure that the loan amount can even be put into the account.
        if (acc.getAccount.depositMoney(lv)) {
          println(f"Your loan request has been accepted and £$lv%9.2f has been deposited into " + acc.getAccount.getAccName())
          println(f"Assuming 10%% of your income is used to repay this loan each month, it is expected to take $yrsToRepay years and $mthsToRepay months to fully repay this loan with the information you have given.")
        } else {
          //Account is refusing to accept so much money
          println("Failed to deposit into account. Please consider making a new account.")
        }
      } catch {
        case e: RepaymentTooLongException => println(e)
      }
      //Upon loan successfully being given, we return the user back to the loan home screen
      state = en.loan
    } catch {
      case e: NumberFormatException => println("Not a valid number. Returning to General Loan screen."); state = en.loan
      case e: LoanOutOfRangeException => println(e)
    }
  }

  //*******************************************************************************************************************
  def mortgageHandler(): Unit = {
    //A mortgage is offered in the same way as a Personal Loan, but with different values.
    //The loan range that the user can request is different.
    //The monthly interest is always 5%, regardless of the loan amount requested.
    //75% of the user's income is used to repay the loan monthly, rather than 10%.
    //The user has 480 months/40 years to repay the mortgage, instead of 60 months/5 years.
    print("Mortgage loan.\nPlease input the loan amount you would like to take out, between £50,000 and £800,000, or input 0 to return.\nPlease remember that, for this task, savings accounts won't hold more than £85,000.\n£")
    try {
      val l: Double = scala.io.StdIn.readDouble()
      if (l == 0.0) {
        state = en.loan
        return
      }
      if (l > 800000.00 || l < 50000.00) {
        throw new LoanOutOfRangeException("You are requesting a mortgage outside of the given range")
      }
      val lv: Double = f"$l%9.2f".toDouble

      print("Please input your monthly income after tax.\n£")
      val i: Double = scala.io.StdIn.readDouble()
      val li: Double = f"$i%9.2f".toDouble

      try {
        val totalMonthsToRepay: Int = loanRepayTime(lv, li, 5, 75, 480, 1)
        val yrsToRepay: Int = totalMonthsToRepay / 12
        val mthsToRepay: Int = totalMonthsToRepay % 12
        //println("Years to repay: " + yrsToRepay)
        //println("Months to repay: " + mthsToRepay)
        if (acc.getAccount.depositMoney(lv)) {
          println(f"Your loan request has been accepted and £$lv%9.2f has been deposited into " + acc.getAccount.getAccName())
          println(f"Assuming 75%% of your income is used to repay this loan each month, it is expected to take $yrsToRepay years and $mthsToRepay months to fully repay this loan with the information you have given.")
        } else {
          println("Failed to deposit into account. Please consider making a new account.")
        }
      } catch {
        case e: RepaymentTooLongException => println(e)
      }
      state = en.loan
    } catch {
      case e: NumberFormatException => println("Not a valid number. Returning to General Loan screen."); state = en.loan
      case e: LoanOutOfRangeException => println(e)
    }
  }

  //*******************************************************************************************************************
  def accountManagementHome(): Unit = {
    //This account management page refers to bank accounts, rather than the user's account.
    //The user can check all of their current accounts and savings accounts, or make a new
    //current or savings account.
    println("Account management")
    println("Please input the number corresponding to the option you'd like.")
    println("1) Get user account details\n2) Make new Current Account\n3) Make new Savings Account\n4) Return")
    print("Input: ")
    try {
      val i: Int = scala.io.StdIn.readInt()
      println("\n\n\n")
      i match {
        case 1 => state = en.getAccountDetails
        case 2 => state = en.makeCurrentAccount
        case 3 => state = en.makeSavingsAccount
        case 4 => state = en.home
        case _ => println("Your input did not match with any option. Please try again.")
      }
    } catch {
      case e: NumberFormatException => println("Your input did not match with any options. I don't think it was even a number! Please try again.")
    }
  }

  //*******************************************************************************************************************
  def accountDetails(): Unit = {
    //The user is not required to input any values.
    //By referring to the UserAccount object saved to the variable "acc" when the user logged in,
    //we make various calls to different methods within the object to get a variety of information.
    //Get their username
    println("Username: " + acc.getUsername)
    if (acc.isAdmin) {
      //If they are an admin, print and extra line to say that they are
      println("Has admin permissions")
    }
    //Save both CurrentAccount and SavingsAccount lists to variables
    val currAccList: List[CurrentAccount] = acc.getCurrentAccountList
    val savAccList: List[SavingsAccount] = acc.getSavingAccountList

    //Print the length of the list so we know how many of that type of account they have,
    //then iterate over each index to print the amount of money within that account and its name.
    println("Current accounts: " + currAccList.length)
    for (a <- currAccList.indices) {
      val b: Double = currAccList(a).checkBalance()
      println("Account " + (a + 1) + ") " + currAccList(a).getAccName() + f" | £$b%9.2f")
    }
    for (a <- savAccList.indices) {
      val b: Double = savAccList(a).checkBalance()
      println("Account " + (a + 1) + ") " + savAccList(a).getAccName() + f" | £$b%9.2f")
    }

    println("\nPress Enter to continue")
    try {
      scala.io.StdIn.readChar() //Returns StringIndexOutOfBoundsException when the user just presses enter
    } catch {
      case e: Throwable => //It doesn't actually matter what error is thrown from the input.
      //We expected Enter to be pressed and we know it'll throw an error. If the user tried anything else,
      //and any other error was thrown, we'd still want to return the user back to the earlier screen.
    }
    state = en.account
  }

  //*******************************************************************************************************************
  def makeCurrentAccountHandler(): Unit = {
    //For the purposes of this task, up to 3 Current Accounts can be made for a single user. This limit is
    //imposed in the UserAccount file, under the createNewCurrentAccount and createNewSavingsAccount methods.
    println("Making a new Current Account for " + acc.getUsername)
    //A new account can be given a name. In other areas of this program when interacting with the user's current
    //accounts and savings accounts, the name is given to help the user identify each of them more easily.
    print("Please input a name for this Current Account to be known by: ")
    try {
      val s: String = scala.io.StdIn.readLine()
      if (acc.createNewCurrentAccount(s)) {
        println("Your new Current Account \"" + s + "\" has been created.")
      } else {
        println("You already have 3 Current Accounts. You cannot create more.")
      }
      state = en.account
    }catch{
      case e: StringIndexOutOfBoundsException => println("Your new account needs to have a name.")
    }
  }

  //*******************************************************************************************************************
  def makeSavingsAccountHandler(): Unit = {
    //3 savings account max. Identical to the above method, except that this method creates Savings Accounts instead.
    println("Making a new Savings Account for " + acc.getUsername)
    print("Please input a name for this Savings Account to be known by: ")
    val s: String = scala.io.StdIn.readLine()
    if (acc.createNewSavingsAccount(s)) {
      println("Your new Savings Account \"" + s + "\" has been created.")
      state = en.account
    } else {
      println("You already have 3 Savings Accounts. You cannot create more.")
      state = en.account
    }
  }

  //*******************************************************************************************************************
  def switchAccountHome(): Unit = {
    //The user can switch their active account to any other that they have created.
    //By doing so, they can use other features on this program to interact with that account instead.
    println("Which type of account would you like to switch to?")
    println("1) Current Account\n2) Savings Account\n3) Return")
    print("Input: ")
    try {
      val i: Int = scala.io.StdIn.readInt()
      println("\n\n\n")
      i match {
        case 1 => state = en.switchToCurrent
        case 2 => state = en.switchToSavings
        case 3 => state = en.home
        case _ => println("Your input did not match with any option. Please try again.")
      }
    } catch {
      case e: NumberFormatException => println("Your input did not match with any options. I don't think it was even a number! Please try again.")
    }
  }

  //*******************************************************************************************************************
  def switchToCurrentAccount(): Unit = {
    //Retrieve list of current accounts from the user logged in. Display it neatly.
    //Then think of how you can make a match statement with unknown amount of options line up with the list

    //The user has attempted to switch to one of their Current Accounts, so we retrieve the user's list of them.
    //Though it shouldn't be possible, since a Current Account is made by default when a user first makes an
    //account, and there is no way to delete them, a check is done to see if there are any Current Accounts in
    //the list. If not, we inform the user of this and go back to the earlier screen.
    val list: List[CurrentAccount] = acc.getCurrentAccountList
    if (list.length <= 0) {
      println("You do not have any Current Accounts to switch to.")
      state = en.switchAccount
      return
    }

    //Each entry in the list is output to the console with an incrementing number alongside it.
    //To make it easier for the user, the incrementing value is adjusted slightly to appear as
    //though it starts with 1, instead of 0 as list entries do.
    println("Please input the number corresponding to the account you'd like to switch to.")
    for (a <- list.indices) {
      println("Account " + (a + 1) + ") " + list(a).getAccName())
    }
    print("Input: ")
    try {
      //The user's input value is adjusted back to correspond to the list's starting values,
      //and using it as an index argument, we get the appropriate CurrentAccount from the list.
      //However, we save it to the variable as the superclass, Account, so that the variable
      //remains open to possibly being switched to a SavingsAccount later.
      val newAcc: Account = list(scala.io.StdIn.readInt() - 1)
      acc.switchActiveAccount(newAcc)
      println("Switched account to " + newAcc.getAccName())
    } catch {
      case e: IndexOutOfBoundsException => println("You picked a number out of range")
      case e: NumberFormatException => println("You didn't even type in a number!")
      case e: Throwable => println("Ok, what was that? " + e)
    } finally {
      state = en.switchAccount
    }
  }

  //*******************************************************************************************************************
  def switchToSavingsAccount(): Unit = {
    //Identical to the above method, except this method checks and deals with the list of SavingsAccounts
    //instead of CurrentAccounts. It is more likely that the user has no Savings Accounts, so checking
    //that the list's length is greater than 0 is more important here.
    val list: List[SavingsAccount] = acc.getSavingAccountList
    if (list.length <= 0) {
      println("You do not have any Savings Accounts to switch to.")
      state = en.switchAccount
      return
    }

    println("Please input the number corresponding to the account you'd like to switch to.")
    for (a <- list.indices) {
      println("Account " + (a + 1) + ") " + list(a).getAccName())
    }
    print("Input: ")
    try {
      val newAcc: Account = list(scala.io.StdIn.readInt() - 1)
      acc.switchActiveAccount(newAcc)
      println("Switched account to " + newAcc.getAccName())
    } catch {
      case e: IndexOutOfBoundsException => println("You picked a number out of range")
      case e: NumberFormatException => println("You didn't even type in a number!")
      case e: Throwable => println("Ok, what was that? " + e)
    } finally {
      state = en.switchAccount
    }

  }


  //*******************************************************************************************************************
  def adminOptions(): Unit = {
    //Menu screen for admins
    println("What would you like to do?")
    println("1) Search for User Account\n2) Delete User Account\n3) Return")
    print("Input: ")
    try {
      val i: Int = scala.io.StdIn.readInt()
      println("\n\n\n")
      i match {
        case 1 => state = en.searchForAccount
        case 2 => state = en.removeAccount
        case 3 => state = en.home
        case _ => println("Your input did not match with any option. Please try again.")
      }
    } catch {
      case e: NumberFormatException => println("Your input did not match with any options. I don't think it was even a number! Please try again.")
    }
  }

  //*******************************************************************************************************************
  def searchForAccountHandler(): Unit = {
    //This method provides different types of searches that can be performed on the accountHashMap to find users.
    println("What kind of search would you like to perform?")
    println("1) Strict search\n2) Search with partial input\n3) Up to 2 character typo in input\n4) Return")
    print("Input: ")
    try {
      val i: Int = scala.io.StdIn.readInt()
      if (i < 1 || i > 4) {
        println("\n\n\nYour input did not match with any option. Please try again.")
      } else if (i == 4) {
        state = en.admin
      } else {
        print("Please input your search term: ")
        val s: String = scala.io.StdIn.readLine()
        if (s.length <= 0) {
          println("No search term given. Searching for all usernames instead.")
          accountHashMap.foreach { case (k, v) => println(k) }
        } else {
          i match {
            case 1 => strictSearch(s)
            case 2 => partialInputSearch(s)
            case 3 => typoSearch(s)
          }
        }
      }
    }catch{
      case e: NumberFormatException => println("Your input did not match with any options. I don't think it was even a number! Please try again.")
    }

    //The strict search simply uses the exact term input by the user as a key for the accountHashMap.
    //If a result is found, the UserAccount value linked to that key is returned, and multiple method
    //calls are performed on the object to get information out of it.
    def strictSearch(s: String): Unit = {
      try {
        val a: UserAccount = accountHashMap(s)

        println("Account found with username: " + a.getUsername)
        println("Admin: " + a.isAdmin)
        println("Current accounts: " + a.getCurrentAccountList.length)
        println(a.getCurrentAccountList)
        println("Savings accounts: " + a.getSavingAccountList.length)
        println(a.getSavingAccountList)
      } catch {
        case e: NoSuchElementException => println("No user was found with that name.")
      } finally {
        state = en.admin
      }
    }

    //The partial input search takes the input from the user and heavily adjusts the string so that each
    //character is turned into a "upper case letter or lower case letter" or itself, and with each character
    //having wildcards placed between them.
    //In this way, the user only needs to know part of the username of the user they are looking for and can
    //get a result, assuming the characters they input are at least in order of appearance in the user's name,
    //even if they're not in the same index position along the name.
    def partialInputSearch(s: String): Unit = {
      import scala.util.matching.Regex

      //For each character in the string, make a "lower case or upper case" regex for each character and place
      //wildcards between each character. .concat only works for strings, so the "regex" needs to be a string
      //first and converted to a regex variable afterwards.
      var sa: String = ""
      for (c <- s) {
        sa = sa.concat("(" + c.toLower + "|" + c.toUpper + ")").concat("(.*)")
      }
      val r: Regex = sa.r

      //Make an empty sequence of strings. Then, for each key/value pairing in the accountHashMap, use
      //the regex on the username to see if there's any kind of match or not. If a match is found,
      //recreate the sequence using the current values it has, with the current foreach iteration's
      //username appended.
      //If the regex didn't find a match in the username, do nothing that time and continue the loop.
      var l: Seq[String] = Seq[String]()
      accountHashMap.foreach { case (k, v) =>
        r.findFirstIn(k) match {
          case Some(x) => l = l :+ k //Got a result, but findFirstIn's result starts from where the match began.
                                     //We want the full string though, so we append k to the seq instead.
          case None => //Didn't match, don't do anything this time and continue the loop
        }
      }

      //Once the hashmap iteration is done, we check whether the seq object is empty or not. Any result found means
      //the seq isn't empty, and therefore, we have something to give to the user.
      if (l.isEmpty) {
        println("No users were found with your search term.")
      } else {
        println("Usernames found using your search term: ")
        l.foreach {
          println
        }
      }
    }

    //The typo search assumes that the user has input up to two incorrect characters in place of where
    //a correct character should be. To resolve this, two threads run at the same time, one performing
    //a loop, dealing with a single typo, while the other performs a nested loop, dealing with two typos.
    //In the single loop, for each iteration of the loop, the character at that index position is replaced
    //with a single character wildcard and is used in a foreach iteration over the accountHashMap, comparing
    //the new search term to each key.
    //In the nested loop, the first loop does the same as the single loop, but the inner loop looks at the
    //remaining characters ahead of the character that the first loop is on, and replaces those characters
    //with single character wildcards too.
    //While the nested loop covers all the same search possibilities that the first loop can, this method
    //was intended to show the use of concurrently acting threads, and the ability to combine substrings and
    //regexes to create a better searching experience.
    def typoSearch(s: String): Unit = {
      import scala.util.matching.Regex

      //A HashSet is created for all the successful searches made later on. As a HashSet cannot contain
      //duplicates, it is incredibly useful as this type of search is likely to find the same result
      //multiple times.
      val l: mutable.HashSet[String] = mutable.HashSet()

      //This class contains all the behaviour for the first thread of the two.
      //By extending Thread and overriding the run() method, it becomes possible to initialise a thread
      //that can act separately and concurrently with the rest of this program.
      class th1 extends Thread {
        override def run(): Unit = {
          //From 0 until the length of the search term string, the loop's iteration is saved as "a"
          //and is used to determine where to create separate substrings. It effectively creates a substring
          //from the start of the search term up until the character we want to replace with a wildcard, then
          //creates another substring, starting from the character after the wildcard-replaced character, until
          //the end of the string. Between the two substrings, a regex is given for the single character wildcard.
          //The string is turned into a Regex object and is used in a foreach iteration over the accountHashMap
          //to see if there's a match with any of the username keys. If there is, the key is added to the earlier
          //HashSet, as we want the full username for later, unchanged.
          var sa: String = s
          for (a <- 0 until s.length) {
            sa = s.substring(0, a) + "(.)" + s.substring(a+1)
            val r: Regex = sa.r
            //println("Searching for " + sa)
            accountHashMap.foreach { case (k, v) =>
              r.findFirstIn(k) match {
                case Some(x) => l += k
                case None =>
              }
            }
          }
        }
      }

      //This class contains the behaviour for the second thread.
      class th2 extends Thread {
        override def run(): Unit = {
          //Similar to the above, however, the inner loop and need for a second wildcard means
          //that the search term is split up further. The inner loop always starts from wherever
          //the outer loop currently is and proceeds to the end of the string from there.
          //The loop variables are adjusted in the substring commands to prevent them from going
          //beyond the string's length, as well as the inner loop's final iteration.
          var sa: String = s
          for (a <- 0 until s.length) {
            for(b <- a until s.length-1){
              sa = s.substring(0,a)+ "(.)" + s.substring(a+1,b+1) + "(.)" + s.substring(b+2)
              val r: Regex = sa.r
              //println("Searching for " + sa)
              accountHashMap.foreach { case (k, v) =>
                r.findFirstIn(k) match {
                  case Some(x) => l += k
                  case None =>
                }
              }
            }
          }
        }
      }

      //The two threads are created here
      val t1 = new th1
      val t2 = new th2

      //and the start method is called on both of them, letting themselves be initiated as a new process
      //that can run concurrently. The second thread's join method is called so that this initial program
      //waits for it to complete its task before proceeding. The second thread, with its nested loop, is
      //likely to take longer than the first thread, and calling the first thread's join method would cause
      //the second thread to wait for the first thread to finish before acting, defeating the purpose of
      //running two threads at the same time.
      t1.start()
      t2.start()
      t2.join()

      //The HashMap in which search results can be added to is checked here. If it is empty, we print to
      //the user that no users were found, but if something was found, we simply iterate over the HashMap,
      //printing each result.
      if (l.isEmpty) {
        println("No users were found with your search term.")
      } else {
        println("Usernames found using your search term: ")
        l.foreach {
          println
        }
      }
    }
  }

  //*******************************************************************************************************************

  //A simple method where the user is able to input a username, which is used to search the accountHashMap.
  //If a result is found, that entry, the key/value pairing, is deleted, meaning the entire UserAccount linked
  //to that user, and all of their bank accounts and money, are gone.
  def removeAccountHandler(): Unit = {
    println("Press Enter without input to return.")
    print("Enter the username of the user account you wish to delete: ")
    try {
      val s: String = scala.io.StdIn.readLine()
      if (s.length <= 0) {
        state = en.admin
        return
      }
      if (accountHashMap.contains(s)) {
        accountHashMap.remove(s)
        println("User " + s + " has been deleted.")
      } else {
        println("No user with that name was found")
      }
    } catch {
      case e: Throwable => state = en.admin
    }
  }

  //*******************************************************************************************************************
  def logOutHandler(): Unit = {
    //Clears the "acc" variable from whichever UserAccount was just logged in, and returns the user to
    //the program's start screen.
    acc = null
    state = en.start
  }

  //*******************************************************************************************************************
  //In this method, UserAccount objects, and their List objects for CurrentAccounts and SavingsAccounts, are saved
  //to a file, while usernames and passwords are saved to a MySQL database.
  def exitProgramHandler(): Unit = {
    import java.io._

    println("Saving user accounts to MySQL and to file")

    try {
      //Using java libraries imported earlier in this method, a FileOutputStream is set up with a .txt file.
      //An ObjectOutputStream is also set up, using the FileOutputStream to determine its destination.
      val fos: FileOutputStream = new FileOutputStream(new File("CallumUserAccounts.txt"))
      val oos: ObjectOutputStream = new ObjectOutputStream(fos)

      accountHashMap.foreach { case (k, v) =>
        //For each key/value pairing in the accountHashMap, we take the key, knowing that it's the username,
        //and call the value's getPassword method. These are saved ahead of time for the MySQL section later on.
        val un = k
        val pw = v.getPassword

        //The value can immediately be written as an Object to the output stream, resulting in it being added
        //to the file.
        oos.writeObject(v)

        //The username key is used to complete a filename for a new pair of FileOutputStream and
        //ObjectOutputStream.
        val fosi: FileOutputStream = new FileOutputStream(new File(k + ".txt"))
        val oosi: ObjectOutputStream = new ObjectOutputStream(fosi)

        //The CurrentAccount List and SavingsAccount List are taken from the user's account
        val list1 = v.getCurrentAccountList
        val list2 = v.getSavingAccountList

        //and written to the file named after the user.
        oosi.writeObject(list1)
        oosi.writeObject(list2)

        //A MySQL statement is made to replace values in the userAccountsCallum table.
        //This avoids errors when a duplicate is found, as the values of that duplicate are updated instead.
        val insertMySQL = """replace into userAccountsCallum (username,password) values(?,?)"""
        val preparedStatement: PreparedStatement = connection.prepareStatement(insertMySQL)
        preparedStatement.setString(1, un)
        preparedStatement.setString(2, pw)
        preparedStatement.execute()
      }

      //The file output and object output streams are then closed
      fos.close()
      oos.close()

    }catch{
      case e: FileNotFoundException => println("Could not find the file to write to.")
      case e: IOException => println("Error initialising stream for object output to file.")
      case e: ClassNotFoundException => e.printStackTrace()
    }

    //With the files successfully written and the database updated, it's time to set programRunning to
    //false, so that when this program returns to the mainLoop, it can finally escape it and reach the
    //end of the program.
    println("Thank you for doing things!")
    programRunning = false
  }

  //*******************************************************************************************************************

  //*******************************************************************************************************************

  //*******************************************************************************************************************

  //The Personal Loan method makes use of this ternary operator to decide on a value to return, based on the
  //argument given to this method. The values returned represent an interest amount charged per month.
  def personalLoanInterestAmt(loanAmt: Double): Double = if (loanAmt < 1000.0) 15.0
  else if (loanAmt >= 1000.0 && loanAmt < 5000.0) 10.0
  else if (loanAmt >= 5000.0 && loanAmt < 10000.0) 7.5
  else 5.0

  //Recursive method
  @throws(classOf[RepaymentTooLongException])
  def loanRepayTime(loanAmt: Double, incomeAmt: Double, interestAmt: Double, incomePercentToRepay: Double, maxMonthsToRepay: Int, monthsToRepay: Int): Int = {

    //To begin with, each time this method is called from within this method, monthsToRepay is incrementing more
    //and more. If it has reached a point where it is greater than the maximum amount of months we have given
    //this loan to be repaid, we throw an exception to exit out of this recursive method entirely.
    if(monthsToRepay>maxMonthsToRepay){
      throw new RepaymentTooLongException("Personal loan repayment will take longer than " + maxMonthsToRepay + " months to repay. Loan rejected.")
    }

    //The percentage amount of income used (10% for Personal Loan, 75% for mortgage) is divided by 100, and the
    //income amount itself is multiplied by that to determine the percentage of the income that can be used for
    //the rest of this method. Because of the calculation, the result is formatted back to having only up to
    //2 decimal places.
    //For example, the user's income is 1000 and they attempted to take a mortgage out.
    //The below line becomes 1000 * (75/100) = 750
    val div: Double = incomeAmt * (incomePercentToRepay / 100.0)
    val income: Double = f"$div%9.2f".toDouble

    //If the remaining amount of loan to pay off is already at or below 0, this method immediately returns the
    //monthsToRepay value as it was given to this instance of the method call.
    if (loanAmt <= 0.0) {
      monthsToRepay
    } else {
      //The interest amount is used as a percentage against the remaining amount of loan left to pay after the
      //percentage of the user's income was taken off. This calculates the amount of money added onto the loan
      //thanks to interest, after the remaining loan has already had the percentage of the user's income taken off.
      //For example, the loan amount is 100,000 and the percentage of the user's income is 750, with an interest
      //amount of 5.
      //((loanAmt - income) * (interestAmt / 100.0 )) becomes ((100,000 - 750) * (5 / 100.0)) = 4962.5
      //That then means that (loanAmt - income) + 4962.5 becomes (100,000 - 750) + 4962.5 = 104,212.5
      val remainingAmt: Double = (loanAmt - income) + ((loanAmt - income) * (interestAmt / 100.0))
      //If remainingAmt is higher than the method's initial loanAmt, you know you're not going to be able to
      //repay the loan, as the amount to repay each month is only going up. Break from this method straight away.
      if (remainingAmt > loanAmt) {
        throw new RepaymentTooLongException("Your income is too low to be able to pay off this loan. Personal loan rejected.")
      } else {
        //However, if the amount left to pay is lower than how it started, there's potential to be able to repay it all.
        //With the new amount of loan left to repay, a call to this method is done once again, with monthsToRepay
        //increasing by 1.
        //In this way, each call to this method represents one month of being able to pay off the loan, and the value
        //of r becomes the amount of times that this method has been called, or the amount of months it would take to
        //repay the entire loan.
        val r: Int = loanRepayTime(remainingAmt, incomeAmt, interestAmt, incomePercentToRepay, maxMonthsToRepay, monthsToRepay+1)

        //When (loanAmt<=0.0) is true, the method will return a value back here, going through each of the earlier
        //calls to this method from this point, each time passing the value of "r" back to the earlier call until
        //it can return to the Personal Loan or Mortgage method that started this recursive method off.
        r
      }
    }
  }
}
