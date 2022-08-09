package com.pnam.note.ui.adapters

import com.pnam.note.database.data.models.EmailPassword

interface LoginItemClickListener {
    fun onClick(emailPassword: EmailPassword)
    fun onDeleteClick(email: String, position: Int)
}