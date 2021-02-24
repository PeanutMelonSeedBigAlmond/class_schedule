package com.wp.csmu.classschedule.exception

open class LoginException : Exception {
    constructor(msg: String) : super(msg)
    constructor() : super()
}