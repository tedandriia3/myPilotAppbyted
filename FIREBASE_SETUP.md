# Structure Firebase pour l'Image du Jour

## Collections Firebase Firestore

### 1. Collection `aircraft` (existante)
Cette collection contient déjà les avions du Patrimoine du Ciel.

**Structure d'un document :**
```json
{
  "name": "Spitfire Mk IX",
  "year": "1942",
  "engine": "Rolls-Royce Merlin 61",
  "maxSpeed": "656 km/h",
  "wingspan": "11.23 m",
  "description": "Le Spitfire Mk IX était l'une des versions les plus réussies du célèbre chasseur britannique...",
  "imageUrl": "https://example.com/spitfire.jpg"
}
```

### 2. Collection `config` (nouvelle)
Cette collection stocke la configuration de l'image du jour.

**Document `daily_image_config` :**
```json
{
  "currentImageId": "aircraft_document_id",
  "lastUpdated": "2024-01-15",
  "rotationStrategy": "random"
}
```

### 3. Collection `image_of_day` (nouvelle)
Cette collection stocke l'historique des images du jour.

**Structure d'un document :**
```json
{
  "aircraftId": "aircraft_document_id",
  "displayDate": "2024-01-15",
  "createdAt": 1705276800000,
  "isActive": true
}
```

## Règles de sécurité Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Collection aircraft - lecture publique
    match /aircraft/{document} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.token.admin == true;
    }
    
    // Collection config - lecture publique, écriture admin uniquement
    match /config/{document} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.token.admin == true;
    }
    
    // Collection image_of_day - lecture publique, écriture admin uniquement
    match /image_of_day/{document} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.token.admin == true;
    }
  }
}
```

## Configuration Firebase Storage

Les images des avions doivent être stockées dans Firebase Storage avec la structure suivante :

```
/aircraft/
  ├── spitfire_mk_ix.jpg
  ├── concorde.jpg
  ├── f16_fighting_falcon.jpg
  └── ...
```

## Permissions Android

Ajoutez ces permissions dans `AndroidManifest.xml` si elles ne sont pas déjà présentes :

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## Initialisation des données

Pour initialiser la fonctionnalité, vous devez :

1. **Créer la configuration initiale :**
   - Ajouter un document dans `config/daily_image_config`
   - Définir `currentImageId` avec l'ID d'un avion existant
   - Définir `lastUpdated` avec la date d'aujourd'hui

2. **Ajouter des avions dans la collection `aircraft` :**
   - Utiliser l'interface d'administration Firebase
   - Ou créer un script de migration

3. **Tester la fonctionnalité :**
   - Lancer l'application
   - Vérifier que l'image du jour s'affiche sur l'écran d'accueil
   - Tester la navigation vers la page détaillée

## Monitoring et maintenance

- **Logs :** Surveillez les logs Firebase pour détecter les erreurs
- **Performance :** Les requêtes sont optimisées avec des limites et des index
- **Sauvegarde :** Les données sont automatiquement sauvegardées par Firebase
- **Mise à jour :** Le Worker met à jour automatiquement l'image chaque jour à 6h00





















