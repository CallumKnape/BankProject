package BankProject

import java.io.Serializable

@SerialVersionUID(114L)
class UserAccount (admin: Boolean, un: String, pw: String)  extends Serializable{

  //Contains details about the user.

  private val adm: Boolean = admin
  private val username: String = un
  private val password: String = pw

  var currAccs: List[CurrentAccount] = List[CurrentAccount](new CurrentAccount("Main current account"))
  var savAccs: List[SavingsAccount] = List[SavingsAccount]()

  var activeAccount: Account = currAccs.head


  //Create a normal account on creation.
  //A savings account needs to be created on demand later.
  //Should these be saved as a list? A user could have more than one, right?
  //It's recommended to have multiple savings account!
  //One for your dream car, one for your holiday, one for emergencies etc.

  def isAdmin: Boolean = { adm }
  def getUsername: String = { username }
  def getPassword: String = { password }
  def getAccount: Account = { activeAccount }

  def getCurrentAccountList: List[CurrentAccount] = { currAccs }
  def getSavingAccountList: List[SavingsAccount] = { savAccs }

  //It'd be nice to give names to your accounts
  //That way, when the user wants to switch to a different account, they can use names to identify them
  def createNewCurrentAccount(accountName: String = ""): Boolean ={
    if(currAccs.length<3) {
      val newAcc: CurrentAccount = CurrentAccount(accountName)
      currAccs = currAccs :+ newAcc
      true
    }else{
      false
    }
  }

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




