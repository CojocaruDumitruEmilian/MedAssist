package com.example.proiectdiploma

class Diseases {
    companion object {
        val diseasesWithSymptoms = listOf<Pair<String, Array<String>>>(
            "Hypertensive disease" to arrayOf(
                "Pain chest",
                "Shortness of breath",
                "Dizziness",
                "Asthenia",
                "Fall",
                "Syncope",
                "Vertigo",
                "Sweat",
                "Sweating increased",
                "Palpitation",
                "Nausea",
                "Angina pectoris",
                "Pressure chest"
            ),
            "Coronavirus disease 2019" to arrayOf(
                "Fever",
                "Dry cough",
                "Fatigue",
                "Pain",
                "Throat sore",
                "Diarrhea",
                "Headache",
                "Loss of taste or smell",
                "Out of breath",
                "Pain chest",
                "Pressure chest"
            ),
            "Depression mental" to arrayOf(
                "Feeling suicidal",
                "Suicidal",
                "Hallucinations auditory",
                "Feeling hopeless",
                "Weepiness",
                "Sleeplessness",
                "Motor retardation",
                "Irritable mood",
                "Blackout",
                "Mood depressed",
                "Hallucinations visual",
                "Worry",
                "Agitation",
                "Tremor",
                "Intoxication",
                "Verbal auditory hallucinations",
                "Energy increased",
                "Difficulty",
                "Nightmare",
                "Unable to concentrate",
                "Homelessness"
            )

        )
    }
}
