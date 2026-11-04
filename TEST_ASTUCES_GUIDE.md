# 🧪 Guide de Test - Astuces de la Semaine

## 📋 Étapes de Test

### **1. Compilation et Installation**

1. **Ouvrir Android Studio**
2. **Synchroniser le projet** : `File → Sync Project with Gradle Files`
3. **Nettoyer le projet** : `Build → Clean Project`
4. **Reconstruire** : `Build → Rebuild Project`
5. **Installer sur l'appareil** : `Run → Run 'app'`

### **2. Vérification des Logs**

1. **Ouvrir Logcat** : `View → Tool Windows → Logcat`
2. **Filtrer par tag** : Tapez `DEBUG` ou `ERROR` dans le filtre
3. **Lancer l'application**
4. **Aller sur l'écran d'accueil**

**Logs attendus :**
```
DEBUG WeeklyTipViewModel: Date: 2024-01-15, Semaine ISO: 3, Jour de l'année: 15
DEBUG: Recherche astuce pour la semaine 3
DEBUG: Recherche dans Firebase pour semaine 3
DEBUG: Firebase a retourné X documents
```

### **3. Test de l'Interface**

1. **Écran d'accueil** : Vérifier la section "Astuce de la Semaine"
2. **États possibles** :
   - ✅ **Succès** : Astuce affichée avec titre et introduction
   - ⏳ **Chargement** : Indicateur de progression
   - ❌ **Erreur** : Message d'erreur avec bouton "Réessayer"
   - 📭 **Vide** : "Aucune astuce disponible"

3. **Bouton Actualiser** : Cliquer pour forcer le rechargement

### **4. Vérification Firebase**

#### **Structure Requise :**
```json
Collection: "weekly_tips"
Document: {
  "weekOfYear": 3,           // ← IMPORTANT: Nombre, pas string
  "title": "Titre de l'astuce",
  "introduction": "Introduction...",
  "fullContent": "Contenu complet...",
  "category": "Catégorie",
  "isActive": true,          // ← IMPORTANT: Boolean, pas string
  "createdAt": 1705276800000,
  "updatedAt": 1705276800000
}
```

#### **Créer un Document de Test :**

1. **Aller sur Firebase Console** : https://console.firebase.google.com
2. **Sélectionner votre projet** : PilotPrepApp2
3. **Firestore Database** → **Commencer une collection**
4. **Nom de collection** : `weekly_tips`
5. **Premier document** : Auto-généré
6. **Champs à ajouter** :

| Champ | Type | Valeur |
|-------|------|--------|
| `weekOfYear` | number | `3` |
| `title` | string | `"Test - Astuce de la Semaine"` |
| `introduction` | string | `"Ceci est une astuce de test pour vérifier le fonctionnement."` |
| `fullContent` | string | `"Contenu complet de l'astuce de test avec plus de détails..."` |
| `category` | string | `"Test"` |
| `isActive` | boolean | `true` |
| `createdAt` | number | `1705276800000` |
| `updatedAt` | number | `1705276800000` |

### **5. Diagnostic des Problèmes**

#### **Si "Aucune astuce disponible" :**

**Vérifier les logs :**
- `DEBUG: Recherche astuce pour la semaine X` → Vérifier le numéro de semaine
- `DEBUG: Firebase a retourné 0 documents` → Problème de structure Firebase
- `ERROR: Exception lors de la récupération` → Problème de connexion/permissions

**Solutions :**
1. **Vérifier la structure Firebase** (voir section 4)
2. **Vérifier les règles de sécurité** :
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /weekly_tips/{document} {
         allow read: if true;
         allow write: if request.auth != null && request.auth.token.admin == true;
       }
     }
   }
   ```

#### **Si erreur de connexion :**
1. **Vérifier la connexion Internet**
2. **Vérifier le fichier `google-services.json`**
3. **Vérifier les permissions dans `AndroidManifest.xml`**

### **6. Test de Navigation**

1. **Cliquer sur l'astuce** → Doit naviguer vers la page détail
2. **Page détail** → Vérifier l'affichage du contenu complet
3. **Bouton retour** → Retour à l'écran d'accueil

### **7. Test de Performance**

1. **Premier chargement** : Temps d'affichage
2. **Rafraîchissement** : Bouton "Actualiser"
3. **Cache** : Fermer/rouvrir l'app (doit utiliser le cache)

## 🔍 Points de Contrôle

- [ ] Application compile sans erreur
- [ ] Logs de debug s'affichent
- [ ] Structure Firebase correcte
- [ ] Astuce s'affiche sur l'écran d'accueil
- [ ] Navigation vers la page détail fonctionne
- [ ] Bouton actualiser fonctionne
- [ ] Gestion d'erreurs appropriée

## 📞 Support

Si le problème persiste après ces tests :

1. **Copier les logs** de Logcat
2. **Screenshot** de l'écran d'accueil
3. **Structure Firebase** (screenshot de la console)
4. **Décrire** le comportement observé

Les corrections apportées devraient résoudre le problème. Le calcul de semaine est maintenant correct et la gestion d'erreurs est améliorée avec des logs détaillés pour le diagnostic.




