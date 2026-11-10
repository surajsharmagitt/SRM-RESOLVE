package com.srmresolve.config;

import com.srmresolve.service.VivaSimulationService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final VivaSimulationService simulationService;

    public GlobalModelAttributes(VivaSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @ModelAttribute("demoMode")
    public boolean isDemoMode() {
        return simulationService.isDemoModeEnabled();
    }
}

