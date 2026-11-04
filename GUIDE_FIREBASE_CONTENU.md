# 🔥 GUIDE FIREBASE - AJOUT DE CONTENU

## 📋 STRUCTURE FIREBASE REQUISE

### Collection: `need_to_know`

### Document Structure:
```json
{
  "license": "need_to_know",
  "title": "Votre titre ici",
  "content": [
    {
      "type": "h2",
      "text": "Titre de section"
    },
    {
      "type": "p",
      "text": "Paragraphe de texte"
    },
    {
      "type": "image",
      "imageUrl": "https://example.com/votre-image.jpg"
    }
  ]
}
```

## 🚀 ÉTAPES POUR AJOUTER DU CONTENU

### ÉTAPE 1: Aller dans Firebase Console
1. **Ouvrez [Firebase Console](https://console.firebase.google.com)**
2. **Sélectionnez votre projet**
3. **Cliquez sur "Firestore Database"**

### ÉTAPE 2: Créer la Collection
1. **Cliquez sur "Commencer une collection"**
2. **Nom de la collection :** `need_to_know`
3. **Cliquez sur "Suivant"**

### ÉTAPE 3: Ajouter un Document
1. **ID du document :** Laissez vide (généré automatiquement)
2. **Cliquez sur "Suivant"**

### ÉTAPE 4: Ajouter les Champs
1. **Champ 1 :**
   - **Champ :** `license`
   - **Type :** string
   - **Valeur :** `need_to_know`

2. **Champ 2 :**
   - **Champ :** `title`
   - **Type :** string
   - **Valeur :** `Votre titre ici`

3. **Champ 3 :**
   - **Champ :** `content`
   - **Type :** array
   - **Valeur :** Voir structure ci-dessous

### ÉTAPE 5: Structure du Contenu (Array)
Pour le champ `content`, ajoutez des éléments :

**Élément 1 (Titre) :**
- **Type :** map
- **Champs :**
  - `type` (string) : `h2`
  - `text` (string) : `Votre titre de section`

**Élément 2 (Paragraphe) :**
- **Type :** map
- **Champs :**
  - `type` (string) : `p`
  - `text` (string) : `Votre paragraphe de texte`

**Élément 3 (Image) :**
- **Type :** map
- **Champs :**
  - `type` (string) : `image`
  - `imageUrl` (string) : `https://example.com/votre-image.jpg`

## 📝 EXEMPLE COMPLET

### Document 1:
```json
{
  "license": "need_to_know",
  "title": "Crew Resource Management (CRM)",
  "content": [
    {
      "type": "h2",
      "text": "Introduction au CRM"
    },
    {
      "type": "p",
      "text": "Le Crew Resource Management (CRM) est une approche systémique de la sécurité aérienne qui met l'accent sur l'utilisation efficace de toutes les ressources disponibles."
    },
    {
      "type": "h2",
      "text": "Les 5 piliers du CRM"
    },
    {
      "type": "p",
      "text": "Le CRM repose sur cinq piliers fondamentaux :"
    },
    {
      "type": "ul",
      "items": [
        "Communication efficace",
        "Leadership situationnel",
        "Prise de décision",
        "Gestion des erreurs",
        "Travail d'équipe"
      ]
    },
    {
      "type": "image",
      "imageUrl": "https://example.com/crm-diagram.jpg"
    }
  ]
}
```

### Document 2:
```json
{
  "license": "need_to_know",
  "title": "Gestion des Pannes Moteur",
  "content": [
    {
      "type": "h2",
      "text": "Procédures d'urgence"
    },
    {
      "type": "p",
      "text": "En cas de panne moteur, les pilotes doivent suivre une séquence de vérifications et d'actions précises pour assurer la sécurité du vol."
    },
    {
      "type": "h2",
      "text": "Checklist de sécurité"
    },
    {
      "type": "ol",
      "items": [
        "Maintenir le contrôle de l'avion",
        "Identifier le problème",
        "Appliquer les procédures d'urgence",
        "Communiquer avec le contrôle aérien",
        "Préparer l'atterrissage d'urgence"
      ]
    },
    {
      "type": "image",
      "imageUrl": "https://example.com/engine-failure-procedure.jpg"
    }
  ]
}
```

## 🔧 TYPES DE CONTENU SUPPORTÉS

### Textes:
- `h1` - Titre principal
- `h2` - Titre de section
- `h3` - Sous-titre
- `p` - Paragraphe

### Listes:
- `ul` - Liste à puces (avec champ `items`)
- `ol` - Liste numérotée (avec champ `items`)

### Images:
- `image` - Image (avec champ `imageUrl`)

### Spéciaux:
- `quote` - Citation
- `warning` - Avertissement
- `tip` - Conseil

## ⚠️ RÈGLES DE SÉCURITÉ FIRESTORE

Assurez-vous que vos règles permettent la lecture :

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /need_to_know/{document} {
      allow read: if true;
      allow write: if true;
    }
  }
}
```

## 🚀 TEST

Après avoir ajouté le contenu :
1. **Retournez à votre application**
2. **Naviguez vers `need_to_know_list`**
3. **Vérifiez si le contenu s'affiche**

---

**Suivez cette structure exacte pour que le contenu s'affiche correctement !**









