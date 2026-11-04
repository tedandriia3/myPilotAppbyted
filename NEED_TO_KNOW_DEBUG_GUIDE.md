# Guide de Diagnostic - Need to Know

## 🔍 **Étapes de Diagnostic**

### **1. Vérifier la Structure Firestore**

**Collection :** `need_to_know`
**Document ID :** Laissez Firebase générer automatiquement

**Structure exacte à utiliser :**
```json
{
  "title": "Test Content",
  "description": "Description de test",
  "category": "Need to Know",
  "content": [
    {
      "type": "h1",
      "text": "Titre de test"
    },
    {
      "type": "p",
      "text": "Contenu de test pour vérifier l'affichage"
    }
  ],
  "images": [],
  "tags": ["test"],
  "difficulty": "beginner",
  "estimatedReadTime": 5,
  "isPublished": true,
  "createdAt": 1703123456789,
  "updatedAt": 1703123456789,
  "orderIndex": 1
}
```

### **2. Vérifier les Règles de Sécurité Firestore**

**Règles à appliquer dans Firebase Console :**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /need_to_know/{document} {
      allow read: if true; // Lecture publique
      allow write: if request.auth != null; // Écriture authentifiée
    }
  }
}
```

### **3. Vérifier les Index Firestore**

**Index requis :**
```json
{
  "collectionGroup": "need_to_know",
  "queryScope": "COLLECTION",
  "fields": [
    {"fieldPath": "isPublished", "order": "ASCENDING"},
    {"fieldPath": "orderIndex", "order": "ASCENDING"}
  ]
}
```

### **4. Tester avec des Données Simples**

**Document de test minimal :**
```json
{
  "title": "Test Simple",
  "description": "Test",
  "category": "Need to Know",
  "content": [
    {
      "type": "p",
      "text": "Test"
    }
  ],
  "images": [],
  "tags": [],
  "difficulty": "beginner",
  "estimatedReadTime": 1,
  "isPublished": true,
  "createdAt": 1703123456789,
  "updatedAt": 1703123456789,
  "orderIndex": 1
}
```

### **5. Vérifier les Logs de l'Application**

**Ajoutez des logs dans NeedToKnowRepository :**
```kotlin
suspend fun getAllContents(): List<NeedToKnowContent> {
    return try {
        println("🔍 DEBUG: Récupération des contenus Need to Know...")
        val snapshot = db.collection("need_to_know")
            .whereEqualTo("isPublished", true)
            .orderBy("orderIndex", Query.Direction.ASCENDING)
            .get()
            .await()
        
        println("📊 DEBUG: ${snapshot.documents.size} documents trouvés")
        
        val contents = snapshot.documents.mapNotNull { doc ->
            val content = doc.toObject<NeedToKnowContent>()?.copy(id = doc.id)
            println("📄 DEBUG: Contenu trouvé - ${content?.title}")
            content
        }
        
        println("✅ DEBUG: ${contents.size} contenus chargés avec succès")
        contents
    } catch (e: Exception) {
        println("❌ DEBUG: Erreur lors du chargement: ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}
```

### **6. Vérifier la Connexion Firebase**

**Test de connexion simple :**
```kotlin
suspend fun testConnection(): Boolean {
    return try {
        val snapshot = db.collection("need_to_know").limit(1).get().await()
        println("✅ DEBUG: Connexion Firebase OK - ${snapshot.documents.size} docs")
        true
    } catch (e: Exception) {
        println("❌ DEBUG: Erreur de connexion Firebase: ${e.message}")
        false
    }
}
```

## 🚨 **Problèmes Courants**

### **1. Collection mal nommée**
- ✅ Correct : `need_to_know`
- ❌ Incorrect : `needToKnow`, `need-to-know`, `NeedToKnow`

### **2. Champ isPublished manquant**
- ✅ Correct : `"isPublished": true`
- ❌ Incorrect : Champ absent ou `false`

### **3. Structure de contenu incorrecte**
- ✅ Correct : Array d'objets avec `type` et `text`
- ❌ Incorrect : String simple ou structure différente

### **4. Timestamps incorrects**
- ✅ Correct : `1703123456789` (millisecondes)
- ❌ Incorrect : `1703123456` (secondes) ou format date

### **5. Règles de sécurité trop restrictives**
- ✅ Correct : `allow read: if true`
- ❌ Incorrect : `allow read: if request.auth != null`

## 🔧 **Actions de Dépannage**

### **1. Vérifier dans Firebase Console**
1. Allez dans Firestore Database
2. Vérifiez que la collection `need_to_know` existe
3. Vérifiez qu'il y a des documents
4. Vérifiez que `isPublished` est `true`

### **2. Tester avec l'outil de gestion**
1. Utilisez `NeedToKnowManagementScreen`
2. Ajoutez des données d'exemple
3. Vérifiez si elles apparaissent

### **3. Vérifier les logs Android**
1. Ouvrez Logcat dans Android Studio
2. Filtrez par "DEBUG"
3. Cherchez les messages de diagnostic

### **4. Tester avec un document simple**
1. Créez un document avec seulement les champs obligatoires
2. Testez l'affichage
3. Ajoutez progressivement les autres champs

## 📱 **Test de Fonctionnement**

### **1. Test de Navigation**
- Allez dans "Théorie du vol" → "Need to Know"
- Vérifiez que l'écran se charge

### **2. Test de Chargement**
- Vérifiez l'animation de chargement
- Vérifiez les messages d'erreur
- Vérifiez l'état vide

### **3. Test de Contenu**
- Cliquez sur un contenu
- Vérifiez la navigation vers le détail
- Vérifiez l'affichage du contenu

## 🎯 **Solution Rapide**

**Si rien ne fonctionne, essayez ceci :**

1. **Supprimez tous les documents** de la collection `need_to_know`
2. **Utilisez l'outil de gestion** pour ajouter des données d'exemple
3. **Vérifiez l'affichage** dans l'application
4. **Ajoutez vos propres contenus** une fois que ça fonctionne

**Cette approche garantit que la structure est correcte !** 🚀










