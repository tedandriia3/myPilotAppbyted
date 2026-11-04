## PilotPrepApp — Documentation UX/UI, Tests UI Compose et Checklist Accessibilité

### 1. Ligne Directrice de Présentation
- **Identité visuelle** : Palettes primaires `MaterialTheme.colorScheme.primary` et secondaires `onPrimary` pour les appels à l’action. Limiter les variantes à trois tons par écran (clair, foncé, accent). 
- **Hiérarchie** : Titre (`headlineSmall` ou `headlineMedium`), sous-titre (`titleMedium`), contenu (`bodyMedium`). Utiliser `weight(1f)` pour équilibrer; éviter les alignements mixtes.
- **Icônes & illustrations** : Icônes M3 Filled/Outlined cohérentes; taille 24 dp. Avatars ou vignettes arrondies (8 dp) pour rester cohérent avec le design général.
- **Espacement** : Marges horizontales 16 dp sur mobile, 24 dp sur tablette; espacement vertical 8–12 dp entre éléments textuels, 16–24 dp entre sections.
- **Feedback** : Snackbar pour succès/erreur, dialogues de confirmation pour toute action destructive, skeletons ou `CircularProgressIndicator` pour chargement >300 ms.

### 2. Tonalité & Microcopie
- **Voix** : Professionnelle, rassurante, centrée sur la réussite de l’élève pilote.
- **Formulation** : Verbes d’action (« Consultez », « Entraînez-vous »). Bannir le jargon interne; privilégier vocabulaire aéronautique validé.
- **Accessibilité linguistique** : Messages ≤90 caractères, éviter les phrases nominales. Prévoir traduction FR/EN future via ressources `strings.xml`.
- **Erreurs** : Expliquer la cause, proposer une solution (« Vérifiez votre connexion », « Réessayez »), lien vers support si nécessaire.

### 3. Tests UI Compose à Mettre en Place
- **Structure** : `app/src/androidTest/java/com/pilotprep/app/ui/` → classes `HistoryScreenTest`, `SearchScreenTest`.
- **Cas recommandés** :
  - États clés (`isLoading`, liste vide, liste remplie, mode sélection).
  - Interaction (clic carte historique → navigation, suppression élément).
  - Vox Search : activation/désactivation, feedback visuel.
  - Recherche : saisie, exécution, applications des filtres, affichage « aucun résultat ».
- **Outils** : `composeTestRule`, `SemanticsNodeInteractions`, `assertIsDisplayed()`, `performClick()`.
- **Automatisation** : Intégrer dans CI `./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=...`.

### 4. Checklist Accessibilité (avant publication)
- **Contrastes** : Ratio ≥4.5:1 pour textes standards, tester via Material Theme Builder.
- **Navigation** : Composants focusables, ordre logique tabulation/clavier, support TalkBack/Lecture.
- **Taille de police** : Vérifier mise à l’échelle >200 % (affichage adaptatif, LazyColumn non tronquée).
- **Gestuelle** : Actions possibles via clic + clavier; interactions critiques doublées par bouton explicite (pas uniquement swipe/long press).
- **Retour vocal** : `contentDescription` descriptifs, évitez répétitions (« bouton bouton »).
- **Animations** : Durée <400 ms ou paramétrée dans les préférences. Assurer compatibilité `animateContentSize` avec réduction des animations système.
- **Formulaires** : Erreurs annoncées vocalement, champs associés à étiquettes explicites.
- **Internationalisation** : Chaînes dans `strings.xml`, formats de date/heures localisés.

### 5. Prochaines Étapes
- Décliner ces lignes dans la charte design centrale (`README_AMELIORATIONS.md`).
- Mettre à jour les `ViewModel` pour instrumenter logs UX (analytics + crash).
- Planifier une revue accessibilité trimestrielle et documenter résultats dans `VERIFICATION_APPLICATION.md`.








