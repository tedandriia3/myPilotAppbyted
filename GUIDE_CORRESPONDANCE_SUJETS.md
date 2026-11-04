# 📋 GUIDE DE CORRESPONDANCE DES SUJETS

## 🎯 **Objectif**
Ce guide vous montre comment nommer les sujets dans Firebase pour qu'ils correspondent exactement aux listes dans l'application.

## 📚 **SUJETS POUR "RÉSUMÉ" (Théorie du vol)**

### ✅ **Noms à utiliser dans Firebase (champ `subject`)**

```
DROIT AÉRIEN ET PROCÉDURES ATC
CELLULE ET SYSTÈMES
INSTRUMENTATION
MASSES ET CENTRAGE
PERFORMANCES AVIONS
PRÉPARATION ET SUIVI DU VOL
PERFORMANCE HUMAINE
MÉTÉOROLOGIE
NAVIGATION
RADIONAVIGATION
PROCÉDURES OPÉRATIONNELLES
PRINCIPES DU VOL
COMMUNICATION
```

### 🔧 **Structure Firebase pour chaque leçon**

```json
{
  "license": "summary",
  "subject": "DROIT AÉRIEN ET PROCÉDURES ATC",
  "title": "Leçon 1 : Structure de l'Espace Aérien",
  "orderIndex": 1,
  "content": [
    {
      "type": "h2",
      "text": "Les Classes d'Espace Aérien"
    },
    {
      "type": "p", 
      "text": "Contenu de la leçon..."
    }
  ]
}
```

## 📚 **SUJETS POUR "MISE EN PRATIQUE" CPL/ATPL**

### ✅ **Noms à utiliser dans Firebase (champ `subject`)**

```
DROIT AÉRIEN ET PROCÉDURES ATC
CELLULE ET SYSTÈMES
INSTRUMENTATION
MASSES ET CENTRAGE
PERFORMANCES AVIONS
PRÉPARATION ET SUIVI DU VOL
PERFORMANCE HUMAINE
MÉTÉOROLOGIE
NAVIGATION
RADIONAVIGATION
PROCÉDURES OPÉRATIONNELLES
PRINCIPES DU VOL
COMMUNICATION
```

### 🔧 **Structure Firebase pour chaque leçon**

```json
{
  "license": "CPL",  // ou "ATPL"
  "subject": "DROIT AÉRIEN ET PROCÉDURES ATC",
  "title": "Leçon 1 : Structure de l'Espace Aérien",
  "orderIndex": 1,
  "content": [...]
}
```

## 📚 **SUJETS POUR "MISE EN PRATIQUE" PPL**

### ✅ **Noms à utiliser dans Firebase (champ `subject`)**

```
DROIT AÉRIEN
CELLULE ET SYSTÈMES
PERFORMANCES AVIONS
PRÉPARATION ET SUIVI DU VOL
PERFORMANCE HUMAINE
MÉTÉOROLOGIE
NAVIGATION (GENERAL + RADIONAVIGATION)
PROCÉDURES OPÉRATIONNELLES
PRINCIPES DU VOL
COMMUNICATION (VFR)
```

### 🔧 **Structure Firebase pour chaque leçon**

```json
{
  "license": "PPL",
  "subject": "DROIT AÉRIEN",
  "title": "Leçon 1 : Structure de l'Espace Aérien",
  "orderIndex": 1,
  "content": [...]
}
```

## 🎯 **RÈGLES IMPORTANTES**

### ✅ **À FAIRE**
- Utilisez **EXACTEMENT** les noms de sujets listés ci-dessus
- Respectez la **casse** (majuscules/minuscules)
- Respectez les **espaces** et **caractères spéciaux**
- Utilisez le bon `license` selon la catégorie

### ❌ **À ÉVITER**
- Ne changez pas les noms de sujets
- Ne mélangez pas les noms entre les catégories
- N'utilisez pas de noms différents de ceux listés

## 🔍 **VÉRIFICATION**

Pour vérifier que vos sujets correspondent :

1. **Allez dans Firebase Console**
2. **Vérifiez la collection `lessons`**
3. **Regardez le champ `subject` de vos documents**
4. **Comparez avec les listes ci-dessus**

## 🚀 **TEST**

Après avoir ajouté du contenu avec les bons noms :

1. **Lancez l'application**
2. **Allez dans "Théorie du vol" > "Résumé"**
3. **Vérifiez que tous les sujets s'affichent**
4. **Cliquez sur un sujet pour voir les leçons**

---

**💡 Conseil :** Copiez-collez exactement les noms de sujets de ce guide dans Firebase pour éviter les erreurs !
