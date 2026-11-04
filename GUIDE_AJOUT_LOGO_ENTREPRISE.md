# 🏢 GUIDE : AJOUTER LE LOGO DE VOTRE ENTREPRISE

## ✅ MODIFICATION EFFECTUÉE

J'ai ajouté le logo de l'entreprise dans la section **"À propos de l'application"** des paramètres, en bas des 4 sélections existantes.

---

## 📍 FICHIER MODIFIÉ

**Fichier:** `app/src/main/java/com/pilotprep/app/ui/screens/AboutScreen.kt`

**Changements:**
- ✅ Ajout d'un composable `CompanyLogo()` pour afficher le logo
- ✅ Logo affiché en bas des informations
- ✅ Espacement de 32dp avant le logo
- ✅ Nom de l'entreprise sous le logo

---

## 🎨 OPTIONS POUR AJOUTER VOTRE LOGO

### **Option 1: Logo depuis les ressources drawable (Recommandé)**

**Étape 1:** Ajoutez votre image dans le dossier de ressources

1. Placez votre fichier image (PNG, JPG, SVG) dans:
   ```
   app/src/main/res/drawable/votre_logo.png
   ```

2. Nommez le fichier en minuscules, sans espaces (ex: `logo_entreprise.png`)

**Étape 2:** Modifiez le code pour utiliser votre image

Dans `AboutScreen.kt`, ligne 95, remplacez:
```kotlin
painter = painterResource(id = com.pilotprep.app.R.drawable.ic_launcher_foreground),
```

Par:
```kotlin
painter = painterResource(id = com.pilotprep.app.R.drawable.votre_logo),
```

**Étape 3:** Personnalisez le nom de l'entreprise

Ligne 115, remplacez:
```kotlin
text = "Nom de votre entreprise",
```

Par:
```kotlin
text = "Votre Nom d'Entreprise",
```

---

### **Option 2: Logo depuis une URL (Alternative)**

Si vous préférez charger le logo depuis une URL (Firebase Storage ou un serveur):

**Étape 1:** Dans `AboutScreen.kt`, commentez l'Option 1 (lignes 92-100)

**Étape 2:** Décommentez l'Option 2 (lignes 102-110)

**Étape 3:** Modifiez l'URL ligne 105:
```kotlin
model = "https://exemple.com/votre-logo.png",
```

Mettez l'URL complète de votre logo.

---

## 📝 PERSONNALISATION AVANCÉE

### Ajuster la taille du logo

Ligne 98, modifiez les dimensions:
```kotlin
modifier = Modifier.size(width = 120.dp, height = 120.dp)
```

Pour un logo plus petit: `80.dp`  
Pour un logo plus grand: `150.dp`

### Changer l'espacement

Ligne 60 (espace avant le logo):
```kotlin
Spacer(modifier = Modifier.height(32.dp))
```

Ligne 113 (espace entre logo et texte):
```kotlin
Spacer(modifier = Modifier.height(8.dp))
```

### Masquer le nom de l'entreprise

Si vous ne voulez pas afficher le nom, commentez les lignes 113-118.

---

## 🎯 RÉSULTAT

L'écran "À propos de l'application" affichera maintenant:

```
┌─────────────────────────┐
│  À propos de l'application │
├─────────────────────────┤
│                         │
│  Version: 1.0 (Build 1) │
│  Android: 8.0 (API 26)  │
│  Concepteur: ...        │
│  Licences: ...          │
│                         │
│                         │
│         🏢              │
│      (Logo 120x120)     │
│                         │
│  Nom de votre entreprise│
└─────────────────────────┘
```

---

## ✅ CHECKLIST

- [ ] Ajouter l'image dans `app/src/main/res/drawable/`
- [ ] Modifier la référence dans `AboutScreen.kt` ligne 95
- [ ] Personnaliser le nom de l'entreprise ligne 115
- [ ] Ajuster la taille si nécessaire
- [ ] Compiler et tester l'application

---

## 🚀 TEST

Pour tester:

1. Compilez l'application:
   ```bash
   ./gradlew assembleDebug
   ```

2. Lancez sur un appareil/émulateur

3. Allez dans **Paramètres** → **À propos de l'application**

4. Vérifiez que le logo s'affiche en bas

---

## 💡 CONSEILS

1. **Format d'image:** Utilisez PNG avec transparence pour un meilleur rendu
2. **Taille:** Résolution recommandée 512x512px pour haute définition
3. **Nom du fichier:** Utilisez des lettres minuscules et underscore (ex: `logo_compagnie.png`)
4. **Couleurs:** Le logo sera affiché tel quel, pensez à adapter les couleurs au thème

---

## 📞 BESOIN D'AIDE?

Si vous rencontrez des problèmes:
- Vérifiez que le nom du fichier image est correct (sans majuscules ni espaces)
- Vérifiez que l'image est bien dans `app/src/main/res/drawable/`
- Regénérez le projet: Build → Clean Project puis Build → Rebuild Project

---

**Modification terminée!** 🎉

*Date: 27 octobre 2025*  
*Expert Android - 15 ans d'expérience*




