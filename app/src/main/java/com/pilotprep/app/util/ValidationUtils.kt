package com.pilotprep.app.util

import com.pilotprep.app.data.model.Aircraft
import com.pilotprep.app.data.model.Question

/**
 * Utilitaires de validation pour les données de l'application
 * Permet de valider les données avant leur utilisation
 */
object ValidationUtils {
    
    /**
     * Résultat de validation
     */
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errors: List<String>) : ValidationResult()
    }
    
    /**
     * Valide un Aircraft
     */
    fun validateAircraft(aircraft: Aircraft?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (aircraft == null) {
            return ValidationResult.Invalid(listOf("Aircraft cannot be null"))
        }
        
        if (aircraft.name.isBlank()) {
            errors.add("Aircraft name cannot be blank")
        }
        
        if (aircraft.year.isBlank()) {
            errors.add("Aircraft year cannot be blank")
        }
        
        if (aircraft.description.isBlank()) {
            errors.add("Aircraft description cannot be blank")
        }
        
        aircraft.imageUrl?.let { url ->
            if (!isValidUrl(url)) {
                errors.add("Invalid image URL: $url")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Valide une Question
     */
    fun validateQuestion(question: Question?): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (question == null) {
            return ValidationResult.Invalid(listOf("Question cannot be null"))
        }
        
        if (question.questionText.isBlank()) {
            errors.add("Question text cannot be blank")
        }
        
        if (question.options.isEmpty()) {
            errors.add("Question must have at least one option")
        } else if (question.options.size < 2) {
            errors.add("Question must have at least 2 options")
        }
        
        if (question.correctOptionIndex < 0) {
            errors.add("Correct option index cannot be negative")
        } else if (question.correctOptionIndex >= question.options.size) {
            errors.add("Correct option index out of bounds: ${question.correctOptionIndex} >= ${question.options.size}")
        }
        
        // Vérifier que les options ne sont pas vides
        question.options.forEachIndexed { index, option ->
            if (option.isBlank()) {
                errors.add("Option $index cannot be blank")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Valide une liste de questions
     */
    fun validateQuestionList(questions: List<Question>): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (questions.isEmpty()) {
            errors.add("Question list cannot be empty")
        }
        
        questions.forEachIndexed { index, question ->
            val result = validateQuestion(question)
            if (result is ValidationResult.Invalid) {
                errors.add("Question at index $index: ${result.errors.joinToString(", ")}")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Vérifie si une string est une URL valide
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            val urlPattern = android.util.Patterns.WEB_URL
            urlPattern.matcher(url).matches() || url.startsWith("gs://")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Valide un email
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Valide une licence de pilotage
     */
    fun isValidLicense(license: String): Boolean {
        return license.isNotBlank() && 
               license.matches(Regex("[A-Z]{2,3}")) && // Ex: PPL, ATPL, CPL
               license.all { it.isLetter() }
    }
    
    /**
     * Valide un sujet
     */
    fun isValidSubject(subject: String): Boolean {
        val validSubjects = setOf(
            "Meteorology", "Navigation", "Aviation Law", "Principles of Flight",
            "Human Performance", "Operational Procedures", "Airframes and Systems",
            "Instruments", "Communications", "Flight Planning", "General"
        )
        return subject.isNotBlank() && validSubjects.contains(subject)
    }
    
    /**
     * Valide un score (pourcentage)
     */
    fun isValidScore(score: Int): Boolean {
        return score >= 0 && score <= 100
    }
    
    /**
     * Valide un ID utilisateur
     */
    fun isValidUserId(userId: String): Boolean {
        return userId.isNotBlank() && 
               userId.length >= 8 && 
               userId.matches(Regex("[a-zA-Z0-9_-]+"))
    }
    
    /**
     * Valide un sujet de besoin-à-savoir
     */
    fun isValidNeedToKnowSubject(subject: String): Boolean {
        return subject.isNotBlank() && 
               subject.length >= 3 && 
               subject.length <= 50
    }
    
    /**
     * Valide une année d'avion
     */
    fun isValidAircraftYear(year: String): Boolean {
        return try {
            val yearInt = year.toInt()
            yearInt >= 1900 && yearInt <= 2100
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Extensions pour faciliter la validation dans les repositories
 */
fun <T> ValidationUtils.ValidationResult.throwIfInvalid(): T? where T : Any {
    when (this) {
        is ValidationUtils.ValidationResult.Valid -> return null
        is ValidationUtils.ValidationResult.Invalid -> {
            val errorMessage = "Validation failed: ${errors.joinToString(", ")}"
            throw IllegalArgumentException(errorMessage)
        }
    }
}

inline fun <T> T.validateAndReturn(validator: (T) -> ValidationUtils.ValidationResult): T {
    return when (val result = validator(this)) {
        is ValidationUtils.ValidationResult.Valid -> this
        is ValidationUtils.ValidationResult.Invalid -> {
            throw IllegalArgumentException("Validation failed: ${result.errors.joinToString(", ")}")
        }
    }
}


