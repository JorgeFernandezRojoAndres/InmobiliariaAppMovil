package com.jorge.inmobiliaria2025.utils;

import android.os.Bundle;

/**
 * 🧭 NavigationCommand
 * Clase simple que representa una orden de navegación emitida por el ViewModel.
 * Permite mantener la lógica de destino dentro del ViewModel,
 * sin que el Fragment tenga condiciones o llamadas directas a NavController.
 */
public class NavigationCommand {
    public final int destinationId;
    public final Bundle args;

    public NavigationCommand(int destinationId, Bundle args) {
        this.destinationId = destinationId;
        this.args = args;
    }
}
