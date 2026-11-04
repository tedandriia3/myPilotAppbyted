# Guide Complet : Configuration Firebase Firestore pour PilotPrep App

## 📋 Table des matières
1. [Configuration initiale Firebase](#configuration-initiale-firebase)
2. [Structure des collections](#structure-des-collections)
3. [Configuration des règles de sécurité](#configuration-des-règles-de-sécurité)
4. [Ajout des données - Image du Jour](#ajout-des-données---image-du-jour)
5. [Ajout des données - Astuce de la Semaine](#ajout-des-données---astuce-de-la-semaine)
6. [Scripts d'initialisation](#scripts-dinitialisation)
7. [Vérification et tests](#vérification-et-tests)

---

## 🔧 Configuration initiale Firebase

### 1. Accéder à Firebase Console
1. Allez sur [https://console.firebase.google.com](https://console.firebase.google.com)
2. Sélectionnez votre projet `PilotPrepApp2`
3. Cliquez sur **Firestore Database** dans le menu de gauche

### 2. Créer la base de données (si pas déjà fait)
1. Cliquez sur **Créer une base de données**
2. Choisissez **Commencer en mode test** (pour le développement)
3. Sélectionnez une région (ex: `europe-west1`)

---

## 📊 Structure des collections

### Collections à créer :
- `aircraft` (pour Image du Jour)
- `weekly_tips` (pour Astuce de la Semaine)
- `config` (configuration générale)

---

## 🔒 Configuration des règles de sécurité

### 1. Aller dans l'onglet "Règles"
1. Dans Firestore Database, cliquez sur l'onglet **Règles**
2. Remplacez le contenu par :

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Collection aircraft - lecture publique, écriture admin uniquement
    match /aircraft/{document} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.token.admin == true;
    }
    
    // Collection weekly_tips - lecture publique, écriture admin uniquement
    match /weekly_tips/{document} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.token.admin == true;
    }
    
    // Collection config - lecture publique, écriture admin uniquement
    match /config/{document} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.token.admin == true;
    }
  }
}
```

### 2. Publier les règles
1. Cliquez sur **Publier**

---

## 🖼️ Ajout des données - Image du Jour

### 1. Créer la collection `aircraft`

#### Méthode 1 : Via l'interface Firebase
1. Dans Firestore Database, cliquez sur **Commencer une collection**
2. Nom de la collection : `aircraft`
3. Premier document : `spitfire_mk_ix` (ou autre nom)

#### Structure d'un document avion :
```json
{
  "name": "Spitfire Mk IX",
  "year": "1942",
  "engine": "Rolls-Royce Merlin 61",
  "maxSpeed": "656 km/h",
  "wingspan": "11.23 m",
  "description": "Le Spitfire Mk IX était l'une des versions les plus réussies du célèbre chasseur britannique. Développé pour contrer les nouveaux chasseurs allemands, il combinait puissance et maniabilité exceptionnelles.",
  "imageUrl": "https://example.com/images/spitfire_mk_ix.jpg"
}
```

#### Exemples d'avions à ajouter :

**Document 1 : Spitfire Mk IX**
```json
{
  "name": "Spitfire Mk IX",
  "year": "1942",
  "engine": "Rolls-Royce Merlin 61",
  "maxSpeed": "656 km/h",
  "wingspan": "11.23 m",
  "description": "Le Spitfire Mk IX était l'une des versions les plus réussies du célèbre chasseur britannique. Développé pour contrer les nouveaux chasseurs allemands, il combinait puissance et maniabilité exceptionnelles.",
  "imageUrl": "https://example.com/images/spitfire_mk_ix.jpg"
}
```

**Document 2 : Concorde**
```json
{
  "name": "Concorde",
  "year": "1976",
  "engine": "4 × Rolls-Royce/Snecma Olympus 593",
  "maxSpeed": "Mach 2.04 (2,179 km/h)",
  "wingspan": "25.56 m",
  "description": "Le Concorde était un avion de ligne supersonique franco-britannique. Il pouvait traverser l'Atlantique en moins de 3h30, offrant une expérience de vol unique dans l'histoire de l'aviation commerciale.",
  "imageUrl": "https://example.com/images/concorde.jpg"
}
```

**Document 3 : F-16 Fighting Falcon**
```json
{
  "name": "F-16 Fighting Falcon",
  "year": "1978",
  "engine": "General Electric F110-GE-129",
  "maxSpeed": "2,124 km/h",
  "wingspan": "9.96 m",
  "description": "Le F-16 Fighting Falcon est un chasseur multirôle américain. Sa conception révolutionnaire avec commandes de vol électriques et cockpit à bulle offre une maniabilité exceptionnelle.",
  "imageUrl": "https://example.com/images/f16_fighting_falcon.jpg"
}
```

### 2. Créer la configuration Image du Jour

1. Créer une nouvelle collection : `config`
2. Document ID : `daily_image_config`
3. Contenu :
```json
{
  "currentImageId": "spitfire_mk_ix",
  "lastUpdated": "2024-01-15",
  "rotationStrategy": "random"
}
```

---

## 💡 Ajout des données - Astuce de la Semaine

### 1. Créer la collection `weekly_tips`

#### Méthode 1 : Via l'interface Firebase
1. Créer une nouvelle collection : `weekly_tips`
2. Ajouter 52 documents (un par semaine)

#### Structure d'un document astuce :
```json
{
  "weekOfYear": 1,
  "title": "Maîtriser la gestion de la fatigue en vol",
  "introduction": "Des stratégies efficaces pour maintenir votre vigilance et vos performances lors de vols longs ou exigeants.",
  "fullContent": "La fatigue en vol est l'un des défis les plus importants pour les pilotes...",
  "category": "Gestion de la fatigue",
  "createdAt": 1705276800000,
  "updatedAt": 1705276800000,
  "isActive": true
}
```

### 2. Exemples d'astuces par catégorie

#### **Semaines 1-13 : Gestion du stress**

**Semaine 1 :**
```json
{
  "weekOfYear": 1,
  "title": "Maîtriser la gestion de la fatigue en vol",
  "introduction": "Des stratégies efficaces pour maintenir votre vigilance et vos performances lors de vols longs ou exigeants.",
  "fullContent": "La fatigue en vol est l'un des défis les plus importants pour les pilotes.\n\n## Reconnaître les signes de fatigue\n\n- Bâillements fréquents\n- Difficultés de concentration\n- Temps de réaction ralentis\n- Irritabilité\n\n## Stratégies de prévention\n\n### 1. Planification du sommeil\n- Maintenir un horaire de sommeil régulier\n- Dormir 7-9 heures par nuit\n- Éviter les écrans 1h avant le coucher\n\n### 2. Nutrition adaptée\n- Éviter les repas lourds avant le vol\n- S'hydrater régulièrement\n- Privilégier les glucides complexes\n\n### 3. Exercices de vigilance\n- Techniques de respiration profonde\n- Exercices oculaires\n- Étirements légers\n\n## En cas de fatigue\n\n- Communiquer avec l'équipage\n- Demander un changement de poste si nécessaire\n- Utiliser les pauses réglementaires\n- Considérer un atterrissage d'urgence si nécessaire",
  "category": "Gestion de la fatigue",
  "createdAt": 1705276800000,
  "updatedAt": 1705276800000,
  "isActive": true
}
```

**Semaine 2 :**
```json
{
  "weekOfYear": 2,
  "title": "Techniques de respiration pour réduire le stress",
  "introduction": "Apprenez des techniques de respiration simples et efficaces pour gérer le stress en vol et améliorer votre concentration.",
  "fullContent": "La respiration contrôlée est l'une des techniques les plus efficaces pour gérer le stress.\n\n## Technique de respiration 4-7-8\n\n1. Inspirez par le nez pendant 4 secondes\n2. Retenez votre souffle pendant 7 secondes\n3. Expirez par la bouche pendant 8 secondes\n4. Répétez 3-4 fois\n\n## Technique de respiration carrée\n\n- Inspirez pendant 4 secondes\n- Retenez pendant 4 secondes\n- Expirez pendant 4 secondes\n- Pause pendant 4 secondes\n\n## Applications pratiques\n\n- Avant un examen\n- En cas de turbulence\n- Lors de situations stressantes\n- Pour améliorer la concentration",
  "category": "Gestion du stress",
  "createdAt": 1705276800000,
  "updatedAt": 1705276800000,
  "isActive": true
}
```

#### **Semaines 14-26 : Cognition et mémoire**

**Semaine 14 :**
```json
{
  "weekOfYear": 14,
  "title": "Développer une mémoire photographique",
  "introduction": "Des techniques pour améliorer votre mémoire visuelle et retenir plus facilement les informations importantes en vol.",
  "fullContent": "La mémoire photographique peut être développée avec des techniques spécifiques.\n\n## Technique de visualisation\n\n1. **Créer des images mentales**\n   - Associez chaque information à une image\n   - Utilisez des couleurs vives et contrastées\n   - Ajoutez des détails absurdes pour la mémoriser\n\n2. **Méthode des loci**\n   - Associez les informations à des lieux familiers\n   - Parcourez mentalement ces lieux\n   - Récupérez les informations en suivant le parcours\n\n## Applications en aviation\n\n- Mémoriser les procédures d'urgence\n- Retenir les fréquences radio\n- Se souvenir des checklists\n- Visualiser les trajectoires de vol",
  "category": "Cognition et mémoire",
  "createdAt": 1705276800000,
  "updatedAt": 1705276800000,
  "isActive": true
}
```

### 3. Créer la configuration Astuce de la Semaine

1. Dans la collection `config`, ajouter un document : `weekly_tip_config`
2. Contenu :
```json
{
  "currentWeekNumber": 1,
  "lastUpdateTimestamp": 1705276800000,
  "rotationStrategy": "weekly"
}
```

---

## 🚀 Scripts d'initialisation

### Script JavaScript pour Firebase Admin SDK

```javascript
const admin = require('firebase-admin');

// Initialiser Firebase Admin
const serviceAccount = require('./path/to/serviceAccountKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Fonction pour ajouter un avion
async function addAircraft(name, year, engine, maxSpeed, wingspan, description, imageUrl) {
  await db.collection('aircraft').add({
    name: name,
    year: year,
    engine: engine,
    maxSpeed: maxSpeed,
    wingspan: wingspan,
    description: description,
    imageUrl: imageUrl
  });
}

// Fonction pour ajouter une astuce
async function addWeeklyTip(weekOfYear, title, introduction, fullContent, category) {
  await db.collection('weekly_tips').add({
    weekOfYear: weekOfYear,
    title: title,
    introduction: introduction,
    fullContent: fullContent,
    category: category,
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isActive: true
  });
}

// Initialiser les configurations
async function initializeConfigs() {
  // Configuration Image du Jour
  await db.collection('config').doc('daily_image_config').set({
    currentImageId: '',
    lastUpdated: new Date().toISOString().split('T')[0],
    rotationStrategy: 'random'
  });

  // Configuration Astuce de la Semaine
  await db.collection('config').doc('weekly_tip_config').set({
    currentWeekNumber: 1,
    lastUpdateTimestamp: Date.now(),
    rotationStrategy: 'weekly'
  });
}

// Exemple d'utilisation
async function main() {
  try {
    // Initialiser les configurations
    await initializeConfigs();
    console.log('Configurations initialisées');

    // Ajouter quelques avions
    await addAircraft(
      "Spitfire Mk IX",
      "1942",
      "Rolls-Royce Merlin 61",
      "656 km/h",
      "11.23 m",
      "Le Spitfire Mk IX était l'une des versions les plus réussies du célèbre chasseur britannique.",
      "https://example.com/images/spitfire_mk_ix.jpg"
    );

    // Ajouter quelques astuces
    await addWeeklyTip(
      1,
      "Maîtriser la gestion de la fatigue en vol",
      "Des stratégies efficaces pour maintenir votre vigilance et vos performances lors de vols longs ou exigeants.",
      "La fatigue en vol est l'un des défis les plus importants pour les pilotes...",
      "Gestion de la fatigue"
    );

    console.log('Données ajoutées avec succès');
  } catch (error) {
    console.error('Erreur:', error);
  }
}

main();
```

---

## ✅ Vérification et tests

### 1. Vérifier les collections
1. Dans Firebase Console, vérifiez que les collections suivantes existent :
   - `aircraft` (avec au moins 3 documents)
   - `weekly_tips` (avec au moins 3 documents)
   - `config` (avec 2 documents)

### 2. Tester l'application
1. Compilez et lancez l'application
2. Vérifiez que l'**Image du Jour** s'affiche sur l'écran d'accueil
3. Vérifiez que l'**Astuce de la Semaine** s'affiche sur l'écran d'accueil
4. Testez la navigation vers les pages détaillées

### 3. Vérifier les logs
1. Ouvrez Logcat dans Android Studio
2. Recherchez les erreurs Firebase
3. Vérifiez que les données sont bien récupérées

---

## 🔧 Dépannage

### Problèmes courants :

1. **Erreur de permissions**
   - Vérifiez que les règles de sécurité sont correctes
   - Assurez-vous que l'authentification Firebase est configurée

2. **Données non affichées**
   - Vérifiez que les collections existent
   - Vérifiez que les documents ont les bons champs
   - Regardez les logs pour les erreurs de requête

3. **Images non chargées**
   - Vérifiez que les URLs d'images sont valides
   - Assurez-vous que les images sont accessibles publiquement

---

## 📝 Checklist finale

- [ ] Collections `aircraft`, `weekly_tips`, `config` créées
- [ ] Règles de sécurité configurées et publiées
- [ ] Au moins 3 avions ajoutés dans `aircraft`
- [ ] Au moins 3 astuces ajoutées dans `weekly_tips`
- [ ] Configurations créées dans `config`
- [ ] Application testée et fonctionnelle
- [ ] Navigation vers les pages détaillées fonctionnelle

Votre Firebase Firestore est maintenant configuré et prêt à être utilisé ! 🎉





















