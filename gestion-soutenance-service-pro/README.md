# Gestion Soutenance Service - UASZ

Microservice Spring Boot pour la partie :

## 4.6 Gestion des soutenances

- Planification des soutenances
- Constitution des jurys
- Archivage des rapports finaux

## Port

```txt
8086
```

## Base de données

```txt
gestion_soutenance_db
```

## Microservices utilisés

```txt
Encadrement : http://localhost:8083/api
Livrables   : http://localhost:8084/api
```

## Règles métier

- Seul l'administrateur planifie une soutenance.
- Une soutenance est liée à un encadrement.
- Une soutenance ne peut être planifiée que si l'encadrement est ACTIF ou TERMINE.
- L'encadreur principal est ajouté automatiquement au jury avec le rôle ENCADREUR.
- L'administrateur peut ajouter les autres membres du jury.
- Un rapport final ne peut être archivé qu'après une soutenance TERMINEE.
- Seul un livrable de type RAPPORT_FINAL peut être archivé.

## Lancement

```bash
mvn clean install
mvn spring-boot:run
```

## Planifier une soutenance

```http
POST http://localhost:8086/api/soutenances
Authorization: Bearer TOKEN_ADMIN
```

```json
{
  "encadrementId": 1,
  "dateHeure": "2026-06-20T09:00:00",
  "salle": "Salle 12",
  "dureeMinutes": 45,
  "observations": "Prévoir vidéoprojecteur"
}
```

## Ajouter un membre de jury

```http
POST http://localhost:8086/api/soutenances/1/jury
Authorization: Bearer TOKEN_ADMIN
```

```json
{
  "enseignantId": 4,
  "enseignantNomComplet": "Mouhamadou Gaye",
  "enseignantEmail": "m.gaye@univ-zig.sn",
  "roleJury": "PRESIDENT"
}
```

## Voir le planning

```http
GET http://localhost:8086/api/soutenances/planning
Authorization: Bearer TOKEN
```

## Mes soutenances

```http
GET http://localhost:8086/api/soutenances/mes-soutenances
Authorization: Bearer TOKEN
```

## Terminer une soutenance

```http
PUT http://localhost:8086/api/soutenances/1/terminer
Authorization: Bearer TOKEN_ADMIN
```

## Archiver un rapport final

```http
POST http://localhost:8086/api/archives/soutenance/1
Authorization: Bearer TOKEN_ADMIN
```

```json
{
  "livrableId": 8,
  "commentaireArchivage": "Rapport final validé après soutenance."
}
```
