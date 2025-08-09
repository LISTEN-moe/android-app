package me.echeung.moemoekyun.domain.user.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.echeung.moemoekyun.domain.user.UserService
import me.echeung.moemoekyun.domain.user.model.DomainUser
import me.echeung.moemoekyun.domain.user.model.UserConverter
import javax.inject.Inject

class GetAuthenticatedUser @Inject constructor(
    private val userService: UserService,
    private val userConverter: UserConverter,
) {

    fun asFlow(): Flow<DomainUser?> = userService.state.map { get() }

    fun get(): DomainUser? {
        if (!userService.isAuthenticated || userService.state.value.user == null) {
            return null
        }

        return userService.state.value.user?.let(userConverter::toDomainUser)
    }

    fun isAuthenticated(): Boolean = userService.isAuthenticated
}
