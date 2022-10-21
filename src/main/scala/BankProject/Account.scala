package BankProject

import java.io.Serializable

@SerialVersionUID(114L)
abstract class Account extends Serializable{
  var money: Double
  var accName: String
  //What else do savings accounts and current accounts have in common?

  //We could make checkBalance a String that's called from a println()
  //withdrawMoney an Int?
  //What about depositMoney?
  def withdrawMoney(request: Double): Boolean
  def depositMoney(request: Double): Boolean
  def checkBalance(): Double
  def getAccName(): String = { accName }
}

case class SavingsAccount(name: String = "") extends Account{

  override var money = 0.0
  override var accName = name

  override def withdrawMoney(request: Double): Boolean ={
    //Did you want different logic here because savings account?
    //You shouldn't allow a typical withdrawal, but you should allow a transfer to another account, right?
    //But you can't do that here. You can't tell which method called this one. So the behaviour needs to
    //be in BankProject.Main.scala
    if(money-request <0.0 || request <= 0.0){
      false
    }else{
      money -= request
      true
    }
  }
  override def depositMoney(request: Double): Boolean ={
    if(money+request>85000.00 || request <= 0.0){
      false
    }else{
      money += request
      true
    }
  }
  override def checkBalance(): Double ={
    money
  }
}

case class CurrentAccount(name: String = "") extends Account{

  override var money = 0.0
  override var accName = name

  override def withdrawMoney(request: Double): Boolean ={
    //We can make a limit to how much money can be withdrawn at once and throw an exception if it's too high

    //Refine this if statement. You're catching everything together here. Separate them for different exceptions,
    //then remove the default message back in BankProject.Main.scala
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

