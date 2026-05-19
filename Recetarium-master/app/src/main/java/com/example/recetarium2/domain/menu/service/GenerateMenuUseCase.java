package com.example.recetarium2.domain.menu.service;

import com.example.recetarium2.domain.menu.Menu;
import com.example.recetarium2.application.usecase.MenuGeneratorService;
import com.example.recetarium2.domain.preset.MenuPreset;

/**
 * Application Use Case: generates a weekly Menu from a preset.
 *
 * Responsibilities:
 *   - Fetch the preset from the repository
 *   - Delegate generation logic to {@link MenuGeneratorService}
 *   - Persist the resulting Menu
 *
 * This class contains no business logic — it only orchestrates
 * domain services and repositories.
 */
public class GenerateMenuUseCase {

    /**
     * Repository interface for loading MenuPresets.
     * Implemented in the data layer.
     */
    public interface MenuPresetRepository {
        /**
         * @param presetId the preset identifier
         * @return the matching MenuPreset
         * @throws IllegalArgumentException if not found
         */
        MenuPreset findById(String presetId);
    }

    /**
     * Repository interface for persisting generated Menus.
     * Implemented in the data layer.
     */
    public interface MenuRepository {
        /**
         * Persists a newly generated Menu.
         *
         * @param menu the menu to save
         */
        void save(Menu menu);
    }

    private final MenuGeneratorService  menuGeneratorService;
    private final MenuPresetRepository  presetRepository;
    private final MenuRepository        menuRepository;

    /**
     * @param menuGeneratorService domain service that handles generation logic
     * @param presetRepository     source of MenuPresets
     * @param menuRepository       destination for the generated Menu
     */
    public GenerateMenuUseCase(
            MenuGeneratorService menuGeneratorService,
            MenuPresetRepository presetRepository,
            MenuRepository menuRepository
    ) {
        this.menuGeneratorService = menuGeneratorService;
        this.presetRepository     = presetRepository;
        this.menuRepository       = menuRepository;
    }

    /**
     * Executes the use case.
     *
     * @param presetId the id of the preset to use for generation
     * @param menuName display name for the resulting menu
     * @return the generated and persisted Menu
     */
    public Menu execute(String presetId, String menuName) {
        MenuPreset preset = presetRepository.findById(presetId);
        Menu menu = menuGeneratorService.generate(preset, menuName);
        menuRepository.save(menu);
        return menu;
    }
}
