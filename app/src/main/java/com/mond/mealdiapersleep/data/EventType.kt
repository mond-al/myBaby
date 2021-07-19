package com.mond.mealdiapersleep.data

enum class EventType(private val str: String) {
    Meal("meal"), Diaper("diaper"), Sleep("sleep"),Unknown("unknown");

    override fun toString(): String {
        return str
    }

    companion object{
        fun valueOf(str: String?): EventType{
            for(type in values()){
                if(type.str == str)
                    return type
            }
            return Unknown
        }
    }

}
