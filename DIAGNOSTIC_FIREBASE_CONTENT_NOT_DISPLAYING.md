# Diagnostic : Pourquoi le contenu ne s'affiche pas dans l'application

## 🔍 Points de vérification

### 1. **Structure des données Firebase**

Vérifiez que vos données dans Firebase respectent exactement cette structure :

```json
{
  "weekOfYear": 1,
  "title": "Votre titre",
  "introduction": "Votre introduction",
  "fullContent": "Votre contenu complet",
  "category": "Votre catégorie",
  "createdAt": 1705276800000,
  "updatedAt": 1705276800000,
  "isActive": true
}
```

**⚠️ Points critiques :**
- `weekOfYear` doit être un **nombre** (pas une chaîne)
- `isActive` doit être `true` (pas "true" en chaîne)
- `createdAt` et `updatedAt` doivent être des **nombres** (timestamps)

### 2. **Nom de la collection**

Assurez-vous que votre collection s'appelle exactement : `weekly_tips`

### 3. **Règles de sécurité Firebase**

Vérifiez vos règles Firestore dans la console Firebase :

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /weekly_tips/{document} {
      allow read: if true;  // ✅ Lecture autorisée
      allow write: if request.auth != null;
    }
  }
}
```

### 4. **Configuration Firebase dans l'app**

Vérifiez que votre fichier `google-services.json` est bien présent dans `app/` et à jour.

### 5. **Logs de débogage**

Ajoutez des logs pour diagnostiquer le problème. Modifiez temporairement le repository :

```kotlin
override suspend fun getCurrentWeeklyTip(): WeeklyTip? {
    val currentWeek = WeekUtils.getCurrentWeekOfYear()
    println("🔍 DEBUG: Semaine actuelle = $currentWeek")
    
    return try {
        val snapshot = db.collection(WEEKLY_TIPS_COLLECTION)
            .whereEqualTo("weekOfYear", currentWeek)
            .whereEqualTo("isActive", true)
            .limit(1)
            .get()
            .await()

        println("🔍 DEBUG: Nombre de documents trouvés = ${snapshot.size()}")
        
        if (snapshot.isEmpty) {
            println("❌ DEBUG: Aucun document trouvé pour la semaine $currentWeek")
            null
        } else {
            val doc = snapshot.documents.first()
            println("🔍 DEBUG: Document trouvé: ${doc.data}")
            doc.toObject(WeeklyTip::class.java)?.copy(id = doc.id)
        }
    } catch (e: Exception) {
        println("❌ DEBUG: Erreur lors de la récupération: ${e.message}")
        e.printStackTrace()
        null
    }
}
```

### 6. **Test de la semaine actuelle**

Le code utilise `WeekUtils.getCurrentWeekOfYear()` qui calcule la semaine actuelle. Vérifiez que :

- Vous avez bien une astuce pour la semaine actuelle
- Le calcul de la semaine est correct

**Test rapide :** Ajoutez une astuce pour la semaine 1 et modifiez temporairement le code pour forcer la semaine 1 :

```kotlin
fun getCurrentWeekOfYear(): Int {
    return 1  // Force la semaine 1 pour le test
}
```

### 7. **Vérification des permissions**

Assurez-vous que votre application a les permissions nécessaires dans `AndroidManifest.xml` :

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 8. **Test de connectivité**

Ajoutez un test de connectivité dans le ViewModel :

```kotlin
fun testFirebaseConnection() {
    viewModelScope.launch {
        try {
            val snapshot = Firebase.firestore.collection("weekly_tips").limit(1).get().await()
            println("✅ DEBUG: Connexion Firebase OK, ${snapshot.size()} documents")
        } catch (e: Exception) {
            println("❌ DEBUG: Erreur de connexion Firebase: ${e.message}")
        }
    }
}
```

## 🛠️ Solutions courantes

### **Problème 1 : Données mal formatées**
**Solution :** Vérifiez que tous les champs sont du bon type (nombres, booléens, chaînes)

### **Problème 2 : Collection mal nommée**
**Solution :** Renommez votre collection en `weekly_tips` exactement

### **Problème 3 : Règles de sécurité trop restrictives**
**Solution :** Autorisez la lecture publique temporairement pour tester

### **Problème 4 : Semaine actuelle sans contenu**
**Solution :** Ajoutez une astuce pour la semaine actuelle ou forcez une semaine avec du contenu

### **Problème 5 : Problème de réseau**
**Solution :** Vérifiez la connectivité et les permissions

## 🔧 Test rapide

1. **Créez une astuce de test** avec ces données exactes :
```json
{
  "weekOfYear": 1,
  "title": "Test Astuce",
  "introduction": "Ceci est un test",
  "fullContent": "Contenu de test complet",
  "category": "Test",
  "createdAt": 1705276800000,
  "updatedAt": 1705276800000,
  "isActive": true
}
```

2. **Forcez la semaine 1** dans le code temporairement
3. **Vérifiez les logs** dans Logcat
4. **Testez l'affichage**

## 📱 Vérification dans l'application

1. **Ouvrez Logcat** dans Android Studio
2. **Filtrez par votre package** (`com.pilotprep.app`)
3. **Recherchez les messages DEBUG** que vous avez ajoutés
4. **Identifiez où le processus s'arrête**

## 🎯 Prochaines étapes

1. **Vérifiez la structure** de vos données Firebase
2. **Ajoutez les logs de débogage** temporairement
3. **Testez avec une astuce simple** (semaine 1)
4. **Vérifiez les logs** pour identifier le problème
5. **Corrigez le problème** identifié

Une fois le problème identifié, nous pourrons le résoudre ensemble ! 🚀


















