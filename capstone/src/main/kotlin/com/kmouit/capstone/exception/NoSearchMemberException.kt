package com.kmouit.capstone.exception

import org.springframework.http.HttpStatus

class NoSearchMemberException ( httpStatus: HttpStatus, message :String) :RuntimeException(message) {

}