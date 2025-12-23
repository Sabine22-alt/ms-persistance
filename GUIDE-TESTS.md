# 🧪 Guide de Test - API Recettes Chef Bot

## 📋 Table des Matières
1. [Prérequis](#prérequis)
2. [Configuration](#configuration)
3. [Démarrage](#démarrage)
4. [Tests avec Postman](#tests-avec-postman)
5. [Scénarios de Test](#scénarios-de-test)
6. [Tests des Fichiers (Images & Documents)](#tests-des-fichiers)
7. [Vérifications](#vérifications)

---

## 🔧 Prérequis

### Logiciels Requis
- ✅ Docker Desktop
- ✅ Java 21 (JDK)
- ✅ Maven 3.8+
- ✅ Postman (ou tout client REST)
- ✅ Git

### Vérification
```powershell
# Vérifier les versions
docker --version
java -version
mvn -version
```

---

## ⚙️ Configuration

### 1. Fichier .env
Le fichier `.env` à la racine du projet contient toutes les variables d'environnement :

```env
# Application
SERVER_PORT=8090

# MySQL
MYSQL_PORT=3306
MYSQL_DATABASE=recettes_db
MYSQL_USERNAME=root
MYSQL_ROOT_PASSWORD=rootpassword

# PhpMyAdmin
PHPMYADMIN_PORT=8080

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin123
MINIO_API_PORT=9000
MINIO_CONSOLE_PORT=9001

# JPA/Hibernate
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
JPA_FORMAT_SQL=true
JPA_USE_SQL_COMMENTS=true

# Logging
LOG_LEVEL_JDBC=INFO
LOG_LEVEL_HIBERNATE_SQL=DEBUG
LOG_LEVEL_HIBERNATE_BINDER=TRACE
```

---

## 🚀 Démarrage

### Étape 1 : Démarrer les Services Docker
```powershell
# Démarrer tous les services
docker-compose up -d

# Vérifier que tous les conteneurs sont en cours d'exécution
docker-compose ps

# Attendez quelques secondes pour l'initialisation
Start-Sleep -Seconds 15
```

### Étape 2 : Compiler et Démarrer l'Application
```powershell
# Compiler le projet
mvn clean install -DskipTests

# Démarrer l'application Spring Boot
mvn spring-boot:run
```

### Étape 3 : Vérifier que l'Application est Prête
```powershell
# Health check
curl http://localhost:8090/actuator/health
```

**Réponse attendue :**
```json
{
  "status": "UP"
}
```

---

## 📮 Tests avec Postman

### Configuration Postman

#### 1. Créer une Collection "Recettes API"
- Créer une nouvelle collection
- Nom : `Recettes Chef Bot API`
- Base URL : `http://localhost:8090`

#### 2. Variables d'Environnement
Créer un environnement "Local" avec ces variables :

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| baseUrl | http://localhost:8090 | http://localhost:8090 |
| recetteId | | (sera rempli dynamiquement) |
| alimentId | | (sera rempli dynamiquement) |
| utilisateurId | | (sera rempli dynamiquement) |
| fichierImageId | | (sera rempli dynamiquement) |
| fichierDocId | | (sera rempli dynamiquement) |

---

## 🎯 Scénarios de Test

### Scénario 1 : CRUD Utilisateurs

#### 1.1 Créer un Utilisateur
```http
POST {{baseUrl}}/api/persistance/utilisateurs
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@example.com",
  "password": "Password123!",
  "numeroTelephone": "+33612345678"
}
```

**Script de test Postman :**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Response has id", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.environment.set("utilisateurId", jsonData.id);
});

pm.test("Email is correct", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.email).to.eql("jean.dupont@example.com");
});
```

#### 1.2 Récupérer Tous les Utilisateurs
```http
GET {{baseUrl}}/api/persistance/utilisateurs
```

**Script de test :**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
});
```

#### 1.3 Récupérer un Utilisateur par ID
```http
GET {{baseUrl}}/api/persistance/utilisateurs/{{utilisateurId}}
```

#### 1.4 Mettre à Jour un Utilisateur
```http
PUT {{baseUrl}}/api/persistance/utilisateurs/{{utilisateurId}}
Content-Type: application/json

{
  "nom": "Dupont",
  "prenom": "Jean-Pierre",
  "email": "jean.dupont@example.com",
  "password": "NewPassword123!",
  "numeroTelephone": "+33612345678"
}
```

#### 1.5 Supprimer un Utilisateur
```http
DELETE {{baseUrl}}/api/persistance/utilisateurs/{{utilisateurId}}
```

---

### Scénario 2 : CRUD Aliments

#### 2.1 Créer un Aliment
```http
POST {{baseUrl}}/api/persistance/aliments
Content-Type: application/json

{
  "nom": "Farine",
  "type": "CEREALE",
  "kcalPour100g": 364.0,
  "proteinesPour100g": 10.0,
  "glucidesPour100g": 76.0,
  "lipidesPour100g": 1.0
}
```

**Script de test :**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Aliment created successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.environment.set("alimentId", jsonData.id);
    pm.expect(jsonData.nom).to.eql("Farine");
});
```

#### 2.2 Créer d'Autres Aliments
```http
POST {{baseUrl}}/api/persistance/aliments
Content-Type: application/json

{
  "nom": "Sucre",
  "type": "AUTRE",
  "kcalPour100g": 387.0,
  "proteinesPour100g": 0.0,
  "glucidesPour100g": 100.0,
  "lipidesPour100g": 0.0
}
```

```http
POST {{baseUrl}}/api/persistance/aliments
Content-Type: application/json

{
  "nom": "Oeuf",
  "type": "PROTEINE",
  "kcalPour100g": 155.0,
  "proteinesPour100g": 13.0,
  "glucidesPour100g": 1.1,
  "lipidesPour100g": 11.0
}
```

#### 2.3 Récupérer Tous les Aliments
```http
GET {{baseUrl}}/api/persistance/aliments
```

#### 2.4 Rechercher des Aliments par Nom
```http
GET {{baseUrl}}/api/persistance/aliments/search?nom=farine
```

---

### Scénario 3 : CRUD Recettes avec Ingrédients et Étapes

#### 3.1 Créer une Recette Complète
```http
POST {{baseUrl}}/api/persistance/recettes
Content-Type: application/json

{
  "titre": "Gâteau au Chocolat Fondant",
  "tempsTotal": 45,
  "kcal": 350,
  "difficulte": "MOYEN",
  "ingredients": [
    {
      "alimentId": 1,
      "quantite": 200.0,
      "unite": "GRAMME",
      "principal": true
    },
    {
      "alimentId": 2,
      "quantite": 150.0,
      "unite": "GRAMME",
      "principal": false
    },
    {
      "alimentId": 3,
      "quantite": 4.0,
      "unite": "UNITE",
      "principal": true
    }
  ],
  "etapes": [
    {
      "ordre": 1,
      "temps": 10,
      "texte": "Préchauffer le four à 180°C. Mélanger la farine et le sucre dans un grand bol."
    },
    {
      "ordre": 2,
      "temps": 5,
      "texte": "Ajouter les œufs battus et mélanger jusqu'à obtenir une pâte homogène."
    },
    {
      "ordre": 3,
      "temps": 30,
      "texte": "Verser dans un moule beurré et cuire au four pendant 30 minutes."
    }
  ]
}
```

**Script de test :**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Recette created with ingredients and etapes", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.environment.set("recetteId", jsonData.id);
    pm.expect(jsonData.titre).to.eql("Gâteau au Chocolat Fondant");
    pm.expect(jsonData.ingredients).to.be.an('array');
    pm.expect(jsonData.ingredients.length).to.eql(3);
    pm.expect(jsonData.etapes).to.be.an('array');
    pm.expect(jsonData.etapes.length).to.eql(3);
});
```

#### 3.2 Récupérer une Recette par ID
```http
GET {{baseUrl}}/api/persistance/recettes/{{recetteId}}
```

**Script de test :**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Recette contains all details", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.ingredients).to.exist;
    pm.expect(jsonData.etapes).to.exist;
});
```

#### 3.3 Mettre à Jour une Recette
```http
PUT {{baseUrl}}/api/persistance/recettes/{{recetteId}}
Content-Type: application/json

