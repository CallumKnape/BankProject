package BankProject

//A list of potential exceptions that could occur throughout the program.
//At this point, all exceptions simply print the message given to them and nothing more.
class UserNotFoundException(msg: String = "") extends Exception(msg){
  //When a user was not found in the accountHashMap
}

class UsernameAlreadyTakenException(msg: String = "") extends Exception(msg){
  //When a user attempts to create a new user using a name that was already taken
}

class PasswordMismatchException(msg: String = "") extends Exception(msg){
  //When a user attempts to create a new user, but the two passwords they input do not match
}

class MonetaryValueBelowZeroException(msg: String = "") extends Exception(msg){
  //For when you try to withdraw or deposit -£17 or something. Let's not deal with negative numbers.
}

class LoanOutOfRangeException(msg: String = "") extends Exception(msg){
  //When a user attempts to withdraw a Personal Loan or a Mortgage, but asked for a value outside the given range.
}

class RepaymentTooLongException(msg: String = "") extends Exception(msg){
  //When a user attempts to withdraw a loan, but their income is too low, resulting in it taking too long to
  //repay the loan within a reasonable amount of time.
}

