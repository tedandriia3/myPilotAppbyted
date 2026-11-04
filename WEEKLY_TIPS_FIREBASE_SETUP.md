# Structure Firebase pour les Astuces Hebdomadaires

## Collections Firebase Firestore

### 1. Collection `weekly_tips` (nouvelle)
Cette collection contient les 52 astuces hebdomadaires.

**Structure d'un document :**
```json
{
  "weekOfYear": 1,
  "title": "Maîtriser la gestion de la fatigue en vol",
  "introduction": "Des stratégies efficaces pour maintenir votre vigilance et vos performances lors de vols longs ou exigeants...",
  "fullContent": "La fatigue en vol est l'un des défis les plus importants pour les pilotes...\n\n## Reconnaître les signes de fatigue\n\n- Bâillements fréquents\n- Difficultés de concentration\n- Temps de réaction ralentis\n\n## Stratégies de prévention\n\n1. **Planification du sommeil**\n   - Maintenir un horaire de sommeil régulier\n   - Dormir 7-9 heures par nuit\n\n2. **Nutrition adaptée**\n   - Éviter les repas lourds avant le vol\n   - S'hydrater régulièrement\n\n3. **Exercices de vigilance**\n   - Techniques de respiration\n   - Exercices oculaires\n\n## En cas de fatigue\n\n- Communiquer avec l'équipage\n- Demander un changement de poste si nécessaire\n- Utiliser les pauses réglementaires",
  "category": "Gestion de la fatigue",
  "createdAt": 1705276800000,
  "updatedAt": 1705276800000,
  "isActive": true
}
```

### 2. Collection `config` (extension)
Ajout d'un document pour la configuration des astuces hebdomadaires.

**Document `weekly_tip_config` :**
```json
{
  "currentWeekNumber": 1,
  "lastUpdateTimestamp": 1705276800000,
  "rotationStrategy": "weekly"
}
```

## Règles de sécurité Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
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

## Structure des astuces (52 semaines)

### Catégories d'astuces :
1. **Gestion du stress** (semaines 1-13)
2. **Cognition et mémoire** (semaines 14-26)
3. **Sommeil et récupération** (semaines 27-39)
4. **Bien-être physique** (semaines 40-52)

### Exemples d'astuces par catégorie :

#### Gestion du stress (semaines 1-13)
- Semaine 1: "Maîtriser la gestion de la fatigue en vol"
- Semaine 2: "Techniques de respiration pour réduire le stress"
- Semaine 3: "Gérer l'anxiété avant un examen"
- Semaine 4: "Maintenir la concentration en situation stressante"
- Semaine 5: "Techniques de relaxation rapide"
- Semaine 6: "Gérer les imprévus en vol"
- Semaine 7: "Préparation mentale avant le vol"
- Semaine 8: "Techniques de visualisation positive"
- Semaine 9: "Gérer la pression des examens"
- Semaine 10: "Maintenir la confiance en soi"
- Semaine 11: "Techniques de méditation pour pilotes"
- Semaine 12: "Gérer les critiques constructives"
- Semaine 13: "Développer la résilience mentale"

#### Cognition et mémoire (semaines 14-26)
- Semaine 14: "Développer une mémoire photographique"
- Semaine 15: "Techniques de mémorisation des procédures"
- Semaine 16: "Améliorer la concentration en vol"
- Semaine 17: "Méthodes de révision efficaces"
- Semaine 18: "Développer l'intuition aéronautique"
- Semaine 19: "Techniques de prise de notes rapides"
- Semaine 20: "Améliorer la perception spatiale"
- Semaine 21: "Développer la mémoire procédurale"
- Semaine 22: "Techniques de visualisation mentale"
- Semaine 23: "Améliorer la vitesse de traitement"
- Semaine 24: "Développer la mémoire de travail"
- Semaine 25: "Techniques de concentration prolongée"
- Semaine 26: "Améliorer la mémoire à long terme"

#### Sommeil et récupération (semaines 27-39)
- Semaine 27: "Optimiser votre hygiène du sommeil"
- Semaine 28: "Gérer le décalage horaire"
- Semaine 29: "Techniques de récupération rapide"
- Semaine 30: "Améliorer la qualité du sommeil"
- Semaine 31: "Gérer les horaires de vol irréguliers"
- Semaine 32: "Techniques de relaxation avant le coucher"
- Semaine 33: "Optimiser l'environnement de sommeil"
- Semaine 34: "Gérer la privation de sommeil"
- Semaine 35: "Techniques de sieste réparatrice"
- Semaine 36: "Améliorer la récupération musculaire"
- Semaine 37: "Gérer le stress post-vol"
- Semaine 38: "Techniques de récupération mentale"
- Semaine 39: "Optimiser la récupération entre vols"

#### Bien-être physique (semaines 40-52)
- Semaine 40: "Le sport au service de la concentration"
- Semaine 41: "Exercices pour améliorer la posture"
- Semaine 42: "Techniques de stretching en vol"
- Semaine 43: "Améliorer la circulation sanguine"
- Semaine 44: "Exercices de renforcement du dos"
- Semaine 45: "Techniques de relaxation musculaire"
- Semaine 46: "Améliorer la flexibilité"
- Semaine 47: "Exercices pour les yeux"
- Semaine 48: "Techniques de respiration profonde"
- Semaine 49: "Améliorer l'endurance physique"
- Semaine 50: "Exercices de coordination"
- Semaine 51: "Techniques de récupération active"
- Semaine 52: "Maintenir un mode de vie équilibré"

## Initialisation des données

### Script d'initialisation (exemple avec Firebase Admin SDK)

```javascript
const admin = require('firebase-admin');
const db = admin.firestore();

// Initialiser la configuration
async function initializeWeeklyTipConfig() {
  await db.collection('config').doc('weekly_tip_config').set({
    currentWeekNumber: 1,
    lastUpdateTimestamp: Date.now(),
    rotationStrategy: 'weekly'
  });
}

// Ajouter une astuce (exemple)
async function addWeeklyTip(weekNumber, title, introduction, fullContent, category) {
  await db.collection('weekly_tips').add({
    weekOfYear: weekNumber,
    title: title,
    introduction: introduction,
    fullContent: fullContent,
    category: category,
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isActive: true
  });
}
```

## Cloud Function pour la rotation automatique (optionnel)

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');

exports.updateWeeklyTip = functions.pubsub
  .schedule('0 6 * * 1') // Chaque lundi à 6h00
  .timeZone('Europe/Paris')
  .onRun(async (context) => {
    const db = admin.firestore();
    
    // Calculer le numéro de semaine actuel
    const now = new Date();
    const weekNumber = getWeekNumber(now);
    
    // Mettre à jour la configuration
    await db.collection('config').doc('weekly_tip_config').update({
      currentWeekNumber: weekNumber,
      lastUpdateTimestamp: Date.now()
    });
    
    console.log(`Weekly tip updated to week ${weekNumber}`);
  });

function getWeekNumber(date) {
  const start = new Date(date.getFullYear(), 0, 1);
  const diff = date - start;
  const oneWeek = 1000 * 60 * 60 * 24 * 7;
  return Math.floor(diff / oneWeek) + 1;
}
```

## Monitoring et maintenance

- **Logs** : Surveillez les logs Firebase pour détecter les erreurs
- **Performance** : Les requêtes sont optimisées avec des index sur `weekOfYear` et `isActive`
- **Sauvegarde** : Les données sont automatiquement sauvegardées par Firebase
- **Mise à jour** : Rotation automatique chaque semaine ou manuelle via l'interface admin





















