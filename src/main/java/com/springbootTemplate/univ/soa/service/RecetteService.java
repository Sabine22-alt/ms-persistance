package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.dto.RecetteDTO;
import com.springbootTemplate.univ.soa.exception.ResourceNotFoundException;
import com.springbootTemplate.univ.soa.model.*;
import com.springbootTemplate.univ.soa.repository.AlimentRepository;
import com.springbootTemplate.univ.soa.repository.NotificationRepository;
import com.springbootTemplate.univ.soa.repository.RecetteRepository;
import com.springbootTemplate.univ.soa.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecetteService {

    @Autowired
    private RecetteRepository recetteRepository;

    @Autowired
    private AlimentRepository alimentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ActiviteService activiteService;

    public List<Recette> findAll() {
        return recetteRepository.findAllOptimized();
    }

    @Transactional(readOnly = true)
    public Optional<Recette> findById(Long id) {
        return recetteRepository.findByIdOptimized(id);
    }

    @Transactional(readOnly = true)
    public List<Recette> findByStatut(Recette.StatutRecette statut) {
        return recetteRepository.findByStatutOptimized(statut);
    }

    @Transactional(readOnly = true)
    public List<Recette> findByUtilisateurId(Long utilisateurId) {
        return recetteRepository.findByUtilisateurIdOptimized(utilisateurId);
    }

    @Transactional
    public Recette save(Recette recette) {
        recette.setId(null);

        // Traiter les ingrédients
        if (recette.getIngredients() != null && !recette.getIngredients().isEmpty()) {
            for (Ingredient ingredient : recette.getIngredients()) {
                ingredient.setId(null);

                if (ingredient.getAliment() != null && ingredient.getAliment().getId() != null) {
                    Aliment aliment = alimentRepository.findById(ingredient.getAliment().getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Aliment non trouvé avec l'ID: " + ingredient.getAliment().getId()
                            ));
                    ingredient.setAliment(aliment);
                }
                ingredient.setRecette(recette);
            }
        }

        // Traiter les étapes
        if (recette.getEtapes() != null && !recette.getEtapes().isEmpty()) {
            for (Etape etape : recette.getEtapes()) {
                etape.setId(null);
                etape.setRecette(recette);
            }
        }

        return recetteRepository.save(recette);
    }

    @Transactional
    public Recette update(Long id, Recette recette) {
        Recette existing = recetteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + id));

        // Mise à jour des champs de base
        existing.setTitre(recette.getTitre());
        existing.setDescription(recette.getDescription());
        existing.setTempsTotal(recette.getTempsTotal());
        existing.setKcal(recette.getKcal());
        existing.setImageUrl(recette.getImageUrl());
        existing.setDifficulte(recette.getDifficulte());
        existing.setActif(recette.getActif());
        existing.setStatut(recette.getStatut());
        existing.setMotifRejet(recette.getMotifRejet());

        // Mise à jour des ingrédients
        if (recette.getIngredients() != null) {
            existing.getIngredients().clear();

            for (Ingredient ingredient : recette.getIngredients()) {
                ingredient.setId(null);

                if (ingredient.getAliment() != null && ingredient.getAliment().getId() != null) {
                    Aliment aliment = alimentRepository.findById(ingredient.getAliment().getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Aliment non trouvé avec l'ID: " + ingredient.getAliment().getId()
                            ));
                    ingredient.setAliment(aliment);
                    ingredient.setRecette(existing);
                    existing.getIngredients().add(ingredient);
                }
            }
        }

        // Mise à jour des étapes
        if (recette.getEtapes() != null) {
            existing.getEtapes().clear();

            for (Etape etape : recette.getEtapes()) {
                etape.setId(null);
                etape.setRecette(existing);
                existing.getEtapes().add(etape);
            }
        }

        return recetteRepository.save(existing);
    }

    /**
     * Méthode pour créer une recette depuis un RecetteDTO
     */
    @Transactional
    public Recette saveFromDTO(RecetteDTO dto) {
        // LOG DEBUG : Vérifier si utilisateurId est reçu
        System.out.println("🔍 DEBUG saveFromDTO - utilisateurId reçu: " + dto.getUtilisateurId());

        // Validation précoce: au moins un ingrédient requis
        if (dto.getIngredients() == null || dto.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Au moins un ingrédient est requis pour créer une recette");
        }

        Recette recette = new Recette();
        recette.setTitre(dto.getTitre());
        recette.setDescription(dto.getDescription());
        recette.setTempsTotal(dto.getTempsTotal());
        recette.setKcal(dto.getKcal());
        recette.setImageUrl(dto.getImageUrl());
        recette.setDifficulte(dto.getDifficulte());
        // par défaut: actif=false, statut=EN_ATTENTE
        recette.setActif(Boolean.FALSE);
        recette.setStatut(Recette.StatutRecette.EN_ATTENTE);
        recette.setMotifRejet(null);
        recette.setUtilisateurId(dto.getUtilisateurId());

        System.out.println("🔍 DEBUG saveFromDTO - utilisateurId set dans recette: " + recette.getUtilisateurId());

        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            for (RecetteDTO.IngredientDTO ingredientDTO : dto.getIngredients()) {
                boolean hasNom = (ingredientDTO.getAlimentNom() != null && !ingredientDTO.getAlimentNom().trim().isEmpty())
                        || (ingredientDTO.getNomAliment() != null && !ingredientDTO.getNomAliment().trim().isEmpty());
                boolean hasId = ingredientDTO.getAlimentId() != null;
                if (!hasNom && !hasId) {
                    throw new IllegalArgumentException("L'ID ou le nom de l'aliment est requis pour chaque ingrédient");
                }

                Ingredient ingredient = new Ingredient();

                // Essayer d'abord alimentNom, sinon nomAliment, sinon alimentId
                final String nomAliment;
                if (ingredientDTO.getAlimentNom() != null && !ingredientDTO.getAlimentNom().trim().isEmpty()) {
                    nomAliment = ingredientDTO.getAlimentNom().trim();
                } else if (ingredientDTO.getNomAliment() != null && !ingredientDTO.getNomAliment().trim().isEmpty()) {
                    nomAliment = ingredientDTO.getNomAliment().trim();
                } else {
                    nomAliment = null;
                }

                // Si un nom est fourni, l'utiliser (priorité au nom)
                if (nomAliment != null) {
                    // Chercher si l'aliment existe déjà (requête optimisée)
                    Optional<Aliment> alimentExistant = alimentRepository.findByNomIgnoreCase(nomAliment);

                    if (alimentExistant.isPresent()) {
                        // L'aliment existe déjà, l'utiliser
                        ingredient.setAliment(alimentExistant.get());
                    } else {
                        // L'aliment n'existe pas, le créer automatiquement
                        Aliment nouvelAliment = new Aliment();
                        nouvelAliment.setNom(nomAliment);
                        // Valeurs par défaut pour les champs nutritionnels
                        nouvelAliment.setCalories(0f);
                        nouvelAliment.setProteines(0f);
                        nouvelAliment.setGlucides(0f);
                        nouvelAliment.setLipides(0f);
                        nouvelAliment.setFibres(0f);
                        nouvelAliment.setCategorieAliment(Aliment.CategorieAliment.AUTRE);

                        // Sauvegarder le nouvel aliment
                        Aliment alimentSauvegarde = alimentRepository.save(nouvelAliment);
                        ingredient.setAliment(alimentSauvegarde);
                    }

                    // On garde aussi le nom libre pour compatibilité
                    ingredient.setNomAliment(nomAliment);
                } else if (ingredientDTO.getAlimentId() != null) {
                    // Si pas de nom mais alimentId fourni, utiliser l'ID
                    Aliment aliment = alimentRepository.findById(ingredientDTO.getAlimentId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Aliment non trouvé avec l'ID: " + ingredientDTO.getAlimentId()
                            ));
                    ingredient.setAliment(aliment);
                } else {
                    // Si ni ID ni nom fourni, erreur
                    throw new IllegalArgumentException(
                            "L'ID ou le nom de l'aliment est requis pour chaque ingrédient"
                    );
                }

                ingredient.setQuantite(ingredientDTO.getQuantite());
                ingredient.setUnite(ingredientDTO.getUnite() != null ?
                        Ingredient.Unite.valueOf(ingredientDTO.getUnite()) : null);
                ingredient.setPrincipal(ingredientDTO.getPrincipal());
                ingredient.setRecette(recette);

                recette.getIngredients().add(ingredient);
            }
        }

        // Traiter les étapes depuis le DTO
        if (dto.getEtapes() != null && !dto.getEtapes().isEmpty()) {
            for (RecetteDTO.EtapeDTO etapeDTO : dto.getEtapes()) {
                Etape etape = new Etape();
                etape.setOrdre(etapeDTO.getOrdre());
                etape.setTemps(etapeDTO.getTemps());
                etape.setTexte(etapeDTO.getTexte());
                etape.setRecette(recette);

                recette.getEtapes().add(etape);
            }
        }

        // Sauvegarder la recette après validation des ingrédients/étapes
        Recette saved = recetteRepository.save(recette);

        // LOG DEBUG : Vérifier que utilisateurId est persisté
        System.out.println("✅ DEBUG saveFromDTO - Recette sauvegardée avec utilisateurId: " + saved.getUtilisateurId());

        // Logger l'activité
        if (saved.getUtilisateurId() != null) {
            activiteService.logActivite(
                saved.getUtilisateurId(),
                Activite.TypeActivite.RECETTE_CREEE,
                "Recette créée : " + saved.getTitre()
            );
        }

        // Notifier tous les admins qu'une recette est en attente de validation
        try {
            if (utilisateurRepository != null && notificationRepository != null) {
                List<Utilisateur> admins = utilisateurRepository.findByRole(Utilisateur.Role.ADMIN);
                if (admins != null && !admins.isEmpty()) {
                    for (Utilisateur admin : admins) {
                        Notification notification = new Notification();
                        notification.setUtilisateurId(admin.getId());
                        notification.setRecetteId(saved.getId());
                        notification.setRecetteTitre(saved.getTitre());
                        notification.setType(Notification.TypeNotification.EN_ATTENTE);
                        notification.setMessage("Une nouvelle recette \"" + saved.getTitre() + "\" est en attente de validation.");
                        notification.setLue(false);
                        notificationRepository.save(notification);
                    }
                }
            }
        } catch (Exception e) {
            // En cas d'erreur de notification, on ne bloque pas la création de la recette
            System.err.println("⚠️ Erreur lors de l'envoi des notifications admin: " + e.getMessage());
        }

        return saved;
    }

    /**
     * Méthode pour mettre à jour une recette depuis un RecetteDTO
     */
    @Transactional
    public Recette updateFromDTO(Long id, RecetteDTO dto) {
        Recette existing = recetteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + id));

        // Mise à jour des champs de base
        existing.setTitre(dto.getTitre());
        existing.setDescription(dto.getDescription());
        existing.setTempsTotal(dto.getTempsTotal());
        existing.setKcal(dto.getKcal());
        existing.setImageUrl(dto.getImageUrl());
        existing.setDifficulte(dto.getDifficulte());
        // ne pas changer actif/statut/motif ici via DTO utilisateur standard

        // Mise à jour des ingrédients
        existing.getIngredients().clear();

        if (dto.getIngredients() != null && !dto.getIngredients().isEmpty()) {
            for (RecetteDTO.IngredientDTO ingredientDTO : dto.getIngredients()) {
                Ingredient ingredient = new Ingredient();

                // Essayer d'abord alimentNom, sinon nomAliment, sinon alimentId
                final String nomAliment;
                if (ingredientDTO.getAlimentNom() != null && !ingredientDTO.getAlimentNom().trim().isEmpty()) {
                    nomAliment = ingredientDTO.getAlimentNom().trim();
                } else if (ingredientDTO.getNomAliment() != null && !ingredientDTO.getNomAliment().trim().isEmpty()) {
                    nomAliment = ingredientDTO.getNomAliment().trim();
                } else {
                    nomAliment = null;
                }

                // Si un nom est fourni, l'utiliser (priorité au nom)
                if (nomAliment != null) {
                    // Chercher si l'aliment existe déjà (requête optimisée)
                    Optional<Aliment> alimentExistant = alimentRepository.findByNomIgnoreCase(nomAliment);

                    if (alimentExistant.isPresent()) {
                        // L'aliment existe déjà, l'utiliser
                        ingredient.setAliment(alimentExistant.get());
                    } else {
                        // L'aliment n'existe pas, le créer automatiquement
                        Aliment nouvelAliment = new Aliment();
                        nouvelAliment.setNom(nomAliment);
                        // Valeurs par défaut pour les champs nutritionnels
                        nouvelAliment.setCalories(0f);
                        nouvelAliment.setProteines(0f);
                        nouvelAliment.setGlucides(0f);
                        nouvelAliment.setLipides(0f);
                        nouvelAliment.setFibres(0f);
                        nouvelAliment.setCategorieAliment(Aliment.CategorieAliment.AUTRE);

                        // Sauvegarder le nouvel aliment
                        Aliment alimentSauvegarde = alimentRepository.save(nouvelAliment);
                        ingredient.setAliment(alimentSauvegarde);
                    }

                    // On garde aussi le nom libre pour compatibilité
                    ingredient.setNomAliment(nomAliment);
                } else if (ingredientDTO.getAlimentId() != null) {
                    // Si pas de nom mais alimentId fourni, utiliser l'ID
                    Aliment aliment = alimentRepository.findById(ingredientDTO.getAlimentId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Aliment non trouvé avec l'ID: " + ingredientDTO.getAlimentId()
                            ));
                    ingredient.setAliment(aliment);
                } else {
                    // Si ni ID ni nom fourni, erreur
                    throw new IllegalArgumentException(
                            "L'ID ou le nom de l'aliment est requis pour chaque ingrédient"
                    );
                }

                ingredient.setQuantite(ingredientDTO.getQuantite());
                ingredient.setUnite(ingredientDTO.getUnite() != null ?
                        Ingredient.Unite.valueOf(ingredientDTO.getUnite()) : null);
                ingredient.setPrincipal(ingredientDTO.getPrincipal());
                ingredient.setRecette(existing);

                existing.getIngredients().add(ingredient);
            }
        }

        // Mise à jour des étapes
        existing.getEtapes().clear();

        if (dto.getEtapes() != null && !dto.getEtapes().isEmpty()) {
            for (RecetteDTO.EtapeDTO etapeDTO : dto.getEtapes()) {
                Etape etape = new Etape();
                etape.setOrdre(etapeDTO.getOrdre());
                etape.setTemps(etapeDTO.getTemps());
                etape.setTexte(etapeDTO.getTexte());
                etape.setRecette(existing);

                existing.getEtapes().add(etape);
            }
        }

        return recetteRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!recetteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recette non trouvée avec l'ID: " + id);
        }
        recetteRepository.deleteById(id);
    }

    /**
     * Valider une recette (passer à VALIDEE et actif=true)
     */
    @Transactional
    public Recette validerRecette(Long id) {
        Recette recette = recetteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + id));

        recette.setActif(true);
        recette.setStatut(Recette.StatutRecette.VALIDEE);
        recette.setMotifRejet(null);

        Recette saved = recetteRepository.save(recette);

        // Créer une notification pour l'utilisateur
        if (saved.getUtilisateurId() != null) {
            Notification notification = new Notification();
            notification.setUtilisateurId(saved.getUtilisateurId());
            notification.setRecetteId(saved.getId());
            notification.setRecetteTitre(saved.getTitre());
            notification.setType(Notification.TypeNotification.VALIDEE);
            notification.setMessage("Votre recette \"" + saved.getTitre() + "\" a été validée et est maintenant visible par tous !");
            notification.setLue(false);
            notificationRepository.save(notification);
        }

        return saved;
    }

    /**
     * Rejeter une recette (passer à REJETEE avec motif)
     */
    @Transactional
    public Recette rejeterRecette(Long id, String motif) {
        Recette recette = recetteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + id));

        recette.setActif(false);
        recette.setStatut(Recette.StatutRecette.REJETEE);
        recette.setMotifRejet(motif);

        Recette saved = recetteRepository.save(recette);

        // Créer une notification pour l'utilisateur
        if (saved.getUtilisateurId() != null) {
            Notification notification = new Notification();
            notification.setUtilisateurId(saved.getUtilisateurId());
            notification.setRecetteId(saved.getId());
            notification.setRecetteTitre(saved.getTitre());
            notification.setType(Notification.TypeNotification.REJETEE);
            notification.setMessage("Votre recette \"" + saved.getTitre() + "\" a été rejetée. Motif : " + motif);
            notification.setLue(false);
            notificationRepository.save(notification);
        }

        return saved;
    }

    /**
     * Récupérer toutes les notifications d'un utilisateur
     */
    public List<Notification> getNotificationsByUtilisateur(Long utilisateurId) {
        return notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(utilisateurId);
    }

    /**
     * Récupérer les notifications non lues d'un utilisateur
     */
    public List<Notification> getNotificationsNonLues(Long utilisateurId) {
        return notificationRepository.findByUtilisateurIdAndLueOrderByDateCreationDesc(utilisateurId, false);
    }

    /**
     * Compter les notifications non lues d'un utilisateur
     */
    public long countNotificationsNonLues(Long utilisateurId) {
        return notificationRepository.countByUtilisateurIdAndLue(utilisateurId, false);
    }

    /**
     * Marquer une notification comme lue
     */
    @Transactional
    public Notification marquerNotificationCommeLue(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec l'ID: " + notificationId));

        notification.setLue(true);
        return notificationRepository.save(notification);
    }

    /**
     * Marquer toutes les notifications d'un utilisateur comme lues
     */
    @Transactional
    public void marquerToutesNotificationsCommeLues(Long utilisateurId) {
        List<Notification> notifications = notificationRepository.findByUtilisateurIdAndLueOrderByDateCreationDesc(utilisateurId, false);
        notifications.forEach(n -> n.setLue(true));
        notificationRepository.saveAll(notifications);
    }

    /**
     * Supprimer une notification par ID
     */
    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification non trouvée avec l'ID: " + id);
        }
        notificationRepository.deleteById(id);
    }
}
