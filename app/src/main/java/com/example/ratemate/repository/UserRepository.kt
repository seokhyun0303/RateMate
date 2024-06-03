package com.example.ratemate.repository

import com.example.ratemate.data.User
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserRepository() {

    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("user")

    // 사용자 추가
    fun addUser(user: User) {
        val userId = user.userId.takeIf { it.isNotBlank() } ?: dbRef.push().key ?: return
        user.userId = userId
        dbRef.child(userId).setValue(user)
    }

    // 사용자 삭제
    fun deleteUser(userId: String) {
        dbRef.child(userId).removeValue()
    }

    // 사용자 업데이트
    fun updateUser(userId: String, updatedFields: Map<String, Any>) {
        dbRef.child(userId).updateChildren(updatedFields)
    }

    // 모든 사용자 조회
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                trySend(users)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    // 특정 사용자 조회
    fun getUser(userId: String): Flow<User?> = callbackFlow {
        val listener = dbRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                trySend(user)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { dbRef.child(userId).removeEventListener(listener) }
    }
}