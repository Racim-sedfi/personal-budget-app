# DESIGN.md — App budget hors ligne (Android)

Référence de conception pour développer l'app. Les maquettes de chaque écran sont dans `design/ecrans/` (HTML de référence : couleurs, tailles et espacements exacts). Canevas d'origine : « App budget — itération 1 » sur claude.ai.

---

## 1. Produit

- Application Android de gestion budgétaire personnelle, **100 % hors ligne**, **sans connexion bancaire** : l'utilisateur saisit lui-même chaque dépense.
- Trois objectifs : suivre son budget, savoir où va l'argent, identifier où faire des économies.
- Cible : étudiants et jeunes actifs (20–35 ans) qui veulent reprendre le contrôle **sans se sentir jugés**.

### Règles non négociables

1. **Aucune culpabilisation** : jamais de rouge ni de vert pour dire bien/mal.
   - Dépassement → ambre sourd. Sous-consommation → bleu calme.
   - Alertes = warnings neutres : icône discrète, ton factuel, pas de point d'exclamation, pas de vocabulaire d'échec.
2. **Honnêteté des données** :
   - Une journée sans saisie n'affiche **jamais** « 0 € » → état « Non renseignée » (contour pointillé + hachures, total « — »).
   - Distinct de « Aucune dépense » déclarée par l'utilisateur (total 0,00 €, coche).
   - Un cycle renseigné à **moins de 80 %** est affiché atténué (hachuré) dans les graphes, avec son taux, et **exclu des moyennes**.
3. **Jamais la couleur seule** : chaque état = couleur + texte + icône.
4. **Hiérarchie** : sur chaque écran, 1 information principale, 2–3 secondaires, le reste en arrière-plan. **Une seule action dominante** par écran.
5. **Peu de cartes** : sections, listes, séparateurs fins et espace blanc. Une carte sert à regrouper, pas à décorer.
6. **Textes courts** : pas de phrases qui expliquent l'évidence.

### Ton et formats

- Français, **tutoiement**, neutre et bienveillant.
  - « Loisirs dépasse son plafond de 35 €. »
  - « Tes charges fixes représentent 40 % de tes revenus. »
  - « 3 journées non renseignées »
- Montants : `1 234,56 €` (espace insécable, virgule décimale). Dates : `25 sept.`, `24 oct.`.
- On parle de **cycle**, pas de mois (le cycle ne suit pas le calendrier).

---

## 2. Concepts métier

