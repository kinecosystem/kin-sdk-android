package kin.sdk.internal.services.helpers

interface EventListener<T> {

    fun onEvent(data: T)
}
