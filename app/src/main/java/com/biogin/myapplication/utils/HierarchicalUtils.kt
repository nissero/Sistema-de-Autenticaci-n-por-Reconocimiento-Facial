package com.biogin.myapplication.utils

class HierarchicalUtils {
    private val firebaseHierarchicalUtils = FirebaseHierarchicalUtils()
    private lateinit var mail: String
    private lateinit var trainingDays: ArrayList<Boolean>

    init {
        firebaseHierarchicalUtils.getMailFromFirebase {
            tempMail -> mail = tempMail
        }

        firebaseHierarchicalUtils.getTrainingDaysFromFirebase {
            tempTrainingDays ->
            run {
                if(tempTrainingDays.isNotEmpty())
                    trainingDays = tempTrainingDays
            }
        }
    }

    fun setMail(newMail: String) {
        firebaseHierarchicalUtils.setMail(newMail) {
            success ->
            run {
                if(success)
                    mail = newMail
            }
        }
    }

    fun getMail(): String {
        return mail
    }
}