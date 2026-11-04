package com.pilotprep.app.data.repository

import com.pilotprep.app.data.model.ContentBlock
import com.pilotprep.app.data.model.NeedToKnowContent

object NeedToKnowTestData {
    
    fun createSampleContent(): List<NeedToKnowContent> {
        return listOf(
            NeedToKnowContent(
                license = "need_to_know",
                title = "Crew Resource Management (CRM) - Les Bases",
                content = listOf(
                    ContentBlock(
                        type = "h2",
                        text = "Introduction au CRM"
                    ),
                    ContentBlock(
                        type = "p",
                        text = "Le Crew Resource Management (CRM) est une approche systémique de la sécurité aérienne qui met l'accent sur l'utilisation efficace de toutes les ressources disponibles pour assurer un vol sûr et efficace."
                    ),
                    ContentBlock(
                        type = "h2",
                        text = "Les 5 piliers du CRM"
                    ),
                    ContentBlock(
                        type = "p",
                        text = "Le CRM repose sur cinq piliers fondamentaux :"
                    ),
                    ContentBlock(
                        type = "ul",
                        items = listOf(
                            "Communication efficace",
                            "Leadership situationnel",
                            "Prise de décision",
                            "Gestion des erreurs",
                            "Travail d'équipe"
                        )
                    ),
                    ContentBlock(
                        type = "image",
                        imageUrl = "https://example.com/crm-diagram.jpg"
                    ),
                    ContentBlock(
                        type = "h2",
                        text = "Application pratique"
                    ),
                    ContentBlock(
                        type = "p",
                        text = "En situation d'urgence, l'application des principes CRM permet à l'équipage de maintenir la sécurité tout en gérant efficacement les ressources disponibles."
                    )
                )
            ),
            NeedToKnowContent(
                license = "need_to_know",
                title = "Gestion des Pannes Moteur",
                content = listOf(
                    ContentBlock(
                        type = "h2",
                        text = "Procédures d'urgence"
                    ),
                    ContentBlock(
                        type = "p",
                        text = "En cas de panne moteur, les pilotes doivent suivre une séquence de vérifications et d'actions précises pour assurer la sécurité du vol."
                    ),
                    ContentBlock(
                        type = "h2",
                        text = "Checklist de sécurité"
                    ),
                    ContentBlock(
                        type = "ol",
                        items = listOf(
                            "Maintenir le contrôle de l'avion",
                            "Identifier le problème",
                            "Appliquer les procédures d'urgence",
                            "Communiquer avec le contrôle aérien",
                            "Préparer l'atterrissage d'urgence"
                        )
                    ),
                    ContentBlock(
                        type = "image",
                        imageUrl = "https://example.com/engine-failure-procedure.jpg"
                    )
                )
            )
        )
    }
}









