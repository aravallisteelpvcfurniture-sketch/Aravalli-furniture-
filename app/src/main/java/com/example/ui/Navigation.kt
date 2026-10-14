package com.example.ui

enum class AppScreen {
    HOME,
    CATALOG,
    SCAN,
    CARPENTERS,
    PROFILE
}

enum class SubScreen {
    NONE,
    ESTIMATE_LIST,
    ESTIMATE_NEW,
    GREETINGS,
    VISITOR_LIST,
    VISITOR_NEW,
    CONFIRM_ORDER_LIST,
    CONFIRM_ORDER_NEW,
    CARPENTER_LIST,
    CARPENTER_NEW,
    SALES_TRACKING
}

data class ChartDayData(
    val dayNumber: Int,
    val estimateAmount: Float,
    val orderAmount: Float
)