{
  "titre": "Gâteau au Chocolat Fondant (Version Améliorée)",
  "tempsTotal": 50,
  "kcal": 380,
  "difficulte": "MOYEN",
  "ingredients": [
    {
      "alimentId": 1,
      "quantite": 250.0,
      "unite": "GRAMME",
      "principal": true
    },
    {
      "alimentId": 2,
      "quantite": 200.0,
      "unite": "GRAMME",
      "principal": false
    }
  ],
  "etapes": [
    {
      "ordre": 1,
      "temps": 15,
      "texte": "Préchauffer le four à 180°C. Faire fondre le chocolat au bain-marie."
    },
    {
      "ordre": 2,
      "temps": 35,
      "texte": "Mélanger tous les ingrédients et cuire pendant 35 minutes."
    }
  ]
}
```

---

### Scénario 4 : Feedbacks sur Recettes

#### 4.1 Ajouter un Feedback
```http
POST {{baseUrl}}/api/persistance/feedbacks
Content-Type: application/json

{
  "utilisateurId": {{utilisateurId}},
  "recetteId": {{recetteId}},
  "note": 5,
  "commentaire": "Excellent gâteau ! Très moelleux et facile à réaliser."
}
```

**Script de test :**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Feedback created", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.note).to.eql(5);
});
```

