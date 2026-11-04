# 🔧 GUIDE DE RÉCUPÉRATION DES DONNÉES

## 🚨 **PROBLÈME IDENTIFIÉ**

Les contenus existants ont disparu :
- ❌ **"DROIT AERIEN ET PROCEDURES ATC"** n'apparaît plus
- ❌ **"Need to Know"** ne s'affiche pas
- ❌ **Erreur :** "Aucune leçon trouvée pour ce sujet"

## 🔍 **DIAGNOSTIC IMMÉDIAT**

### **ÉTAPE 1 : Test de Diagnostic**
1. **Lancez l'application**
2. **Naviguez vers :** `simple_diagnostic`
3. **Cliquez sur :** "Lancer le diagnostic"
4. **Vérifiez :** Les résultats des deux collections

### **ÉTAPE 2 : Vérification Firebase Console**
1. **Ouvrez :** [Firebase Console](https://console.firebase.google.com)
2. **Sélectionnez :** Votre projet `pilot-prep-app`
3. **Allez dans :** Firestore Database
4. **Vérifiez :** Les collections `lessons` et `need_to_know`

## 📋 **RÉCUPÉRATION DES DONNÉES**

### **1. Collection `lessons` (pour "DROIT AERIEN ET PROCEDURES ATC")**

Si la collection `lessons` est vide, ajoutez ce document :

```json
{
  "category": "summary",
  "subject": "DROIT AÉRIEN ET PROCÉDURES ATC",
  "title": "Règles de l'air et procédures ATC",
  "license": "summary",
  "orderIndex": 1,
  "content": [
    {
      "type": "h2",
      "text": "Introduction au droit aérien"
    },
    {
      "type": "p",
      "text": "Le droit aérien régit l'ensemble des règles qui s'appliquent à la navigation aérienne. Ces règles sont établies par l'Organisation de l'Aviation Civile Internationale (OACI)."
    },
    {
      "type": "h2",
      "text": "Les classes d'espace aérien"
    },
    {
      "type": "p",
      "text": "L'espace aérien est divisé en plusieurs classes : A, B, C, D, E, F et G. Chaque classe a ses propres règles d'accès et de communication."
    },
    {
      "type": "h2",
      "text": "Responsabilités du pilote"
    },
    {
      "type": "p",
      "text": "Le pilote commandant de bord est responsable de la sécurité de l'aéronef et de ses occupants. Il doit respecter les règles de l'air et suivre les instructions du contrôle aérien."
    }
  ]
}
```

### **2. Collection `need_to_know` (pour "Need to Know")**

Ajoutez ce document :

```json
{
  "license": "need_to_know",
  "title": "Mécanique du Vol",
  "content": [
    {
      "type": "h2",
      "text": "Introduction à la Mécanique du Vol"
    },
    {
      "type": "p",
      "text": "La mécanique du vol est l'étude des forces qui agissent sur un aéronef en vol. Comprendre ces principes est essentiel pour tout pilote."
    },
    {
      "type": "h2",
      "text": "Les Quatre Forces Principales"
    },
    {
      "type": "p",
      "text": "En vol, quatre forces principales agissent sur l'avion :"
    },
    {
      "type": "ul",
      "items": [
        "Portance (Lift)",
        "Poids (Weight)",
        "Traction (Thrust)",
        "Traînée (Drag)"
      ]
    }
  ]
}
```

## 🔧 **RÈGLES DE SÉCURITÉ FIRESTORE**

Assurez-vous que ces règles sont configurées :

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /lessons/{document} {
      allow read: if true;
      allow write: if true;
    }
    match /need_to_know/{document} {
      allow read: if true;
      allow write: if true;
    }
  }
}
```

## 🧪 **TEST DE RÉCUPÉRATION**

### **ÉTAPE 1 : Ajout des Données**
1. **Ajoutez** les documents ci-dessus dans Firebase Console
2. **Vérifiez** que les collections existent

### **ÉTAPE 2 : Test de l'Application**
1. **Lancez** l'application
2. **Allez dans :** "Théorie du vol" → "Résumé"
3. **Vérifiez :** Que "DROIT AERIEN ET PROCEDURES ATC" s'affiche
4. **Allez dans :** "Théorie du vol" → "Need To Know"
5. **Vérifiez :** Que le contenu s'affiche en grille

### **ÉTAPE 3 : Diagnostic Final**
1. **Naviguez vers :** `simple_diagnostic`
2. **Cliquez sur :** "Lancer le diagnostic"
3. **Vérifiez :** Que les deux collections montrent des données

## 🚨 **SI LE PROBLÈME PERSISTE**

### **Vérifications Supplémentaires :**
1. **`google-services.json`** présent dans `app/`
2. **Connexion internet** active
3. **Permissions** de l'application
4. **Logs Logcat** pour les erreurs Firebase

### **Logs à Surveiller :**
- Messages commençant par "DEBUG:"
- Erreurs Firebase
- Erreurs de connexion

## ✅ **RÉSULTAT ATTENDU**

Après ces étapes :
- ✅ **"DROIT AERIEN ET PROCEDURES ATC"** s'affiche dans "Résumé"
- ✅ **"Need to Know"** s'affiche en grille 2x2
- ✅ **Pas d'erreur** "Aucune leçon trouvée"
- ✅ **Navigation** fonctionnelle

## 🚀 **PROCHAINES ÉTAPES**

1. **Testez** le diagnostic
2. **Ajoutez** les données manquantes
3. **Vérifiez** l'affichage dans l'application
4. **Confirmez** que tout fonctionne

**Les données devraient être récupérées et affichées correctement !** 🎉








