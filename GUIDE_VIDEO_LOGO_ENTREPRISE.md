# 🎥 GUIDE : LOGO VIDÉO EN BOUCLE

## ✅ MODIFICATION EFFECTUÉE

J'ai remplacé le logo statique par une vidéo en format MP4 qui se joue automatiquement en boucle dans la section "À propos de l'application".

---

## 📝 FICHIER MODIFIÉ

**Fichier:** `app/src/main/java/com/pilotprep/app/ui/screens/AboutScreen.kt`

**Nouveaux composables:**
- ✅ `LoopingVideoPlayer()` - Lecteur vidéo avec boucle automatique
- ✅ `CompanyLogo()` - Affiche maintenant une vidéo au lieu d'une image
- ✅ **Fallback intelligent** - Affiche une image si la vidéo n'existe pas encore

**Correction appliquée:** Utilisation de `getIdentifier()` pour éviter les erreurs de compilation si le fichier vidéo n'existe pas encore.

---

## 🎬 COMMENT AJOUTER VOTRE VIDÉO

### Étape 1: Ajouter votre fichier vidéo

1. **Copiez votre fichier vidéo MP4** dans le dossier:
   ```
   app/src/main/res/raw/logo_entreprise.mp4
   ```

2. **Nom du fichier:** Le fichier doit s'appeler `logo_entreprise.mp4`

3. **Format recommandé:**
   - Format: MP4 (codec H.264)
   - Taille: 300x300px ou carrée (ratio 1:1)
   - Durée: 3-10 secondes pour une boucle fluide
   - Poids: Gardez la vidéo légère (< 5MB)

---

### Étape 2: Personnaliser le nom de l'entreprise

Dans `AboutScreen.kt`, ligne 114, modifiez:

```kotlin
text = "Nom de votre entreprise",
```

Par:

```kotlin
text = "Votre Nom d'Entreprise",
```

---

### Étape 3: Personnaliser la taille de la vidéo

Ligne 107-108, modifiez les dimensions:

```kotlin
modifier = Modifier.size(width = 120.dp, height = 120.dp)
```

**Options:**
- Plus petit: `80.dp`
- Plus grand: `150.dp`
- Format carré: `120.dp` (recommandé)

---

## 🎯 CARACTÉRISTIQUES DE LA VIDÉO

✅ **Lecture automatique** - La vidéo démarre dès l'ouverture de l'écran  
✅ **Boucle infinie** - La vidéo se répète automatiquement  
✅ **Sans contrôles** - Interface propre sans boutons  
✅ **Optimisée** - ExoPlayer pour une performance maximale  
✅ **Libération automatique** - Libère les ressources en quittant l'écran  

---

## 🛠️ ALTERNATIVES

### Option 1: Vidéo depuis une URL (Firebase Storage)

Si vous préférez charger la vidéo depuis une URL:

**Modifiez la ligne 106:**

```kotlin
LoopingVideoPlayer(
    videoUri = Uri.parse("https://votre-firebase-storage.com/video.mp4"),
    modifier = Modifier.size(width = 120.dp, height = 120.dp)
)
```

---

### Option 2: Vidéo depuis un serveur externe

```kotlin
LoopingVideoPlayer(
    videoUri = Uri.parse("https://exemple.com/logo.mp4"),
    modifier = Modifier.size(width = 120.dp, height = 120.dp)
)
```

---

## 📐 CODE AJOUTÉ

### Nouveau composable `LoopingVideoPlayer`

```kotlin
@Composable
fun LoopingVideoPlayer(
    videoUri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Créer et configurer ExoPlayer
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ALL // Mode boucle
            prepare()
            playWhenReady = true
        }
    }
    
    // Libérer le player quand on quitte l'écran
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }
    
    // Afficher le player
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false // Masquer les contrôles
                }
            },
            modifier = modifier
        )
    }
}
```

**Fonctionnalités:**
- `repeatMode = Player.REPEAT_MODE_ALL` - Active la boucle
- `playWhenReady = true` - Démarre automatiquement
- `useController = false` - Masque les contrôles de lecture
- `DisposableEffect` - Libère les ressources automatiquement

---

## 🎨 PERSONNALISATION AVANCÉE

### Ajuster l'espacement

**Avant la vidéo (ligne 60):**
```kotlin
Spacer(modifier = Modifier.height(32.dp))
```

**Entre la vidéo et le texte (ligne 112):**
```kotlin
Spacer(modifier = Modifier.height(8.dp))
```

---

### Ajouter un fond/bordures

```kotlin
LoopingVideoPlayer(
    videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.logo_entreprise}"),
    modifier = Modifier
        .size(width = 120.dp, height = 120.dp)
        .padding(4.dp)
        .background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        )
        .padding(8.dp)
)
```

---