#### 4.2 Récupérer les Feedbacks d'une Recette
```http
GET {{baseUrl}}/api/persistance/feedbacks/recette/{{recetteId}}
```

---

## 📸 Tests des Fichiers (Images & Documents)

### Scénario 5 : Gestion des Images de Recettes

#### 5.1 Upload d'une Image pour une Recette

**Dans Postman :**
1. Créer une nouvelle requête
2. Méthode : `POST`
3. URL : `{{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/images`
4. Onglet "Body" → Sélectionner "form-data"
5. Ajouter une clé "file" avec type "File"
6. Sélectionner une image (JPG, PNG, GIF, WEBP)

```http
POST {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/images
Content-Type: multipart/form-data

file: [sélectionner une image]
```

**Script de test :**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Image uploaded successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.environment.set("fichierImageId", jsonData.id);
    pm.expect(jsonData.type).to.eql("IMAGE");
    pm.expect(jsonData.nomOriginal).to.exist;
    pm.expect(jsonData.urlTelechargement).to.exist;
    pm.expect(jsonData.taille).to.be.above(0);
});

pm.test("URL is valid", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.urlTelechargement).to.include("http");
});
```

#### 5.2 Upload d'un Document pour une Recette

```http
POST {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/documents
Content-Type: multipart/form-data

file: [sélectionner un PDF ou document]
```

**Script de test :**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Document uploaded successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.environment.set("fichierDocId", jsonData.id);
    pm.expect(jsonData.type).to.eql("DOCUMENT");
    pm.expect(jsonData.contentType).to.be.oneOf([
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain"
    ]);
});
```

#### 5.3 Récupérer Toutes les Images d'une Recette

```http
GET {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/images
```

**Script de test :**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array of images", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
    if (jsonData.length > 0) {
        pm.expect(jsonData[0].type).to.eql("IMAGE");
        pm.expect(jsonData[0].urlTelechargement).to.exist;
    }
});
```

#### 5.4 Récupérer Tous les Documents d'une Recette

```http
GET {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/documents
```

**Script de test :**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response is an array of documents", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
    if (jsonData.length > 0) {
        pm.expect(jsonData[0].type).to.eql("DOCUMENT");
    }
});
```

#### 5.5 Récupérer Tous les Fichiers d'une Recette

```http
GET {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers
```

**Script de test :**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response contains images and documents", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an('array');
    
    var hasImages = jsonData.some(f => f.type === "IMAGE");
    var hasDocuments = jsonData.some(f => f.type === "DOCUMENT");
    
    pm.expect(hasImages || hasDocuments).to.be.true;
});
```

#### 5.6 Récupérer les Métadonnées d'un Fichier

```http
GET {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/{{fichierImageId}}
```

**Script de test :**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("File metadata is complete", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.expect(jsonData.nomOriginal).to.exist;
    pm.expect(jsonData.nomStocke).to.exist;
    pm.expect(jsonData.contentType).to.exist;
    pm.expect(jsonData.taille).to.exist;
    pm.expect(jsonData.type).to.exist;
    pm.expect(jsonData.urlTelechargement).to.exist;
    pm.expect(jsonData.dateUpload).to.exist;
});
```

#### 5.7 Télécharger un Fichier

```http
GET {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/{{fichierImageId}}/download
```

**Note :** Cette requête télécharge le fichier. Dans Postman, vous pouvez cliquer sur "Send and Download" pour sauvegarder le fichier.

**Script de test :**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Content-Disposition header exists", function () {
    pm.response.to.have.header("Content-Disposition");
});

