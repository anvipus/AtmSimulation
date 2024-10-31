import java.util.Scanner

fun main() {
    val atm = ATM()
    atm.login("Alice")
    atm.deposit(100)
    atm.withdraw(50)
    atm.transfer("Bob", 30)
    atm.logout()
    val scanner = Scanner(System.`in`)

    while (true) {
        if (!scanner.hasNextLine()) break // Checks if there’s more input
        val input = scanner.nextLine().split(" ")

        when (input[0].lowercase()) {
            "login" -> atm.login(input[1])
            "logout" -> atm.logout()
            "deposit" -> atm.deposit(input[1].toInt())
            "withdraw" -> atm.withdraw(input[1].toInt())
            "transfer" -> atm.transfer(input[1], input[2].toInt())
            "exit" -> break
            else -> println("Invalid command.")
        }
    }
}

data class Customer(
    val name: String,
    var balance: Int = 0,
    var owes: MutableMap<String, Int> = mutableMapOf()
)

class ATM {
    private val customers = mutableMapOf<String, Customer>()
    private var currentCustomer: Customer? = null

    fun login(name: String) {
        currentCustomer = customers.getOrPut(name) { Customer(name) }
        println("Hello, $name!")
        showBalance()
    }

    fun logout() {
        if (currentCustomer != null) {
            println("Goodbye, ${currentCustomer!!.name}!")
            currentCustomer = null
        } else {
            println("No customer is currently logged in.")
        }
    }

    fun deposit(amount: Int) {
        val customer = currentCustomer ?: return println("Please log in first.")
        customer.balance += amount
        settleDebt(customer, amount)
        showBalance()
    }

    fun withdraw(amount: Int) {
        val customer = currentCustomer ?: return println("Please log in first.")
        if (customer.balance >= amount) {
            customer.balance -= amount
            println("Your balance is \$${customer.balance}")
        } else {
            println("Insufficient balance. Your balance is \$${customer.balance}")
        }
    }

    fun transfer(targetName: String, amount: Int) {
        val customer = currentCustomer ?: return println("Please log in first.")
        if (customer.name == targetName) {
            println("Cannot transfer to yourself.")
            return
        }

        val target = customers.getOrPut(targetName) { Customer(targetName) }
        if (customer.balance >= amount) {
            customer.balance -= amount
            target.balance += amount
            println("Transferred \$$amount to ${target.name}")
        } else {
            val remainingAmount = amount - customer.balance
            target.balance += customer.balance
            target.owes[customer.name] = target.owes.getOrDefault(customer.name, 0) + remainingAmount
            println("Transferred \$$customer.balance to ${target.name}")
            println("Owed \$$remainingAmount to ${target.name}")
            customer.balance = 0
        }
        showBalance()
    }

    private fun settleDebt(customer: Customer, amount: Int) {
        customer.owes.forEach { (creditor, debt) ->
            val payment = minOf(amount, debt)
            if (payment > 0) {
                val creditorCustomer = customers[creditor]
                creditorCustomer?.balance = (creditorCustomer?.balance ?: 0) + payment
                customer.owes[creditor] = debt - payment
            }
        }
        customer.owes = customer.owes.filterValues { it > 0 }.toMutableMap()
    }

    private fun showBalance() {
        val customer = currentCustomer ?: return
        println("Your balance is \$${customer.balance}")
        customer.owes.forEach { (creditor, amount) ->
            println("Owed \$$amount to $creditor")
            }
        }
}