package com.biogin.myapplication.logs

class Log (val logEventType : LogEventType, val logEventName : LogEventName,
           val dniMasterUser : String, val dniUserAffected : String,
           val userCategory : String,
           val timestamp : String){


    enum class LogEventName(val value: String) {
        FIREBASE_SUCCESSFUL_CONNECTION("FIREBASE DATABASE SUCCESSFUL CONNECTION"),
        BACK4APP_SUCCESSFUL_CONNECTION("BACK4APP DATABASE SUCCESSFUL CONNECTION"),
        USER_UPDATE("USER UPDATE"),
        USER_DNI_UPDATE("USER DNI UPDATE"),
        USER_CREATION("USER CREATION"),
        USER_ACTIVATION("USER ACTIVATION"),
        USER_INACTIVATION("USER INACTIVATION"),
        SECURITY_SUCCESSFUL_LOGIN("SECURITY SUCCESSFUL LOGIN"),
        SECURITY_UNSUCCESSFUL_LOGIN("SECURITY UNSUCCESSFUL LOGIN"),
        RRHH_SUCCESSFUL_LOGIN("RRHH SUCCESSFUL LOGIN"),
        RRHH_UNSUCCESSFUL_LOGIN("RRHH UNSUCCESSFUL LOGIN"),
        ADMIN_SUCCESSFUL_LOGIN("ADMIN SUCCESSFUL LOGIN"),
        ADMIN_UNSUCCESSFUL_LOGIN("ADMIN UNSUCCESSFUL LOGIN"),
        HIERARCHICAL_SUCCESSFUL_LOGIN("HIERARCHICAL SUCCESSFUL LOGIN"),
        HIERARCHICAL_UNSUCCESSFUL_LOGIN("HIERARCHICAL UNSUCCESSFUL LOGIN"),
        USER_UNSUCCESSFUL_AUTHENTICATION("USER UNSUCCESSFUL AUTHENTICATION"),
        USER_SUCCESSFUL_AUTHENTICATION("USER SUCCESSFUL AUTHENTICATION"),
        USER_SUCCESSFUL_AUTHENTICATION_IN("USER SUCCESSFUL AUTHENTICATION IN"),
        USER_SUCCESSFUL_AUTHENTICATION_OUT("USER SUCCESSFUL AUTHENTICATION OUT"),
        START_OF_SHIFT("START OF SHIFT"),
        END_OF_SHIFT("END OF SHIFT"),
        AUTOMATIC_END_OF_SHIFT("AUTOMATIC END OF SHIFT"),
        CATEGORY_CREATED("CATEGORY CREATED"),
        CATEGORY_UPDATE("CATEGORY UPDATED"),
        CATEGORY_DEACTIVATED("CATEGORY DEACTIVATED"),
        CATEGORY_REACTIVATED("CATEGORY REACTIVATED"),
        GRANT_TEMPORAL_ACCESS("GRANT TEMPORAL ACCESS"),
        USER_TEMPORAL_INACTIVATION("USER TEMPORAL INACTIVATION"),
        EMPTY_LOG("")
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

        fun filterLogsByEventName(logs : List<Log>, eventName: LogEventName) : List<Log> {
            val filteredLogs = ArrayList<Log>()
            if (logs.isEmpty()) {
                return filteredLogs
            }

            for (log in logs) {
                if(log.logEventName.value == eventName.value) {
                    filteredLogs.add(log)
                }
            }

            return filteredLogs
        }
    }
    enum class LogEventType(s: String) {
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR")
    }

}