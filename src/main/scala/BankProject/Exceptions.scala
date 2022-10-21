package BankProject

class UserNotFoundException(msg: String = "") extends Exception(msg){ }

class UsernameAlreadyTakenException(msg: String = "") extends Exception(msg){ }

class PasswordMismatchException(msg: String = "") extends Exception(msg){ }

class MonetaryValueBelowZeroException(msg: String = "") extends Exception(msg){
  //For when you try to withdraw or deposit -£17 or something. Let's not deal with negative numbers.
}

class LoanOutOfRangeException(msg: String = "") extends Exception(msg){ }

class RepaymentTooLongException(msg: String = "") extends Exception(msg){ }

class IntOutOfRangeException(msg: String = "") extends Exception(msg){ }

//What else?
//You're going to get more dealing with money. Not enough in account, exceeding limits etc.
