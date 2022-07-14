package com.pnam.note.utils

object RetrofitUtils {
    private const val PORT: Int = 3000
    private const val STATIC_IP: String = "192.168.20.112"
    private const val IP_URL: String = "$STATIC_IP:$PORT"
    const val BASE_URL: String = "http://$IP_URL/"

    const val NOT_FOUND: Int = 404
    const val CONFLICT: Int = 409
    const val SUCCESS: Int = 200
    const val INTERNAL_SERVER_ERROR: Int = 500
}