### Désactiver la lecture automatique

Si vous voulez que l'utilisateur clique pour démarrer:

Dans `LoopingVideoPlayer`, ligne 134, remplacez:
```kotlin
playWhenReady = true
```

Par:
```kotlin
playWhenReady = false
```

---

## ✅ CHECKLIST

- [ ] Créer le fichier vidéo en MP4 (ratio carré recommandé)
- [ ] Placer le fichier dans `app/src/main/res/raw/logo_entreprise.mp4`
- [ ] Personnaliser le nom de l'entreprise (ligne 114)
- [ ] Ajuster la taille si nécessaire (ligne 107-108)
- [ ] Compiler et tester l'application

---

## 🚀 TEST

Pour tester:

1. **Compilez l'application:**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Lancez sur un appareil/émulateur**

3. **Allez dans:** Paramètres → À propos de l'application

4. **Vérifiez que:**
   - La vidéo démarre automatiquement
   - La vidéo se répète en boucle
   - La vidéo est centrée
   - L'animation est fluide

---

## 💡 CONSEILS PRO

### Créer votre vidéo

**Logiciels recommandés:**
- Adobe After Effects
- Premiere Pro
- DaVinci Resolve
- Canva (version web simple)

**Paramètres optimaux:**
```
Format: MP4
Codec: H.264
Résolution: 300x300px (carré)
FPS: 30 (fluide)
Durée: 3-10 secondes
Poids: < 5MB
Couleurs: RGB
```

### Optimiser la taille du fichier

Utilisez un outil de compression:
- **HandBrake** (gratuit)
- **FFmpeg** (ligne de commande)
- **Compressor** (Mac)

**Commande FFmpeg:**
```bash
ffmpeg -i input.mp4 -vf "scale=300:300" -crf 28 -b:v 1M logo_entreprise.mp4
```

---

## 🐛 DÉPANNAGE

### La vidéo ne se charge pas

**Vérifiez:**
1. Le fichier est bien dans `app/src/main/res/raw/`
2. Le nom du fichier est exactement `logo_entreprise.mp4`
3. Le fichier n'est pas trop lourd (> 50MB peut causer des problèmes)
4. Regénérez le projet: Build → Clean puis Build → Rebuild

---

### La vidéo ne démarre pas automatiquement

**Vérifiez:**
- `playWhenReady = true` dans le code
- Les permissions d'accès aux fichiers
- Le format de la vidéo (MP4 recommandé)

---

### Performance médiocre

**Solutions:**
- Réduire la résolution de la vidéo (300x300px max)
- Réduire la durée (max 10 secondes)
- Utiliser un format optimisé (H.264)
- Compresser la vidéo

---

## 🎯 RÉSULTAT ATTENDU

```
À propos de l'application
├─ Version de l'application
├─ Version Android minimale  
├─ Concepteur
├─ Licences
└─ [Espace de 32dp]
   └─ 🎥 Vidéo MP4 en boucle (120x120)
   └─ Nom de votre entreprise
```

**Comportement:**
- ✅ Vidéo démarre automatiquement
- ✅ Se répète en boucle infinie
- ✅ Affichage fluide
- ✅ Interface propre
- ✅ Ressources libérées en quittant

---

## 📞 BESOIN D'AIDE?

Si vous rencontrez des problèmes:
1. Vérifiez que ExoPlayer est bien dans les dépendances
2. Testez avec une vidéo de test simple d'abord
3. Vérifiez les logs Android: `adb logcat`

---

## 🔧 COMMENT FONCTIONNE LE CODE

### Mécanisme de Fallback

Le code est conçu pour fonctionner **même sans la vidéo** grâce à un système de fallback:

```kotlin
// 1. Cherche la ressource vidéo dynamiquement
val resourceId = context.resources.getIdentifier(
    "logo_entreprise",
    "raw",
    context.packageName
)

// 2. Si la vidéo existe, on l'affiche
if (resourceId != 0) {
    LoopingVideoPlayer(...)
}
// 3. Sinon, on affiche une image de fallback
else {
    Image(...)
}
```

**Avantages:**
- ✅ Le code compile sans le fichier vidéo
- ✅ Aucune erreur si le fichier n'existe pas encore
- ✅ Affichage automatique d'une image en attendant

---

## 📦 DÉPENDANCES (déjà présentes)

Ces dépendances sont déjà dans votre projet:
```kotlin
implementation("androidx.media3:media3-exoplayer:1.3.1")
implementation("androidx.media3:media3-ui:1.3.1")
```

Pas besoin de modification dans `build.gradle.kts` ✅

---

**Modification terminée!** 🎉

Votre application affichera maintenant une vidéo en boucle au lieu d'un logo statique.

*Date: 27 octobre 2025*  
*Expert Android - 15 ans d'expérience*