pm.test("Response has binary content", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.exist;
});
```

#### 5.8 Supprimer un Fichier

```http
DELETE {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/{{fichierImageId}}
```

**Script de test :**
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});
```

#### 5.9 Supprimer Tous les Fichiers d'une Recette

```http
DELETE {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers
```

---

## 🧪 Tests de Validation et Erreurs

### Test 1 : Upload d'un Fichier Trop Volumineux
```http
POST {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/images
Content-Type: multipart/form-data

file: [fichier > 10MB]
```

**Réponse attendue : 400 Bad Request**
```json
{
  "error": "La taille du fichier ne peut pas dépasser 10MB"
}
```

### Test 2 : Upload d'un Type de Fichier Non Autorisé
```http
POST {{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/images
Content-Type: multipart/form-data

file: [fichier .exe ou .zip]
```

**Réponse attendue : 400 Bad Request**
```json
{
  "error": "Type de fichier non autorisé. Types acceptés: JPEG, PNG, GIF, WEBP"
}
```

### Test 3 : Upload pour une Recette Inexistante
```http
POST {{baseUrl}}/api/persistance/recettes/99999/fichiers/images
Content-Type: multipart/form-data

file: [image valide]
```

**Réponse attendue : 404 Not Found**

### Test 4 : Créer une Recette Sans Titre
```http
POST {{baseUrl}}/api/persistance/recettes
Content-Type: application/json

{
  "tempsTotal": 30,
  "kcal": 200
}
```

**Réponse attendue : 400 Bad Request**
```json
{
  "error": "Le titre de la recette est obligatoire"
}
```

---

## ✅ Vérifications

### Vérifier les Données dans MySQL

#### Via PhpMyAdmin
1. Ouvrir http://localhost:8080
2. Connexion : `root` / `rootpassword`
3. Sélectionner la base `recettes_db`
4. Explorer les tables :
   - `utilisateurs`
   - `aliments`
   - `recettes`
   - `ingredients`
   - `etapes`
   - `feedbacks`
   - `fichiers_recette` ⭐ (nouvelle table)

#### Via Ligne de Commande
```powershell
# Se connecter au conteneur MySQL
docker exec -it mysql-local mysql -u root -prootpassword recettes_db

# Vérifier les tables
SHOW TABLES;

# Voir les recettes
SELECT id, titre, temps_total, kcal FROM recettes;

# Voir les fichiers uploadés
SELECT id, nom_original, type, taille, recette_id, date_upload FROM fichiers_recette;

# Quitter
exit;
```

### Vérifier les Fichiers dans MinIO

#### Via MinIO Console
1. Ouvrir http://localhost:9001
2. Connexion : `minioadmin` / `minioadmin123`
3. Naviguer vers les buckets :
   - **recettes-bucket** : contient les images
   - **documents-bucket** : contient les documents
4. Vérifier la structure :
   ```
   recettes-bucket/recettes/{recetteId}/images/{uuid}.jpg
   documents-bucket/recettes/{recetteId}/documents/{uuid}.pdf
   ```

### Vérifier les Logs de l'Application
```powershell
# Voir les logs en temps réel
mvn spring-boot:run

# Ou si l'application est en cours d'exécution dans un terminal
# vérifier les logs SQL, les uploads, etc.
```

---

## 📊 Collection Postman Complète

### Importer la Collection
Créez un fichier `Recettes_API.postman_collection.json` avec la structure suivante :

```json
{
  "info": {
    "name": "Recettes Chef Bot API",
    "description": "Collection complète pour tester l'API de gestion des recettes",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Utilisateurs",
      "item": [
        {
          "name": "Créer Utilisateur",
          "request": {
            "method": "POST",
            "header": [],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"nom\": \"Dupont\",\n  \"prenom\": \"Jean\",\n  \"email\": \"jean.dupont@example.com\",\n  \"password\": \"Password123!\",\n  \"numeroTelephone\": \"+33612345678\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "{{baseUrl}}/api/persistance/utilisateurs",
              "host": ["{{baseUrl}}"],
              "path": ["api", "persistance", "utilisateurs"]
            }
          }
        }
      ]
    },
    {
      "name": "Fichiers",
      "item": [
        {
          "name": "Upload Image",
          "request": {
            "method": "POST",
            "body": {
              "mode": "formdata",
              "formdata": [
                {
                  "key": "file",
                  "type": "file",
                  "src": []
                }
              ]
            },
            "url": {
              "raw": "{{baseUrl}}/api/persistance/recettes/{{recetteId}}/fichiers/images",
              "host": ["{{baseUrl}}"],
              "path": ["api", "persistance", "recettes", "{{recetteId}}", "fichiers", "images"]
            }
          }
        }
      ]
    }
  ]
}
```

---

## 🎯 Résumé des Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| **Utilisateurs** |
| POST | `/api/persistance/utilisateurs` | Créer un utilisateur |
| GET | `/api/persistance/utilisateurs` | Liste des utilisateurs |
| GET | `/api/persistance/utilisateurs/{id}` | Récupérer un utilisateur |
| PUT | `/api/persistance/utilisateurs/{id}` | Modifier un utilisateur |
| DELETE | `/api/persistance/utilisateurs/{id}` | Supprimer un utilisateur |
| **Aliments** |
| POST | `/api/persistance/aliments` | Créer un aliment |
| GET | `/api/persistance/aliments` | Liste des aliments |
| GET | `/api/persistance/aliments/search?nom={nom}` | Rechercher des aliments |
| **Recettes** |
| POST | `/api/persistance/recettes` | Créer une recette |
| GET | `/api/persistance/recettes` | Liste des recettes |
| GET | `/api/persistance/recettes/{id}` | Récupérer une recette |
| PUT | `/api/persistance/recettes/{id}` | Modifier une recette |
| DELETE | `/api/persistance/recettes/{id}` | Supprimer une recette |
| **Feedbacks** |
| POST | `/api/persistance/feedbacks` | Créer un feedback |
| GET | `/api/persistance/feedbacks/recette/{id}` | Feedbacks d'une recette |
| **Fichiers (Images & Documents)** ⭐ |
| POST | `/api/persistance/recettes/{id}/fichiers/images` | Upload une image |
| POST | `/api/persistance/recettes/{id}/fichiers/documents` | Upload un document |
| GET | `/api/persistance/recettes/{id}/fichiers` | Tous les fichiers |
| GET | `/api/persistance/recettes/{id}/fichiers/images` | Images uniquement |
| GET | `/api/persistance/recettes/{id}/fichiers/documents` | Documents uniquement |
| GET | `/api/persistance/recettes/{id}/fichiers/{fid}` | Métadonnées d'un fichier |
| GET | `/api/persistance/recettes/{id}/fichiers/{fid}/download` | Télécharger un fichier |
| DELETE | `/api/persistance/recettes/{id}/fichiers/{fid}` | Supprimer un fichier |
| DELETE | `/api/persistance/recettes/{id}/fichiers` | Supprimer tous les fichiers |

---

## 🚀 Script de Test Rapide PowerShell

```powershell
# Script de test complet
$baseUrl = "http://localhost:8090/api/persistance"

Write-Host "🧪 Test de l'API Recettes Chef Bot" -ForegroundColor Green

# 1. Créer un utilisateur
Write-Host "`n📝 1. Création d'un utilisateur..." -ForegroundColor Yellow
$user = @{
    nom = "TestUser"
    prenom = "John"
    email = "test@example.com"
    password = "Test123!"
    numeroTelephone = "+33612345678"
} | ConvertTo-Json

$userResponse = Invoke-RestMethod -Uri "$baseUrl/utilisateurs" -Method Post -Body $user -ContentType "application/json"
Write-Host "✅ Utilisateur créé avec ID: $($userResponse.id)" -ForegroundColor Green

# 2. Créer une recette
Write-Host "`n📝 2. Création d'une recette..." -ForegroundColor Yellow
$recette = @{
    titre = "Recette de Test"
    tempsTotal = 30
    kcal = 250
    difficulte = "FACILE"
    ingredients = @()
    etapes = @(
        @{
            ordre = 1
            temps = 30
            texte = "Préparer la recette"
        }
    )
} | ConvertTo-Json -Depth 10

$recetteResponse = Invoke-RestMethod -Uri "$baseUrl/recettes" -Method Post -Body $recette -ContentType "application/json"
$recetteId = $recetteResponse.id
Write-Host "✅ Recette créée avec ID: $recetteId" -ForegroundColor Green

# 3. Récupérer les fichiers (devrait être vide)
Write-Host "`n📝 3. Récupération des fichiers (devrait être vide)..." -ForegroundColor Yellow
$fichiers = Invoke-RestMethod -Uri "$baseUrl/recettes/$recetteId/fichiers" -Method Get
Write-Host "✅ Nombre de fichiers: $($fichiers.Count)" -ForegroundColor Green

Write-Host "`n✨ Tests terminés avec succès!" -ForegroundColor Green
Write-Host "📊 Recette ID: $recetteId" -ForegroundColor Cyan
Write-Host "👤 Utilisateur ID: $($userResponse.id)" -ForegroundColor Cyan
```

---

## 📞 Support

En cas de problème :
1. Vérifier les logs de l'application
2. Vérifier que Docker est en cours d'exécution
3. Vérifier que MinIO est accessible : http://localhost:9001
4. Vérifier que MySQL est accessible via PhpMyAdmin : http://localhost:8080
5. Consulter les guides : `GUIDE-FICHIERS-RECETTES.md` et `QUICKSTART-FICHIERS.md`

---

**Développé avec ❤️ pour Chef Bot Recettes**
