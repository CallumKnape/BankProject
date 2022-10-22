package BankProject

import java.io.Serializable

//This abstract class extends Java's Serializable class and is given a SerialVersionUID Long to allow it to be
//written to, and read from a file
@SerialVersionUID(114L)
abstract class Account extends Serializable{
  //As an abstract class, variables and methods are declared without a body or a value.
  //Classes that extend this abstract class are forced to assign these variables themselves,
  //and to define the methods' behaviours
  var money: Double
  var accName: String

  def withdrawMoney(request: Double): Boolean
  def depositMoney(request: Double): Boolean
  def checkBalance(): Double
  def getAccName(): String = { accName }
}

case class SavingsAccount(name: String = "") extends Account{

  //By extending Account, these two variables must be overridden with an assigned value.
  override var money = 0.0
  override var accName = name

  //The methods must also be defined.
  override def withdrawMoney(request: Double): Boolean ={
    //The "request" parameter refers to the amount of money that has been asked to withdraw from this account.
    //If that amount is taken off this account's current amount of money and results in less than 0, or if the
    //requested amount is 0 or a negative number, we reject the request by returning false and performing no
    //other actions.
    if(money-request <0.0 || request <= 0.0){
      false
    }else{
      //Otherwise, we take away the requested amount from this account's money and return true.
      money -= request
      true
    }
  }
  override def depositMoney(request: Double): Boolean ={
    //For the purposes of this project, SavingsAccounts cannot hold more than £85,000.
    //This if statement ensures that this limit is not exceeded by the money coming in, nor does the requested amount
    //equal 0 or less.
    if(money+request>85000.00 || request <= 0.0){
      false
    }else{
      //If the amount to deposit is acceptable, we simply add it to this account's current money and return true
      money += request
      true
    }
  }
  override def checkBalance(): Double ={
    //Effectively a getter method, returning the amount of money in this account
    money
  }
}

//Almost identical to the SavingsAccount class above, except that this class does not have a £85,000 limit
case class CurrentAccount(name: String = "") extends Account{

  override var money = 0.0
  override var accName = name

  override def withdrawMoney(request: Double): Boolean ={
    if (money - request < 0.0 || request <= 0.0) {
      false
    } else {
      money -= request
      true
    }
  }
  override def depositMoney(request: Double): Boolean ={
    if(request <= 0.0){
      false
    }else {
      money += request
      true
    }
  }
  override def checkBalance(): Double ={
    money
  }
}
