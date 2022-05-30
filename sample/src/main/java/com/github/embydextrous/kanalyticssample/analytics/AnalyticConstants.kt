package com.github.embydextrous.kanalyticssample.analytics

class AnalyticConstants {

    object Event {
        const val BUTTON_CLICKED = "Button Clicked"
        const val APP_STARTED = "App Started"
    }

    object Property {
        const val APP_VERSION_CODE: String = "App Version Code"
        const val APP_VERSION_NAME: String = "App Version Name"
        const val OS: String = "Os"
        const val OS_VERSION: String = "Os Version"
        const val BRAND: String = "Brand"
        const val MANUFACTURER: String = "Manufacturer"
        const val MODEL: String = "Model"
        const val TIMEZONE: String = "Timezone"

        const val PHONE = "Phone"
        const val EMAIL = "Email"
        const val NAME = "Name"

        const val BUTTON_TEXT = "Button Text"

        const val FIRST_TIME_APP_START = "First Time App Start"
        const val TOTAL_TIME_APP_START = "Total Time App Start"
        const val LAST_TIME_APP_START = "Last Time App Start"

        const val DUMMY_RANDOM_PROPERTY = "Dummy Random Property"
    }
}
