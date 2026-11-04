// Script de test pour vérifier la structure Firebase des astuces hebdomadaires
// À exécuter dans la console Firebase ou via Firebase Admin SDK

const admin = require('firebase-admin');

// Initialiser Firebase Admin (remplacez par votre clé de service)
const serviceAccount = require('./path/to/serviceAccountKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function testWeeklyTipsStructure() {
  console.log('🔍 Test de la structure Firebase pour les astuces hebdomadaires...\n');
  
  try {
    // 1. Vérifier la collection weekly_tips
    console.log('1. Vérification de la collection "weekly_tips":');
    const weeklyTipsSnapshot = await db.collection('weekly_tips').get();
    console.log(`   - Nombre de documents: ${weeklyTipsSnapshot.size}`);
    
    if (weeklyTipsSnapshot.empty) {
      console.log('   ❌ Aucun document trouvé dans weekly_tips');
      return;
    }
    
    // 2. Analyser chaque document
    console.log('\n2. Analyse des documents:');
    weeklyTipsSnapshot.forEach((doc, index) => {
      const data = doc.data();
      console.log(`   Document ${index + 1} (ID: ${doc.id}):`);
      console.log(`   - weekOfYear: ${data.weekOfYear} (type: ${typeof data.weekOfYear})`);
      console.log(`   - isActive: ${data.isActive} (type: ${typeof data.isActive})`);
      console.log(`   - title: "${data.title}"`);
      console.log(`   - category: "${data.category}"`);
      console.log('');
    });
    
    // 3. Calculer la semaine actuelle
    const now = new Date();
    const currentWeek = getCurrentWeekOfYear(now);
    console.log(`3. Semaine actuelle calculée: ${currentWeek}`);
    console.log(`   Date: ${now.toISOString().split('T')[0]}`);
    
    // 4. Chercher l'astuce de la semaine actuelle
    console.log('\n4. Recherche de l\'astuce pour la semaine actuelle:');
    const currentWeekSnapshot = await db.collection('weekly_tips')
      .where('weekOfYear', '==', currentWeek)
      .where('isActive', '==', true)
      .get();
    
    if (currentWeekSnapshot.empty) {
      console.log('   ❌ Aucune astuce trouvée pour la semaine actuelle');
      
      // Chercher n'importe quelle astuce active
      console.log('\n5. Recherche d\'une astuce active quelconque:');
      const anyActiveSnapshot = await db.collection('weekly_tips')
        .where('isActive', '==', true)
        .limit(1)
        .get();
      
      if (anyActiveSnapshot.empty) {
        console.log('   ❌ Aucune astuce active trouvée');
      } else {
        const doc = anyActiveSnapshot.docs[0];
        const data = doc.data();
        console.log(`   ✅ Astuce active trouvée: "${data.title}" (semaine ${data.weekOfYear})`);
      }
    } else {
      const doc = currentWeekSnapshot.docs[0];
      const data = doc.data();
      console.log(`   ✅ Astuce trouvée: "${data.title}"`);
    }
    
    // 5. Vérifier la configuration
    console.log('\n6. Vérification de la configuration:');
    const configDoc = await db.collection('config').doc('weekly_tip_config').get();
    if (configDoc.exists) {
      const configData = configDoc.data();
      console.log('   Configuration trouvée:');
      console.log(`   - currentWeekNumber: ${configData.currentWeekNumber}`);
      console.log(`   - rotationStrategy: ${configData.rotationStrategy}`);
    } else {
      console.log('   ❌ Configuration weekly_tip_config non trouvée');
    }
    
  } catch (error) {
    console.error('❌ Erreur lors du test:', error);
  }
}

function getCurrentWeekOfYear(date) {
  // Calcul ISO de la semaine (similaire à Java)
  const start = new Date(date.getFullYear(), 0, 1);
  const days = Math.floor((date - start) / (24 * 60 * 60 * 1000));
  const weekNumber = Math.ceil((days + start.getDay() + 1) / 7);
  return weekNumber;
}

// Fonction pour créer un document de test
async function createTestWeeklyTip() {
  console.log('\n🧪 Création d\'un document de test...');
  
  const testTip = {
    weekOfYear: getCurrentWeekOfYear(new Date()),
    title: "Test - Astuce de la Semaine",
    introduction: "Ceci est une astuce de test pour vérifier le fonctionnement.",
    fullContent: "Contenu complet de l'astuce de test...",
    category: "Test",
    createdAt: Date.now(),
    updatedAt: Date.now(),
    isActive: true
  };
  
  try {
    const docRef = await db.collection('weekly_tips').add(testTip);
    console.log(`✅ Document de test créé avec l'ID: ${docRef.id}`);
    console.log(`   Semaine: ${testTip.weekOfYear}`);
    console.log(`   Titre: ${testTip.title}`);
  } catch (error) {
    console.error('❌ Erreur lors de la création:', error);
  }
}

// Exécuter les tests
async function main() {
  await testWeeklyTipsStructure();
  
  // Décommentez la ligne suivante pour créer un document de test
  // await createTestWeeklyTip();
}

main();
























































