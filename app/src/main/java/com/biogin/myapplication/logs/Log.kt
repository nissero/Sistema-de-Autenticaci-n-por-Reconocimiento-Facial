package com.biogin.myapplication.logs

class Log (val logEventType : LogEventType, val logEventName : LogEventName,
           val dniMasterUser : String, val dniUserAffected : String,
           val userCategory : String,
           val timestamp : String){


    enum class LogEventName(val value: String) {
        USER_UPDATE("USER UPDATE"),
        USER_DNI_UPDATE("USER DNI UPDATE"),
        USER_CREATION("USER CREATION"),
        USER_INACTIVATION("USER INACTIVATION"),
        SECURITY_SUCCESSFUL_LOGIN("SECURITY SUCCESSFUL LOGIN"),
        SECURITY_UNSUCCESSFUL_LOGIN("SECURITY UNSUCCESSFUL LOGIN"),
        RRHH_SUCCESSFUL_LOGIN("RRHH SUCCESSFUL LOGIN"),
        RRHH_UNSUCCESSFUL_LOGIN("RRHH UNSUCCESSFUL LOGIN"),
        ADMIN_SUCCESSFUL_LOGIN("ADMIN SUCCESSFUL LOGIN"),
        ADMIN_UNSUCCESSFUL_LOGIN("ADMIN UNSUCCESSFUL LOGIN"),
        USER_UNSUCCESSFUL_AUTHENTICATION("USER UNSUCCESSFUL AUTHENTICATION"),
        USER_SUCCESSFUL_AUTHENTICATION("USER SUCCESSFUL AUTHENTICATION"),
        START_OF_SHIFT("START OF SHIFT"),
        END_OF_SHIFT("END OF SHIFT"),
        EMPTY_LOG(""),
        GRANT_TEMPORAL_ACCESS("GRANT TEMPORTAL ACCESS"),
        USER_TEMPORAL_INACTIVATION("USER TEMPORAL INACTIVATION")
    }

    companion object {
        fun getLogEventNameFromValue(value : String) : LogEventName {
            for (entry in LogEventName.entries) {
                if (entry.value == value) {
                    return entry
                }
            }

            return LogEventName.EMPTY_LOG
        }
    }
    enum class LogEventType(s: String) {
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR")
    }

}