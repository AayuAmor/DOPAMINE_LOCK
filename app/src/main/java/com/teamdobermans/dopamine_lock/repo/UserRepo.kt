package com.teamdobermans.dopamine_lock.repo

import com.teamdobermans.dopamine_lock.model.UserModel

interface UserRepo {

//    {
//    "success": true
//     "message": "OTP link has been sent to your email"
//    }

    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit
    )


    fun forgetPassword(
        email: String,
        callback: (Boolean, String) -> Unit
    )

    //    {
//    "success": true
//     "message": "all user fetched"
//    }

    fun getUserById(
        id: String,
        callback: (Boolean, String, UserModel?) -> Unit
    )

    fun getAllUser(callback: (Boolean, String, List<UserModel?>) -> Unit)


    fun logout(callback: (Boolean, String) -> Unit)

    //    {
//    "success": true
//     "message": "registered",
//    "userId":""fsdgdfhrtserawedsfdg"
//    }
    //authentication
    fun register(
        email: String, password: String,
        callback: (Boolean, String, String) -> Unit
    )

    //    {
//    "success": true
//     "message": "registered",
//    }
    //real-time database
    fun addUser(id: String, model: UserModel, callback: (Boolean, String) -> Unit)

    fun editProfile(id: String, model: UserModel, callback: (Boolean, String) -> Unit)

    fun deleteUser(id: String,callback: (Boolean, String) -> Unit)
}