| Concept | Règle |
|---|---|
| Cycle budgétaire | Du jour J (réglable, 1–28) au jour J-1 du mois suivant. Ex. 25 sept. → 24 oct. |
| Budget à dépenser | Revenus − Épargne planifiée − Charges fixes du cycle |
| **Reste disponible** | Revenus − Épargne planifiée − Charges fixes − Dépenses variables |
| Enveloppes | Catégories variables avec plafond : Courses, Restaurants, Transports, Loisirs, Shopping, Santé… |
| Imprévus | Enveloppe « fusible » qui couvre les dépassements des autres |
| Charges fixes | Loyer, électricité, etc. Fréquence mensuelle / trimestrielle / annuelle. Confirmées en un tap à l'échéance. Une charge trimestrielle ou annuelle compte dans le cycle de son échéance. |
| Transaction | Dépense **ou** remboursement (un remboursement réduit les dépenses de sa catégorie) |
| États d'une journée | Avec transactions · « Aucune dépense » déclarée · « Non renseignée » (journée passée sans rien) · journée en cours |
| Taux de complétion | Journées renseignées / journées écoulées du cycle (hors aujourd'hui) |
| Cycle d'observation | Démarrage sans budget : on saisit seulement. À la clôture, l'app propose des plafonds = dépense observée arrondie aux 5 € supérieurs, Imprévus ≈ 10 % |
| Clôture | Bilan → choix pour le reste (mettre de côté / reporter au cycle suivant) → ajustements de plafonds → « Valider le cycle » → transition vers le nouveau cycle |

### États d'une enveloppe

| État | Condition | Rendu |
|---|---|---|
| Normal | < 70 % du plafond | Barre bleu calme, « X € restants » |
| Proche de la limite | ≥ 70 % du plafond | Barre violette, icône horloge, « Proche de la limite · 72 % » |
| Dépassé | > 100 % | Barre ambre, icône triangle, « Plafond dépassé · 95 € sur 60 € », « 35,00 € de trop » |

### Données fictives de référence (cohérentes entre tous les écrans)

- Aujourd'hui : lun. 12 oct., jour 18 sur 30 du cycle 25 sept. → 24 oct.
- Revenus 1 850 € (salaire 1 650 € le 25 + aide au logement 200 € le 5).
- Épargne planifiée 150 € (virement le 26).
- Charges fixes du cycle 743 € : loyer 650 · électricité 48 · internet 30 · téléphone 15. Hors cycle : eau 45 €/trimestre, assurance habitation 96 €/an.
- Budget à dépenser 957 € · plafonds des enveloppes 645 € · non alloué 312 €.
- Dépensé 399,90 € → **reste disponible 557,10 €**.
- Enveloppes (dépensé / plafond) : Courses 142,30/250 · Restaurants 64,50/90 · Transports 38,20/75 · Loisirs 95,00/60 (dépassé) · Shopping 47,90/80 · Santé 12,00/30 · Imprévus 0/60.
- Complétion : 14 journées sur 17 (82 %), 3 non renseignées.

---

## 3. Design system (inspiré de SnowUI, adapté mobile, mode clair)

### Couleurs

| Rôle | Valeur |
|---|---|
| Fond | `#FFFFFF` |
| Surface discrète (pavé numérique, segments) | `#F1F4F8` |
| Texte principal | `#1C1C1C` |
| Texte secondaire | `#5B6068` (sur fond clair) · `#3E444B` (plus appuyé) |
| Texte sur dégradé | `#22364A` (bleu) · `#2A2F4A` (observation) |
| Séparateurs | `#EEF1F5` |
| Bordure de contrôle | `#C9D0D9` / `#D5DAE1` |
| Bleu calme (normal, sous-consommation) | barre `#4F7FB0`, clair `#A8C5DA`, foncé `#1F3550` |
| Proche de la limite | barre `#5B55B8`, texte `#3A3270` |
| Dépassement (ambre sourd) | barre `#D39A3A`, texte `#6B4510`, fond `#FCF0D8` |
| Piste de jauge | `#EDF0F4` |

### Dégradés (un seul par écran, en haut)

- En-tête principal : `180° #D4EAFF → #E4E1FC (72 %) → #FFFFFF`.
- En-tête observation : `180° #E4E1FC → #D4EAFF → #FFFFFF`.
- Bouton principal : `135° #2F3540 → #1C1C1C`, ombre douce.

### Typographie — Inter (embarquée dans l'app)

| Usage | Taille (sp) / graisse |
|---|---|
| Montant principal (hero) | 52 / SemiBold, interlettrage −3,5 % |
| Montant de saisie | 60 / SemiBold |
| Titre d'écran | 22 / SemiBold |
| Titre de section | 15 / SemiBold |
| Texte courant, lignes de liste | 14 / Regular–Medium |
| Secondaire, légendes | 12–13 |
| Barre de navigation | 11 |
| Chiffres | tabulaires (`fontFeatureSettings = "tnum"`) |

### Espacements et formes

- Marge latérale 16 dp · espace entre sections 28 dp · lignes de liste 52–64 dp avec séparateur.
- Rayons : boutons 14 dp · pastilles/chips 22 dp (pilule) · bottom sheet 24 dp en haut.
- **Zones tactiles ≥ 44 dp** (48 dp recommandé Android).
- Icônes linéaires fines (trait 1,5), 20–22 dp.

---

## 4. Navigation et écrans

Barre du bas : **Accueil · Historique · [+] · Analyse · Budget**. Le [+] est surélevé et ouvre la saisie rapide depuis partout. Paramètres : icône en haut de l'Accueil.

| Écran | Fichier de référence | Info principale | Action dominante |
|---|---|---|---|
| Accueil (cycle budgété) | `Main.dc.html` | « Il te reste 557,10 € » | [+] Ajouter une dépense |
| Accueil (observation) | `Observation.dc.html` | « Tu as dépensé 399,90 € » | [+] |
| Accueil (nouveau cycle, vide) | `AccueilVide.dc.html` | « Il te reste 957,00 € » | Ajouter une dépense |
| Saisie rapide (bottom sheet) | `Saisie.dc.html` | Le montant | Enregistrer X € |
| Historique | `Historique.dc.html` | Transactions par jour + total | Aucune dépense aujourd'hui |
| Analyse | `Analyse.dc.html` | Où va ton argent (anneau) | Ajuster mes plafonds |
| Analyse (données insuffisantes) | `AnalyseVide.dc.html` | 1 cycle utilisable sur 2 | Compléter les journées |
| Budget | `Budget.dc.html` | 957 € à dépenser par cycle | Modifier le budget |
| Clôture (budgété) | `Cloture.dc.html` | « Il te reste 355,60 € » | Valider le cycle |
| Clôture (après observation) | `ClotureObservation.dc.html` | Plafonds proposés | Valider ces plafonds |
| Onboarding 1–4 | `Onboarding1..4.dc.html` | Confidentialité · jour de début · configurer ou observer · biométrie | Continuer |
| Paramètres | `Parametres.dc.html` | Statistiques d'usage locales | — |
| Widgets | `Widget.dc.html` | Reste disponible | [+] Saisir |

### Saisie rapide (parcours cible : 3 secondes)

`[+]` → montant (pavé intégré) → un tap sur une catégorie → **Enregistrer**.
- Catégories toutes visibles (retour à la ligne), les plus utilisées en premier.
- Date « Aujourd'hui » et note **repliées** derrière « Aujourd'hui · ajouter une note » (note avec suggestions par catégorie).
- Bascule Dépense / Remboursement discrète en haut.
- Bouton désactivé tant que le montant est vide ou nul (« Saisis un montant »).

### Micro-interactions

- Dépense ajoutée → snackbar « Dépense ajoutée · 8,40 € · Courses » + **Annuler**, le reste disponible se met à jour immédiatement.
- Charge fixe confirmée → la ligne passe à « Électricité confirmée » + Annuler.
- Cycle validé → coche animée, « 355,60 € mis de côté », nouveau cycle et montant disponible, puis « Commencer le cycle ».
- Dépassement → visible immédiatement dans la liste des enveloppes.
- Respecter le réglage système « réduire les animations ».

---

## 5. Accessibilité (dès maintenant)

- [ ] Zones tactiles ≥ 44 dp (48 dp idéalement).
- [ ] Contraste texte ≥ 4,5:1 (3:1 au-delà de 24 sp).
- [ ] Tailles en `sp`, écrans testés à **200 %** de taille de police : les gros montants doivent passer à la ligne proprement.
- [ ] `contentDescription` sur les boutons-icônes (Paramètres, Fermer, Effacer…), icônes décoratives ignorées.
- [ ] `Modifier.semantics` : barres de progression avec texte (« Jour 18 sur 30 », « 399,90 euros dépensés sur 957 »), graphes avec une description complète.
- [ ] Montant de saisie annoncé à chaque changement (live region).
- [ ] Jamais la couleur seule (texte + icône).
- [ ] Ordre de focus logique, bottom sheet modal pour TalkBack.
- [ ] Tests réguliers avec **TalkBack**.

---

## 6. Architecture technique

### Stack (déjà déclarée dans `gradle/libs.versions.toml` et `app/build.gradle.kts`)

- Kotlin · Jetpack Compose + Material 3 (thème personnalisé) · Navigation Compose.
- ViewModel + StateFlow + Coroutines · Hilt (DI, avec KSP).
- Room (base locale) · DataStore Preferences (réglages) · kotlinx.serialization (export/import JSON).
- androidx.biometric (verrouillage) · WorkManager + hilt-work (rappel quotidien) · Glance (widget).
- Tests : JUnit, kotlinx-coroutines-test, Turbine, room-testing, Compose UI test.
- `minSdk 26` pour utiliser `java.time` sans desugaring.

### Règles d'implémentation

- **Montants en centimes (`Long`)**, jamais en `Double`. Formatage via `NumberFormat.getCurrencyInstance(Locale.FRANCE)`.
- **Pas de permission `INTERNET`** dans le manifest : preuve technique du « 100 % hors ligne ».
- Ajouter `POST_NOTIFICATIONS` (Android 13+) pour le rappel quotidien.
- `@HiltAndroidApp` sur une classe `Application`, `@AndroidEntryPoint` sur `MainActivity`.
- Graphiques (anneau, histogramme) dessinés avec le `Canvas` de Compose.

### Modèle de données proposé (Room)

- `Category` (id, nom, icône, plafondCentimes?, estFusible, ordre)
- `Transaction` (id, montantCentimes, type DEPENSE/REMBOURSEMENT, categoryId, date `LocalDate`, note?)
- `DayStatus` (date, statut AUCUNE_DEPENSE) — « Non renseignée » est **calculé** (jour passé sans transaction ni statut)
- `Income` (id, nom, montantCentimes, jour)
- `PlannedSaving` (id, libellé, montantCentimes, jour)
- `FixedCharge` (id, nom, montantCentimes, fréquence MENSUELLE/TRIMESTRIELLE/ANNUELLE, prochaineÉchéance)
- `FixedChargeConfirmation` (chargeId, débutCycle, confirméLe)
- `CycleClosure` (débutCycle, restantCentimes, choix METTRE_DE_COTE/REPORTER, clôturéLe)
- Réglages DataStore : jour de début de cycle, mode (budget/observation), verrouillage, rappel (heure).

### Organisation des packages suggérée

```
com.application.personal_budget_app
├── data/        (room: entities, dao, database · datastore · repository)
├── domain/      (calculs : cycle, reste disponible, complétion, états d'enveloppe, propositions de plafonds)
├── ui/
│   ├── theme/   (couleurs, typo Inter, dégradés, formes)
│   ├── components/ (MoneyText, EnvelopeRow, StatusBadge, NumericKeypad, BottomNav…)
│   ├── home/ history/ entry/ analysis/ budget/ closure/ onboarding/ settings/
├── widget/      (Glance)
└── work/        (rappel quotidien)
```

Commencer par `domain/` avec des tests unitaires (calcul du cycle 25 → 24, reste disponible, complétion, arrondis) : c'est le cœur de l'app.

---

## 7. Points ouverts

- **Chiffrement de la base (SQLCipher)** : recommandé pour des données financières, mais la dernière version de `net.zetetic:sqlcipher-android` (4.18.0) exige `compileSdk 37` alors que le projet est en 36. À ajouter en même temps que le passage à compileSdk 37, ou avec une version antérieure compatible.
- **`android:allowBackup="true"`** dans le manifest : la sauvegarde Android envoie les données vers le cloud Google, ce qui contredit la promesse « rien ne quitte ton téléphone ». À passer à `false` (l'export/import manuel reste disponible).
- Charges trimestrielles/annuelles : comptées dans le cycle de leur échéance (choix actuel) ou provisionnées chaque cycle — décision métier à confirmer.
- Seuil « proche de la limite » fixé à 70 % du plafond — à ajuster après tests utilisateurs.
