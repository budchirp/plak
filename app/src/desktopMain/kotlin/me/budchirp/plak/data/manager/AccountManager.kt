package me.budchirp.plak.data.manager

import me.budchirp.plak.CONFIG_FILE
import me.budchirp.plak.data.model.Account
import me.budchirp.plak.utils.JSON
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isReadable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import kotlin.io.path.writeText

class AccountManager {
    private val accountsDir = CONFIG_FILE.parent.resolve("accounts")

    init {
        if (!accountsDir.exists()) {
            accountsDir.createDirectories()
        }
    }

    fun get(username: String): Account {
        return JSON.parse(Account.serializer(), accountsDir.resolve(username).readText())
    }

    fun getAll(): List<Account> {
        val instancePaths = accountsDir.listDirectoryEntries().filter { it.isReadable() }

        return instancePaths.map { get(it.fileName.fileName.toString()) }
    }

    fun set(account: Account) {
        accountsDir.resolve("${account.username}.json").writeText(JSON.stringify(Account.serializer(), account))
    }
}