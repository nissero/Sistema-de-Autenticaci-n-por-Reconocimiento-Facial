package com.biogin.myapplication.logs

class Log (val logEventType : LogEventType, val logEventName : LogEventName,
           val dniMasterUser : String, val dniUserAffected : String,
           val userCategory : String,
           val timestamp : String){

    enum class LogEventName(s: String) {
        USER_UPDATE("USER UPDATE"),
        USER_DNI_UPDATE("USER DNI UPDATE"),
        USER_CREATION("USER CREATION"),
        USER_INACTIVATION("USER INACTIVATION"),
        USER_UNSUCCESSFUL_AUTHENTICATION("USER SUCCESSFUL AUTHENTICATION"),
        USER_SUCCESSFUL_AUTHENTICATION("USER SUCCESSFUL AUTHENTICATION"),
        START_OF_SHIFT("START OF SHIFT"),
        END_OF_SHIFT("END OF SHIFT") // Fin de turno
    }

    enum class LogEventType(s: String) {
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR")
    }

}