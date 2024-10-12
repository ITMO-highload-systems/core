package org.example.notion.app.user

object UserContext {
    private val currentUser: ThreadLocal<Long> = ThreadLocal()

    fun setCurrentUser(user: Long) {
        currentUser.set(user)
    }

    fun getCurrentUser(): Long {
        return currentUser.get()
    }

    fun clear() {
        currentUser.remove()
    }
}