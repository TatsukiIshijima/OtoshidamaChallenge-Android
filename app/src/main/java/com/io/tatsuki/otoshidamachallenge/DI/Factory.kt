package com.io.tatsuki.otoshidamachallenge.DI

interface Factory<T> {
    fun create() :  T
}