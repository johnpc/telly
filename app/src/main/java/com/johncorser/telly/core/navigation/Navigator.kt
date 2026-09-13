package com.johncorser.telly.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Hand-rolled typed back stack (see CLAUDE.md decisions log). The UI renders
 * the top of [stack]; BACK pops one entry via [pop].
 */
class Navigator(
    start: Route = Route.Welcome,
) {
    private val mutableStack = MutableStateFlow(listOf(start))

    /** The full back stack, oldest first; never empty. */
    val stack: StateFlow<List<Route>> = mutableStack.asStateFlow()

    /** The route currently on screen. */
    val current: Route get() = mutableStack.value.last()

    fun push(route: Route) {
        mutableStack.update { it + route }
    }

    /** Pops one entry. Returns false (and does nothing) at the root. */
    fun pop(): Boolean {
        if (mutableStack.value.size <= 1) return false
        mutableStack.update { it.dropLast(1) }
        return true
    }

    /** Clears history and makes [route] the new root (e.g. after onboarding). */
    fun replaceAll(route: Route) {
        mutableStack.value = listOf(route)
    }
}
