package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.exception.ResourceNotFoundException;
import com.springbootTemplate.univ.soa.model.Activite;
import com.springbootTemplate.univ.soa.model.Feedback;
import com.springbootTemplate.univ.soa.model.Recette;
import com.springbootTemplate.univ.soa.model.Utilisateur;
import com.springbootTemplate.univ.soa.repository.FeedbackRepository;
import com.springbootTemplate.univ.soa.repository.RecetteRepository;
import com.springbootTemplate.univ.soa.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RecetteRepository recetteRepository;

    @Autowired
    private ActiviteService activiteService;

    public List<Feedback> findAll() {
        return feedbackRepository.findAll();
    }

    public Optional<Feedback> findById(Long id) {
        return feedbackRepository.findById(id);
    }

    public boolean existsByUtilisateurIdAndRecetteId(Long utilisateurId, Long recetteId) {
        return feedbackRepository.existsByUtilisateurIdAndRecetteId(utilisateurId, recetteId);
    }

    public List<Feedback> findByUtilisateurId(Long utilisateurId) {
        return feedbackRepository.findByUtilisateur_Id(utilisateurId);
    }

    public List<Feedback> findByRecetteId(Long recetteId) {
        return feedbackRepository.findByRecette_Id(recetteId);
    }

    @Transactional
    public Feedback save(Feedback feedback, Long utilisateurId, Long recetteId) {
        feedback.setId(null);

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + utilisateurId));

        Recette recette = recetteRepository.findByIdSimple(recetteId)
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + recetteId));

        feedback.setUtilisateur(utilisateur);
        feedback.setRecette(recette);

        Feedback saved = feedbackRepository.save(feedback);

        // Logger l'activité
        activiteService.logActivite(
            utilisateurId,
            Activite.TypeActivite.FEEDBACK_AJOUT,
            "Avis ajouté à la recette : " + recette.getTitre() + " (Note: " + saved.getEvaluation() + "/5)"
        );

        // Mettre à jour la moyenne d'évaluation de la recette
        updateRecetteMoyenneEvaluation(recetteId);

        return saved;
    }

    @Transactional
    public Feedback update(Long id, Feedback feedback) {
        Feedback existing = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback non trouvé avec l'ID: " + id));

        existing.setEvaluation(feedback.getEvaluation());
        existing.setCommentaire(feedback.getCommentaire());

        Feedback updated = feedbackRepository.save(existing);

        // Mettre à jour la moyenne d'évaluation de la recette
        updateRecetteMoyenneEvaluation(existing.getRecette().getId());

        // Enregistrer l'activité
        activiteService.logActivite(
            existing.getUtilisateur().getId(),
            Activite.TypeActivite.FEEDBACK_MODIFIE,
            "Avis modifié sur la recette : " + existing.getRecette().getTitre() + " (Note: " + updated.getEvaluation() + "/5)"
        );

        return updated;
    }

    @Transactional
    public void deleteById(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback non trouvé avec l'ID: " + id));

        Long recetteId = feedback.getRecette().getId();
        Long utilisateurId = feedback.getUtilisateur().getId();
        String recetteTitre = feedback.getRecette().getTitre();

        feedbackRepository.deleteById(id);

        // Mettre à jour la moyenne d'évaluation de la recette
        updateRecetteMoyenneEvaluation(recetteId);

        // Enregistrer l'activité
        activiteService.logActivite(
            utilisateurId,
            Activite.TypeActivite.FEEDBACK_SUPPRIME,
            "Avis supprimé de la recette : " + recetteTitre
        );
    }

    /**
     * Recalcule et met à jour la moyenne d'évaluation pour une recette
     */
    @Transactional
    public void updateRecetteMoyenneEvaluation(Long recetteId) {
        Recette recette = recetteRepository.findByIdSimple(recetteId)
                .orElseThrow(() -> new ResourceNotFoundException("Recette non trouvée avec l'ID: " + recetteId));

        // Calculer la moyenne via la requête BD
        Optional<Double> moyenne = feedbackRepository.calculateAverageEvaluationByRecetteId(recetteId);

        // Mettre à jour le champ dénormalisé
        recette.setMoyenneEvaluation(moyenne.orElse(0.0));
        recetteRepository.save(recette);
    }
}