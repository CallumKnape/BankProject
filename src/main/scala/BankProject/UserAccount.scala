package BankProject

import java.io.Serializable

//This class extends Java's Serializable class and is given a SerialVersionUID Long to allow it to be
//written to, and read from a file
@SerialVersionUID(114L)
class UserAccount (admin: Boolean, un: String, pw: String, list1: List[CurrentAccount], list2: List[SavingsAccount])  extends Serializable{

  private val adm: Boolean = admin
  private val username: String = un
  private val password: String = pw

  var currAccs: List[CurrentAccount] = list1
  var savAccs: List[SavingsAccount] = list2

  //Regardless of whether a list was provided to this class, or one had to be made using the auxiliary
  //constructor later, the activeAccount is set to the first element of the CurrentAccounts list.
  var activeAccount: Account = currAccs.head

  //When a new user account is first made, it won't have a list of Current Accounts or Savings Accounts to pass to
  //this class. Therefore, this constructor is used instead, calling the main constructor with a CurrentAccount list
  //with one new CurrentAccount entry, and an empty SavingsAccount list.
  def this(admin: Boolean, un: String, pw: String){
    this(admin,un,pw, List[CurrentAccount](new CurrentAccount("Main current account")), List[SavingsAccount]())
  }

  //Getter methods that return this class' values.
  def isAdmin: Boolean = { adm }
  def getUsername: String = { username }
  def getPassword: String = { password }
  def getAccount: Account = { activeAccount }

  def getCurrentAccountList: List[CurrentAccount] = { currAccs }
  def getSavingAccountList: List[SavingsAccount] = { savAccs }

  //When the user attempts to make a new Current Account for themselves, this method is called.
  //It checks that the user hasn't already got 3 or more Current Accounts, and if so, creates a
  //new one. currAccs :+ newAcc doesn't exactly mean that the account is being added to the list,
  //but rather that an entirely new list is made, using all the contents of the current list, plus
  //the newAcc.
  def createNewCurrentAccount(accountName: String = ""): Boolean ={
    if(currAccs.length<3) {
      val newAcc: CurrentAccount = CurrentAccount(accountName)
      currAccs = currAccs :+ newAcc
      true
    }else{
      false
    }
  }

  //Almost identical to the above, just with the SavingsAccount list.
  def createNewSavingsAccount(accountName: String = ""): Boolean ={
    if(savAccs.length<3) {
      val newAcc: SavingsAccount = SavingsAccount(accountName)
      savAccs = savAccs :+ newAcc
      true
    }else{
      false
    }
  }

  def switchActiveAccount(acc: Account): Boolean ={
    try {
      activeAccount = acc
      true
    }catch{
      case e: Throwable => println("Failed to switch account: " + e); false
    }
  }
}
