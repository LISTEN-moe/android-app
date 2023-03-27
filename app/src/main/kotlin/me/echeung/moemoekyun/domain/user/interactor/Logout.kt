package me.echeung.moemoekyun.domain.user.interactor

import me.echeung.moemoekyun.domain.user.UserService
import javax.inject.Inject

class Logout @Inject constructor(
    private val userService: UserService,
) {

    fun logout() {
        return userService.logout()
    }
}
