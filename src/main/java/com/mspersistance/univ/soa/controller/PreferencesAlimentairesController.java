package com.mspersistance.univ.soa.controller;

import com.mspersistance.univ.soa.model.Allergene;
import com.mspersistance.univ.soa.model.RegimeAlimentaire;
import com.mspersistance.univ.soa.model.TypeCuisine;
import com.mspersistance.univ.soa.service.AllergeneService;
import com.mspersistance.univ.soa.service.RegimeAlimentaireService;
import com.mspersistance.univ.soa.service.TypeCuisineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/persistance/preferences")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PreferencesAlimentairesController {

    private final RegimeAlimentaireService regimeService;
    private final AllergeneService allergeneService;
    private final TypeCuisineService typeCuisineService;

    /**
     * GET /api/persistance/preferences/regimes
     */
    @GetMapping("/regimes")
    public ResponseEntity<List<RegimeAlimentaire>> getAllRegimes() {
        return ResponseEntity.ok(regimeService.findAll());
    }

    /**
     * GET /api/persistance/preferences/allergenes
     */
    @GetMapping("/allergenes")
    public ResponseEntity<List<Allergene>> getAllAllergenes() {
        return ResponseEntity.ok(allergeneService.findAll());
    }

    /**
     * GET /api/persistance/preferences/cuisines
     */
    @GetMapping("/cuisines")
    public ResponseEntity<List<TypeCuisine>> getAllTypesCuisine() {
        return ResponseEntity.ok(typeCuisineService.findAll());
    }
}
