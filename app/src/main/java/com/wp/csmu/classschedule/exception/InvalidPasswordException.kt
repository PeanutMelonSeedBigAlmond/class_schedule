package com.wp.csmu.classschedule.exception

class InvalidPasswordException(val msg: String = "用户名或密码错误") : LoginException(msg)