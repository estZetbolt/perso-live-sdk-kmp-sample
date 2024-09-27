package com.example.androidapp

class OneTimeEvent<T>(value: T) {

    private var value: T? = value

    fun getValue(): T? {
        val ret = value
        if (ret != null) {
            value = null
        }

        return ret
    }

}